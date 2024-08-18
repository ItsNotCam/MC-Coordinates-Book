package net.axiiom.CoordinatesBook.Main;

import net.axiiom.CoordinatesBook.Coordinate;
import net.axiiom.CoordinatesBook.Utilities.NBT.NBTTag;
import net.axiiom.CoordinatesBook.Utilities.NBT.NBTWrapper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/*
    This class listens to the chat for any commands that match those declared in the plugin.
    When it receives a matching command, it will execute their corresponding functionality
 */
public class CommandExecutor implements org.bukkit.command.CommandExecutor
{
    private final CoordinatesBookPlugin plugin;

    /*
        The list of players that have been sent coordinates. This is modified when a player accepts or denies
        a sent coordinate.
     */
    public HashMap<UUID, Coordinate> awaitingShareResponse;

    // Initialize
    public CommandExecutor(CoordinatesBookPlugin _plugin) {
        this.plugin = _plugin;
        this.awaitingShareResponse = new HashMap<>();
    }

    /*
        Listens for any commands defined in the plugin's metadata
     */
    @Override
    public boolean onCommand(CommandSender _sender, Command _command, String _label, String[] _args)
    {
        /*
            Only runs if the command sender is a player - this means someone operating on the server cli cannot
            interact with this system.

            Returns a boolean which represents whether the command executed successfully
        */
        if(_sender instanceof Player)
        {
            Player player = (Player)_sender;
            switch(_command.getName())
            {
                case "removecoordinate" :  return removeCoordinate(player,_args);
                case "compasstarget"    :  return compassTarget(player, _args);
                case "savecoordinate"   :  return createCoordinate(player,_args);
                case "fasttravel"       :  return fastTravel(player, _args);
                case "coords"           :  return openBook(player);
                case "rename"           :  return renameCoordinate(player, _args);
                case "sharecoordinate"  :  return share(player, _args);
                case "receivecoordinate":  return receive(player);
                case "denycoordinate"   :  return deny(player);
            }
        }

        return false;
    }

    /*
        Renames the coordinate of the player.
        Takes in the old description and the new description and replaces it
     */
    private boolean renameCoordinate(Player _player, String[] _args) {
        if(_args.length > 2) {
            String description = _args[0];
            String newDescription = _args[1];

            // If the player has a coordinate with this description, change it
            if(plugin.bookManager.hasCoordinate(_player, description)) {
                return plugin.bookManager.changeDescription(_player, description, newDescription);
            }
        }

        return false;
    }

    /*
        This function is run when a player accepts a coordinate share request.

        When a player sends a coordinate to another player, they are presented with a dialog in which they can
        accept the coordinate or deny it. This handles the accept case.
     */
    private boolean receive(Player _player) {
        if(awaitingShareResponse.containsKey(_player.getUniqueId())) {
            plugin.bookManager.addCoordinate(_player,awaitingShareResponse.get(_player.getUniqueId()));
            awaitingShareResponse.remove(_player.getUniqueId());

            _player.sendMessage(ChatColor.GREEN + "Saved Coordinate as: _received_");
            _player.sendMessage("You can rename it by typing " + ChatColor.AQUA + "/coords" + ChatColor.WHITE + ", navigating to its page, and clicking "
                + ChatColor.AQUA + "rename");
            return true;
        }

        return false;
    }

    /*
        This function is run when a player denies a coordinate share request.

        When a player sends a coordinate to another player, they are presented with a dialog in which they can
        accept the coordinate or deny it. This handles the denial case.
     */
    private boolean deny(Player _player) {
        if(awaitingShareResponse.containsKey(_player.getUniqueId())) {
            awaitingShareResponse.remove(_player.getUniqueId());
            return true;
        }

        return false;
    }


    /*
        This function is run when a player wishes to share a coordinate with another player

        Takes in 5 arguments:
        * UUID to validate the share request
        * The dimension that the coordinate is in (Overworld, Nether, The End)
        * X coordinate component
        * Y coordinate component
        * Z coordinate component
     */
    private boolean share(Player _player, String[] _args) {
        if(_args.length == 5 && checkValidRequest(_player.getUniqueId(), _args[0])) {
            String validatorUUID = _args[0];
            String worldName = _args[4];
            String coords = String.format("%s %s %s", _args[1], _args[2], _args[3]);

            // This creates a new array with all of the players except for the player sending the command
            ArrayList<String> players = new ArrayList<>();
            plugin.getServer().getOnlinePlayers().forEach(p -> players.add(p.getName()));
            players.remove(_player.getName());

            // this is the "large chest" inventory size
            int size = 54;
            Inventory shareInventory = Bukkit.createInventory(null, size, "Share Your Coordinate");

            //fill spacer slots
            ArrayList<Integer> spacerSlots = new ArrayList<>();
            for(int i = 4; i+9 <= size; i+=9) {
                shareInventory.setItem(i, new ItemStack(Material.PURPLE_STAINED_GLASS_PANE));
                spacerSlots.add(i);
            }

            /*
                Fill empty spaces with player heads.
                When a player head is clicked, the coordinate is sent
             */
            ArrayList<Integer> userIndexes = new ArrayList(Arrays.asList(0,1,2,3,9,10,11,12,18,19,20,21,27,28,29,30,36,37,38,39,45,46,47,48));
            for(int i = 0, playersIndex = 0; i < size && playersIndex < players.size(); i++) {
                if(!userIndexes.contains(i) && !spacerSlots.contains(i)){
                    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
                        skullMeta.setOwningPlayer(Bukkit.getPlayer(players.get(playersIndex)));
                    playerHead.setItemMeta(skullMeta);
                    shareInventory.setItem(i,playerHead);

                    playersIndex++;
                }
            }

            // Add item containing the coordinate and assign it to inventory slot 49
            ItemStack coord = new ItemStack(Material.BOOK);
            NBTWrapper.setNBTTags(new NBTTag[] {
              new NBTTag("validatorUUID", validatorUUID),
              new NBTTag("coords", coords),
              new NBTTag("worldName", worldName)
            }, coord);
            shareInventory.setItem(49, coord);

            // fill remaining slots with nothing
            for(int i = 0; i < 54; i++) {
                if(shareInventory.getItem(i) == null)
                    shareInventory.setItem(i,new ItemStack(Material.AIR));
            }

            shareInventory.setItem(22, new ItemStack(Material.LIME_CONCRETE));
            _player.openInventory(shareInventory);
        }

        return false;
    }


