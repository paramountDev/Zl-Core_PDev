package dev.paramountdev.zlomCore_PDev.LOL.occupation.galaxyeconomy.commands;

import dev.paramountdev.zlomCore_PDev.LOL.occupation.galaxyeconomy.utils.MessageUtil;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class AhBlockCommand implements CommandExecutor {

    private final ZlomCore_PDev plugin;

    public AhBlockCommand(ZlomCore_PDev plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("galaxyeconomy.admin")) return true;

        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /ahblock <ник> <время в минутах> <причина>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        UUID targetUUID = target.getUniqueId();

        long time;
        try {
            time = Long.parseLong(args[1]) * 60;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cНекорректное время.");
            return true;
        }
        String reason = String.join(" ", args).substring(args[0].length() + args[1].length() + 2);
        long blockUntil = System.currentTimeMillis() + (time * 1000);

        plugin.getAhBlockManager().block(targetUUID, blockUntil);

        if (target.isOnline()) {
            target.getPlayer().sendMessage(MessageUtil.format("ah_blocked")
                    .replace("%time%", (time / 60) + " мин.")
                    .replace("%reason%", reason));
        }

        sender.sendMessage("§aИгрок " + target.getName() + " заблокирован в /ah на " + (time / 60) + " мин.");

        return true;
    }
}
