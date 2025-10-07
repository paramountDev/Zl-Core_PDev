package dev.paramountdev.zlomCore_PDev.crazyorders;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class OrdersGUI implements Listener {

    private final OrderManager manager;
    private final Economy economy;

    public OrdersGUI(OrderManager manager) {
        this.manager = manager;
        this.economy = ZlomCore_PDev.getInstance().getEconomy();
    }

    public void openOrdersMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "📦 Заказы");

        ItemStack filler = ZlomCoreHelper.createFiller();
        for(int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }


        List<Order> orders = manager.getOrders();
        for (Order order : orders) {
            ItemStack item = new ItemStack(order.getMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§e" + order.getMaterial().name());
            meta.setLore(List.of(
                    "§7Осталось: " + order.getAmountRemaining(),
                    "§7Цена: " + order.getPricePerUnit() + " за 1",
                    "",
                    "§7Заказчик: " + Bukkit.getPlayer(order.getCreator()).getName(),
                    "§aКликни, чтобы продать!"
            ));
            item.setItemMeta(meta);
            gui.addItem(item);
        }

        // Кнопка "Добавить заказ"
        ItemStack addOrderButton = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addOrderButton.getItemMeta();
        addMeta.setDisplayName("§a➕ Добавить заказ");
        addOrderButton.setItemMeta(addMeta);
        gui.setItem(52, addOrderButton);

        player.openInventory(gui);


        ItemStack purchasedButton = new ItemStack(Material.CHEST);
        ItemMeta meta = purchasedButton.getItemMeta();
        meta.setDisplayName("§bКупленные предметы");
        purchasedButton.setItemMeta(meta);
        gui.setItem(53, purchasedButton);

        player.openInventory(gui);
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = e.getView().getTitle();

        if (title.equals("📦 Заказы")) {
            e.setCancelled(true);
            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            if (clicked.getType() == Material.CHEST &&
                    clicked.getItemMeta().getDisplayName().equals("§bКупленные предметы")) {
                PurchasedItemsManager.openPurchasedMenu(player);
                return;
            }

            if (clicked.getType() == Material.EMERALD &&
                    clicked.getItemMeta().getDisplayName().equals("§a➕ Добавить заказ")) {
                player.closeInventory();
                ItemSelectGUI.open(player, 1); // открыть с первой страницы
                return;
            }

            Material material = clicked.getType();
            Order selectedOrder = null;

            for (Order order : manager.getOrders()) {
                if (order.getMaterial() == material && !order.isCompleted()) {
                    selectedOrder = order;
                    break;
                }
            }

            if (selectedOrder == null) return;

            int inInventory = countMaterial(player, material);
            if (inInventory == 0) {
                player.sendMessage("§cУ тебя нет нужного материала.");
                return;
            }

            SellAmountMenu menu = new SellAmountMenu(player, selectedOrder);
            SellAmountListener.menuMap.put(player.getUniqueId(), menu);
            menu.open();
        }
    }


    private int countMaterial(Player player, Material mat) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) {
                total += item.getAmount();
            }
        }
        return total;
    }

    private void removeMaterial(Player player, Material mat, int amount) {
        int toRemove = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == mat) {
                int amt = item.getAmount();
                if (amt <= toRemove) {
                    player.getInventory().setItem(i, null);
                    toRemove -= amt;
                } else {
                    item.setAmount(amt - toRemove);
                    toRemove = 0;
                }
                if (toRemove <= 0) break;
            }
        }
    }
}

