package dev.paramountdev.zlomCore_PDev.galaxyeconomy.managers;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class BalanceManager {
    private final ZlomCore_PDev plugin;
    private final File file;
    private final FileConfiguration config;
    private final HashMap<UUID, Double> balances = new HashMap<>();

    public BalanceManager(ZlomCore_PDev plugin) {
        this.plugin = plugin;
        file = new File(plugin.getDataFolder(), "balances.yml");
        config = YamlConfiguration.loadConfiguration(file);
        loadBalances();
    }

    public void loadBalances() {
        if (config.getConfigurationSection("balances") == null) return;
        for (String uuid : config.getConfigurationSection("balances").getKeys(false)) {
            balances.put(UUID.fromString(uuid), config.getDouble("balances." + uuid));
        }
    }

    public void saveBalances() {
        for (UUID uuid : balances.keySet()) {
            config.set("balances." + uuid.toString(), balances.get(uuid));
        }
        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, 0.0);
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, amount);
    }

    public void addBalance(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean subtractBalance(UUID uuid, double amount) {
        if (getBalance(uuid) < amount) return false;
        setBalance(uuid, getBalance(uuid) - amount);
        return true;
    }
}
