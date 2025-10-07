package dev.paramountdev.zlomCore_PDev.LOL.occupation.parachats;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.isInJail;


public class JailChat implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();
        Location senderLoc = sender.getLocation();

        if (isInJail(senderLoc)) {
            if (message.startsWith("%")) {
                int radius = ZlomCoreHelper.getChatRadius("jail");
                String formatted = ZlomCoreHelper.formatMessage("jail", sender, message.substring(1).trim());

                event.setCancelled(true);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().equals(sender.getWorld())) continue;

                    if (isInJail(player.getLocation()) &&
                            player.getLocation().distanceSquared(senderLoc) <= radius * radius) {
                        player.sendMessage(formatted);
                    }
                }
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Вы можете использовать только тюремный чат (%сообщение) в тюрьме.");
                event.setCancelled(true);
            }
        } else if (message.startsWith("%")) {
            sender.sendMessage(ChatColor.DARK_RED + "Вы не можете использовать тюремный чат вне тюрьмы.");
            event.setCancelled(true);
        }
    }
}

