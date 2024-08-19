package net.axiiom.CoordinatesBook.Main;

import net.axiiom.CoordinatesBook.Coordinate.CoordinateManager;
import net.axiiom.CoordinatesBook.Listeners.BookListener;
import net.axiiom.CoordinatesBook.Utilities.Database;
import net.axiiom.CoordinatesBook.Listeners.ShareListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/*
    This is the main class and entry point of the plugin.
    It initializes all the modules, dependencies, and files to make the plugin work.
 */

//TODO: IMPLEMENT USES/COOLDOWNS
//TODO: ADD RENAMING FEATURE
//TODO: ADD ENDER DRAGON:
// Custom Mob AI Guide -> https://www.spigotmc.org/threads/tutorial-creating-custom-entities-with-pathfindergoals.18519/
public final class CoordinatesBookPlugin extends JavaPlugin {
	  private CommandExecutor commandExecutor;
    private Database database;
    private CoordinateManager coordinateManager;

    /*
        Override onEnable behavior from superclass - this registers the commands that the
        plugin requires, and initializes required modules
     */
    @Override
    public void onEnable() {
        registerCommands();
        initializeModules();
    }

    /*
        When the plugin is disabled, commit any changes to the database, disconnect from it, and
        stop the auto commit process.
     */
    @Override
    public void onDisable() {
        this.database.close();

//        if(this.autoSave != null) {
//            this.autoSave.cancel();
//        }
    }

    /*
        Initializes the modules required for the plugin to run

        1. Pull information from config file
        2. Initializes and connects to the database (database stores player coordinate information)
        3. Initializes the book manager class (this class manages the entire book system)
        4. Initializes the listener and events required by the API
     */
    private void initializeModules() {
        //Pull config.yml
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.database = new Database(this);
        this.database.connect();

        //Initialize bookManager
        this.coordinateManager = new CoordinateManager(this);

        //initialize listeners
        this.getServer().getPluginManager().registerEvents(new ShareListener(this), this);
        this.getServer().getPluginManager().registerEvents(new BookListener(this), this);
    }

    /*
        Registers the commands that are enumerated in the plugin definition.
        These commands are entered into the chat within the game in order to allow the user to interact with
        their savec coordinates
     */
    private void registerCommands() {
        PluginCommand[] commands = new PluginCommand[] {
            getCommand("coords"),
            getCommand("fasttravel"),
            getCommand("savecoordinate"),
            getCommand("removecoordinate"),
            getCommand("compasstarget"),
          getCommand("renamecoordinate"),

            getCommand("sharecoordinate"),
            getCommand("receivecoordinate"),
            getCommand("denycoordinate")
        };

        commandExecutor = new CommandExecutor(this);
        for (PluginCommand command : commands) {
					assert command != null;
					command.setExecutor(commandExecutor);
				}
    }

    public Database getDatabase() {
        return database;
    }

    public CoordinateManager getCoordinateManager() {
        return coordinateManager;
    }

    public CommandExecutor getCommandHandler() {
        return commandExecutor;
    }
}
