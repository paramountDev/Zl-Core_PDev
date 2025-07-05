package dev.paramountdev.zlomCore_PDev.orders;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemSelectGUI implements Listener {
    private static final int ITEMS_PER_PAGE = 45;
    private static final Map<UUID, Boolean> awaitingSearchInput = new HashMap<>();

    public static void open(Player player, int page) {
        Inventory inv = Bukkit.createInventory(null, 54, "Выбор предмета - стр. " + page);

        Material[] allMaterials = Material.values();
        int start = (page - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allMaterials.length);
        List<String> blacklist = ZlomCore_PDev.getInstance().getConfig().getStringList("blacklisted-materials");

        for (int i = start; i < end; i++) {
            Material mat = allMaterials[i];
            if (!mat.isItem() || mat == Material.AIR || blacklist.contains(mat.name())) continue;

            inv.addItem(new ItemStack(mat));
        }

        // Назад
        inv.setItem(45, ZlomCoreHelper.getBackButton());

        // Кнопка ПОИСК
        inv.setItem(49, createNavButton("§b🔍 Поиск", Material.SPYGLASS));

        // Вперёд/назад
        if (page > 1) {
            inv.setItem(46, createNavButton("§e← Назад", Material.ARROW));
        }
        if (end < allMaterials.length) {
            inv.setItem(52, createNavButton("§eВперёд →", Material.ARROW));
        }

        player.openInventory(inv);
    }

    private static ItemStack createNavButton(String name, Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = e.getView().getTitle();
        if (!title.startsWith("Выбор предмета")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if(clicked.hasItemMeta()) {
            String name = clicked.getItemMeta().getDisplayName();

            // Навигация
            if (clicked.getType() == Material.ARROW) {
                int currentPage = Integer.parseInt(title.split("стр. ")[1]);
                if (name.contains("Назад")) {
                    open(player, currentPage - 1);
                } else if (name.contains("Вперёд")) {
                    open(player, currentPage + 1);
                }
                return;
            }

            // Поиск
            if (clicked.getType() == Material.SPYGLASS && name.contains("Поиск")) {
                player.closeInventory();
                awaitingSearchInput.put(player.getUniqueId(), true);
                player.sendMessage("§aВведите имя предмета на английском:");
                return;
            }
        }
        // Назад
        if (clicked.getType() == Material.PRISMARINE_SHARD) {
            return;
        }

        // Выбор предмета
        Material selected = clicked.getType();
        player.closeInventory();
        ChatInputHandler.startAmountInput(player, selected);
    }




    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!awaitingSearchInput.containsKey(uuid)) return;

        event.setCancelled(true);
        awaitingSearchInput.remove(uuid);

        String input = event.getMessage().toUpperCase().replace(" ", "_");
        List<String> blacklist = ZlomCore_PDev.getInstance().getConfig().getStringList("blacklisted-materials");

        List<Material> matches = Arrays.stream(Material.values())
                .filter(Material::isItem)
                .filter(mat -> !blacklist.contains(mat.name()))
                .filter(mat -> mat.name().startsWith(input))
                .toList();

        if (matches.isEmpty()) {
            player.sendMessage("§cПредметы по запросу не найдены.");
            Bukkit.getScheduler().runTask(ZlomCore_PDev.getInstance(), () -> ItemSelectGUI.open(player, 1));
            return;
        }

        Bukkit.getScheduler().runTask(ZlomCore_PDev.getInstance(), () -> {
            Inventory result = Bukkit.createInventory(null, 54, "Результат поиска");

            int slot = 0;
            for (Material match : matches) {
                if (slot >= 45) break;
                result.setItem(slot++, new ItemStack(match));
            }

            result.setItem(45, ZlomCoreHelper.getBackButton());
            player.openInventory(result);
        });
    }

    @EventHandler
    public void onSearchResultClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = e.getView().getTitle();
        if (!title.equalsIgnoreCase("Результат поиска")) return;

        e.setCancelled(true);
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Назад в главное меню
        if (clicked.equals(ZlomCoreHelper.getBackButton())) {
            player.performCommand("orders");
            return;
        }

        // Выбор предмета
        Material selected = clicked.getType();
        player.closeInventory();
        ChatInputHandler.startAmountInput(player, selected);
    }


}
