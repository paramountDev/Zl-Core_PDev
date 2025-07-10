package dev.paramountdev.zlomCore_PDev.paraclans.statistic;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
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
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // --- Онлайн (часы)
        String topOnline = statsTracker.getTopPlayerByOnline(clanMemberUUIDs);
        List<String> topOnlineList = statsTracker.getTopNByOnline(clanMemberUUIDs, 10);
        inv.setItem(0, createIcon(Material.CLOCK, "§eТоп по онлайну", topOnlineList));
        inv.setItem(1, createPlayerHead(topOnline, "§f" + topOnline, statsTracker.getStats(Bukkit.getOfflinePlayer(topOnline).getUniqueId())));

        // --- Баланс
        String topBalance = statsTracker.getTopPlayerByBalance(clanMemberUUIDs);
        List<String> topBalanceList = statsTracker.getTopNByBalance(clanMemberUUIDs, 10);
        inv.setItem(9, createIcon(Material.GOLD_NUGGET, "§eТоп по балансу", topBalanceList));
        inv.setItem(10, createPlayerHead(topBalance, "§f" + topBalance, statsTracker.getStats(Bukkit.getOfflinePlayer(topBalance).getUniqueId())));

        // --- Блоки
        String topBlocks = statsTracker.getTopPlayerByBlocks(clanMemberUUIDs);
        List<String> topBlocksList = statsTracker.getTopNByBlocks(clanMemberUUIDs, 10);
        inv.setItem(18, createIcon(Material.GOLDEN_PICKAXE, "§eТоп по блокам", topBlocksList));
        inv.setItem(19, createPlayerHead(topBlocks, "§f" + topBlocks, statsTracker.getStats(Bukkit.getOfflinePlayer(topBlocks).getUniqueId())));


        Map<String, Set<String>> allClans = ZlomCore_PDev.getInstance().getAllClansAsMap();

        List<Map.Entry<String, Double>> topClanBalances = statsTracker.getTopClansByBalance(allClans, 10);

        List<String> clanBalanceLore = new ArrayList<>();
        for (int i = 0; i < topClanBalances.size(); i++) {
            Map.Entry<String, Double> entry = topClanBalances.get(i);
            String clanName = entry.getKey();
            double balance = entry.getValue();
            clanBalanceLore.add("§f" + ". §b" + clanName + " §7- §e" + df.format(balance));
        }

        ItemStack balanceIcon = createIcon(Material.EMERALD, "§aТоп кланы по балансу", clanBalanceLore);
        inv.setItem(27, balanceIcon); // например, в 4-й ряд

        inv.setItem(inv.getSize() - 1, ZlomCoreHelper.getBackButton());

        player.openInventory(inv);
    }



    private ItemStack createIcon(Material material, String name, List<String> topList) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add("§7Топ 10:");
        for (int i = 0; i < topList.size(); i++) {
            lore.add("§f" + (i + 1) + ". §a" + topList.get(i));
        }

        meta.setLore(lore);
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
