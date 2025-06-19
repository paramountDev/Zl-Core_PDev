package dev.paramountdev.zlomCore_PDev.galaxyeconomy.commands;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MoneyCommand implements CommandExecutor {

    private final ZlomCore_PDev plugin;

    public MoneyCommand(ZlomCore_PDev plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        UUID uuid = player.getUniqueId();

        if (args.length == 0 || args[0].equalsIgnoreCase("balance")) {
            double balance = plugin.getBalanceManager().getBalance(uuid);
            player.sendMessage(MessageUtil.format("balance").replace("%balance%", String.format("%.2f", balance)));
            return true;
        }

        if (args.length >= 3) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cИгрок не найден.");
                return true;
            }
            UUID targetUUID = target.getUniqueId();
            double amount;

            try {
                amount = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cНекорректная сумма.");
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "send" -> {
                    if (plugin.getBalanceManager().subtractBalance(uuid, amount)) {
                        plugin.getBalanceManager().addBalance(targetUUID, amount);
                        player.sendMessage(MessageUtil.format("sent_money")
                                .replace("%amount%", String.format("%.2f", amount))
                                .replace("%player%", target.getName()));
                        target.sendMessage(MessageUtil.format("received_money")
                                .replace("%amount%", String.format("%.2f", amount))
                                .replace("%player%", player.getName()));
                    } else {
                        player.sendMessage("§cНедостаточно средств.");
                    }
                }
                case "add" -> {
                    if (!player.hasPermission("galaxyeconomy.admin")) return true;
                    plugin.getBalanceManager().addBalance(targetUUID, amount);
                    player.sendMessage(MessageUtil.format("added_money")
                            .replace("%amount%", String.format("%.2f", amount))
                            .replace("%player%", target.getName()));
                }
                case "take" -> {
                    if (!player.hasPermission("galaxyeconomy.admin")) return true;
                    plugin.getBalanceManager().subtractBalance(targetUUID, amount);
                    player.sendMessage(MessageUtil.format("taken_money")
                            .replace("%amount%", String.format("%.2f", amount))
                            .replace("%player%", target.getName()));
                }
            }
        }

        return true;
    }
}

