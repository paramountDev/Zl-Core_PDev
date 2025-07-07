package dev.paramountdev.zlomCore_PDev.basecommands.tpa;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaCommand implements CommandExecutor {

    private static final Map<UUID, UUID> pendingRequests = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Использование: /tpa <ник>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "Игрок не найден или оффлайн.");
            return true;
        }

        if (player == target) {
            player.sendMessage(ChatColor.RED + "Вы не можете отправить запрос самому себе.");
            return true;
        }

        if (!ZlomCore_PDev.getInstance().getConfig().getBoolean("tpa.cross-world", false) &&
                !player.getWorld().equals(target.getWorld())) {
            player.sendMessage(ChatColor.RED + "Телепортация между измерениями запрещена.");
            return true;
        }

        double price = getPriceFor(player);
        player.sendMessage(ChatColor.YELLOW + "Вы отправили запрос на телепортацию к " + target.getName() + ChatColor.GRAY + " (Цена: " + price + ")");

        pendingRequests.put(target.getUniqueId(), player.getUniqueId());

        TextComponent accept = new TextComponent(ChatColor.GREEN + "[ПРИНЯТЬ]");
        accept.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept"));
        TextComponent deny = new TextComponent(ChatColor.RED + " [ОТКЛОНИТЬ]");
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny"));

        target.sendMessage(ChatColor.YELLOW + player.getName() + " хочет телепортироваться к вам (Цена: " + price + ")");
        target.spigot().sendMessage(accept, deny);

        return true;
    }

    public static UUID getRequester(UUID target) {
        return pendingRequests.get(target);
    }

    public static void removeRequest(UUID target) {
        pendingRequests.remove(target);
    }

    public double getPriceFor(Player player) {
        for (int i = 0; i <= 30000; i += 1000) {
            if (player.hasPermission("zlomcore.tpa.cost." + i)) return i;
        }
        return ZlomCore_PDev.getInstance().getConfig().getDouble("tpa.default-cost", 30000);
    }
}
