package net.axiiom.skye_coordinatesbook.features;

import net.axiiom.skye_coordinatesbook.Main.CoordinatesBookPlugin;
import net.axiiom.skye_coordinatesbook.utilities.BookBuilder;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutOpenBook;
import net.minecraft.world.EnumHand;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

// Class responsible for all of the book generation and modification

public class BookManager
{
    private CoordinatesBookPlugin plugin;
    // Maps the UUID of the player to the UUID of the coordinate
    private HashMap<UUID, UUID> validators;
    // Maps the UUID of the player to a list of coordinates associated with them
    private HashMap<UUID, List<Coordinate>> coordinatesHash;

    public BookManager(CoordinatesBookPlugin _plugin) {
        this.plugin = _plugin;

        this.coordinatesHash = new HashMap<>();
        this.validators = new HashMap<>();
    }

    // Validates that the player has a coordinate with this UUID in the buffer
    public boolean isValidRequest(UUID _playerUUID, UUID _validUUID) {
        return this.validators.containsKey(_playerUUID) && validators.get(_playerUUID).equals(_validUUID);
    }

    public HashMap<UUID, List<Coordinate>> getCoordinates() {
        return this.coordinatesHash;
    }

    // Adds a coordinate to a player's book
    public boolean addCoordinate(Player _player, Coordinate _coordinate) {
        UUID playerUUID = _player.getUniqueId();
        return addCoordinate(playerUUID,_coordinate);
    }

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
            this.plugin.database.addCoordinate(_playerUUID, _coordinate);
            return true;
        }

        return false;
    }

    // Removes a coordinate from a player's book
    public void removeCoordinate(Player _player, Location _location) {
        UUID playerUUID = _player.getUniqueId();
        if (this.coordinatesHash.containsKey(playerUUID)) {
            List<Coordinate> coordinates = this.coordinatesHash.get(playerUUID);

            for (int i = 0; i < coordinates.size(); i++) {
                if (coordinates.get(i).equals(_location)) {
                    this.plugin.database.removeCoordinate(playerUUID, coordinates.get(i));
                    coordinates.remove(i);
                    break;
                }
            }

            if(coordinates.size() == 0) {
                this.coordinatesHash.remove(playerUUID);
            } else  {
                this.coordinatesHash.put(playerUUID, coordinates);
            }
        }
    }

    // Retrieves a coordinate from its description
    public Coordinate getCoordinateByDescription(Player _player, String _description) {
        if(this.coordinatesHash.containsKey(_player.getUniqueId())) {
            List<Coordinate> coordinateList = this.coordinatesHash.get(_player.getUniqueId());
            for(int i = 0; i < coordinateList.size(); i++) {
                if(coordinateList.get(i).getDescription().equals(_description)) {
                    return coordinateList.get(i);
                }
            }
        }

        return null;
    }

    public boolean hasCoordinate(Player _player, String _description) {
        return getCoordinateByDescription(_player, _description) != null;
    }

    public boolean changeDescription(Player _player, String _description, String _newDescription) {
        Coordinate oldCoordinate = getCoordinateByDescription(_player, _description);
        if(oldCoordinate != null) {
            List<Coordinate> coordinateList = coordinatesHash.get(_player.getUniqueId());
            for(Coordinate coord : coordinateList) {
                if(coord.equals(oldCoordinate)) {
                    coord.setDescription(_newDescription);
                    coordinatesHash.put(_player.getUniqueId(),coordinateList);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean openBook(Player _player) {
        if (!this.coordinatesHash.containsKey(_player.getUniqueId()))
            return false;

        this.validators.put(_player.getUniqueId(),UUID.randomUUID());

        //Create book
        ItemStack book = createBook(_player.getUniqueId());

        //Open book
        int slot = _player.getInventory().getHeldItemSlot();
        ItemStack old = _player.getInventory().getItem(slot);
        _player.getInventory().setItem(slot, book);

        // idk if this will work lol
        CraftPlayer craftPlayer = (CraftPlayer) _player;
        craftPlayer.openBook(book);

        _player.getInventory().setItem(slot, old);
        return true;
    }


    //PRIVATE METHODS

    //Create book meta: https://www.spigotmc.org/wiki/interactive-books/#creating-the-book
    private ItemStack createBook(UUID _playerUUID)
    {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

        List<Coordinate> coordinateList = this.coordinatesHash.get(_playerUUID);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();

        BookBuilder bb = new BookBuilder();
        List<BaseComponent[]> pages = new ArrayList<>();

        for(Coordinate coordinate : coordinateList)
        {
            String validatorUUID = this.validators.get(_playerUUID).toString();
            ComponentBuilder page = bb.buildCoordinatePage(coordinate, validatorUUID);
            pages.add(page.create());
        }

        pages.add(0,bb.getTableOfContents().create());
        bookMeta.spigot().setPages(pages);
        bookMeta.setAuthor("Axii0m");
        bookMeta.setTitle("Interactive Book");

        book.setItemMeta(bookMeta);
        return book;
    }
}
