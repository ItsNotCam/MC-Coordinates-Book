package net.axiiom.CoordinatesBook.Main;

import net.axiiom.CoordinatesBook.features.BookManager;
import net.axiiom.CoordinatesBook.utilities.Database;
import net.axiiom.CoordinatesBook.utilities.ShareInventoryListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/*
    This is the main class and entry point of the plugin.
    It initializes all the modules, dependencies, and files to make the plugin work.
 */

//TODO: IMPLEMENT USES/COOLDOWNS
//TODO: ADD RENAMING FEATURE
//TODO: ADD ENDER DRAGON:
// Custom Mob AI Guide -> https://www.spigotmc.org/threads/tutorial-creating-custom-entities-with-pathfindergoals.18519/
public final class CoordinatesBookPlugin extends JavaPlugin {
    private AutoSave autoSave;
    private ShareInventoryListener listener;

    public CommandExecutor commandExecutor;
    public Database database;
    BookManager bookManager;

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
        this.database.commit();
        this.database.disconnect();

        if(this.autoSave != null) {
            this.autoSave.cancel();
        }
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

        //Initialize SQLite Database
//        createDirectory();
        this.database = new Database(this);
        this.database.connect();

        //Initialize bookManager
        this.bookManager = database.pull(this);


        //initialize listeners
        this.listener = new ShareInventoryListener(this);
        this.getServer().getPluginManager().registerEvents(listener, this);
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

            getCommand("sharecoordinate"),
            getCommand("receivecoordinate"),
            getCommand("denycoordinate")
        };

        commandExecutor = new CommandExecutor(this);
        for (PluginCommand command : commands)
            command.setExecutor(commandExecutor);
    }

    //Helpers
    /*
        Create the directory required to set up the database and config files
     */
    private void createDirectory() {
        final String PATH = "plugins/SkyeCoordBook";

        File directory = new File(PATH);
        if(!directory.exists())
            directory.mkdirs();
    }

    //Private Classes
    /*
        Class used asynchronously to automatically commit changes to the database
     */
    private class AutoSave extends BukkitRunnable
    {
        @Override
        public void run() {
            database.commit();
            getServer().getConsoleSender().sendMessage("[CoordinatesBookPlugin] Committed changes to Database");
        }
    }
}
