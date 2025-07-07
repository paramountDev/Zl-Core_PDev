package dev.paramountdev.zlomCore_PDev.basecommands.spawn;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда доступна только игрокам.");
            return true;
        }

        if (!player.hasPermission("ZlomCore_PDev.spawn")) {
            player.sendMessage(ChatColor.RED + "Нет прав.");
            return true;
        }

        Location spawn = SpawnManager.getSpawn();
        if (spawn == null) {
            player.sendMessage(ChatColor.RED + "Точка спавна не установлена.");
            return true;
        }

        boolean allowCrossWorld = ZlomCore_PDev.getInstance().getConfig().getBoolean("spawn.allow-cross-world", false);
        if (!allowCrossWorld && !player.getWorld().equals(spawn.getWorld())) {
            player.sendMessage(ChatColor.RED + "Нельзя телепортироваться на спавн из другого измерения.");
            return true;
        }

        double price = ZlomCore_PDev.getInstance().getConfig().getDouble("spawn.price");
        if (!ZlomCore_PDev.getInstance().getEconomy().has(player, price)) {
            player.sendMessage(ChatColor.RED + "Недостаточно средств. Нужно: " + price);
            return true;
        }

        ZlomCore_PDev.getInstance().getEconomy().withdrawPlayer(player, price);
        player.teleport(spawn);
        player.sendMessage(ChatColor.GREEN + "Телепорт на спавн за " + price + "$");
        return true;
    }
}
