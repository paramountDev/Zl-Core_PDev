package dev.paramountdev.zlomCore_PDev.basecommands.spawn;


import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SpawnManager {

    private static final String PATH = "spawn.location";
    private static File file;
    private static FileConfiguration config;

    static {
        setup();
    }

    private static void setup() {
        file = new File(ZlomCore_PDev.getInstance().getDataFolder(), "spawn.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public static void setSpawn(Location loc) {
        config.set(PATH + ".world", loc.getWorld().getName());
        config.set(PATH + ".x", loc.getX());
        config.set(PATH + ".y", loc.getY());
        config.set(PATH + ".z", loc.getZ());
        config.set(PATH + ".yaw", loc.getYaw());
        config.set(PATH + ".pitch", loc.getPitch());
        save();
    }

    public static Location getSpawn() {
        if (!config.contains(PATH + ".world")) return null;

        String worldName = config.getString(PATH + ".world");
        if (Bukkit.getWorld(worldName) == null) return null;

        return new Location(
                Bukkit.getWorld(worldName),
                config.getDouble(PATH + ".x"),
                config.getDouble(PATH + ".y"),
                config.getDouble(PATH + ".z"),
                (float) config.getDouble(PATH + ".yaw"),
                (float) config.getDouble(PATH + ".pitch")
        );
    }

    private static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
