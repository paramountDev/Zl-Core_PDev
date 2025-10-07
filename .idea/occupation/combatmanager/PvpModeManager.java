package dev.paramountdev.zlomCore_PDev.LOL.occupation.combatmanager;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.entity.Player;

import java.util.*;

public class PvpModeManager {

    private static final Set<UUID> pvpAllowed = new HashSet<>(); // игроки, выполнившие /pveoff
    private static final Set<UUID> forcedPvp = new HashSet<>();  // те, кто уже вступил в бой
    private static final Map<UUID, Long> firstJoinTimestamps = new HashMap<>();

    private static final long PROTECTION_MS = ZlomCore_PDev.getInstance()
            .getConfig().getLong("pvp_system.protection_duration_days", 7) * 24 * 60 * 60 * 1000;

    public static void registerPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (!firstJoinTimestamps.containsKey(uuid)) {
            firstJoinTimestamps.put(uuid, System.currentTimeMillis());
        }
    }

    // Игрок разрешил себе участвовать в PVP
    public static void allowPvP(Player player) {
        pvpAllowed.add(player.getUniqueId());
    }

    // Игрок вступил в бой — теперь он насовсем в PvP
    public static void forcePvP(Player player) {
        forcedPvp.add(player.getUniqueId());
    }

    public static boolean hasForcedPvp(Player player) {
        return forcedPvp.contains(player.getUniqueId());
    }

    public static boolean hasPvpPermission(Player player) {
        return forcedPvp.contains(player.getUniqueId()) || pvpAllowed.contains(player.getUniqueId());
    }

    public static boolean isInPvE(Player player) {
        UUID uuid = player.getUniqueId();
        if (hasForcedPvp(player)) return false;
        if (!hasPvpPermission(player)) return true;

        long joinedAt = firstJoinTimestamps.getOrDefault(uuid, 0L);
        return System.currentTimeMillis() - joinedAt < PROTECTION_MS;
    }
}


