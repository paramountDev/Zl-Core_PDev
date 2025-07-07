package dev.paramountdev.zlomCore_PDev.basecommands.tpa;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TpAcceptCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player target)) return true;

        UUID requesterId = TpaCommand.getRequester(target.getUniqueId());
        if (requesterId == null) {
            target.sendMessage(ChatColor.RED + "Нет активных запросов.");
            return true;
        }

        Player requester = target.getServer().getPlayer(requesterId);
        if (requester == null) {
            target.sendMessage(ChatColor.RED + "Запрашивающий вышел с сервера.");
            TpaCommand.removeRequest(target.getUniqueId());
            return true;
        }

        double price = new TpaCommand().getPriceFor(requester);
        Economy econ = ZlomCore_PDev.getInstance().getEconomy();

        if (econ != null && econ.getBalance(requester) < price) {
            requester.sendMessage(ChatColor.RED + "Недостаточно средств (" + price + ")");
            TpaCommand.removeRequest(target.getUniqueId());
            return true;
        }

        if (econ != null) econ.withdrawPlayer(requester, price);
        requester.teleport(target.getLocation());
        requester.sendMessage(ChatColor.GREEN + "Вы были телепортированы к " + target.getName());
        target.sendMessage(ChatColor.GREEN + "Вы приняли запрос от " + requester.getName());
        TpaCommand.removeRequest(target.getUniqueId());
        return true;
    }
}
