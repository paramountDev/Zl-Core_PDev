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

public class ClansConfigGUI implements Listener {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "Настройки: Кланы");

        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();

        ItemStack members = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = members.getItemMeta();
        meta.setDisplayName("§eМакс. участников: §f" + config.getInt("max-members"));
        meta.setLore(List.of("§7ЛКМ +1", "§7ПКМ -1"));
        members.setItemMeta(meta);
        gui.setItem(3, members);

        ItemStack price = new ItemStack(Material.EMERALD);
        ItemMeta priceMeta = price.getItemMeta();
        priceMeta.setDisplayName("§eЦена создания: §f" + config.getDouble("create-price"));
        priceMeta.setLore(List.of("§7ЛКМ +1000", "§7ПКМ -1000"));
        price.setItemMeta(priceMeta);
        gui.setItem(5, price);

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals("Настройки: Кланы")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();

        switch (clicked.getType()) {
            case PLAYER_HEAD -> {
                int members = config.getInt("max-members");
                if (e.getClick().isLeftClick()) members++;
                else if (e.getClick().isRightClick()) members = Math.max(1, members - 1);
                config.set("max-members", members);
                ZlomCore_PDev.getInstance().saveConfig();
                open(player);
            }
            case EMERALD -> {
                double price = config.getDouble("create-price");
                if (e.getClick().isLeftClick()) price += 1000;
                else if (e.getClick().isRightClick()) price = Math.max(0, price - 1000);
                config.set("create-price", price);
                ZlomCore_PDev.getInstance().saveConfig();
                open(player);
            }
        }
    }
}
