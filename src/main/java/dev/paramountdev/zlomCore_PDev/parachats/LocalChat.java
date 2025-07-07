package dev.paramountdev.zlomCore_PDev.parachats;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.isInJail;

public class LocalChat implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();
        Location senderLoc = sender.getLocation();

        if (isInJail(senderLoc)) return;

        if (message.startsWith("$")) {
            sendLocalMessage(event, sender, message.substring(1).trim());
        } else if (!message.startsWith("!") && !message.startsWith("%") && !message.startsWith("#") && !message.startsWith("@")) {
            sendLocalMessage(event, sender, message);
        }
    }

    private void sendLocalMessage(AsyncPlayerChatEvent event, Player sender, String rawMessage) {
        int radius = ZlomCoreHelper.getChatRadius("local");
        String formatted = ZlomCoreHelper.formatMessage("local", sender, rawMessage);

        event.setCancelled(true);
        Location senderLoc = sender.getLocation();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().equals(sender.getWorld()) &&
                    player.getLocation().distanceSquared(senderLoc) <= radius * radius &&
                    !isInJail(player.getLocation())) {
                player.sendMessage(formatted);
            }
        }
    }
}
