package dev.paramountdev.zlomCore_PDev.paraclans;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.FurnaceProtectionManager;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.ProtectionRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.getItemStack;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.updatePlayerNames;
import static dev.paramountdev.zlomCore_PDev.ZlomCore_PDev.getRoleManager;

public class ClanMenu implements Listener {

    private final Map<String, Boolean> clanPvpEnabled = new HashMap<>();
    private final Map<String, ChatColor> clanPrefixColor = new HashMap<>();
    private final Map<UUID, Boolean> renameWaiting = new HashMap<>();
    private final ZlomCore_PDev plugin;

    private final List<ChatColor> prefixColors = Arrays.asList(
            ChatColor.AQUA, ChatColor.YELLOW, ChatColor.RED,
            ChatColor.BLUE, ChatColor.LIGHT_PURPLE, ChatColor.GREEN
    );

    public ClanMenu(ZlomCore_PDev plugin) {
        this.plugin = plugin;
    }

    public void openClanMenu(Player player, Clan clan) {

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_GREEN + "Меню клана");

        // Чёрные стеклянные панели
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        // Игроки в клане (слизь)
        ItemStack slime = new ItemStack(Material.SLIME_BALL);
        ItemMeta slimeMeta = slime.getItemMeta();
        slimeMeta.setDisplayName(ChatColor.GREEN + "Игроки в клане");
        slimeMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Нажмите, чтобы посмотреть участников"));
        slime.setItemMeta(slimeMeta);
        inv.setItem(10, slime);

