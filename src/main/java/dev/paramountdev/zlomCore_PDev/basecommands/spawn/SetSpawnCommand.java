package dev.paramountdev.zlomCore_PDev.basecommands.spawn;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SetSpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }

        if (!player.hasPermission("teleportcore.setspawn")) {
            player.sendMessage(ChatColor.RED + "У тебя нет прав.");
            return true;
        }

        SpawnManager.setSpawn(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Точка спавна установлена!");
        return true;
    }
}

