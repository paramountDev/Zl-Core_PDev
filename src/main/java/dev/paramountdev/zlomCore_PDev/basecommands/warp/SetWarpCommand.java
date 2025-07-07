package dev.paramountdev.zlomCore_PDev.basecommands.warp;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetWarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Использование: /setwarp <имя> [цена]");
            return true;
        }

        String name = args[0];
        double cost = args.length >= 2 ? Double.parseDouble(args[1]) : 0;
        Economy econ = ZlomCore_PDev.getInstance().getEconomy();

        int limit = ZlomCore_PDev.getInstance().getConfig().getInt("warp.default-limit", 1);
        for (int i = 1; i <= 100; i++) {
            if (player.hasPermission("zlomcore.warp.limit." + i)) {
                limit = i;
                break;
            }
        }

        long existing = WarpManager.getAllWarpNames().stream()
                .filter(w -> ZlomCore_PDev.getInstance().getWarpOwner(w).equalsIgnoreCase(player.getName()))
                .count();

        if (existing >= limit) {
            player.sendMessage(ChatColor.RED + "Вы достигли лимита варпов (" + limit + ").");
            return true;
        }

        double creationCost = ZlomCore_PDev.getInstance().getConfig().getDouble("warp.create-cost", 500000);
        if (econ != null && econ.getBalance(player) < creationCost) {
            player.sendMessage(ChatColor.RED + "Недостаточно средств для создания варпа (" + creationCost + ").");
            return true;
        }
        if (econ != null) econ.withdrawPlayer(player, creationCost);

        if (!WarpManager.createWarp(name, player.getLocation(), cost)) {
            player.sendMessage(ChatColor.RED + "Варп с таким именем уже существует.");
            return true;
        }

        ZlomCore_PDev.getInstance().setWarpOwner(name, player.getName());
        player.sendMessage(ChatColor.GREEN + "Варп успешно создан: " + name);
        return true;
    }
}