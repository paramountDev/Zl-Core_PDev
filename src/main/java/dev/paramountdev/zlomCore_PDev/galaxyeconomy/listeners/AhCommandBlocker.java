package dev.paramountdev.zlomCore_PDev.galaxyeconomy.listeners;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.utils.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class AhCommandBlocker implements Listener {

    private final ZlomCore_PDev plugin;

    public AhCommandBlocker(ZlomCore_PDev plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().toLowerCase();
        if(event.getMessage().startsWith("/ahblock") && message.equalsIgnoreCase("/ahblock")) {return;}

        if (message.startsWith("/ah") || message.startsWith("/ca") ||
                message.startsWith("/crazyauction") || message.startsWith("/crazyauctions") ) {
            if (plugin.getAhBlockManager().isBlocked(event.getPlayer().getUniqueId())) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(MessageUtil.format("ah_blocked")
                        .replace("%time%", plugin.getAhBlockManager().getRemaining(event.getPlayer().getUniqueId()))
                        .replace("%reason%", "Нарушение правил"));
            }
        }
    }
}
