package net.axiiom.skye_coordinatesbook.models;

import java.io.*;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/*
    Serializable model representing the Coordinate
 */
public class Coordinate extends SerializableModel implements Serializable{
    private Location location;

    public Coordinate(Location location) {
        this.location = location;
    }

    public Coordinate(int x, int y, int z, Object world) throws NullPointerException{
        World found;
        if(world instanceof World) {
        	found = (World) world;
        } else if(world instanceof UUID) {
        	found = Bukkit.getWorld((UUID) world);
        } else {
        	found = Bukkit.getWorld((String) world);
        }

        this.location = new Location(found, x, y, z);
    }

    public Coordinate(String serialized) throws IOException, ClassNotFoundException {
        Coordinate coord = (Coordinate) super.deserialize(serialized);
        this.location = coord.location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %d",
            this.location.getWorld().getUID().toString(),
            this.location.getBlockX(), this.location.getBlockY(),
            this.location.getBlockZ()
        );
    }
}
