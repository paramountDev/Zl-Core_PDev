package dev.paramountdev.zlomCore_PDev.basecommands.tpa;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TpDenyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player target)) return true;

        UUID requesterId = TpaCommand.getRequester(target.getUniqueId());
        if (requesterId != null) {
            TpaCommand.removeRequest(target.getUniqueId());
            target.sendMessage(ChatColor.YELLOW + "Запрос отклонён.");
        } else {
            target.sendMessage(ChatColor.RED + "Нет активных запросов.");
        }
        return true;
    }
}
