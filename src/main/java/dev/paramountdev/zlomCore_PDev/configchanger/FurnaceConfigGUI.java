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

public class FurnaceConfigGUI implements Listener {

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "Настройки: Приватная Печь");

        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();

        ItemStack burnMult = new ItemStack(Material.BLAZE_POWDER);
        ItemMeta burnMeta = burnMult.getItemMeta();
        burnMeta.setDisplayName("§eМножитель горения: §f" + config.getInt("burn_multiplier"));
        burnMeta.setLore(List.of("§7ЛКМ +1", "§7ПКМ -1"));
        burnMult.setItemMeta(burnMeta);
        gui.setItem(11, burnMult);

        ItemStack adminBypass = new ItemStack(Material.BARRIER);
        ItemMeta adminMeta = adminBypass.getItemMeta();
        adminMeta.setDisplayName("§eОбход администратора: §f" + config.getBoolean("admin_bypass"));
        adminMeta.setLore(List.of("§7Клик для переключения"));
        adminBypass.setItemMeta(adminMeta);
        gui.setItem(13, adminBypass);

        ItemStack cooldown = new ItemStack(Material.CLOCK);
        ItemMeta cdMeta = cooldown.getItemMeta();
        cdMeta.setDisplayName("§eКД в бою (сек): §f" + config.getInt("combat_cooldown_seconds"));
        cdMeta.setLore(List.of("§7ЛКМ +1", "§7ПКМ -1"));
        cooldown.setItemMeta(cdMeta);
        gui.setItem(15, cooldown);

        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals("Настройки: Приватная Печь")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();

        switch (clicked.getType()) {
            case BLAZE_POWDER -> {
                int current = config.getInt("burn_multiplier");
                if (e.getClick().isLeftClick()) current++;
                else if (e.getClick().isRightClick()) current = Math.max(1, current - 1);
                config.set("burn_multiplier", current);
                ZlomCore_PDev.getInstance().saveConfig();
                open(player);
            }
            case BARRIER -> {
                boolean val = config.getBoolean("admin_bypass");
                config.set("admin_bypass", !val);
                ZlomCore_PDev.getInstance().saveConfig();
                open(player);
            }
            case CLOCK -> {
                int cd = config.getInt("combat_cooldown_seconds");
                if (e.getClick().isLeftClick()) cd++;
                else if (e.getClick().isRightClick()) cd = Math.max(0, cd - 1);
                config.set("combat_cooldown_seconds", cd);
                ZlomCore_PDev.getInstance().saveConfig();
                open(player);
            }
        }
    }
}

