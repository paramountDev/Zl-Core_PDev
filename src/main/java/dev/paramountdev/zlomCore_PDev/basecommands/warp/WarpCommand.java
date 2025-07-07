package dev.paramountdev.zlomCore_PDev.basecommands.warp;


import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Доступные варпы: " + String.join(", ", WarpManager.getAllWarpNames()));
            return true;
        }

        String name = args[0];
        WarpManager.WarpData warp = WarpManager.getWarp(name);

        if (warp == null) {
            player.sendMessage(ChatColor.RED + "Варп с таким именем не найден.");
            return true;
        }

        if (!ZlomCore_PDev.getInstance().getConfig().getBoolean("warp.cross-world", false) &&
                !player.getWorld().equals(warp.location.getWorld())) {
            player.sendMessage(ChatColor.RED + "Телепортация между измерениями запрещена.");
            return true;
        }

        double cost = warp.cost;
        Economy econ = ZlomCore_PDev.getInstance().getEconomy();
        if (econ != null && econ.getBalance(player) < cost) {
            player.sendMessage(ChatColor.RED + "Недостаточно средств. Стоимость: " + cost);
            return true;
        }
        if (econ != null) econ.withdrawPlayer(player, cost);

        player.teleport(warp.location);
        player.sendMessage(ChatColor.GREEN + "Вы были телепортированы на варп " + name);
        return true;
    }
}
