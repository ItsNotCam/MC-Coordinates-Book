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
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.SQLException;
import java.util.*;

import static net.axiiom.CoordinatesBook.Main.Commands.*;

/*
    This class listens to the chat for any commands that match those declared in the plugin.
    When it receives a matching command, it will execute their corresponding functionality
 */
public class CommandExecutor implements org.bukkit.command.CommandExecutor
{
    private final CoordinatesBookPlugin plugin;

    public CoordinatesBookPlugin getPlugin() {
        return plugin;
    }

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
        if(!(_sender instanceof Player))
            return false;

        Player player = (Player) _sender;
        plugin.getLogger().info("Command: " + _command.getName());
        switch(_command.getName())
        {
            case "removecoordinate" :  return removeCoordinate(this, player,_args);
            case "compasstarget"    :  return setCompassTarget(player, _args);
            case "savecoordinate"   :  return createCoordinate(this, player,_args);
            case "fasttravel"       :  return fastTravel(this, player, _args);
            case "coords"           :  return openBook(this, player);
            case "renamecoordinate" :  return renameCoordinate(this, player, _args);
            case "sharecoordinate"  :  return shareCoordinate(this, player, _args);
            case "receivecoordinate":  return receiveCoordinate(this, player);
            case "denycoordinate"   :  return denyCoordinate(this, player);
        }

        return true;
    }
}
