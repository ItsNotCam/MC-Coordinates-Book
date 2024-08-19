package net.axiiom.CoordinatesBook;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

/*
    Class representing a coordinate. Contains the information:
    * World (Overworld, Nether, The End)
    * Description (name of the coordinate)
    * X Y Z position
 */
public class Coordinate
{
    private String uuid;
    private World world;
    private String name;
    private int x;
    private int y;
    private int z;


    // Construct coordinate from server location
    public Coordinate(Location _location, String _name) {
        this.world = _location.getWorld();
        this.x = _location.getBlockX();
        this.y = _location.getBlockY();
        this.z = _location.getBlockZ();
        this.name = _name;
        this.uuid = UUID.randomUUID().toString();
    }

    // Construct coordinate from inputted position
    public Coordinate(int _x, int _y, int _z, String _worldName, String _name) {
        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.world = Bukkit.getWorld(_worldName);
        this.name = _name;
        this.uuid = UUID.randomUUID().toString();
    }

    // Construct coordinate from inputted position
    public Coordinate(String _uuid, int _x, int _y, int _z, String _worldName, String _name) {
        this.uuid = _uuid;
        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.world = Bukkit.getWorld(_worldName);
        this.name = _name;
    }

    public Coordinate(String coord, String _worldName, String _description) {
        String[] coords = coord.split(" ");
        this.x = Integer.parseInt(coords[0]);
        this.y = Integer.parseInt(coords[1]);
        this.z = Integer.parseInt(coords[2]);
        this.world = Bukkit.getWorld(_worldName);
        this.name = _description;
        this.uuid = UUID.randomUUID().toString();
    }

    public Location getLocation() {
        return new Location(world,x,y,z);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String _description) {
        this.name = _description;
    }

    public String getUuid() {
        return uuid;
    }

    public World getWorld() {
        return world;
    }

    public String getWorldName() {
        switch(world.getName()) {
            case "world_nether":
                return "Nether";
            case "world_the_end":
                return "End";
            default:
                return "Overworld";
        }
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    // This can equal a Coordinate and Location object
    @Override
    public boolean equals(Object _obj) {
        if(_obj instanceof Coordinate) {
            Coordinate input = (Coordinate) _obj;
            boolean sameCoords = input.x == this.x && input.y == this.y && input.z == this.z;
            boolean sameWorld  = input.world.getName().equals(this.world.getName());
            boolean sameDesc   = input.name.equals(this.name);

            return sameCoords && sameDesc && sameWorld;
        }

        else if(_obj instanceof Location) {
            return _obj.equals(this.getLocation());
        }

        return false;
    }
}
