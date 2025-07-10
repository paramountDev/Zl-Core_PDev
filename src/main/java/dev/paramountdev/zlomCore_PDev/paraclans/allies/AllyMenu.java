package dev.paramountdev.zlomCore_PDev.paraclans.allies;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import dev.paramountdev.zlomCore_PDev.paraclans.ClanManager;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

public class AllyMenu {
    private final Plugin plugin;
    private final ClanManager clanManager;

    public AllyMenu(Plugin plugin, ClanManager clanManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
    }

    public void open(Player player, Clan clan) {
        Map<String, AllyPermissions> allies = clan.getAllies();

        if (allies == null || allies.isEmpty()) {
            openInviteAnvilGUI(player, clan); // ⬅ Если союзов нет, открываем AnvilGUI
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 27, "Allies: " + clan.getName());

        for(int a = 0; a < inv.getSize(); a++){
            inv.setItem(a, ZlomCoreHelper.createFiller());
        }

        inv.setItem(27, ZlomCoreHelper.getBackButton());

        int i = 0;
        for (Map.Entry<String, AllyPermissions> entry : allies.entrySet()) {
            String allyName = entry.getKey();
            AllyPermissions perms = entry.getValue();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            if (headMeta != null) {
                headMeta.setDisplayName("§bСоюз: " + allyName);
                headMeta.setLore(List.of(
                        "§7Приват: " + (perms.canAccessClaims() ? "§aДоступен" : "§cНет"),
                        "§7PvP: " + (perms.isPvpDisabled() ? "§aОтключен" : "§cРазрешен"),
                        "§eЛКМ: переключить доступ к привату",
                        "§eПКМ: переключить PvP"
                ));
                head.setItemMeta(headMeta);
            }
            inv.setItem(i++, head);
        }

        // Кнопка добавления союза (внизу, центр — слот 22)
        ItemStack addAlly = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta meta = addAlly.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aДобавить союзника");
            meta.setLore(List.of("§7Нажмите, чтобы добавить клан в союз"));
            addAlly.setItemMeta(meta);
        }
        inv.setItem(22, addAlly);

        player.openInventory(inv);
    }

    public void openInviteAnvilGUI(Player inviter, Clan clan) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Введите имя клана:")
                .itemLeft(new ItemStack(Material.PLAYER_HEAD))
                .text("Имя клана")
                .onClick((slot, stateSnapshot) -> {
                    if (slot == AnvilGUI.Slot.OUTPUT) {
                        String input = stateSnapshot.getText();
                        if (input == null || input.trim().isEmpty()) {
                            inviter.sendMessage("§cВы не ввели название клана.");
                            return AnvilGUI.Response.close();
                        }

                        String targetName = ChatColor.stripColor(input.trim());
                        Clan target = clanManager.getClan(targetName);

                        if (target == null) {
                            inviter.sendMessage("§cКлан не найден.");
                            return AnvilGUI.Response.close();
                        }

                        if (clanManager.hasRequest(clan.getName(), targetName)) {
                            inviter.sendMessage("§eЗапрос уже отправлен.");
                            return AnvilGUI.Response.close();
                        }

                        clanManager.sendAllyRequest(clan.getName(), targetName);
                        inviter.sendMessage("§aЗапрос на союз с §e" + targetName + " §aотправлен.");
                        return AnvilGUI.Response.close();
                    }
                    return AnvilGUI.Response.close();
                })
                .open(inviter);
    }

    public void handleClick(InventoryClickEvent e, Player player, Clan playerClan) {
        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String displayName = clickedItem.getItemMeta().getDisplayName();
        if (displayName == null) return;

        if (displayName.contains("Добавить союзника")) {
            player.closeInventory();
            openInviteAnvilGUI(player, playerClan);
            return;
        }

        if (!displayName.startsWith("§bСоюз: ")) {
            return;
        }

        String targetClanName = displayName.substring(8);
        Clan targetClan = clanManager.getClan(targetClanName);

        if (targetClan == null) {
            return;
        }

        AllyPermissions perms = playerClan.getPermissionsForAlly(targetClanName);
        if (perms == null) {
            return;
        }



        switch (e.getClick()) {
            case LEFT:

                perms.setAccessClaims(!perms.canAccessClaims());
                player.sendMessage("§7Доступ к привату для " + targetClanName + ": " +
                        (perms.canAccessClaims() ? "§aразрешен" : "§cзапрещен"));
                break;
            case RIGHT:

                boolean newPvpStatus = !perms.isPvpDisabled();
                perms.setPvpDisabled(newPvpStatus);
                player.sendMessage("§7PvP для " + targetClanName + ": " +
                        (newPvpStatus ? "§cразрешен" : "§aотключен"));
                togglePvpBetweenClans(playerClan, targetClan, newPvpStatus);
                break;
            default:
                break;
        }

        open(player, playerClan);
    }


    private void togglePvpBetweenClans(Clan clan1, Clan clan2, boolean enabled) {
        // Твоя логика включения/отключения PvP между игроками этих кланов
        // Например:
        // pvpManager.setPvp(clan1.getName(), clan2.getName(), enabled);
    }

}
