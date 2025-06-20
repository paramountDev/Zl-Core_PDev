package dev.paramountdev.zlomCore_PDev.paraclans;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.updatePlayerNames;

public class PlayerListener implements Listener {

    private final ZlomCore_PDev plugin;

    public PlayerListener(ZlomCore_PDev plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updatePlayerNames(event.getPlayer(), plugin.getPlayerClan(), plugin.getClanMenu(), plugin.getMainBoard());
    }
}

