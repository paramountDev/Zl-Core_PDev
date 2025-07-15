package dev.paramountdev.zlomCore_PDev.worlds;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.createFiller;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.createPlusHead;

public class WorldMenu implements Listener {
    private final ZlomCore_PDev plugin;

    public WorldMenu(ZlomCore_PDev plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openWorldMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Мои миры");

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        // Плюсик для создания мира
        int owned = getWorldsCount(player);
        double price = 1000000 * (owned + 1);
        ItemStack createWorld = createPlusHead(ChatColor.GREEN + "Создать мир", Arrays.asList(
                ChatColor.GOLD + "Нажмите, чтобы создать новый мир.",
                ChatColor.GRAY + "Стоимость: " + ChatColor.GREEN + price
        ));
        inv.setItem(20, createWorld);

        // Список миров игрока
        List<String> ownedWorlds = getOwnedWorlds(player);
        int index = 22;
        for (String worldName : ownedWorlds) {
            inv.setItem(index, getWorldIcon(worldName));
            index++;
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onWorldMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        if (title.startsWith(ChatColor.DARK_GRAY + "Мои миры")) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.PLAYER_HEAD && clicked.getItemMeta().getDisplayName().contains("Создать мир")) {
                openWorldCreateAnvilGUI(player);
                return;
            }

            if (clicked.getType() == Material.GRASS_BLOCK) {
                String worldName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                openWorldManageMenu(player, worldName);
            }
            return;
        }

        // Меню управления одним миром
        if (title.startsWith(ChatColor.DARK_GREEN + "Управление: ")) {
            event.setCancelled(true);
            String worldName = ChatColor.stripColor(title.replace("Управление: ", ""));

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.ENDER_PEARL) {
                plugin.getWorldManager().teleportToWorld(player, worldName);
                player.closeInventory();
            }

            if (clicked.getType() == Material.NAME_TAG) {
                openAccessControlMenu(player, "pworld_" + worldName);
            }
        }

        if (title.startsWith(ChatColor.GOLD + "Доступ к ")) {
            event.setCancelled(true);
            String worldName = "pworld_" + ChatColor.stripColor(title.replace("Доступ к ", ""));

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            if (clicked.getType() == Material.PLAYER_HEAD && clicked.getItemMeta().getDisplayName().contains("Добавить")) {
                new AnvilGUI.Builder()
                        .plugin(plugin)
                        .title("Ник игрока:")
                        .text("")
                        .itemLeft(createPlusHead(ChatColor.YELLOW + "Ник игрока", List.of("Введите ник")))
                        .onClick((slot, state) -> {
                            if (slot != AnvilGUI.Slot.OUTPUT) return AnvilGUI.Response.close();
                            String name = state.getText();

                            if (plugin.getAccessManager().addAccess(worldName, name)) {
                                player.sendMessage("§aДоступ выдан игроку §e" + name);
                            } else {
                                player.sendMessage("§cНе удалось выдать доступ. Лимит 5 игроков.");
                            }
                            return AnvilGUI.Response.close();
                        }).open(player);
                return;
            }

            if (clicked.getType() == Material.PLAYER_HEAD) {
                String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                plugin.getAccessManager().removeAccess(worldName, name);
                player.sendMessage("§cДоступ удалён у игрока §e" + name);
                player.closeInventory();
                Bukkit.getScheduler().runTaskLater(plugin, () -> openAccessControlMenu(player, worldName), 2L);
            }
        }
    }

    private void openWorldCreateAnvilGUI(Player player) {
        int count = getWorldsCount(player);
        double rawCost = plugin.getConfig().getDouble("world.creation-cost", 500.0);
        double cost = rawCost * (count + 1);

        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Имя нового мира:")
                .text("")
                .itemLeft(createPlusHead(ChatColor.GOLD + "Имя мира", List.of(ChatColor.GRAY + "Введите уникальное имя мира.")))
                .onClick((slot, state) -> {
                    if (slot != AnvilGUI.Slot.OUTPUT) return AnvilGUI.Response.close();
                    String name = state.getText();

                    if (name == null || name.trim().isEmpty() || name.contains(" ")) {
                        player.sendMessage("§cНекорректное имя мира.");
                        return AnvilGUI.Response.close();
                    }

                    // Проверка баланса и создание
                    if (!plugin.getEconomy().has(player, cost)) {
                        player.sendMessage("§cНедостаточно средств для создания мира!");
                        return AnvilGUI.Response.close();
                    }

                    plugin.getEconomy().withdrawPlayer(player, cost);
                    plugin.getWorldManager().createWorld(player, name);
                    player.sendMessage("§aМир успешно создан: §e" + name);
                    return AnvilGUI.Response.close();
                })
                .open(player);
    }

    private void openWorldManageMenu(Player player, String worldName) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Управление: " + worldName);

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        ItemStack join = getItemStack(Material.ENDER_PEARL, ChatColor.GREEN + "Зайти в мир",
                ChatColor.GRAY + "Нажмите для телепортации в мир");

        ItemStack access = getItemStack(Material.NAME_TAG, ChatColor.YELLOW + "Управление доступом",
                ChatColor.GRAY + "Нажмите, чтобы добавить/удалить игрока");

        inv.setItem(11, join);
        inv.setItem(15, access);

        player.openInventory(inv);
    }


    private void openAccessControlMenu(Player player, String worldName) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Доступ к " + worldName.replace("pworld_", ""));

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, filler);

        List<String> allowedPlayers = plugin.getAccessManager().getAllowed(worldName);
        int index = 10;
        for (String name : allowedPlayers) {
            inv.setItem(index++, getItemStack(Material.PLAYER_HEAD, ChatColor.YELLOW + name,
                    ChatColor.GRAY + "Нажмите, чтобы удалить доступ"));
        }

        ItemStack addAccess = createPlusHead(ChatColor.GREEN + "Добавить игрока",
                List.of(ChatColor.GRAY + "Нажмите, чтобы ввести ник игрока."));
        inv.setItem(49, addAccess);

        player.openInventory(inv);
    }


    private int getWorldsCount(Player player) {
        return getOwnedWorlds(player).size();
    }

    private List<String> getOwnedWorlds(Player player) {
        FileConfiguration worlds = plugin.getWorldsConfig();
        List<String> result = new ArrayList<>();
        String base = "worlds." + player.getUniqueId();
        Object value = worlds.get(base);
        if (value instanceof String) result.add((String) value);
        else if (value instanceof List) result.addAll((List<String>) value);
        return result;
    }

    private ItemStack getWorldIcon(String worldName) {
        return getItemStack(Material.GRASS_BLOCK, ChatColor.GREEN + worldName,
                ChatColor.GRAY + "Нажмите для управления этим миром.");
    }


    private ItemStack getItemStack(Material mat, String name, String... loreLines) {
        return getItemStack(mat, name, Arrays.asList(loreLines));
    }

    private ItemStack getItemStack(Material mat, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(loreLines);
            item.setItemMeta(meta);
        }
        return item;
    }
}

