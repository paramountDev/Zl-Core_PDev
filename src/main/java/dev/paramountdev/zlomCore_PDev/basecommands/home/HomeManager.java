package dev.paramountdev.zlomCore_PDev.basecommands.home;


import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class HomeManager {

    private static File file;
    private static FileConfiguration config;

    static {
        setup();
    }

    private static void setup() {
        file = new File(ZlomCore_PDev.getInstance().getDataFolder(), "homes.yml");
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

    public static void setHome(UUID uuid, Location loc) {
        String path = "homes." + uuid.toString();
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
        save();
    }

    public static Location getHome(UUID uuid) {
        String path = "homes." + uuid.toString();
        if (!config.contains(path + ".world")) return null;

        String worldName = config.getString(path + ".world");
        if (Bukkit.getWorld(worldName) == null) return null;

        return new Location(
                Bukkit.getWorld(worldName),
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z"),
                (float) config.getDouble(path + ".yaw"),
                (float) config.getDouble(path + ".pitch")
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

