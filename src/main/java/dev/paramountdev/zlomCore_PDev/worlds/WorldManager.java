package dev.paramountdev.zlomCore_PDev.worlds;


import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WorldManager {
    private final ZlomCore_PDev plugin;

    public WorldManager(ZlomCore_PDev plugin) {
        this.plugin = plugin;
    }

    public void createWorld(Player player, String name) {
        String worldName = "pworld_" + name.toLowerCase();
        if (Bukkit.getWorld(worldName) != null) {
            player.sendMessage("§cМир с таким именем уже существует.");
            return;
        }

        double cost = plugin.getConfig().getDouble("world.creation-cost", 500.0);
        Economy econ = plugin.getEconomy();
        if (econ != null && !econ.has(player, cost)) {
            player.sendMessage(plugin.getConfig().getString("messages.not-enough-money-world")
                    .replace("%cost%", String.valueOf(cost)).replace("&", "§"));
            return;
        }

        if (econ != null) econ.withdrawPlayer(player, cost);

        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        creator.generateStructures(false);

        World world = creator.createWorld();
        int size = plugin.getConfig().getInt("world.size", 1000);
        world.setSpawnLocation(size / 2, 100, size / 2);
        world.getWorldBorder().setCenter(size / 2.0, size / 2.0);
        world.getWorldBorder().setSize(size);

        player.teleport(world.getSpawnLocation());
        player.sendMessage(plugin.getConfig().getString("messages.world-created")
                .replace("%world%", name).replace("&", "§"));

        // Сохраняем в worlds.yml
        FileConfiguration worlds = plugin.getWorldsConfig();
        worlds.set("worlds." + player.getUniqueId(), worldName);
        plugin.saveWorldsConfig();
    }


    public void requestJoin(Player requester, String worldName) {
        String fullName = "pworld_" + worldName.toLowerCase();
        World world = Bukkit.getWorld(fullName);
        if (world == null) {
            requester.sendMessage("§cМир не найден.");
            return;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(worldName);
        if (!owner.isOnline()) {
            requester.sendMessage("§cВладелец мира не в сети.");
            return;
        }

        Player ownerPlayer = owner.getPlayer();
        plugin.getRequestManager().addRequest(ownerPlayer, requester);
        ownerPlayer.sendMessage("§eИгрок §b" + requester.getName() + " §eхочет подключиться к вашему миру. Используйте §a/pworld accept " + requester.getName() + "§e или §c/pworld deny " + requester.getName());
        requester.sendMessage("§aЗапрос отправлен владельцу мира.");
    }

    public void teleportToOwnWorld(Player player, Map<UUID, Location> lastLocation) {
        UUID uuid = player.getUniqueId();
        FileConfiguration worlds = plugin.getWorldsConfig();
        String worldName = worlds.getString("worlds." + uuid);

        if (worldName == null || Bukkit.getWorld(worldName) == null) {
            player.sendMessage("§cВаш личный мир не найден.");
            return;
        }

        lastLocation.put(uuid, player.getLocation());
        World world = Bukkit.getWorld(worldName);
        player.teleport(world.getSpawnLocation());
        player.sendMessage(plugin.getConfig().getString("messages.teleported-to-own").replace("&", "§"));
    }


    public void returnBack(Player player, Location lastLocation) {
        String mainWorldName = plugin.getConfig().getString("world.main-world", "world");
        World mainWorld = Bukkit.getWorld(mainWorldName);

        if (mainWorld == null) {
            player.sendMessage("§cГлавный мир не найден.");
            return;
        }

        Location targetLoc = mainWorld.getSpawnLocation();

        if (lastLocation != null) {
            // Используем координаты из предыдущего местоположения, но применяем их к главному миру
            targetLoc = new Location(mainWorld, lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(),
                    lastLocation.getYaw(), lastLocation.getPitch());
        }

        player.teleport(targetLoc);
        player.sendMessage(plugin.getConfig().getString("messages.returned-back").replace("&", "§"));
    }


    public List<String> getAvailableWorldNames() {
        List<String> names = new ArrayList<>();
        FileConfiguration worlds = plugin.getWorldsConfig();
        Set<String> keys = worlds.getConfigurationSection("worlds").getKeys(false);

        for (String uuidKey : keys) {
            String worldName = worlds.getString("worlds." + uuidKey);
            if (worldName != null && worldName.startsWith("pworld_")) {
                names.add(worldName.replace("pworld_", ""));
            }
        }
        return names;
    }

    public void teleportToWorld(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage("§cМир не найден.");
            return;
        }

        String ownerName = worldName.replace("pworld_", "");
        boolean isAdmin = player.hasPermission("pworld.admin");
        boolean isOwner = player.getName().equalsIgnoreCase(ownerName);
        boolean hasAccess = plugin.getAccessManager().hasAccess(worldName, player.getName());

        if (isOwner || hasAccess || isAdmin) {
            player.teleport(world.getSpawnLocation());
            player.sendMessage("§aВы телепортированы в мир §e" + worldName);
        } else {
            player.sendMessage("§cУ вас нет доступа к этому миру.");
        }
    }

}

