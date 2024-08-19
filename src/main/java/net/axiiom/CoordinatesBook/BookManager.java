package net.axiiom.CoordinatesBook;

import net.axiiom.CoordinatesBook.Main.CoordinatesBookPlugin;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import org.bukkit.Material;

import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import java.sql.SQLException;
import java.util.*;

// Class responsible for all of the book generation and modification

public class BookManager
{
    private CoordinatesBookPlugin plugin;
    // Maps the UUID of the player to a list of coordinates associated with them
    private HashMap<UUID, List<Coordinate>> coordinatesHash;

    public BookManager(CoordinatesBookPlugin _plugin) {
        this.plugin = _plugin;
        this.coordinatesHash = new HashMap<>();
    }

    public HashMap<UUID, List<Coordinate>> getCoordinates() {
        return this.coordinatesHash;
    }

    public void addCoordinates(UUID _playerUUID, List<Coordinate> _coordinates) {
        this.coordinatesHash.put(_playerUUID, _coordinates);
    }

    public List<Coordinate> getCoordinates(Player _player) {
        if(this.coordinatesHash.containsKey(_player.getUniqueId())) {
            return this.coordinatesHash.get(_player.getUniqueId());
        } else {
            try {
                List<Coordinate> coordinates = this.plugin.getDatabase().getPlayerCoordinates(_player.getUniqueId());
                this.coordinatesHash.put(_player.getUniqueId(), coordinates);
                return coordinates;
            } catch(SQLException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
    }

    // Adds a coordinate to a player's book
//    public boolean addCoordinate(Player _player, Coordinate _coordinate) {
//        UUID playerUUID = _player.getUniqueId();
//        return addCoordinate(playerUUID,_coordinate);
//    }

    public boolean addCoordinate(UUID _playerUUID, Coordinate _coordinate) {
        List<Coordinate> coordinateList = (this.coordinatesHash.containsKey(_playerUUID))
            ? this.coordinatesHash.get(_playerUUID)
            : new ArrayList<>();

        if(!coordinateList.contains(_coordinate))
        {
            if(coordinateList.size() >= 10)
                return false;

            coordinateList.add(_coordinate);
            this.coordinatesHash.put(_playerUUID, coordinateList);

					try {
						this.plugin.getDatabase().addPlayerToCoordinate(_playerUUID, _coordinate);
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
					return true;
        }

        return false;
    }

    public boolean createCoordinate(UUID _playerUUID, Coordinate _coordinate) {
        List<Coordinate> coordinateList = (this.coordinatesHash.containsKey(_playerUUID))
                ? this.coordinatesHash.get(_playerUUID)
                : new ArrayList<>();

        if(!coordinateList.contains(_coordinate))
        {
            if(coordinateList.size() >= 10)
                return false;

            coordinateList.add(_coordinate);
            this.coordinatesHash.put(_playerUUID, coordinateList);
          try {
            this.plugin.getDatabase().createCoordinate(_playerUUID, _coordinate);
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }
          return true;
        }

        return false;
    }

    public Coordinate getCoordinateByUUID(String _uuid) {
			return this.coordinatesHash.values().stream()
            .flatMap(List::stream)
            .filter(coordinate -> coordinate.getUuid().equals(_uuid))
            .findFirst()
            .orElse(null);
    }

    public void removeCoordinate(Player _player, String _uuid) {
        UUID playerUUID = _player.getUniqueId();
        if (this.coordinatesHash.containsKey(playerUUID)) {
            List<Coordinate> coordinates = this.coordinatesHash.get(playerUUID);
            for (Coordinate coordinate : coordinates) {
                if (coordinate.getUuid().equals(_uuid)) {
                    try {
                        plugin.getDatabase().removeCoordinate(playerUUID, coordinate);
                        coordinates.remove(coordinate);
                        if(coordinates.isEmpty()) {
                            this.coordinatesHash.remove(playerUUID);
                        } else {
                            this.coordinatesHash.put(playerUUID, coordinates);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
    }

    // Removes a coordinate from a player's book
    public boolean removeCoordinate(Player _player, Coordinate _coordinate) {
        UUID playerUUID = _player.getUniqueId();
        if (this.coordinatesHash.containsKey(playerUUID)) {
            List<Coordinate> coordinates = this.coordinatesHash.get(playerUUID);
            coordinates.remove(_coordinate);
				    try {
                plugin.getDatabase().removeCoordinate(playerUUID, _coordinate);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
				}
        return true;
    }

    // Retrieves a coordinate from its description
    public Coordinate getCoordinateByDescription(Player _player, String _description) {
        if(this.coordinatesHash.containsKey(_player.getUniqueId())) {
            List<Coordinate> coordinateList = this.coordinatesHash.get(_player.getUniqueId());
            for (Coordinate coordinate : coordinateList) {
                if (coordinate.getName().equals(_description)) {
                    return coordinate;
                }
            }
        }

        return null;
    }

    public boolean hasCoordinate(Player _player, String _description) {
        return getCoordinateByDescription(_player, _description) != null;
    }

    public boolean changeCoordinateName(Player _player, String _description, String _newDescription) {
        Coordinate oldCoordinate = getCoordinateByDescription(_player, _description);
        if(oldCoordinate != null) {
            List<Coordinate> coordinateList = coordinatesHash.get(_player.getUniqueId());
            for(Coordinate coord : coordinateList) {
                if(coord.equals(oldCoordinate)) {
                    coord.setName(_newDescription);
                    coordinatesHash.put(_player.getUniqueId(),coordinateList);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean openBook(Player _player) {
        //Create book
        ItemStack book = createBook(_player);

        //Open book
        int slot = _player.getInventory().getHeldItemSlot();
        ItemStack old = _player.getInventory().getItem(slot);
        _player.getInventory().setItem(slot, book);

        CraftPlayer craftPlayer = (CraftPlayer) _player;
        craftPlayer.openBook(book);

        _player.getInventory().setItem(slot, old);
        return true;
    }


    //PRIVATE METHODS

    //Create book meta: https://www.spigotmc.org/wiki/interactive-books/#creating-the-book
    private ItemStack createBook(Player player)
    {
        List<Coordinate> coordinateList = getCoordinates(player);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        List<BaseComponent[]> pages = new ArrayList<>();
        pages.add(BookBuilder.getTableOfContents(coordinateList).create());

        for(Coordinate coordinate : coordinateList)         {
            ComponentBuilder page = BookBuilder.buildCoordinatePage(coordinate);
            pages.add(page.create());
        }

        bookMeta.spigot().setPages(pages);
        bookMeta.setAuthor(player.getDisplayName());
        bookMeta.setTitle("Coordinates Book");
        book.setItemMeta(bookMeta);

        return book;
    }
}
