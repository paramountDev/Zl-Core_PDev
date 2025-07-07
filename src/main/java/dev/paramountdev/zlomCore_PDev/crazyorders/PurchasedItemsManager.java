package dev.paramountdev.zlomCore_PDev.crazyorders;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Bukkit;

import java.util.*;

public class PurchasedItemsManager implements Listener {

    private static final Map<UUID, Map<Material, Integer>> purchasedItems = new HashMap<>();

    @EventHandler
    public void onPurchasedItemClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals("Купленные предметы")) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        UUID uuid = player.getUniqueId();
        Material mat = clicked.getType();
        int clickedAmount = clicked.getAmount();

        // Проверка и получение количества
        Map<Material, Integer> purchased = PurchasedItemsManager.getPurchasedItems(uuid);
        int totalAvailable = purchased.getOrDefault(mat, 0);
        if (totalAvailable <= 0) return;

        // Удаляем из менеджера купленных предметов
        int remaining = totalAvailable - clickedAmount;
        if (remaining > 0) {
            purchased.put(mat, remaining);
        } else {
            purchased.remove(mat);
        }

        // Выдаем обычный предмет (без меты)
        ItemStack plain = new ItemStack(mat, clickedAmount);
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(plain);

        if (!leftover.isEmpty()) {
            player.sendMessage("§cНедостаточно места в инвентаре!");
            // Возвращаем обратно в менеджер
            purchased.put(mat, totalAvailable);  // откатываем
            return;
        }

        // Перерисовать GUI через тик
        Bukkit.getScheduler().runTaskLater(ZlomCore_PDev.getInstance(), () -> {
            PurchasedItemsManager.openPurchasedMenu(player);
        }, 1L);
    }



    public static void addPurchasedItem(UUID playerUUID, Material material, int amount) {
        purchasedItems.putIfAbsent(playerUUID, new HashMap<>());
        Map<Material, Integer> items = purchasedItems.get(playerUUID);
        items.put(material, items.getOrDefault(material, 0) + amount);
    }

    public static Map<Material, Integer> getPurchasedItems(UUID playerUUID) {
        return purchasedItems.getOrDefault(playerUUID, Collections.emptyMap());
    }

    public static void clearPurchasedItems(UUID playerUUID) {
        purchasedItems.remove(playerUUID);
    }

    public static void openPurchasedMenu(Player player) {
        Map<Material, Integer> items = getPurchasedItems(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 27, "Купленные предметы");
        int slot = 0;

        for (Map.Entry<Material, Integer> entry : items.entrySet()) {
            ItemStack item = new ItemStack(entry.getKey(), entry.getValue());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a" + entry.getKey().name());
            meta.setLore(List.of("§7Количество: " + entry.getValue()));
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
            if (slot >= inv.getSize()) break; // чтобы не выйти за границы
        }

        ItemStack backbutton = ZlomCoreHelper.getBackButton();
        inv.setItem(18, backbutton);

        player.openInventory(inv);
    }
}
