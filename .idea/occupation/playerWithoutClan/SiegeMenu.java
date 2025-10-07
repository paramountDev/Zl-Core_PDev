package dev.paramountdev.zlomCore_PDev.LOL.occupation.playerWithoutClan;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.LOL.occupation.furnaceprivates.ProtectionRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlastFurnace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SiegeMenu implements Listener {

    public static final Map<UUID, PendingOutcome> occupationChoiceMap = new HashMap<>();

    public void openSiegeResultMenu(Player player, Location location, ProtectionRegion region) {
        Inventory inv = Bukkit.createInventory(null, 9, "Исход оккупации");

        ItemStack capture = new ItemStack(Material.BLACK_BANNER);
        ItemMeta captureMeta = capture.getItemMeta();
        captureMeta.setDisplayName(ChatColor.GREEN + "Захват");
        capture.setItemMeta(captureMeta);

        ItemStack loot = new ItemStack(Material.GOLD_INGOT);
        ItemMeta lootMeta = loot.getItemMeta();
        lootMeta.setDisplayName(ChatColor.YELLOW + "Грабеж");
        loot.setItemMeta(lootMeta);

        ItemStack ruin = new ItemStack(Material.TNT);
        ItemMeta ruinMeta = ruin.getItemMeta();
        ruinMeta.setDisplayName(ChatColor.RED + "Разорение");
        ruin.setItemMeta(ruinMeta);

        inv.setItem(2, capture);
        inv.setItem(4, loot);
        inv.setItem(6, ruin);

        player.openInventory(inv);

        // Сохраняем текущую оккупацию в мапу
        occupationChoiceMap.put(player.getUniqueId(), new PendingOutcome(region));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Исход оккупации")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            PendingOutcome outcome = occupationChoiceMap.remove(player.getUniqueId());
            if (outcome == null) return;

            String name = clicked.getItemMeta().getDisplayName();

            switch (ChatColor.stripColor(name)) {
                case "Захват":
                    handleCapture(player, outcome);
                    break;
                case "Грабеж":
                    handleLoot(player, outcome);
                    break;
                case "Разорение":
                    handleRuin(player, outcome);
                    break;
            }
            SiegeScheduler.processMenuSelection(player.getUniqueId(), name, outcome.getLocation(), outcome.getRegion());
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Исход оккупации")) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (occupationChoiceMap.containsKey(uuid)) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (event.getPlayer().isValid()) {
                            event.getPlayer().openInventory(event.getInventory());
                        }
                    }
                }.runTaskLater(ZlomCore_PDev.getInstance(), 1L);
            }
        }
    }

    public void handleCapture(Player player, PendingOutcome data) {
        ProtectionRegion region = data.getRegion();
        Location loc = data.getLocation();

        BlastFurnace furnace = getValidFurnaceOrNull(loc, player);
        if (furnace == null) return;
        FurnaceInventory inv = furnace.getInventory();

        ItemStack[] contents = inv.getContents();
        List<ItemStack> ownerShare = new ArrayList<>();
        List<ItemStack> attackerShare = new ArrayList<>();

        for (ItemStack item : contents) {
            if (item == null) continue;
            int total = item.getAmount();
            int toOwner = (int) (total * 0.75);
            int toAttacker = total - toOwner;

            ItemStack ownerItem = item.clone(); ownerItem.setAmount(toOwner);
            ItemStack attackerItem = item.clone(); attackerItem.setAmount(toAttacker);

            ownerShare.add(ownerItem);
            attackerShare.add(attackerItem);
        }

        Player oldOwner = Bukkit.getPlayer(region.getOwner());
        if (oldOwner != null) oldOwner.getInventory().addItem(ownerShare.toArray(new ItemStack[0]));
        player.getInventory().addItem(attackerShare.toArray(new ItemStack[0]));

        inv.clear();
        region.setOwner(player.getUniqueId());

        Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + " захватил регион!");
    }

    public void handleLoot(Player player, PendingOutcome data) {
        ProtectionRegion region = data.getRegion();
        Location loc = data.getLocation();

        BlastFurnace furnace = (BlastFurnace) loc.getBlock().getState();
        FurnaceInventory inv = furnace.getInventory();

        ItemStack[] contents = inv.getContents();
        for (ItemStack item : contents) {
            if (item == null) continue;
            player.getInventory().addItem(item);
        }

        inv.clear();

        Player owner = Bukkit.getPlayer(region.getOwner());
        if (owner != null) {
            owner.sendMessage(ChatColor.RED + "Ваш регион был ограблен игроком " + player.getName() + "!");
        }

        player.sendMessage(ChatColor.GOLD + "Вы ограбили регион и забрали всё содержимое.");
    }

    public void handleRuin(Player player, PendingOutcome data) {
        ProtectionRegion region = data.getRegion();
        Location loc = data.getLocation();

        BlastFurnace furnace = (BlastFurnace) loc.getBlock().getState();
        FurnaceInventory inv = furnace.getInventory();
        inv.clear();

        region.unprotect();
        region.setOwner(null);

        player.sendMessage(ChatColor.RED + "Вы разрушили приват. Все ресурсы уничтожены.");

        Bukkit.broadcastMessage(ChatColor.DARK_RED + player.getName() + " разорил регион на X: "
                + loc.getBlockX() + ", Y: " + loc.getBlockY() + ", Z: " + loc.getBlockZ() + "!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SiegeScheduler.onPlayerJoin(event.getPlayer());
    }

    private BlastFurnace getValidFurnaceOrNull(Location loc, Player player) {
        if (loc == null || loc.getBlock().getType() != Material.BLAST_FURNACE) {
            player.sendMessage(ChatColor.RED + "Регион больше не активен.");
            return null;
        }

        return (BlastFurnace) loc.getBlock().getState();
    }


    public static Map<UUID, PendingOutcome> getOccupationChoiceMap() {
        return occupationChoiceMap;
    }


}
