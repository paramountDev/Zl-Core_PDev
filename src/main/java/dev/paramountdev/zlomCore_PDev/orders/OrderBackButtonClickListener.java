package dev.paramountdev.zlomCore_PDev.orders;


import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OrderBackButtonClickListener implements Listener {


    @EventHandler
    public void onBackButtonClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();

        // Проверяем, открыто ли нужное меню
        if (inv != null && event.getView().getTitle() != null
                && (event.getView().getTitle().equals("Купленные предметы")
                || event.getView().getTitle().equals("Выбор предмета - стр. 1"))) {

            if (event.getCurrentItem() == null) {
                event.setCancelled(true);
                return;
            }
            if (event.getCurrentItem().getItemMeta() == null) {
                event.setCancelled(true);
                return;
            }
            if (event.getCurrentItem().getItemMeta().getDisplayName() == null) {
                event.setCancelled(true);
                return;
            }

            ItemStack item = event.getCurrentItem();

            String clickedName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            String backName = ChatColor.stripColor(ZlomCoreHelper.getBackButton().getItemMeta().getDisplayName());

            if (clickedName.equalsIgnoreCase(backName)) {
                if (event.getWhoClicked() instanceof Player player) {
                    event.setCancelled(true);
                    player.closeInventory();
                    player.performCommand("orders");
                }
            }
            event.setCancelled(true);
        }
    }
}
