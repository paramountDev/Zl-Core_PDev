package dev.paramountdev.zlomCore_PDev.parachats;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.isInJail;


public class GlobalChat implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        if (isInJail(sender.getLocation())) {
            return;
        }

        if (message.startsWith("!")) {
            ZlomCore_PDev plugin = ZlomCore_PDev.getInstance();
            String formatTemplate = plugin.getConfig().getString("chat.formats.global", "&f[&cGlobal&f] &r{player}: {message}");
            String globalMessage = ChatColor.translateAlternateColorCodes('&',
                    formatTemplate.replace("{player}", sender.getDisplayName())
                            .replace("{message}", message.substring(1).trim()));
            event.setCancelled(true);
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(globalMessage));
        }
    }

}
