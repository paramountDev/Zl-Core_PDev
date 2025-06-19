package dev.paramountdev.zlomCore_PDev.galaxyeconomy.commands;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class PenaltyCommand implements CommandExecutor {

    private final ZlomCore_PDev plugin;

    public PenaltyCommand(ZlomCore_PDev plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("galaxyeconomy.admin")) return true;

        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /penalty <ник> <сумма> <причина>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cНекорректная сумма.");
            return true;
        }

        String reason = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
        UUID targetUUID = target.getUniqueId();

        plugin.getBalanceManager().subtractBalance(targetUUID, amount);

        if (target.isOnline()) {
            target.getPlayer().sendMessage(MessageUtil.format("penalty")
                    .replace("%amount%", String.format("%.2f", amount))
                    .replace("%reason%", reason));
        }

        sender.sendMessage("§aШтраф применён к игроку " + target.getName() + ".");
        return true;
    }
}
