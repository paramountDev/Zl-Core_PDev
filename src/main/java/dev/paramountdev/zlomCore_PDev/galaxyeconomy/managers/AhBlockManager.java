package dev.paramountdev.zlomCore_PDev.galaxyeconomy.managers;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class AhBlockManager {
    private final ZlomCore_PDev plugin;
    private final File file;
    private final FileConfiguration config;
    private final HashMap<UUID, Long> blocks = new HashMap<>();

    public AhBlockManager(ZlomCore_PDev plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "ahblocks.yml");
        config = YamlConfiguration.loadConfiguration(file);
        loadBlocks();
    }

    public void loadBlocks() {
        if (config.getConfigurationSection("blocks") == null) return;
        for (String uuid : config.getConfigurationSection("blocks").getKeys(false)) {
            blocks.put(UUID.fromString(uuid), config.getLong("blocks." + uuid));
        }
    }

    public void saveBlocks() {
        for (UUID uuid : blocks.keySet()) {
            config.set("blocks." + uuid.toString(), blocks.get(uuid));
        }
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void block(UUID uuid, long until) {
        blocks.put(uuid, until);
    }

    public boolean isBlocked(UUID uuid) {
        return blocks.containsKey(uuid) && blocks.get(uuid) > System.currentTimeMillis();
    }

    public String getRemaining(UUID uuid) {
        long millis = blocks.getOrDefault(uuid, 0L) - System.currentTimeMillis();
        return millis > 0 ? (millis / 1000 / 60) + " мин." : "0 мин.";
    }
}
