package net.axiiom.CoordinatesBook.Main;

import net.axiiom.CoordinatesBook.Coordinate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import static net.axiiom.CoordinatesBook.Main.Commands.*;

/*
    This class listens to the chat for any commands that match those declared in the plugin.
    When it receives a matching command, it will execute their corresponding functionality
 */
public class CommandHandler implements org.bukkit.command.CommandExecutor
{
    private final CoordinatesBookPlugin plugin;

    public CoordinatesBookPlugin getPlugin() {
        return plugin;
    }

    /*
        The list of players that have been sent coordinates. This is modified when a player accepts or denies
        a sent coordinate.
     */
    private final HashMap<UUID, Coordinate> awaitingShareResponse;

    // Initialize
    public CommandHandler(CoordinatesBookPlugin _plugin) {
        this.plugin = _plugin;
        this.awaitingShareResponse = new HashMap<>();
    }

    public void put(UUID key, Coordinate value) {
        awaitingShareResponse.put(key, value);
    }

    public Coordinate get(UUID key) {
        return awaitingShareResponse.get(key);
    }

    public boolean contains(UUID key) {
        return awaitingShareResponse.containsKey(key);
    }

    public void remove(UUID key) {
        awaitingShareResponse.remove(key);
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

        final Player player = (Player) _sender;
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
