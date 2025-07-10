package dev.paramountdev.zlomCore_PDev.paraclans.allies;

import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import dev.paramountdev.zlomCore_PDev.paraclans.ClanManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuClickListener implements Listener {
    private final ClanManager clanManager;
    private final AllyMenu allyMenu;

    public MenuClickListener(ClanManager clanManager, AllyMenu allyMenu) {
        this.clanManager = clanManager;
        this.allyMenu = allyMenu;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().startsWith("Allies: ")) return;

        String clanName = e.getView().getTitle().substring(8);
        Clan clan = clanManager.getClan(clanName);
        if (clan == null) return;

        allyMenu.handleClick(e, player, clan);
    }
}
