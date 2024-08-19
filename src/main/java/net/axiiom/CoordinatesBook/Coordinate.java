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

    public Coordinate(String _uuid, int _x, int _y, int _z, String _worldName, String _name) {
        this.uuid = _uuid;
        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.world = Bukkit.getWorld(_worldName);
        this.name = _name;
    }

    public Location getLocation() {
        return new Location(world,x,y,z);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String _name) {
        this.name = _name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getWorldName() {
        switch(world.getName()) {
            case "world_nether":  return "Nether";
            case "world_the_end": return "End";
            default: return "Overworld";
        }
    }
}
