package dev.paramountdev.zlomCore_PDev.crazyorders;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

public class SellAmountMenu {

    private final Player player;
    private final Order order;
    private int amount = 0;

    public SellAmountMenu(Player player, Order order) {
        this.player = player;
        this.order = order;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27, "Продажа: " + order.getMaterial());

        inv.setItem(10, createButton("§a+1", Material.LIME_STAINED_GLASS_PANE));
        inv.setItem(11, createButton("§a+10", Material.LIME_STAINED_GLASS_PANE));
        inv.setItem(12, createButton("§c-1", Material.RED_STAINED_GLASS_PANE));
        inv.setItem(13, createButton("§c-10", Material.RED_STAINED_GLASS_PANE));
        inv.setItem(15, createButton("§eПродать §f" + amount, Material.GOLD_INGOT));

        // Храним текущее меню в static map для обработчика
        SellAmountListener.menuMap.put(player.getUniqueId(), this);

        player.openInventory(inv);
    }

    private ItemStack createButton(String name, Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public void changeAmount(int delta) {
        amount = Math.max(0, Math.min(order.getAmountRemaining(), amount + delta));
        open(); // перерисовать меню
    }

    public int getAmount() {
        return amount;
    }

    public Order getOrder() {
        return order;
    }

    public Player getPlayer() {
        return player;
    }
}
