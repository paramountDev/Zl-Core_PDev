package dev.paramountdev.zlomCore_PDev.configchanger;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BoostyConfigGUI implements Listener {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Настройки: Boosty");

        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();

        gui.setItem(10, createNumberItem(Material.BOOK, "Лимит по умолчанию", "default-limit"));
        gui.setItem(11, createNumberItem(Material.BOOK, "Boosty L1", "boostyL1"));
        gui.setItem(12, createNumberItem(Material.BOOK, "Boosty L2", "boostyL2"));
        gui.setItem(13, createNumberItem(Material.BOOK, "Boosty L3", "boostyL3"));
        gui.setItem(14, createNumberItem(Material.BOOK, "Boosty L4", "boostyL4"));

        player.openInventory(gui);
    }

    private static ItemStack createNumberItem(Material material, String name, String configKey) {
        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();
        int value = config.getInt(configKey);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e" + name + ": §f" + value);
        meta.setLore(List.of("§7ЛКМ +1", "§7ПКМ -1"));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals("Настройки: Boosty")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String display = clicked.getItemMeta().getDisplayName().replace("§e", "").split(":")[0];
        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();

        switch (display) {
            case "Лимит по умолчанию" -> updateInt(player, "default-limit", e);
            case "Boosty L1" -> updateInt(player, "boostyL1", e);
            case "Boosty L2" -> updateInt(player, "boostyL2", e);
            case "Boosty L3" -> updateInt(player, "boostyL3", e);
            case "Boosty L4" -> updateInt(player, "boostyL4", e);
        }
    }

    private void updateInt(Player player, String key, InventoryClickEvent e) {
        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();
        int val = config.getInt(key);
        if (e.getClick().isLeftClick()) val++;
        else if (e.getClick().isRightClick()) val = Math.max(0, val - 1);
        config.set(key, val);
        ZlomCore_PDev.getInstance().saveConfig();
        BoostyConfigGUI.open(player);
    }
}
