package com.lothus.engines.bedwars.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationUtils {

    public static String getData(Location location) {
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        String world = location.getWorld().getName();
        return x + ";" + y + ";" + z + ";" + yaw + ";" + pitch + ";" + world;
    }

    public static Location getLocation(String data) {
        String[] s = data.split(";");
        double x = Double.parseDouble(s[0]);
        double y = Double.parseDouble(s[1]);
        double z = Double.parseDouble(s[2]);
        float yaw = Float.parseFloat(s[3]);
        float pitch = Float.parseFloat(s[4]);
        World world = Bukkit.getWorld(s[5]);
        Location location = new Location(world, x, y, z);
        location.setYaw(yaw);
        location.setPitch(pitch);
        return location;
    }
}