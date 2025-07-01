package dev.paramountdev.zlomCore_PDev.combatmanager;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatManager implements Listener {

    private final Plugin plugin;
    private final int combatCooldown;
    private final Map<UUID, Long> combatTagged = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public CombatManager(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.combatCooldown = config.getInt("combat_cooldown_seconds", 10);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTimerTask();
    }

    private void startTimerTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                combatTagged.entrySet().removeIf(entry -> {
                    UUID uuid = entry.getKey();
                    long endTime = entry.getValue();
                    Player player = Bukkit.getPlayer(uuid);

                    if (now >= endTime) {
                        if (player != null && bossBars.containsKey(uuid)) {
                            bossBars.get(uuid).removeAll();
                            bossBars.remove(uuid);
                        }
                        return true;
                    } else {
                        if (player != null && bossBars.containsKey(uuid)) {
                            double secondsLeft = (endTime - now) / 1000.0;
                            double progress = secondsLeft / combatCooldown;
                            bossBars.get(uuid).setProgress(Math.max(0, progress));
                        }
                        return false;
                    }
                });
            }
        }.runTaskTimer(plugin, 0L, 20L); // Every second
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        long endTime = System.currentTimeMillis() + (combatCooldown * 1000L);

        for (Player p : new Player[]{victim, attacker}) {
            combatTagged.put(p.getUniqueId(), endTime);

            BossBar bar = bossBars.get(p.getUniqueId());
            if (bar == null) {
                bar = Bukkit.createBossBar("ПВП РЕЖИМ", BarColor.RED, BarStyle.SEGMENTED_10);
                bar.setProgress(1.0);
                bar.addPlayer(p);
                bossBars.put(p.getUniqueId(), bar);
            } else {
                bar.setProgress(1.0);
                bar.addPlayer(p);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (combatTagged.containsKey(uuid)) {
            event.getPlayer().setHealth(0.0);
        }
    }
}

