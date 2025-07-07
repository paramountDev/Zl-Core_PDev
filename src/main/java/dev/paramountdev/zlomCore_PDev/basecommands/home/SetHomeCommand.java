package dev.paramountdev.zlomCore_PDev.basecommands.home;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }

        if (!ZlomCore_PDev.getInstance().getConfig().getBoolean("homes.enabled")) {
            player.sendMessage(ChatColor.RED + "Команда /sethome отключена на сервере.");
            return true;
        }

        HomeManager.setHome(player.getUniqueId(), player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Точка дома установлена!");
        return true;
    }
}