        // Голова владельца
        ItemStack ownerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) ownerHead.getItemMeta();
        OfflinePlayer owner = Bukkit.getOfflinePlayer(clan.getOwner());
        skullMeta.setOwningPlayer(owner);
        skullMeta.setDisplayName(ChatColor.GOLD + "Владелец клана - " + owner.getName());
        skullMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Этот игрок владеет этим кланом."));
        ownerHead.setItemMeta(skullMeta);
        inv.setItem(12, ownerHead);

        // Враждующие кланы
        ItemStack war = new ItemStack(Material.RED_DYE);
        List<String> warLore = new ArrayList<>();
        if (clan.getWars().isEmpty()) {
            warLore.add(ChatColor.GRAY + "Нет враждующих кланов");
        } else {
            for (Clan enemy : clan.getWars()) {
                warLore.add(ChatColor.GRAY + enemy.getName());
            }
        }
        ItemMeta warMeta = war.getItemMeta();
        warMeta.setDisplayName(ChatColor.RED + "Враждующие кланы");
        warMeta.setLore(warLore);
        war.setItemMeta(warMeta);
        inv.setItem(14, war);

        ItemStack furnace = new ItemStack(Material.BLAST_FURNACE);
        ItemMeta furnaceMeta = furnace.getItemMeta();
        furnaceMeta.setDisplayName(ChatColor.GOLD + "Приваты игроков клана");
        furnaceMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Нажмите, чтобы увидеть все приваты соклановцев"));
        furnace.setItemMeta(furnaceMeta);
        inv.setItem(22, furnace); // Центр GUI

        // Торговые соглашения
        ItemStack trade = new ItemStack(Material.GREEN_DYE);
        ItemMeta tradeMeta = trade.getItemMeta();
        tradeMeta.setDisplayName(ChatColor.GREEN + "Торговые соглашения");
        List<String> tradeLore = new ArrayList<>();
        if (clan.getTradecontrats().isEmpty()) {
            tradeLore.add(ChatColor.GRAY + "Нет торговых соглашений");
        } else {
            for (Clan partner : clan.getTradecontrats()) {
                tradeLore.add(ChatColor.GRAY + partner.getName());
            }
        }
        tradeMeta.setLore(tradeLore);
        trade.setItemMeta(tradeMeta);
        inv.setItem(16, trade);

        // Кнопка списка всех кланов
        ItemStack clanList = new ItemStack(Material.SKULL_BANNER_PATTERN);
        ItemMeta clanListMeta = clanList.getItemMeta();
        clanListMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Дипломатия");
        clanListMeta.setLore(Collections.singletonList(ChatColor.GRAY + "Нажмите, чтобы открыть список вашей дипломатии"));
        clanList.setItemMeta(clanListMeta);
        inv.setItem(4, clanList);

        player.openInventory(inv);
    }

    public void openClanMembersMenu(Player viewer, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Участники клана");

        int index = 0;
        boolean isOwner = clan.getOwner().equals(viewer.getUniqueId());

        for (String uuidString : clan.getMembers()) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);

                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setOwningPlayer(member);
                meta.setDisplayName("§e" + member.getName());

                if (isOwner && !uuid.equals(viewer.getUniqueId())) {
                    int level = getRoleManager().getMemberLevel(clan.getName(), uuid);
                    Integer next = getRoleManager().getNextLevel(level);
                    Integer prev = getRoleManager().getPreviousLevel(level);

                    List<String> lore = new ArrayList<>();
                    if (prev != null) {
                        String name = getRoleManager().getRole(prev).getName();
                        lore.add("§7ЛКМ - понизить до §c" + name);
                    }
                    if (next != null) {
                        String name = getRoleManager().getRole(next).getName();
                        lore.add("§7ПКМ - повысить до §a" + name);
                    }
                    meta.setLore(lore);
                }
                skull.setItemMeta(meta);
                inv.setItem(index++, skull);
            } catch (IllegalArgumentException ex) {
                viewer.sendMessage("§cОшибка при загрузке игрока: " + uuidString);
            }
        }

        inv.setItem(53, getBackButton());
        viewer.openInventory(inv);
    }

    @EventHandler
    public void onClanMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.equalsIgnoreCase("Меню клана")
                && !title.equalsIgnoreCase("Участники клана")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Clan clan = plugin.getClans().get(plugin.getPlayerClan().get(player.getUniqueId()));
        if (clan == null) return;

        switch (clicked.getType()) {
            case BLAST_FURNACE:
                openClanRegionsMenu(player, clan);
                break;

            case SLIME_BALL:
                openClanMembersMenu(player, clan);
                break;

            case SKULL_BANNER_PATTERN:
                openAllClansMenu(player, clan);
                break;

            case PLAYER_HEAD:
                if (!clan.getOwner().equals(player.getUniqueId())) return;

                SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                if (meta == null || meta.getOwningPlayer() == null) return;

                OfflinePlayer target = meta.getOwningPlayer();
                UUID targetUuid = target.getUniqueId();
                if (targetUuid.equals(player.getUniqueId())) return; // нельзя менять себе уровень

                int currentLevel = getRoleManager().getMemberLevel(clan.getName(), targetUuid);

                if (event.getClick() == ClickType.LEFT) {
                    Integer newLevel = getRoleManager().getPreviousLevel(currentLevel);
                    if (newLevel != null) {
                        getRoleManager().setMemberLevel(clan.getName(), targetUuid, newLevel);
                        player.sendMessage("§eИгрок §6" + target.getName() + " §eпонижен до §c" +
                                getRoleManager().getRole(newLevel).getName());
                        openClanMembersMenu(player, clan);
                    }
                } else if (event.getClick() == ClickType.RIGHT) {
                    Integer newLevel = getRoleManager().getNextLevel(currentLevel);
                    if (newLevel != null) {
                        getRoleManager().setMemberLevel(clan.getName(), targetUuid, newLevel);
                        player.sendMessage("§eИгрок §6" + target.getName() + " §eповышен до §a" +
                                getRoleManager().getRole(newLevel).getName());
                        openClanMembersMenu(player, clan);
                    }
                }
                break;

            case RED_DYE:
            case GREEN_DYE:
                event.setCancelled(true);
        }
    }

    public void openAllClansMenu(Player player, Clan currentClan) {
        Inventory inv = Bukkit.createInventory(null, 54, "§bДипломатия");
        int index = 0;

        for (Clan other : plugin.clans.values()) {
            if (other.getName().equals(currentClan.getName())) continue;

            ItemStack skull = new ItemStack(Material.VAULT);
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + other.getName());

            List<String> lore = new ArrayList<>();
            boolean isAtWar = currentClan.getWars().contains(other);
            boolean hasTrade = currentClan.getTradecontrats().contains(other);

            if (isAtWar) {
                lore.add(ChatColor.RED + "ЛКМ - Завершить войну");
            } else {
                lore.add(ChatColor.GREEN + "ЛКМ - Объявить войну");
            }

            if (hasTrade) {
                lore.add(ChatColor.RED + "ПКМ - Разорвать торговое соглашение");
            } else {
                lore.add(ChatColor.GREEN + "ПКМ - Заключить торговое соглашение");
            }

            meta.setLore(lore);
            skull.setItemMeta(meta);
            inv.setItem(index++, skull);
            if (index >= 54) break;
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onAllClansMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.equalsIgnoreCase("Дипломатия")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.VAULT) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) return;

        String clanName = ChatColor.stripColor(meta.getDisplayName());
        Clan targetClan = plugin.clans.get(clanName);
        Clan currentClan = plugin.clans.get(plugin.playerClan.get(player.getUniqueId()));

        if (targetClan == null || currentClan == null || targetClan.getName().equals(currentClan.getName())) return;

        if (event.getClick() == ClickType.LEFT) {
            if (currentClan.getWars().contains(targetClan)) {
                player.performCommand("pclans warend " + targetClan.getName());
                player.closeInventory();
            } else {
                player.performCommand("pclans wardeclare " + targetClan.getName());
                player.closeInventory();
            }
        } else if (event.getClick() == ClickType.RIGHT) {
            if (currentClan.getTradecontrats().contains(targetClan)) {
                player.performCommand("pclans tradecontractend " + targetClan.getName());
                player.closeInventory();
            } else {
                player.performCommand("pclans tradecontract " + targetClan.getName());
                player.closeInventory();
            }
        }
    }

    public void openClanRegionsMenu(Player viewer, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Приваты соклановцев");
        int index = 0;
        FurnaceProtectionManager fpm = FurnaceProtectionManager.getFpm();
        if (fpm == null) {
            viewer.sendMessage(ChatColor.RED + "Не удалось загрузить данные приватов.");
            return;
        }

        // Преобразуем список участников в UUID
        Set<UUID> clanMembers = clan.getMembers().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        // Для каждого региона на сервере проверяем, принадлежит ли он соклановцу
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (ProtectionRegion region : fpm.getRegionsFor(player.getUniqueId())) {
                UUID ownerUUID = region.getOwner();

                // Только если владелец региона — член текущего клана
                if (clanMembers.contains(ownerUUID)) {
                    OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
                    Location loc = region.getCenter();

                    ItemStack furnace = new ItemStack(Material.BLAST_FURNACE);
                    ItemMeta meta = furnace.getItemMeta();
                    meta.setDisplayName("§eРегион - " + owner.getName());
                    meta.setLore(Arrays.asList(
                            "§7X: " + loc.getBlockX(),
                            "§7Y: " + loc.getBlockY(),
                            "§7Z: " + loc.getBlockZ()
                    ));

                    furnace.setItemMeta(meta);
                    inv.setItem(index++, furnace);

                    if (index >= 54) break; // инвентарь заполнен
                }
            }

            inv.setItem(53, getBackButton());

            viewer.openInventory(inv);
        }
    }

    public void openSettingsMenu(Player player, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Настройки клана");

        // PvP toggle
        ItemStack pvpToggle = new ItemStack(
                clanPvpEnabled.getOrDefault(clan.getName(), true) ? Material.LEVER : Material.REDSTONE_TORCH);
        ItemMeta pvpMeta = pvpToggle.getItemMeta();
        pvpMeta.setDisplayName(clanPvpEnabled.getOrDefault(clan.getName(), true) ? "§cОтключить PvP в клане" : "§aВключить PvP в клане");
        pvpMeta.setLore(clanPvpEnabled.getOrDefault(clan.getName(), true) ?
                Collections.singletonList("§7Запретить дружеский огонь в клане")
                : Collections.singletonList("§7Включить дружеский огонь в клане"));

        pvpToggle.setItemMeta(pvpMeta);
        inv.setItem(11, pvpToggle);

        // Prefix color
        ChatColor currentColor = clanPrefixColor.getOrDefault(clan.getName(), ChatColor.AQUA);
        Material dyeMaterial = switch (currentColor) {
            case RED -> Material.RED_DYE;
            case YELLOW -> Material.YELLOW_DYE;
            case BLUE -> Material.BLUE_DYE;
            case GREEN -> Material.GREEN_DYE;
            case LIGHT_PURPLE -> Material.MAGENTA_DYE;
            case AQUA -> Material.CYAN_DYE;
            default -> Material.GRAY_DYE;
        };
        ItemStack prefixColor = new ItemStack(dyeMaterial);
        ItemMeta colorMeta = prefixColor.getItemMeta();
        colorMeta.setDisplayName("§bЦвет префикса: " + currentColor.name());
        prefixColor.setItemMeta(colorMeta);
        inv.setItem(13, prefixColor);

        // Rename
        ItemStack rename = new ItemStack(Material.OAK_SIGN);
        ItemMeta renameMeta = rename.getItemMeta();
        renameMeta.setDisplayName("§eИзменить название клана");
        renameMeta.setLore(Collections.singletonList("§7Нажмите, чтобы изменить имя клана"));
        rename.setItemMeta(renameMeta);
        inv.setItem(15, rename);

        // Украшения
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onPrivatesClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();

        // Проверяем, открыто ли нужное меню
        if (inv != null && event.getView().getTitle() != null && (event.getView().getTitle().equals("§6Приваты соклановцев") || event.getView().getTitle().equals("§8Участники клана"))) {
            if (event.getCurrentItem() == null) {
                event.setCancelled(true);
                return;
            }
            if (event.getCurrentItem().getItemMeta() == null) {
                event.setCancelled(true);
                return;
            }
            if (event.getCurrentItem().getItemMeta().getDisplayName() == null) {
                event.setCancelled(true);
                return;
            }

            ItemStack item = event.getCurrentItem();

            String clickedName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            String backName = ChatColor.stripColor(getBackButton().getItemMeta().getDisplayName());

            if (clickedName.equalsIgnoreCase(backName)) {
                if (event.getWhoClicked() instanceof Player player) {
                    event.setCancelled(true);
                    player.closeInventory();
                    player.performCommand("pclans menu");
                }
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.equals("§8Настройки клана")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String clanName = plugin.getPlayerClan().get(player.getUniqueId());
        Clan clan = plugin.getClans().get(clanName);
        if (clan == null) return;

        Material type = clicked.getType();
        if (type == Material.LEVER || type == Material.REDSTONE_TORCH) {
            boolean current = clanPvpEnabled.getOrDefault(clan.getName(), true);
            clanPvpEnabled.put(clan.getName(), !current);
            player.sendMessage("§aPvP между участниками клана теперь " + (!current ? "§aвключено" : "§cзапрещено"));
            openSettingsMenu(player, clan);
        } else if (type.name().endsWith("_DYE")) {
            ChatColor current = clanPrefixColor.getOrDefault(clan.getName(), ChatColor.AQUA);
            int nextIndex = (prefixColors.indexOf(current) + 1) % prefixColors.size();
            ChatColor nextColor = prefixColors.get(nextIndex);
            clanPrefixColor.put(clan.getName(), nextColor);
            player.sendMessage("§aЦвет префикса изменён на " + nextColor + nextColor.name());
            // Обновить всем участникам префикс
            for (String uuid : clan.getMembers()) {
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));
                if (p != null) {
                    updatePlayerNames(p, plugin.getPlayerClan(), plugin.getClanMenu(), plugin.getMainBoard());
                }
            }
            updatePlayerNames(player, plugin.getPlayerClan(), plugin.getClanMenu(), plugin.getMainBoard());

            openSettingsMenu(player, clan);
        } else if (type == Material.OAK_SIGN) {
            if (!clan.getOwner().equals(player.getUniqueId())) {
                player.sendMessage("§cТолько владелец может изменить название клана.");
                return;
            }
            renameWaiting.put(player.getUniqueId(), true);
            player.closeInventory();
            player.sendMessage("§eВведите новое название клана в чат.");
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (!renameWaiting.containsKey(uuid)) return;

        e.setCancelled(true);
        Player player = e.getPlayer();
        String newName = e.getMessage().trim();

        if (plugin.getClans().containsKey(newName.toLowerCase())) {
            player.sendMessage("§cКлан с таким именем уже существует.");
            renameWaiting.remove(uuid);
            return;
        }

        String oldName = plugin.getPlayerClan().get(uuid);
        Clan oldClan = plugin.getClans().remove(oldName.toLowerCase());
        oldClan.setName(newName, player);
        player.sendMessage("§aИмя клана успешно изменено на §e" + newName);
        renameWaiting.remove(uuid);

        for (String memberUUID : oldClan.getMembers()) {
            UUID memberId = UUID.fromString(memberUUID);
            plugin.getPlayerClan().put(memberId, newName.toLowerCase());
            Player p = Bukkit.getPlayer(memberId);
            if (p != null) {
                updatePlayerNames(p, plugin.getPlayerClan(), plugin.getClanMenu(), plugin.getMainBoard());
            }
        }
    }

    public boolean isClanPvpEnabled(String clanName) {
        return clanPvpEnabled.getOrDefault(clanName, true);
    }

    public ChatColor getClanColor(String clanName) {
        return clanPrefixColor.getOrDefault(clanName, ChatColor.AQUA);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) return;

        String clanVictim = plugin.getPlayerClan().get(victim.getUniqueId());
        String clanAttacker = plugin.getPlayerClan().get(attacker.getUniqueId());

        if (clanVictim != null && clanVictim.equals(clanAttacker)) {
            if (!plugin.getClanMenu().isClanPvpEnabled(clanVictim)) {
                event.setCancelled(true);
                attacker.sendMessage("§cPvP между участниками клана отключено.");
            }
        }
    }

    private ItemStack getBackButton() {
        return getItemStack(
                Material.PRISMARINE_SHARD,
                "Назад",
                ChatColor.GRAY,
                "Вернуться в главное меню",
                ChatColor.DARK_GRAY
        );
    }
}

