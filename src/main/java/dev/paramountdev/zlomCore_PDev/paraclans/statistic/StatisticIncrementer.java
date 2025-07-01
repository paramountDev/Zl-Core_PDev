package dev.paramountdev.zlomCore_PDev.paraclans.statistic;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatisticIncrementer implements Listener {

    private final ClanStatsTracker statsTracker;
    private final Economy economy = ZlomCore_PDev.getInstance().getEconomy();

    public StatisticIncrementer(ClanStatsTracker statsTracker) {
        this.statsTracker = statsTracker;
        startBalanceUpdater();
    }

    Map<UUID, Long> loginTimes = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loginTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        long joinTime = loginTimes.getOrDefault(uuid, System.currentTimeMillis());
        long sessionLength = (System.currentTimeMillis() - joinTime) / 1000; // в секундах
        statsTracker.incrementOnline(uuid, sessionLength);
        loginTimes.remove(uuid);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        statsTracker.incrementBlocksMined(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKill(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player victim) {
            if (victim.getKiller() != null) {
                statsTracker.incrementKills(victim.getKiller().getUniqueId());
            }
        }
    }

    public void startBalanceUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    double balance = economy.getBalance(player);
                    statsTracker.updateBalance(player.getUniqueId(), balance);
                }
            }
        }.runTaskTimer(ZlomCore_PDev.getInstance(), 0L, 20 * 60); // обновление раз в минуту
    }

}
