package dev.paramountdev.zlomCore_PDev.occupation.playerWithoutClan;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.ProtectionRegion;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.RegionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SiegeScheduler {

    private static final Map<UUID, Long> pendingSieges = new HashMap<>(); // UUID региона -> время окончания
    private static final Map<UUID, PendingOutcome> pendingMenus = new HashMap<>(); // игрок -> данные
    private static final Map<UUID, OccupationTimedOutcome> outcomeToCheck = new HashMap<>();

    public static void scheduleSiege(UUID playerId, PendingOutcome outcome) {
        int hours = ZlomCore_PDev.getInstance().getConfig().getInt("occupation.siege-delay-hours", 24);
        long finishTime = System.currentTimeMillis() + hours * 60L * 60 * 1000;

        pendingSieges.put(outcome.getRegion().getRegionId(), finishTime);
        pendingMenus.put(playerId, outcome);
    }

    public static void checkExpirations() {
        long now = System.currentTimeMillis();
        List<UUID> expired = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : pendingSieges.entrySet()) {
            if (entry.getValue() <= now) {
                expired.add(entry.getKey());
            }
        }

        for (UUID regionId : expired) {
            pendingSieges.remove(regionId);

            // Находим игрока
            for (Map.Entry<UUID, PendingOutcome> menuEntry : pendingMenus.entrySet()) {
                if (menuEntry.getValue().getRegion().getRegionId().equals(regionId)) {
                    Player p = Bukkit.getPlayer(menuEntry.getKey());
                    if (p != null && p.isOnline()) {
                        new SiegeMenu().openSiegeResultMenu(p,
                                menuEntry.getValue().getLocation(),
                                menuEntry.getValue().getRegion());
                        pendingMenus.remove(menuEntry.getKey());
                    }
                }
            }
        }
    }

    public static void onPlayerJoin(Player player) {
        if (pendingMenus.containsKey(player.getUniqueId())) {
            PendingOutcome outcome = pendingMenus.get(player.getUniqueId());
            long now = System.currentTimeMillis();
            long finish = pendingSieges.getOrDefault(outcome.getRegion().getRegionId(), Long.MAX_VALUE);

            if (now >= finish) {
                new SiegeMenu().openSiegeResultMenu(player, outcome.getLocation(), outcome.getRegion());
                pendingMenus.remove(player.getUniqueId());
                pendingSieges.remove(outcome.getRegion().getRegionId());
            }
        }
    }

    public static void processMenuSelection(UUID playerId, String outcomeType, Location loc, ProtectionRegion region) {
        if (outcomeToCheck.containsKey(playerId)) return; // уже есть
        outcomeToCheck.put(playerId, new OccupationTimedOutcome(outcomeType, loc, region));
    }

    public static void checkOutcomeExecutions() {
        long now = System.currentTimeMillis();
        List<UUID> toExecute = new ArrayList<>();

        for (Map.Entry<UUID, OccupationTimedOutcome> entry : outcomeToCheck.entrySet()) {
            if (entry.getValue().getExecutionTime() <= now) {
                toExecute.add(entry.getKey());
            }
        }

        for (UUID playerId : toExecute) {
            OccupationTimedOutcome outcome = outcomeToCheck.remove(playerId);
            if (outcome == null) continue;

            int required = ZlomCore_PDev.getInstance().getConfig().getInt("occupation.min-attackers-for-success", 2);
            boolean require = ZlomCore_PDev.getInstance().getConfig().getBoolean("occupation.require-attackers", true);

            int enemies = RegionUtils.countPlayers(outcome.getRegion(), playerId, true);

            if (!require || enemies >= required) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    switch (outcome.getOutcomeType()) {
                        case "Захват":
                            new SiegeMenu().handleCapture(player, new PendingOutcome(outcome.getRegion()));
                            break;
                        case "Грабеж":
                            new SiegeMenu().handleLoot(player, new PendingOutcome(outcome.getRegion()));
                            break;
                        case "Разорение":
                            new SiegeMenu().handleRuin(player, new PendingOutcome(outcome.getRegion()));
                            break;
                    }
                }
            } else {
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Оккупация не удалась. Недостаточно атакующих на территории региона.");
            }
        }
    }
}
