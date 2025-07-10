package dev.paramountdev.zlomCore_PDev.crazyorders;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SellAmountMenu {

    private final Player player;
    private final Order order;
    private int amount = 0;

    public SellAmountMenu(Player player, Order order) {
        this.player = player;
        this.order = order;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54, "Продажа: " + order.getMaterial());

        ZlomCoreHelper.fillInventory(inv, 0);

        // Вертикальное расположение кнопок
        inv.setItem(11, ZlomCoreHelper.createMinusHead("§c-10", List.of("")));
        inv.setItem(20, ZlomCoreHelper.createMinusHead("§c-1", List.of("")));
        inv.setItem(24, ZlomCoreHelper.createPlusHead("§a+1", List.of("")));
        inv.setItem(33, ZlomCoreHelper.createPlusHead("§a+10", List.of("")));

        // Кнопка продажи
        inv.setItem(49, createButton("§eПродать §f" + amount, Material.GOLD_INGOT));

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
