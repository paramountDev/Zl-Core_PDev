
package dev.paramountdev.zlomCore_PDev.paraclans.levels;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class ClanLevelMenu implements Listener {

    private static final String MENU_TITLE = ChatColor.DARK_GREEN + "Уровень клана";

    public void openLevelMenu(Player player, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 27, MENU_TITLE);


        ItemStack wool = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Повысить уровень клана");

        int currentLevel = clan.getLevel();
        double cost = getLevelCost(currentLevel + 1);

        meta.setLore(List.of(
                "",
                ChatColor.GRAY + "Нажмите, чтобы повысить уровень клана.",
                "",
                ChatColor.YELLOW + "Текущий уровень: " + currentLevel,
                ChatColor.GOLD + "Стоимость повышения: " + cost + " зломов",
                "",
                ChatColor.AQUA + "Что даст следующий уровень:",
                ChatColor.GRAY + "- Приватов: " + (2 + currentLevel) + " → " + (3 + currentLevel),
                ChatColor.GRAY + "- Макс. диаметр привата: " + (100 + 25 * currentLevel) + " → " + (100 + 25 * (currentLevel + 1)),
                ChatColor.GRAY + "- Повышение позиции в рейтинге кланов"
        ));
        wool.setItemMeta(meta);


        ItemStack filler = ZlomCoreHelper.createFiller();
        for(int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        inv.setItem(inv.getSize() - 1, ZlomCoreHelper.getBackButton());

        inv.setItem(13, wool);
        player.openInventory(inv);
    }

    private double getLevelCost(int level) {
        FileConfiguration config = ZlomCore_PDev.getInstance().getConfig();
        return config.getDouble("clan.level-costs.level-" + level, 1000 * level);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getView().getTitle().equals(MENU_TITLE)) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.GREEN_WOOL) return;

            UUID uuid = player.getUniqueId();
            Clan clan = ZlomCore_PDev.getInstance().getClanByPlayer(uuid);
            if (clan == null) {
                player.sendMessage(ChatColor.RED + "Вы не состоите в клане!");
                player.closeInventory();
                return;
            }

            double cost = getLevelCost(clan.getLevel() + 1);
            double balance = ZlomCore_PDev.getInstance().getEconomy().getBalance(player);

            if (balance < cost) {
                player.sendMessage(ChatColor.RED + "Недостаточно зломов для повышения уровня клана!");
                return;
            }

            ZlomCore_PDev.getInstance().getEconomy().withdrawPlayer(player, cost);
            clan.setLevel(clan.getLevel() + 1);

            for (String memberId : clan.getMembers()) {
                Player member = Bukkit.getPlayer(UUID.fromString(memberId));
                if (member != null && member.isOnline()) {
                    member.sendTitle(ChatColor.GREEN + "Уровень вашего клана повышен!",
                            ChatColor.YELLOW + "Повысил игрок: " + player.getName());
                    member.playSound(member.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
            player.closeInventory();
        }
    }
}
