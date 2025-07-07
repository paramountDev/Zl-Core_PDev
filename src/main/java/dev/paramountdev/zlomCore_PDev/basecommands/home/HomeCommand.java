package dev.paramountdev.zlomCore_PDev.basecommands.home;


import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }

        double cost = ZlomCore_PDev.getInstance().getConfig().getDouble("homes.cost", 0.0);
        boolean crossWorld = ZlomCore_PDev.getInstance().getConfig().getBoolean("homes.cross-world", false);
        boolean homeEnabled = ZlomCore_PDev.getInstance().getConfig().getBoolean("homes.enabled");

        Location targetLoc = null;

        if (homeEnabled) {
            targetLoc = HomeManager.getHome(player.getUniqueId());
            if (targetLoc == null) {
                player.sendMessage(ChatColor.RED + "Вы не установили точку дома. Используйте /sethome.");
                return true;
            }
        } else {
            targetLoc = player.getBedSpawnLocation();
            if (targetLoc == null) {
                player.sendMessage(ChatColor.RED + "Вы не установили кровать или она недоступна.");
                return true;
            }
        }

        if (!crossWorld && !player.getWorld().equals(targetLoc.getWorld())) {
            player.sendMessage(ChatColor.RED + "Телепортация в другое измерение запрещена.");
            return true;
        }

        if (!withdraw(player, cost)) {
            player.sendMessage(ChatColor.RED + "Недостаточно средств для телепортации домой. Нужно: " + cost);
            return true;
        }

        player.teleport(targetLoc);
        player.sendMessage(ChatColor.GREEN + "Вы телепортировались домой.");
        return true;
    }


    private boolean withdraw(Player player, double amount) {
        Economy econ = ZlomCore_PDev.getInstance().getEconomy();
        if (econ == null) return true;
        if (econ.getBalance(player) < amount) return false;
        econ.withdrawPlayer(player, amount);
        return true;
    }
}

