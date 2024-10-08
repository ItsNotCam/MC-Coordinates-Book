package net.axiiom.CoordinatesBook.Coordinate;

import net.axiiom.CoordinatesBook.Book.BookBuilder;
import net.axiiom.CoordinatesBook.Main.CoordinatesBookPlugin;
import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.Material;

import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import java.sql.SQLException;
import java.util.*;

// Class responsible for all of the book generation and modification

public class CoordinateManager
{
    private final CoordinatesBookPlugin plugin;
    // Maps the UUID of the player to a list of coordinates associated with them
    private final HashMap<UUID, List<Coordinate>> playerCoordinateMap;

    public CoordinateManager(CoordinatesBookPlugin _plugin) {
        plugin = _plugin;
        playerCoordinateMap = new HashMap<>();
    }

    public List<Coordinate> getCoordinates(Player _player) {
        if(playerCoordinateMap.containsKey(_player.getUniqueId())) {
            return playerCoordinateMap.get(_player.getUniqueId());
        } else {
            try {
                final List<Coordinate> coordinates = plugin.getDatabase().getPlayerCoordinates(_player.getUniqueId());
                playerCoordinateMap.put(_player.getUniqueId(), coordinates);
                return coordinates;
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }

    public void addCoordinate(UUID _playerUUID, Coordinate _coordinate) {
        List<Coordinate> coordinateList = playerCoordinateMap.getOrDefault(_playerUUID, new ArrayList<>());
        if(coordinateList.size() >= 10)
            return;

        coordinateList.add(_coordinate);
        playerCoordinateMap.put(_playerUUID, coordinateList);

      try {
        plugin.getDatabase().addPlayerToCoordinate(_playerUUID, _coordinate);
      } catch (SQLException e) {
        plugin.getLogger().warning("Failed to add player to coordinate");
      }
    }

    public boolean createCoordinate(UUID _playerUUID, Coordinate _coordinate) {
        List<Coordinate> coordinateList = (playerCoordinateMap.containsKey(_playerUUID))
            ? playerCoordinateMap.get(_playerUUID)
            : new ArrayList<>();

        if(!coordinateList.contains(_coordinate))
        {
            if(coordinateList.size() >= 10)
                return false;

            coordinateList.add(_coordinate);
            playerCoordinateMap.put(_playerUUID, coordinateList);
          try {
            plugin.getDatabase().createCoordinate(_playerUUID, _coordinate);
          } catch (SQLException e) {
            throw new RuntimeException(e);
          }

          return true;
        }

        return false;
    }

    public Coordinate getCoordinateByUUID(String _uuid) {
			return playerCoordinateMap.values().stream()
            .flatMap(List::stream)
            .filter(coordinate -> coordinate.getUuid().equals(_uuid))
            .findFirst()
            .orElse(null);
    }

    public Coordinate getCoordinateByUUID(Player _player, String _uuid) {
        if(!playerCoordinateMap.containsKey(_player.getUniqueId())) {
            return null;
        }

        Coordinate coordinate = playerCoordinateMap.get(_player.getUniqueId()).stream()
            .filter(c -> c.getUuid().equals(_uuid))
            .findFirst()
            .orElse(null);

        if(coordinate == null) {
            try {
                coordinate = plugin.getDatabase().getCoordinateFromUUID(_player, _uuid);
                if(coordinate != null) {
                    addCoordinate(_player.getUniqueId(), coordinate);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return coordinate;
    }

    public void removeCoordinate(Player _player, String _uuid) {
        UUID playerUUID = _player.getUniqueId();
        if (playerCoordinateMap.containsKey(playerUUID)) {
            List<Coordinate> coordinates = playerCoordinateMap.get(playerUUID);
            for (Coordinate coordinate : coordinates) {
                if (coordinate.getUuid().equals(_uuid)) {
                    try {
                        plugin.getDatabase().removeCoordinate(playerUUID, coordinate);
                        coordinates.remove(coordinate);
                        if(coordinates.isEmpty()) {
                            playerCoordinateMap.remove(playerUUID);
                        } else {
                            playerCoordinateMap.put(playerUUID, coordinates);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }
        }
    }

    public boolean changeCoordinateName(Player _player, String _coordinateUUID, String _newName) {
        Coordinate coordinate = getCoordinateByUUID(_coordinateUUID);
        if(coordinate != null) {
            final List<Coordinate> coordinateList = playerCoordinateMap.get(_player.getUniqueId());
            for(Coordinate coord : coordinateList) {
                if(coord.equals(coordinate)) {
                    coord.setName(_newName);
                    playerCoordinateMap.put(_player.getUniqueId(),coordinateList);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasCoordinate(Player _player, String _coordinateUUID) {
        return getCoordinates(_player).stream().anyMatch(coordinate -> coordinate.getUuid().equals(_coordinateUUID));
    }

    public boolean renameCoordinate(Player _player, String _coordinateUUID, String _newName) {
        try {
            plugin.getDatabase().renameCoordinate(_player, _coordinateUUID, _newName);
            final List<Coordinate> coordinates = playerCoordinateMap.get(_player.getUniqueId());
            for(Coordinate coordinate : coordinates) {
                if(coordinate.getUuid().equals(_coordinateUUID)) {
                    coordinate.setName(_newName);
                    plugin.getDatabase().renameCoordinate(_player, _coordinateUUID, _newName);
                    plugin.getLogger().info("Coordinate found");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean openBook(Player _player) {
        List<Coordinate> coordinates = getCoordinates(_player);
        if(coordinates.isEmpty()) {
            return false;
        }

        //Create book
        ItemStack book = createBook(coordinates);

        //Open book
        final int slot = _player.getInventory().getHeldItemSlot();
        final ItemStack old = _player.getInventory().getItem(slot);
        _player.getInventory().setItem(slot, book);

        final CraftPlayer craftPlayer = (CraftPlayer) _player;
        craftPlayer.openBook(book);

        _player.getInventory().setItem(slot, old);
        return true;
    }

    //Create book meta: https://www.spigotmc.org/wiki/interactive-books/#creating-the-book
    private ItemStack createBook(List<Coordinate> coordinates)
    {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        List<BaseComponent[]> pages = new ArrayList<>();
        pages.add(BookBuilder.getTableOfContents(coordinates).create());

        coordinates.forEach(coordinate -> pages.add(BookBuilder.buildCoordinatePage(coordinate).create()));

        if(bookMeta != null) {
            bookMeta.spigot().setPages(pages);
            bookMeta.setAuthor("Cam");
            bookMeta.setTitle("Coordinates Book");
            book.setItemMeta(bookMeta);
        }

        return book;
    }
}
