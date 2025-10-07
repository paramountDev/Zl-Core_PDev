package dev.paramountdev.zlomCore_PDev.diamondshopper;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ShopMenu implements Listener {

    private final ZlomCore_PDev plugin = ZlomCore_PDev.getInstance();
    private final String TITLE = ChatColor.DARK_GREEN + "Магазин";

    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);


        ItemStack filler = ZlomCoreHelper.createFiller();
        for(int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        inv.setItem(11, createItem(Material.DIAMOND, "Алмаз"));
        inv.setItem(15, createItem(Material.NETHERITE_INGOT, "Незерит"));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name) {
        FileConfiguration config = plugin.getConfig();
        double buy = config.getDouble("shop.items." + mat.name() + ".buy-price", 0);
        double sell = config.getDouble("shop.items." + mat.name() + ".sell-price", 0);

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + name);
        meta.setLore(List.of(
                ChatColor.GREEN + "ЛКМ: продать 1 за " + sell,
                ChatColor.GREEN + "Shift+ЛКМ: продать всё",
                ChatColor.YELLOW + "ПКМ: купить 1 за " + buy,
                ChatColor.YELLOW + "Shift+ПКМ: купить на все деньги"
        ));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(TITLE)) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Material material = clicked.getType();
        FileConfiguration config = plugin.getConfig();
        double buyPrice = config.getDouble("shop.items." + material.name() + ".buy-price", 0);
        double sellPrice = config.getDouble("shop.items." + material.name() + ".sell-price", 0);
        double taxPercent = config.getDouble("shop.tax-percent", 0);

        ClickType click = event.getClick();

        if (click.isLeftClick()) {
            if (click.isShiftClick()) {
                // Продать всё
                int amount = countItems(player, material);
                if (amount <= 0) {
                    player.sendMessage(ChatColor.RED + "У вас нет этого предмета!");
                    return;
                }
                removeItems(player, material, amount);
                double total = sellPrice * amount;
                total -= total * (taxPercent / 100.0);
                plugin.getEconomy().depositPlayer(player, total);
                player.sendMessage(ChatColor.GREEN + "Вы продали " + amount + " " + material + " за " + total);
            } else {
                // Продать 1
                if (!player.getInventory().contains(material)) {
                    player.sendMessage(ChatColor.RED + "У вас нет этого предмета!");
                    return;
                }
                removeItems(player, material, 1);
                double total = sellPrice - (sellPrice * (taxPercent / 100.0));
                plugin.getEconomy().depositPlayer(player, total);
                player.sendMessage(ChatColor.GREEN + "Вы продали 1 " + material + " за " + total);
            }
        } else if (click.isRightClick()) {
            if (click.isShiftClick()) {
                // Купить на все деньги
                double balance = plugin.getEconomy().getBalance(player);
                int count = (int) (balance / buyPrice);
                if (count <= 0) {
                    player.sendMessage(ChatColor.RED + "Недостаточно зломов!");
                    return;
                }
                double total = buyPrice * count;
                total += total * (taxPercent / 100.0);
                plugin.getEconomy().withdrawPlayer(player, total);
                player.getInventory().addItem(new ItemStack(material, count));
                player.sendMessage(ChatColor.YELLOW + "Вы купили " + count + " " + material + " за " + total);
            } else {
                // Купить 1
                double total = buyPrice + (buyPrice * (taxPercent / 100.0));
                double balance = plugin.getEconomy().getBalance(player);
                if (balance < total) {
                    player.sendMessage(ChatColor.RED + "Недостаточно зломов!");
                    return;
                }
                plugin.getEconomy().withdrawPlayer(player, total);
                player.getInventory().addItem(new ItemStack(material, 1));
                player.sendMessage(ChatColor.YELLOW + "Вы купили 1 " + material + " за " + total);
            }
        }
    }

    private int countItems(Player player, Material mat) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == mat) {
                total += item.getAmount();
            }
        }
        return total;
    }

    private void removeItems(Player player, Material mat, int amount) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() != mat) continue;
            int amt = item.getAmount();
            if (amt <= amount) {
                player.getInventory().setItem(i, null);
                amount -= amt;
            } else {
                item.setAmount(amt - amount);
                break;
            }
            if (amount <= 0) break;
        }
    }
}
