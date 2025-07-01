package dev.paramountdev.zlomCore_PDev.paraclans.statistic;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.DecimalFormat;
import java.util.*;

public class ClanTopMenu {

    private final ClanStatsTracker statsTracker;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public ClanTopMenu(ClanStatsTracker statsTracker) {
        this.statsTracker = statsTracker;
    }

    public void openForPlayer(Player player, Set<String> clanMemberUUIDs) {
        Inventory inv = Bukkit.createInventory(null, 45, "§6Топ кланы");

        ItemStack filler = ZlomCoreHelper.createFiller();
        for(int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // --- Онлайн (часы)
        String topOnline = statsTracker.getTopPlayerByOnline(clanMemberUUIDs);
        inv.setItem(0, createIcon(Material.CLOCK, "§eТоп по онлайну"));
        inv.setItem(1, createPlayerHead(topOnline, "§f" + topOnline, statsTracker.getStats(Bukkit.getOfflinePlayer(topOnline).getUniqueId())));

        // --- Баланс игрока
        String topBalance = statsTracker.getTopPlayerByBalance(clanMemberUUIDs);
        inv.setItem(9, createIcon(Material.GOLD_NUGGET, "§eТоп по балансу"));
        inv.setItem(10, createPlayerHead(topBalance, "§f" + topBalance, statsTracker.getStats(Bukkit.getOfflinePlayer(topBalance).getUniqueId())));

        // --- Блоки
        String topBlocks = statsTracker.getTopPlayerByBlocks(clanMemberUUIDs);
        inv.setItem(18, createIcon(Material.GOLDEN_PICKAXE, "§eТоп по блокам"));
        inv.setItem(19, createPlayerHead(topBlocks, "§f" + topBlocks, statsTracker.getStats(Bukkit.getOfflinePlayer(topBlocks).getUniqueId())));

        // --- Убийства
        String topKills = statsTracker.getTopPlayerByKills(clanMemberUUIDs);
        inv.setItem(27, createIcon(Material.GOLDEN_SWORD, "§eТоп по убийствам"));
        inv.setItem(28, createPlayerHead(topKills, "§f" + topKills, statsTracker.getStats(Bukkit.getOfflinePlayer(topKills).getUniqueId())));

        player.openInventory(inv);
    }

    private ItemStack createIcon(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(Arrays.asList("§e------>"));
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPlayerHead(String playerName, String title, ClanStatsTracker.PlayerStats stats) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        meta.setOwningPlayer(offlinePlayer);
        meta.setDisplayName(title);

        List<String> lore = new ArrayList<>();
        lore.add("§7Онлайн: §f" + stats.getOnlineSeconds() + " сек.");
        lore.add("§7Баланс: §f" + df.format(stats.getBalance()) + " монет");
        lore.add("§7Блоки: §f" + stats.getBlocksMined());
        lore.add("§7Убийства: §f" + stats.getKills());
        meta.setLore(lore);

        skull.setItemMeta(meta);
        return skull;
    }
}
