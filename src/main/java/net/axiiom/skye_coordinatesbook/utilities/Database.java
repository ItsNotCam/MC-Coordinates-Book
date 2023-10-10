package net.axiiom.skye_coordinatesbook.utilities;

import static net.axiiom.skye_coordinatesbook.utilities.Queries.*;
import net.axiiom.skye_coordinatesbook.Main.CoordinatesBookPlugin;
import net.axiiom.skye_coordinatesbook.features.BookManager;
import net.axiiom.skye_coordinatesbook.features.Coordinate;
import org.bukkit.Location;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


// Interacts with the database that stores player coordinates
public class Database
{
    private final String DB_PATH = "jdbc:sqlite:plugins/SkyeCoordBook/CoordDB.sqlite";

    private CoordinatesBookPlugin plugin;
    private Connection connection;

    public Database(CoordinatesBookPlugin _plugin) {
        plugin = _plugin;
    }

    public boolean connect() {
        try {
            //connect
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_PATH);
            this.connection.setAutoCommit(false);

            //initialize
            this.connection.createStatement().execute(CREATE_BASE.query());

            //commit changes
            return this.commit();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean commit() {
        try {
            this.connection.commit();
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Pulls the information within the database and stores it in memory for fast lookups
    public BookManager pull(CoordinatesBookPlugin _plugin)
    {
        //check to see if connection exists
        boolean isConnected = false;
        try { isConnected = (this.connection != null && !this.connection.isClosed()); }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        BookManager bookManager = new BookManager(_plugin);
        try {
            if(!isConnected) throw new SQLException("Not connected to database");
            else System.out.println("Connected to database");

            //Gets all tables from database
            ResultSet databaseRS = this.connection.createStatement().executeQuery(GET_ALL_TABLES.query());
            while(databaseRS.next()) {
                String dbName = databaseRS.getString(1);

                //Selects all rows from current table
                ResultSet tableRS = this.connection.createStatement().executeQuery(SELECT_ALL_FROM.query(dbName));
                while(tableRS.next()) {
                    Coordinate coordinate = getCoordFromRS(tableRS);
                    UUID uuid = UUID.fromString(dbName);

                    bookManager.addCoordinate(uuid,coordinate);
                }
            }

            return bookManager;
        } catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean removeCoordinate(UUID _uuid, Coordinate _coordinate)
    {
        //get data
        String tableName = _uuid.toString();
        Location location = _coordinate.getLocation();
        Object[] args = new Object[] {
                tableName,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld().getName(),
                _coordinate.getDescription()
        };

        String REMOVE = REMOVE_COORDINATE_FROM_TABLE.query(args);
        try {
            this.connection.createStatement().execute(REMOVE);
            this.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean addCoordinates(BookManager _bookManager)
    {
        boolean completeSuccess = true;
        HashMap<UUID, List<Coordinate>> coordinatesHash = _bookManager.getCoordinates();
        for(UUID uuid : coordinatesHash.keySet())
        {
            List<Coordinate> coordinates = coordinatesHash.get(uuid);
            for(Coordinate coord : coordinates) {
                if(!addCoordinate(uuid, coord))
                    completeSuccess = false;
            }
        }

        return completeSuccess;
    }

    public boolean addCoordinate(UUID _uuid, Coordinate _coordinate) {
        //get data
        String tableName = _uuid.toString();
        Location location = _coordinate.getLocation();
        Object[] args = new Object[] {
                tableName,
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld().getName(),
                _coordinate.getDescription()
        };

        //queries
        String CREATE = CREATE_TABLE_IF_NOT_EXISTS.query(tableName);
        String CHECK  = CHECK_ROW_EXISTS.query(args);
        String INSERT = INSERT_COORDINATE_INTO_TABLE.query(args);

        try {

            this.connection.createStatement().execute(CREATE);
            if(this.connection.createStatement().execute(INSERT))
                this.commit();
            /*
            boolean notDuplicate = this.connection.createStatement().executeQuery(CHECK).isClosed();
            if(notDuplicate) {
                System.out.println("No duplicate exists - " + INSERT);
                return this.connection.createStatement().execute(INSERT);
            } else {
                System.out.println("Duplicate exists");
            }
            this.commit();*/

            return true;

        } catch(SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean disconnect() {
        try {
            this.connection.commit();
            this.connection.close();
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Generates a coordinate from an SQL response
    private Coordinate getCoordFromRS(ResultSet _rs) throws SQLException {
        int x = _rs.getInt(1);
        int y = _rs.getInt(2);
        int z = _rs.getInt(3);

        String worldName = _rs.getString("WorldName");
        String description = _rs.getString("Description");

        return new Coordinate(x,y,z,worldName,description);
    }
}
