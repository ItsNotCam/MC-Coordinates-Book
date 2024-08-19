package net.axiiom.CoordinatesBook.Utilities;

import net.axiiom.CoordinatesBook.Main.CoordinatesBookPlugin;
import net.axiiom.CoordinatesBook.Coordinate;
import org.bukkit.entity.Player;

import java.io.*;
import java.sql.*;
import java.util.*;

public class Database
{
    private final String DB_PATH;
    private final String INIT_DB_FILE;

    private final CoordinatesBookPlugin plugin;
    private Connection connection;

    public Database(CoordinatesBookPlugin _plugin) {
        plugin = _plugin;

        ENVReader env = new ENVReader();
        this.DB_PATH = env.get("DB_PATH");
        this.INIT_DB_FILE = env.get("INIT_DB_FILENAME");
		}

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_PATH);

            // open resources file 'initdb.sql' and execute the commands
            this.plugin.getLogger().info("Connected to database");

            Statement statement = this.connection.createStatement();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(INIT_DB_FILE);
            if(inputStream == null) {
                this.plugin.getLogger().severe("Failed to open init.db file");
                return;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                    if(line.contains(";")) {
                        statement.addBatch(sb.toString());
                        sb = new StringBuilder();
                    } else {
                        sb.append(System.lineSeparator());
                    }
                }

            }
            statement.executeBatch();
            this.plugin.getLogger().info("Database initialized");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            this.plugin.getLogger().severe("Failed to find JDBC driver");
        } catch (SQLException e) {
            e.printStackTrace();
            this.plugin.getLogger().severe("Failed to connect to database");
        } catch (IOException e) {
            e.printStackTrace();
            this.plugin.getLogger().severe("Failed to open init.db file");
        }
    }

    public void close() {
        try { this.connection.close(); }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }

    // Pulls the information within the database and stores it in memory for fast lookups
    public List<Coordinate> getPlayerCoordinates(UUID _playerUUID) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement(
            "SELECT Coordinate.*, Coordinate_Users.NAME " +
            "FROM Coordinate " +
            "INNER JOIN Coordinate_Users ON Coordinate.UUID = Coordinate_Users.COORDINATE_UUID " +
            "WHERE Coordinate_Users.PLAYER_UUID = ?"
        );
        statement.setString(1, _playerUUID.toString());

        ResultSet rs = statement.executeQuery();
        List<Coordinate> coordinates = new ArrayList<>();
        while(rs.next()) {
            coordinates.add(getCoordFromRS(rs));
        }

        return coordinates;
    }

    public void removeCoordinate(UUID _playerUUID, Coordinate _coordinate) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = this.connection.prepareStatement(
              "DELETE FROM Coordinate_Users WHERE PLAYER_UUID = ? AND COORDINATE_UUID = ?;"
            );
            statement.setString(1, _playerUUID.toString());
            statement.setString(2, _coordinate.getUuid());
            statement.execute();
        } finally {
            if(statement != null && !statement.isClosed()) {
                statement.close();
            }
        }
    }

    public void createCoordinate(UUID _playerUUID, Coordinate _coordinate) throws SQLException {
        PreparedStatement statement = null;
        try {
            String sql = "INSERT INTO Coordinate VALUES (?, ?, ?, ?, ?);";
            statement = this.connection.prepareStatement(sql);
            statement.setString(1, _coordinate.getUuid());
            statement.setInt(2, _coordinate.getLocation().getBlockX());
            statement.setInt(3, _coordinate.getLocation().getBlockY());
            statement.setInt(4, _coordinate.getLocation().getBlockZ());
            statement.setString(5, Objects.requireNonNull(_coordinate.getLocation().getWorld()).getName());

            statement.execute();
            statement.close();

            addPlayerToCoordinate(_playerUUID, _coordinate);
        } finally {
            if(statement != null && !statement.isClosed()) {
                statement.close();
            }
        }
    }

    public void addPlayerToCoordinate(UUID _playerUUID, Coordinate _coordinate) throws SQLException {
        PreparedStatement statement = null;
        try {
            String sql = "INSERT INTO Coordinate_Users VALUES (?, ?, ?);";

            statement = this.connection.prepareStatement(sql);
            statement.setString(1, _playerUUID.toString());
            statement.setString(2, _coordinate.getUuid());
            statement.setString(3, _coordinate.getName());
            statement.execute();

            statement.close();
        } finally {
            if(statement != null && !statement.isClosed()) {
                statement.close();
            }
        }
    }

    // Generates a coordinate from an SQL response
    private Coordinate getCoordFromRS(ResultSet _rs) throws SQLException {
        String uuid = _rs.getString(1);
        int x = _rs.getInt(2);
        int y = _rs.getInt(3);
        int z = _rs.getInt(4);
        String worldName = _rs.getString(5);
        String name = _rs.getString(6);

        return new Coordinate(uuid, x,y,z,worldName,name);
    }

    public Coordinate getCoordinateFromUUID(Player player, String coordUUid) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement("" +
            "SELECT Coordinate.*, Coordinate_Users.Name " +
            "FROM Coordinate " +
            "INNER JOIN Coordinate_Users ON Coordinate.UUID = Coordinate_Users.COORDINATE_UUID " +
            "WHERE PLAYER_UUID = ? AND COORDINATE_UUID = ?;"
        );
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, coordUUid);

        ResultSet rs = statement.executeQuery();
        if(rs.next()) {
            return getCoordFromRS(rs);
        }

        return null;
    }

    public void renameCoordinate(Player _player, String _cooordinateUUID, String _newName) throws SQLException {
        PreparedStatement statement = this.connection.prepareStatement("" +
          "UPDATE Coordinate_Users SET NAME = ? WHERE PLAYER_UUID = ? AND COORDINATE_UUID = ?"
        );
        statement.setString(1, _newName);
        statement.setString(2, _player.getUniqueId().toString());
        statement.setString(3, _cooordinateUUID);

        statement.execute();
		}
}
