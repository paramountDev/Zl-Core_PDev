package dev.paramountdev.zlomCore_PDev.worlds;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RequestManager {
    private final Map<UUID, List<UUID>> requests = new HashMap<>();

    public void addRequest(Player owner, Player requester) {
        requests.computeIfAbsent(owner.getUniqueId(), k -> new ArrayList<>()).add(requester.getUniqueId());
    }

    public void acceptRequest(Player owner, String requesterName) {
        Player requester = Bukkit.getPlayerExact(requesterName);
        if (requester == null || !requester.isOnline()) {
            owner.sendMessage("§cИгрок не в сети.");
            return;
        }

        List<UUID> list = requests.getOrDefault(owner.getUniqueId(), new ArrayList<>());
        if (!list.contains(requester.getUniqueId())) {
            owner.sendMessage("§cНет запроса от этого игрока.");
            return;
        }

        String worldName = "pworld_" + owner.getName().toLowerCase();
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            requester.teleport(world.getSpawnLocation());
            requester.sendMessage("§aВы подключились к миру игрока " + owner.getName());
            owner.sendMessage("§aИгрок " + requester.getName() + " подключен.");
        }

        list.remove(requester.getUniqueId());
    }

    public void denyRequest(Player owner, String requesterName) {
        Player requester = Bukkit.getPlayerExact(requesterName);
        if (requester == null) return;

        List<UUID> list = requests.getOrDefault(owner.getUniqueId(), new ArrayList<>());
        list.remove(requester.getUniqueId());

        requester.sendMessage("§cВаш запрос на подключение был отклонен.");
        owner.sendMessage("§eЗапрос от " + requester.getName() + " отклонён.");
    }

    public List<String> getPendingRequestNames(Player player) {
        UUID ownerUUID = player.getUniqueId();
        List<String> names = new ArrayList<>();

        // Пример: если у тебя есть Map<UUID, List<UUID>> pendingRequests
        List<UUID> requesters = requests.getOrDefault(ownerUUID, Collections.emptyList());

        for (UUID requesterUUID : requesters) {
            Player requester = Bukkit.getServer().getPlayer(requesterUUID);
            if (requester != null) {
                names.add(requester.getName());
            }
        }

        return names;
    }
}