    /*
        Removes a coordinate from a player's coordinates list

        Takes in 5 arguments:
        * UUID to validate the share request
        * The dimension that the coordinate is in (Overworld, Nether, The End)
        * X coordinate component
        * Y coordinate component
        * Z coordinate component

        Returns a boolean indicating if the book was successfully removed
     */
    private boolean removeCoordinate(Player _player, String[] _args)
    {
        if(_args.length == 5)
        {
            String uuid = _args[0];
            this.plugin.bookManager.removeCoordinate(_player, uuid);
            this.plugin.bookManager.openBook(_player);

            return true;
        }

        return false;
    }

    /*
        Set the player's compass to point at the coordinate's location
     */
    private boolean compassTarget(Player _player, String[] _args)
    {
        if(_args.length == 5 && checkValidRequest(_player.getUniqueId(), _args[0]))
        {
            boolean sameWorld = _player.getWorld().getName().equals(_args[4]);
            if(sameWorld) {
                int x = Integer.parseInt(_args[1]);
                int y = Integer.parseInt(_args[2]);
                int z = Integer.parseInt(_args[3]);
                _player.setCompassTarget(new Location(_player.getWorld(),x,y,z));
                return true;
            }

            _player.sendMessage(ChatColor.RED + "You must be in the same world as that coordinate in order "
                    + "to set your compass target to it.");
        }

        return false;
    }

    /*
        Takes in the current world, location, and inputted description.
        Creates a new coordinate in the database.

        Takes in 1 or more arguments that are used as the name of the coordinate
     */
    private boolean createCoordinate(Player _player, String[] _args)
    {
        if(_args.length > 0) {
            /*
                Gets the description given by the user.
                Removes the leading and trailing brackets as well as the commas created by the Arrays.toString() method
             */
            String name = Arrays.toString(_args)
                    .substring(1, Arrays.toString(_args).length() - 1)
                    .replaceAll(",", "");

            // Creates a new coordinate based on the player's current location and description
            Coordinate coordinate = new Coordinate(_player.getLocation(), name);
            boolean successful = this.plugin.bookManager.createCoordinate(_player.getUniqueId(), coordinate);

            if(!successful) _player.sendMessage(ChatColor.RED + "You cannot store more than 10 coordinates");
            else _player.sendMessage(ChatColor.GREEN + "Saved new coordinate!\n"
                    + ChatColor.GRAY + "" + ChatColor.ITALIC + "Use: " + ChatColor.AQUA + "/coords"
                    + ChatColor.GRAY + "" + ChatColor.ITALIC + " to view your saved coordinates");
        } else {
            _player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC
                    + "Usage: " + ChatColor.GOLD + "/savecoord <name>"
            );
        }

        return _args.length > 0;
    }

    /*
        Teleports the player to the coordinate's location

        Takes in 5 arguments:
        * UUID to validate the share request
        * The dimension that the coordinate is in (Overworld, Nether, The End)
        * X coordinate component
        * Y coordinate component
        * Z coordinate component
     */
    private boolean fastTravel(Player _player, String[] _args)
    {
        if(_args.length == 5 && checkValidRequest(_player.getUniqueId(), _args[0]))
        {
            World world = this.plugin.getServer().getWorld(_args[4]);
            if(world == null) return false;

            int x = Integer.parseInt(_args[1]);
            int y = Integer.parseInt(_args[2]);
            int z = Integer.parseInt(_args[3]);
            Location tpLocation = new Location(world, x, y, z);

            _player.teleport(tpLocation);
            return true;
        }

        return false;
    }

    /*
        Generates a book, fills it with the coordinates that of the player, and forecfully opens
        it on the client side
     */
    private boolean openBook(Player _player) {
        HashMap<UUID, List<Coordinate>> coords = this.plugin.bookManager.getCoordinates();
        if(coords.containsKey(_player.getUniqueId()) && coords.get(_player.getUniqueId()).size() > 0) {
            System.out.println("Found player and opening book...");
            return this.plugin.bookManager.openBook(_player);
        }

        _player.sendMessage(ChatColor.RED + "" + ChatColor.ITALIC + "First save a " +
                "location with" + ChatColor.GOLD + " /savecoord ");
        return true;
    }

    // Helper
    /*
        Validates that the UUID is valid
     */
    private boolean checkValidRequest(UUID _player, String _uuid) {
        UUID validatorUUID;
        try {
            validatorUUID = UUID.fromString(_uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }

//        return this.plugin.bookManager.isValidRequest(_player, validatorUUID);
    return true;
    }
}
