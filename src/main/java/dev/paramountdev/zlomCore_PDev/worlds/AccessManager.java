package dev.paramountdev.zlomCore_PDev.worlds;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class AccessManager {
    private final ZlomCore_PDev plugin;

    public AccessManager(ZlomCore_PDev plugin) {
        this.plugin = plugin;
    }

    public List<String> getAllowed(String worldName) {
        FileConfiguration data = plugin.getWorldsConfig();
        return data.getStringList("access." + worldName);
    }

    public boolean addAccess(String worldName, String playerName) {
        List<String> allowed = getAllowed(worldName);
        if (allowed.contains(playerName)) return true;
        if (allowed.size() >= 5) return false;

        allowed.add(playerName);
        plugin.getWorldsConfig().set("access." + worldName, allowed);
        plugin.saveWorldsConfig();
        return true;
    }

    public void removeAccess(String worldName, String playerName) {
        List<String> allowed = getAllowed(worldName);
        allowed.remove(playerName);
        plugin.getWorldsConfig().set("access." + worldName, allowed);
        plugin.saveWorldsConfig();
    }

    public boolean hasAccess(String worldName, String playerName) {
        return getAllowed(worldName).contains(playerName);
    }
}
