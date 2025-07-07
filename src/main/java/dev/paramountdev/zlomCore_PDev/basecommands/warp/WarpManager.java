package dev.paramountdev.zlomCore_PDev.basecommands.warp;


import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WarpManager {

    private static final Map<String, WarpData> warps = new HashMap<>();
    private static final File warpFile = new File(ZlomCore_PDev.getInstance().getDataFolder(), "warps.yml");
    private static YamlConfiguration config;

    public static void loadWarps() {
        if (!warpFile.exists()) {
            try {
                warpFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(warpFile);
        warps.clear();
        for (String name : config.getKeys(false)) {
            String world = config.getString(name + ".world");
            double x = config.getDouble(name + ".x");
            double y = config.getDouble(name + ".y");
            double z = config.getDouble(name + ".z");
            float yaw = (float) config.getDouble(name + ".yaw");
            float pitch = (float) config.getDouble(name + ".pitch");
            double cost = config.getDouble(name + ".cost");

            World w = Bukkit.getWorld(world);
            if (w != null) {
                warps.put(name.toLowerCase(), new WarpData(new Location(w, x, y, z, yaw, pitch), cost));
            }
        }
    }

    public static boolean createWarp(String name, Location loc, double cost) {
        if (warps.containsKey(name.toLowerCase())) return false;
        warps.put(name.toLowerCase(), new WarpData(loc, cost));

        config.set(name + ".world", loc.getWorld().getName());
        config.set(name + ".x", loc.getX());
        config.set(name + ".y", loc.getY());
        config.set(name + ".z", loc.getZ());
        config.set(name + ".yaw", loc.getYaw());
        config.set(name + ".pitch", loc.getPitch());
        config.set(name + ".cost", cost);

        try {
            config.save(warpFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static WarpData getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public static Set<String> getAllWarpNames() {
        return warps.keySet();
    }

    public static class WarpData {
        public final Location location;
        public final double cost;

        public WarpData(Location location, double cost) {
            this.location = location;
            this.cost = cost;
        }
    }
}
