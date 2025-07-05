package dev.paramountdev.zlomCore_PDev.orders;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SellAmountListener implements Listener {

    public static final Map<UUID, SellAmountMenu> menuMap = new HashMap<>();

    private final OrderManager manager;

    public SellAmountListener() {
        this.manager = ZlomCore_PDev.getInstance().getOrderManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        String title = e.getView().getTitle();
        if (!title.startsWith("Продажа: ")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        SellAmountMenu menu = menuMap.get(player.getUniqueId());
        if (menu == null) return;

        String display = clicked.getItemMeta().getDisplayName();
        if (display.contains("+10")) menu.changeAmount(10);
        else if (display.contains("+1")) menu.changeAmount(1);
        else if (display.contains("-10")) menu.changeAmount(-10);
        else if (display.contains("-1")) menu.changeAmount(-1);
        else if (display.contains("Продать")) {
            int amountToSell = menu.getAmount();
            Order order = menu.getOrder();

            if (amountToSell <= 0) {
                player.sendMessage("§cВыберите количество.");
                return;
            }

            if (order.getCreator().equals(player.getUniqueId())) {
                player.sendMessage("§cНельзя продавать самому себе.");
                return;
            }

            OfflinePlayer buyer = Bukkit.getOfflinePlayer(order.getCreator());
            Economy econ = ZlomCore_PDev.getInstance().getEconomy();

            double total = amountToSell * order.getPricePerUnit();

            if (econ.getBalance(buyer) < total) {
                player.sendMessage("§cУ заказчика недостаточно денег.");
                if (buyer.isOnline()) {
                    ((Player) buyer).sendMessage("§cУ тебя недостаточно денег для оплаты заказа.");
                }
                return;
            }

            if (!hasEnoughItems(player, order.getMaterial(), amountToSell)) {
                player.sendMessage("§cУ тебя нет такого количества.");
                return;
            }

            removeItems(player, order.getMaterial(), amountToSell);
            econ.withdrawPlayer(buyer, total);
            econ.depositPlayer(player, total);

            order.reduceAmount(amountToSell);

            // Добавляем купленные предметы в заказчика
            PurchasedItemsManager.addPurchasedItem(order.getCreator(), order.getMaterial(), amountToSell);

            // Оповещение заказчика, если онлайн
            if (buyer.isOnline()) {
                ((Player) buyer).sendMessage("§aТы получил " + amountToSell + " " + order.getMaterial().name() + " за §6" + total + "!");
            }

            // Оповещение продавца
            player.sendMessage("§aПродажа успешна! Получено §6" + total);

            // Если заказ выполнен — удалить и оповестить заказчика
            if (order.isCompleted()) {
                manager.removeCompletedOrders();
                if (buyer.isOnline()) {
                    ((Player) buyer).sendMessage("§aЗаказ на " + order.getMaterial().name() + " выполнен!");
                }
            }

            menuMap.remove(player.getUniqueId());
            player.closeInventory();
        }
    }


    private boolean hasEnoughItems(Player player, Material type, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == type) {
                count += item.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    private void removeItems(Player player, Material type, int amount) {
        int left = amount;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == type) {
                int amt = item.getAmount();
                if (amt <= left) {
                    player.getInventory().setItem(i, null);
                    left -= amt;
                } else {
                    item.setAmount(amt - left);
                    break;
                }
            }
        }
    }
}

