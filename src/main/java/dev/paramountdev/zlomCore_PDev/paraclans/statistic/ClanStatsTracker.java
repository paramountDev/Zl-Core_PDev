package dev.paramountdev.zlomCore_PDev.paraclans.statistic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ClanStatsTracker {


    public static class PlayerStats {
        private long onlineSeconds = 0;
        private long blocksMined = 0;
        private long kills = 0;
        private double balance = 0.0;

        public void addOnlineSeconds(long seconds) {
            this.onlineSeconds += seconds;
        }

        public void addBlocksMined(long blocks) {
            this.blocksMined += blocks;
        }

        public void addKill() {
            this.kills++;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }

        public long getOnlineSeconds() {
            return onlineSeconds;
        }

        public long getBlocksMined() {
            return blocksMined;
        }

        public long getKills() {
            return kills;
        }

        public double getBalance() {
            return balance;
        }
    }

    private final Map<UUID, PlayerStats> statsMap = new ConcurrentHashMap<>();

    public PlayerStats getStats(UUID uuid) {
        return statsMap.computeIfAbsent(uuid, k -> new PlayerStats());
    }

    public void updateBalance(UUID uuid, double newBalance) {
        getStats(uuid).setBalance(newBalance);
    }

    public void incrementBlocksMined(UUID uuid) {
        getStats(uuid).addBlocksMined(1);
    }

    public void incrementKills(UUID uuid) {
        getStats(uuid).addKill();
    }

    public void incrementOnline(UUID uuid, long seconds) {
        getStats(uuid).addOnlineSeconds(seconds);
    }

    // ---------------------- TOP METHODS ----------------------

    public String getTopPlayerByOnline(Set<String> clanMembers) {
        return getTop(clanMembers, Comparator.comparingLong(p -> getStats(UUID.fromString(p)).getOnlineSeconds()));
    }

    public String getTopPlayerByBalance(Set<String> clanMembers) {
        return getTop(clanMembers, Comparator.comparingDouble(p -> getStats(UUID.fromString(p)).getBalance()));
    }

    public String getTopPlayerByBlocks(Set<String> clanMembers) {
        return getTop(clanMembers, Comparator.comparingLong(p -> getStats(UUID.fromString(p)).getBlocksMined()));
    }

    public String getTopPlayerByKills(Set<String> clanMembers) {
        return getTop(clanMembers, Comparator.comparingLong(p -> getStats(UUID.fromString(p)).getKills()));
    }

    private String getTop(Set<String> clanMembers, Comparator<String> comparator) {
        return clanMembers.stream()
                .max(comparator)
                .map(uuidStr -> {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuidStr));
                    return offlinePlayer.getName() != null ? offlinePlayer.getName() : "Неизвестно";
                })
                .orElse("Нет данных");
    }
}
