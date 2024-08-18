package net.axiiom.CoordinatesBook.features;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/*
    Class representing a coordinate. Contains the information:
    * World (Overworld, Nether, The End)
    * Description (name of the coordinate)
    * X Y Z position
 */
public class Coordinate
{
    private World world;
    private String description;
    private int x;
    private int y;
    private int z;


    // Construct coordinate from server location
    public Coordinate(Location _location, String _description) {
        this.world = _location.getWorld();
        this.x = _location.getBlockX();
        this.y = _location.getBlockY();
        this.z = _location.getBlockZ();

        this.description = "";
        for(char c : _description.toCharArray()) {
            if(c != ';' && c != ',' && c != '&' && c != ':')
                this.description += c;
        }
    }

    // Construct coordinate from inputted position
    public Coordinate(int _x, int _y, int _z, String _worldName, String _description) {
        this.x = _x;
        this.y = _y;
        this.z = _z;
        this.world = Bukkit.getWorld(_worldName);
        this.description = _description;
    }

    public Coordinate(String coord, String _worldName, String _description) {
        String[] coords = coord.split(" ");
        this.x = Integer.parseInt(coords[0]);
        this.y = Integer.parseInt(coords[1]);
        this.z = Integer.parseInt(coords[2]);
        this.world = Bukkit.getWorld(_worldName);
        this.description = _description;
    }

    public Location getLocation() {
        return new Location(world,x,y,z);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String _description) {
        this.description = _description;
    }

    // This can equal a Coordinate and Location object
    @Override
    public boolean equals(Object _obj) {
        if(_obj instanceof Coordinate) {
            Coordinate input = (Coordinate) _obj;
            boolean sameCoords = input.x == this.x && input.y == this.y && input.z == this.z;
            boolean sameWorld  = input.world.getName().equals(this.world.getName());
            boolean sameDesc   = input.description.equals(this.description);

            return sameCoords && sameDesc && sameWorld;
        }

        else if(_obj instanceof Location) {
            return _obj.equals(this.getLocation());
        }

        return false;
    }
}
