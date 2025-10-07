package dev.paramountdev.zlomCore_PDev.LOL.occupation.playerWithoutClan;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.LOL.occupation.furnaceprivates.ProtectionRegion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class OnJoinMessage implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        ProtectionRegion region = ZlomCore_PDev.getInstance().getManager().getRegionByOwner(uuid);
        if (region != null && !region.isSiegeAvailable()) {
            long millisLeft = region.getSiegeAvailableAt() - System.currentTimeMillis();
            long hours = millisLeft / (60 * 60 * 1000);
            player.sendMessage(ChatColor.RED + "⚠ Ваш приват может быть осаждён через " + hours + " ч.");
        }
    }

}
