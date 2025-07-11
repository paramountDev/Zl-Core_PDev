package dev.paramountdev.zlomCore_PDev.combatmanager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PveOffCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (PvpModeManager.hasPvpPermission(player)) {
            player.sendMessage(ChatColor.GRAY + "Вы уже отключили PvE защиту.");
            return true;
        }

        PvpModeManager.allowPvP(player);
        player.sendMessage(ChatColor.RED + "Вы отключили PvE защиту. Теперь вы можете участвовать в PvP, но режим активируется при первом бою.");
        return true;
    }
}

