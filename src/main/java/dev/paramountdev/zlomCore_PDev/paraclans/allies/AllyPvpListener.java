package dev.paramountdev.zlomCore_PDev.paraclans.allies;

import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import dev.paramountdev.zlomCore_PDev.paraclans.ClanManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class AllyPvpListener implements Listener {

    private final ClanManager clanManager;

    public AllyPvpListener(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @EventHandler
    public void onAllyPvp(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        if (!(damager instanceof Player attacker) || !(victim instanceof Player defender)) return;

        Clan attackerClan = clanManager.getClanByPlayer(attacker);
        Clan defenderClan = clanManager.getClanByPlayer(defender);

        if (attackerClan == null || defenderClan == null) return;

        if (!attackerClan.getAllies().containsKey(defenderClan.getName())) return;

        AllyPermissions perms = attackerClan.getPermissionsForAlly(defenderClan.getName());
        if (perms == null) return;

        if (perms.isPvpDisabled()) {
            attacker.sendMessage("§cPvP с этим кланом отключено.");
            event.setCancelled(true);
        }
    }
}

