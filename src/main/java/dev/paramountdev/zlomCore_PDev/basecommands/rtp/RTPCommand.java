package dev.paramountdev.zlomCore_PDev.basecommands.rtp;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RTPCommand implements CommandExecutor {

    private final Random random = new Random();
    private final Map<UUID, Location> pendingTeleport = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эту команду может использовать только игрок.");
            return true;
        }

        if (pendingTeleport.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Подождите... телепортация уже начата.");
            return true;
        }

        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();
        boolean enabled = config.getBoolean("rtp.enabled", false);
        double cost = config.getDouble("rtp.cost", 75.0);
        int maxRadius = config.getInt("rtp.max-radius", 10000);

        if (!enabled) {
            player.sendMessage(ChatColor.RED + "Команда /rtp отключена на сервере.");
            return true;
        }

        if (!player.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
            player.sendMessage(ChatColor.RED + "Команду можно использовать только в обычном мире.");
            return true;
        }

        if (!withdraw(player, cost)) {
            player.sendMessage(ChatColor.RED + "У вас недостаточно денег. Стоимость: " + cost);
            return true;
        }

        Location rtpLocation = findSafeLocation(player.getWorld(), maxRadius);
        if (rtpLocation == null) {
            player.sendMessage(ChatColor.RED + "Не удалось найти безопасную локацию.");
            return true;
        }

        Location startLocation = player.getLocation().clone();
        pendingTeleport.put(player.getUniqueId(), startLocation);

        player.sendTitle(ChatColor.AQUA + "Телепортация...", ChatColor.GRAY + "Не двигайтесь!", 10, 60, 10);
        player.sendMessage(ChatColor.GRAY + "Телепортация через 3 секунды...");

        new BukkitRunnable() {
            int timer = 3;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    pendingTeleport.remove(player.getUniqueId());
                    return;
                }

                Location currentLoc = player.getLocation();
                Location initialLoc = pendingTeleport.get(player.getUniqueId());

                if (currentLoc.distanceSquared(initialLoc) > 0.1) {
                    player.sendMessage(ChatColor.RED + "Телепортация отменена. Вы пошевелились.");
                    cancel();
                    pendingTeleport.remove(player.getUniqueId());
                    return;
                }

                if (timer == 0) {
                    player.teleport(rtpLocation);
                    player.getWorld().spawnParticle(Particle.PORTAL, rtpLocation, 100, 1, 1, 1, 0.2);
                    player.sendTitle(ChatColor.GREEN + "Готово!", ChatColor.GRAY + "Вы были телепортированы.", 10, 40, 10);
                    pendingTeleport.remove(player.getUniqueId());
                    cancel();
                } else {
                    player.getWorld().spawnParticle(Particle.WITCH, currentLoc.clone().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                    timer--;
                }
            }
        }.runTaskTimer(ZlomCore_PDev.getInstance(), 0L, 20L);

        return true;
    }

    private Location findSafeLocation(World world, int maxRadius) {
        for (int attempts = 0; attempts < 1000; attempts++) {
            int x = random.nextInt(maxRadius * 2) - maxRadius;
            int z = random.nextInt(maxRadius * 2) - maxRadius;
            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);

            Block block = world.getBlockAt(x, y - 1, z);
            if (isSafeBlock(block.getType())) {
                return loc;
            }
        }
        return null;
    }

    private boolean isSafeBlock(Material mat) {
        return mat.isSolid() && !mat.name().toLowerCase().contains("lava");
    }

    private boolean withdraw(Player player, double amount) {
        Economy econ = ZlomCore_PDev.getInstance().getEconomy();
        if (econ == null) return true;
        if (econ.getBalance(player) < amount) return false;
        econ.withdrawPlayer(player, amount);
        return true;
    }
}
