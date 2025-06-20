package dev.paramountdev.zlomCore_PDev.boostyconnector;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoostConnector implements Listener, CommandExecutor {

    private final Map<UUID, Integer> customLimits = new HashMap<>();
    private final Map<UUID, List<PendingPayment>> pendingPayments = new HashMap<>();
    private final Map<UUID, Integer> playerSlots = new HashMap<>();
    private final Economy economy = ZlomCore_PDev.getInstance().getEconomy();

    private int defaultLimit;
    private String successMessage;
    private String denyMessage;


    public void loadSettings() {
        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();
        defaultLimit = config.getInt("default-limit", 3);
        denyMessage = ChatColor.translateAlternateColorCodes('&',
                config.getString("deny-message", "&cВы достигли лимита лотов."));
        successMessage = ChatColor.translateAlternateColorCodes('&',
                config.getString("success-message", "&aВам был выдан дополнительный слот за продажу."));

        startBalanceMonitor();
    }

    @EventHandler
    public void onAuctionSellCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!(message.startsWith("/ah") || message.startsWith("/ca") ||
                message.startsWith("/crazyauction") || message.startsWith("/crazyauctions"))) return;

        if (!(message.contains("sell") || message.contains("add"))) return;

        int currentSlots = playerSlots.getOrDefault(uuid, 0);
        if (currentSlots >= defaultLimit) {
            player.sendMessage(denyMessage);
            event.setCancelled(true);
            return;
        }

        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(message);
        long price = -1;

        while (matcher.find()) {
            try {
                price = Long.parseLong(matcher.group(1));
                break;
            } catch (NumberFormatException ignored) {}
        }

        if (price <= 0) {
            player.sendMessage(ChatColor.RED + "Не удалось определить сумму лота.");
            event.setCancelled(true);
            return;
        }

        double currentBalance = economy.getBalance(player);
        List<PendingPayment> payments = pendingPayments.computeIfAbsent(uuid, k -> new ArrayList<>());
        double lastTarget = payments.isEmpty() ? currentBalance : payments.get(payments.size() - 1).getTargetBalance();

        double newTarget = lastTarget + price;
        payments.add(new PendingPayment(newTarget));
        playerSlots.put(uuid, currentSlots + 1);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("boostysetlimit")) {
            if (!sender.hasPermission("boosty.admin")) {
                sender.sendMessage(ChatColor.RED + "У вас нет прав для этой команды.");
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Использование: /boostysetlimit <ник> <слоты>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Игрок не найден или не в сети.");
                return true;
            }

            int slots;
            try {
                slots = Integer.parseInt(args[1]);
                if (slots < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Введите положительное число слотов.");
                return true;
            }

            customLimits.put(target.getUniqueId(), slots);
            sender.sendMessage(ChatColor.GREEN + "Игроку " + target.getName() + " установлен лимит: " + slots + " слотов.");
            target.sendMessage(ChatColor.GOLD + "Администратор установил вам " + slots + " доступных слотов на аукционе.");
            return true;
        }
        if (command.getName().equalsIgnoreCase("checkslots")) {
            if (!(sender instanceof Player player)) return false;
            UUID uuid = player.getUniqueId();
            int used = playerSlots.getOrDefault(uuid, 0);
            int max = customLimits.getOrDefault(uuid, defaultLimit);
            sender.sendMessage(ChatColor.GOLD + "У вас занятых слотов: " + used + "/" + max);
            return true;
        }
        return true;
    }

    private void startBalanceMonitor() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (UUID uuid : new HashSet<>(pendingPayments.keySet())) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) continue;

                    List<PendingPayment> payments = pendingPayments.get(uuid);
                    if (payments == null || payments.isEmpty()) continue;

                    double nowBalance = economy.getBalance(player);
                    Iterator<PendingPayment> iterator = payments.iterator();

                    while (iterator.hasNext()) {
                        PendingPayment pending = iterator.next();
                        if (nowBalance >= pending.getTargetBalance()) {
                            int currentSlots = playerSlots.getOrDefault(uuid, 1);
                            playerSlots.put(uuid, Math.max(0, currentSlots - 1));

                            player.sendMessage(successMessage.replace("%amount%", String.valueOf((long)(pending.getTargetBalance()))));

                            iterator.remove();
                            break; // только 1 слот за проверку
                        }
                    }

                    if (payments.isEmpty()) {
                        pendingPayments.remove(uuid);
                    }
                }
            }
        }.runTaskTimer(ZlomCore_PDev.getInstance(), 40L, 40L); // каждые 2 секунды
    }

    static class PendingPayment {
        private final double targetBalance;
        public PendingPayment(double targetBalance) {
            this.targetBalance = targetBalance;
        }
        public double getTargetBalance() {
            return targetBalance;
        }
    }
}
