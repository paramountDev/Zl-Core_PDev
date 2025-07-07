package dev.paramountdev.zlomCore_PDev.parachats;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.FurnaceProtectionManager;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.ProtectionRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.isInJail;


import java.util.List;

public class PrivateChat implements Listener {

    @EventHandler
    public void onPrivateChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player sender = event.getPlayer();
        Location senderLoc = sender.getLocation();

        if (!message.startsWith("@")) return;
        if (isInJail(senderLoc)) return;

        event.setCancelled(true);
        String cleanMessage = message.substring(1).trim();

        List<ProtectionRegion> regions = FurnaceProtectionManager.getFpm().getAllRegions();
        ProtectionRegion senderRegion = regions.stream()
                .filter(region -> region.isInside(senderLoc))
                .findFirst()
                .orElse(null);

        if (senderRegion == null) {
            sender.sendMessage(ChatColor.RED + "Вы не находитесь в приватной зоне!");
            return;
        }

        String formatted = ZlomCoreHelper.formatMessage("private", sender, cleanMessage);

        for (Player target : sender.getWorld().getPlayers()) {
            if (senderRegion.isInside(target.getLocation())) {
                target.sendMessage(formatted);
            }
        }
    }
}

