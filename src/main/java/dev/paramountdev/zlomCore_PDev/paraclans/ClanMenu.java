package dev.paramountdev.zlomCore_PDev.paraclans;


import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.FurnaceProtectionManager;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.ProtectionRegion;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.BlastFurnace;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.createCastleHead;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.createPlusHead;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.createFiller;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.getItemStack;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.getMessage;
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


    // NEW METHODS

    //

    // СОЗДАТЬ И ПРИСОЕДИНИТЬСЯ К КЛАНУ

    public void openCreateMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GRAY + "Меню клана");

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        ItemStack joinList = getItemStack(Material.PAPER,
                "Подключится к клану",
                ChatColor.GOLD,
                "Нажмите что бы ввести имя интересующего клана.",
                ChatColor.DARK_GREEN);

        inv.setItem(21, joinList);


        ItemStack addClanHead = createPlusHead(ChatColor.GREEN + "Создать клан", Arrays.asList(
                ChatColor.GOLD + "Нажмите что бы создать клан.",
                ChatColor.GOLD + "Текущая стоимость создания клана - " + ChatColor.GREEN + plugin.getConfig().getDouble("create-price", 0.0)));
        inv.setItem(22, addClanHead);

        player.openInventory(inv);
    }

    @EventHandler
    public void onPlayerClickAtCreateMenu(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getView().getTitle().equalsIgnoreCase(ChatColor.DARK_GRAY + "Меню клана")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) {
                return;
            }
            Player player = (Player) event.getWhoClicked();
            Clan clan = plugin.getClans().get(plugin.getPlayerClan().get(player.getUniqueId()));
            if (clan != null) {
                player.closeInventory();
                player.sendMessage(getMessage("already-in-clan"));
                return;
            }
            playClickSound(player);
            switch (clicked.getType()) {

                case PAPER:
                    openJoinAnvilGUI(player);
                    break;

                case PLAYER_HEAD:
                    openCreateClanMenu(player);
                    break;

            }
        }
    }

    public void openJoinAnvilGUI(Player player) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Введите имя клана:")
                .text("")
                .itemLeft(createCastleHead(ChatColor.GOLD + "Имя клана", List.of(ChatColor.GRAY + "Введите имя клана к которому хотите присоединиться")))
                .text(ChatColor.GOLD + "Имя клана")
                .onClick((slot, stateSnapshot) -> {
                    if (slot == AnvilGUI.Slot.OUTPUT) {
                        String input = stateSnapshot.getText();

                        if (input == null || input.trim().isEmpty()) {
                            player.sendMessage("§cВы не ввели имя клана.");
                            return AnvilGUI.Response.close();
                        }

                        if (input.equalsIgnoreCase(ChatColor.GOLD + "Имя клана")) {
                            player.sendMessage("§cВы не ввели имя клана.");
                            return AnvilGUI.Response.close();
                        }
                        if (input.contains(" ")) {
                            player.sendMessage("§cВы не ввели имя клана.");
                            return AnvilGUI.Response.close();
                        }
                        if (input.startsWith(ChatColor.GOLD + "")) {
                            player.sendMessage("§cВы не ввели имя клана.");
                            return AnvilGUI.Response.close();
                        }

                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.addPlayerToClan(player, input);
                        });

                        return AnvilGUI.Response.close();
                    }

                    if (slot == AnvilGUI.Slot.INPUT_LEFT) {
                        // Отмена — закрываем
                        return AnvilGUI.Response.close();
                    }

                    return AnvilGUI.Response.close();
                })
                .open(player);
    }

    public void openCreateClanMenu(Player player) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Имя нового клана:")
                .text("")
                .itemLeft(createCastleHead(ChatColor.GOLD + "Имя клана", List.of(ChatColor.GRAY + "Введите имя клана который хотите создать.")))
                .text(ChatColor.GOLD + "Имя клана")
                .onClick((slot, stateSnapshot) -> {
                    if (slot == AnvilGUI.Slot.OUTPUT) {
                        String input = stateSnapshot.getText();

                        if (input == null || input.trim().isEmpty()) {
                            player.sendMessage("§cВы не ввели имя клана.");
                            return AnvilGUI.Response.close();
                        }

                        if (input.equalsIgnoreCase(ChatColor.GOLD + "Имя клана")) {
                            player.sendMessage("§cВы не ввели имя клана.");
                            return AnvilGUI.Response.close();
                        }

                        if (input.contains(" ")) {
                            player.sendMessage("§cВы не ввели имя клана.");
                            return AnvilGUI.Response.close();
                        }
                        if (input.startsWith(ChatColor.GOLD + "")) {
                            player.sendMessage("§cВы не ввели имя клана.");
                            return AnvilGUI.Response.close();
                        }

                        // Выполнить команду
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.createClan(player, input);
                        });

                        return AnvilGUI.Response.close();
                    }

                    if (slot == AnvilGUI.Slot.INPUT_LEFT) {
                        // Отмена — закрываем
                        return AnvilGUI.Response.close();
                    }

                    return AnvilGUI.Response.close();
                })
                .open(player);
    }

    // СОЗДАТЬ И ПРИСОЕДИНИТЬСЯ К КЛАНУ

    //

    // ГЛАВНОЕ МЕНЮ КЛАНА

    public void openMainClanMenu(Player player, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 45, ChatColor.DARK_GRAY + "Меню клана: " + clan.getName());

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        ItemStack members = getItemStack(Material.FIELD_MASONED_BANNER_PATTERN, "Участники клана",
                ChatColor.GOLD, "Посмотреть список участников клана.", ChatColor.DARK_PURPLE);
        inv.setItem(0, members);

        ItemStack invite = createPlusHead(ChatColor.DARK_GREEN + "Пригласить игрока",
                List.of(ChatColor.GREEN + "Нажмите, что бы пригласить своих друзей вступить в клан!"));
        inv.setItem(1, invite);

        ItemStack privates = createCastleHead(ChatColor.GOLD + "Приваты клана",
                List.of(ChatColor.DARK_RED + "Посмотреть текущие и активные приваты клана."));
        inv.setItem(9, privates);

        ItemStack occupation = getItemStack(Material.PODZOL, "Битвы",
                ChatColor.DARK_GRAY, "Нажмите, что бы посмотреть текущие захваченные територии вражеских кланов.", ChatColor.GRAY);
        inv.setItem(18, occupation);

        ItemStack lvl = getItemStack(Material.EXPERIENCE_BOTTLE, "Уровень клана",
                ChatColor.AQUA, "Текущий уровень клана: УРОВЕНЬ", ChatColor.GREEN);
        inv.setItem(27, lvl);

        ItemStack top = getItemStack(Material.GOLD_BLOCK, "Топ кланов",
                ChatColor.YELLOW, "Самые популярные и топовые кланы на данный момент.", ChatColor.BLUE);
        inv.setItem(3, top);

        ItemStack allies = getItemStack(Material.BELL, "Союзные кланы",
                ChatColor.DARK_GREEN, "Нажмите, что бы увидеть и настроить союзы с другими кланами.", ChatColor.GREEN);
        inv.setItem(5, allies);

        ItemStack trades = getItemStack(Material.BLUE_BUNDLE, "Торговые соглашения",
                ChatColor.DARK_AQUA, "Торговые контракты с различными кланами.", ChatColor.AQUA);
        inv.setItem(6, trades);

        ItemStack wars = getItemStack(Material.GOAT_HORN, "Войны",
                ChatColor.DARK_RED, "Нажмите, что бы посмотреть с кем воюет клан.", ChatColor.RED);
        inv.setItem(7, wars);

        ItemStack pvp;
        if (!clanPvpEnabled.getOrDefault(clan.getName(), true)) {
            pvp = getItemStack(Material.WOODEN_SWORD, "PVP - выключено",
                    ChatColor.DARK_GREEN, "Драки - плохо. Поэтому вы не можете бить сокланоцвев", ChatColor.DARK_RED);
        } else {
            pvp = getItemStack(Material.NETHERITE_SWORD, "PVP - включено",
                    ChatColor.DARK_RED, "Вы можете нанести вред своим союзникам. Будьте осторожны.", ChatColor.DARK_GREEN);
        }
        inv.setItem(26, pvp);

        ItemStack leave = getItemStack(Material.IRON_DOOR, "Покинуть клан",
                ChatColor.DARK_RED, "Если вы нажмете эту кнопку - вы покините этот клан", ChatColor.RED);
        inv.setItem(44, leave);

        player.openInventory(inv);
    }

    @EventHandler
    public void onMainMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.startsWith("Меню клана:")) {
            return;
        }
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Clan clan = plugin.getClans().get(plugin.getPlayerClan().get(player.getUniqueId()));
        if (clan == null) return;

        playClickSound(player);
        switch (clicked.getType()) {

            case FIELD_MASONED_BANNER_PATTERN:
                openClanMembersMenu(player, clan);
                break;

            case PLAYER_HEAD:
                if (clicked.getItemMeta().getDisplayName().contains("Пригласить игрока")) {
                    player.sendMessage("Вы нажали на Пригласить друга, эта функция будет доступна в следующем обновлении");
                }
                if (clicked.getItemMeta().getDisplayName().contains("Приваты клана")) {
                    openRegionsMenu(player, clan);
                }
                break;

            case PODZOL:
                player.sendMessage("Вы нажали на Оккупация, эта функция будет доступна в следующем обновлении");
                break;

            case EXPERIENCE_BOTTLE:

                break;

            case GOLD_BLOCK:
                player.sendMessage("Вы нажали на Топ кланы, эта функция будет доступна в следующем обновлении");
                break;

            case BELL:
                player.sendMessage("Вы нажали на Союзные кланы, эта функция будет доступна в следующем обновлении");
                break;

            case BLUE_BUNDLE:
                openClanTradeContracts(player, clan);
                break;

            case GOAT_HORN:
                openClanWars(player, clan);
                break;

            case IRON_DOOR:
                player.closeInventory();
                player.performCommand("pclans leave");
                break;
        }
    }

    // ГЛАВНОЕ МЕНЮ КЛАНА

    //

    // УЧАСТНИКИ

    public void openClanMembersMenu(Player viewer, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Участники клана");

        int index = 0;
        boolean isOwner = clan.getOwner().equals(viewer.getUniqueId());

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

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
    public void onMembersMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!title.equalsIgnoreCase("Участники клана")) {
            return;
        }
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        Clan clan = plugin.getClans().get(plugin.getPlayerClan().get(player.getUniqueId()));
        if (clan == null) return;

        playClickSound(player);
        switch (clicked.getType()) {
            case PLAYER_HEAD:
                if (!clan.getOwner().equals(player.getUniqueId())) return;

                SkullMeta meta = (SkullMeta) clicked.getItemMeta();
                if (meta == null || meta.getOwningPlayer() == null) return;

                OfflinePlayer target = meta.getOwningPlayer();
                UUID targetUuid = target.getUniqueId();
                if (targetUuid.equals(player.getUniqueId())) return;

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
        }
    }

    // УЧАСТНИКИ

    //

    // ПРИВАТЫ

    public void openRegionsMenu(Player viewer, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 54, "§6Приваты клана");
        int index = 0;
        FurnaceProtectionManager fpm = FurnaceProtectionManager.getFpm();
        if (fpm == null) {
            viewer.sendMessage(ChatColor.RED + "Не удалось загрузить данные приватов.");
            return;
        }

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        Set<UUID> clanMembers = clan.getMembers().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());

        List<ProtectionRegion> allRegions = fpm.getRegionsForClanMembersIncludingArchived(clanMembers);

        for (ProtectionRegion region : allRegions) {
            UUID ownerUUID = region.getOwner();

            // Только если владелец региона — член текущего клана
            if (!clanMembers.contains(ownerUUID)) continue;

            OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUUID);
            Location loc = region.getCenter();
            String status = getFurnaceStatus(loc); // "АКТИВЕН" / "НЕАКТИВЕН" / null

            if (status == null) continue; // печка была сломана — не отображаем

            ItemStack furnace = new ItemStack(Material.BLAST_FURNACE);
            ItemMeta meta = furnace.getItemMeta();
            meta.setDisplayName("§eРегион - " + owner.getName());
            meta.setLore(Arrays.asList(
                    "§7X: " + loc.getBlockX(),
                    "§7Y: " + loc.getBlockY(),
                    "§7Z: " + loc.getBlockZ(),
                    "",
                    (status.equals("АКТИВЕН") ? "§aСТАТУС: АКТИВЕН" : "§cСТАТУС: НЕАКТИВЕН")
            ));
            furnace.setItemMeta(meta);
            inv.setItem(index++, furnace);

            if (index >= 53) break;
        }
        inv.setItem(53, getBackButton());
        viewer.openInventory(inv);


    }

    // ПРИВАТЫ

    //

    // ВОЙНЫ

    public void openClanWars(Player player, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 54, "Войны клана: " + clan.getName());

        List<Clan> wars = clan.getWars();

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        for (int i = 0; i < wars.size() && i < 54; i++) {
            Clan enemyClan = wars.get(i);

            ItemStack horn = new ItemStack(Material.GOAT_HORN);
            ItemMeta meta = horn.getItemMeta();

            meta.setDisplayName("§cКлан §4" + enemyClan.getName());
            meta.setLore(List.of(
                    "",
                    "§7Вы находитесь в войне с этим кланом.",
                    "§eНажмите §cПКМ§e, чтобы заключить перемирие.",
                    ""
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            horn.setItemMeta(meta);

            inv.setItem(i, horn);
        }
        inv.setItem(53, getBackButton());

        player.openInventory(inv);
    }

    @EventHandler
    public void onWarClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Войны клана: ") && event.getWhoClicked() instanceof Player player) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.GOAT_HORN) return;

            if (event.isRightClick()) {
                ItemStack item = event.getCurrentItem();
                if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) return;

                String displayName = item.getItemMeta().getDisplayName();
                String enemyClanName = displayName.replace("§cКлан §4", "").trim();

                playClickSound(player);
                player.closeInventory();
                player.performCommand("pclans warend " + enemyClanName);

                // Перезапустить меню через небольшую задержку
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Clan playerClan = plugin.getClans().get(plugin.getPlayerClan().get(player.getUniqueId()));
                        if (playerClan != null) {
                            openClanWars(player, playerClan);
                        }
                    }
                }.runTaskLater(plugin, 10L);
            }
        }
    }

    // ВОЙНЫ

    //

    // ТОРГОВЫЕ КОНТРАКТЫ

    public void openClanTradeContracts(Player player, Clan clan) {
        Inventory inv = Bukkit.createInventory(null, 54, "Торговые контракты клана: " + clan.getName());

        List<Clan> tradecontrats = clan.getTradecontrats();

        ItemStack filler = createFiller();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        for (int i = 0; i < tradecontrats.size() && i < 54; i++) {
            Clan enemyClan = tradecontrats.get(i);

            ItemStack sunflower = new ItemStack(Material.SUNFLOWER);
            ItemMeta meta = sunflower.getItemMeta();

            meta.setDisplayName("§3Клан §b" + enemyClan.getName());
            meta.setLore(List.of(
                    "",
                    "§7У вас есть торговый контракт с этим кланом",
                    "§eНажмите §6ПКМ§e, чтобы разорвать торговые отношения.",
                    ""
            ));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            sunflower.setItemMeta(meta);

            inv.setItem(i, sunflower);
        }
        inv.setItem(53, getBackButton());

        player.openInventory(inv);
    }

    @EventHandler
    public void onTradeContractClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Торговые контракты клана: ") && event.getWhoClicked() instanceof Player player) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() != Material.SUNFLOWER) return;

            if (event.isRightClick()) {
                ItemStack item = event.getCurrentItem();
                if (item.getItemMeta() == null || !item.getItemMeta().hasDisplayName()) return;

                String displayName = item.getItemMeta().getDisplayName();
                String enemyClanName = displayName.replace("§3Клан §b", "").trim();

                playClickSound(player);
                player.closeInventory();
                player.performCommand("pclans tradecontractend " + enemyClanName);

                // Перезапустить меню через небольшую задержку
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Clan playerClan = plugin.getClans().get(plugin.getPlayerClan().get(player.getUniqueId()));
                        if (playerClan != null) {
                            openClanWars(player, playerClan);
                        }
                    }
                }.runTaskLater(plugin, 10L);
            }
        }
    }

    // ТОРГОВЫЕ КОНТРАКТЫ

    //

    // МЕНЮ НАСТРОЕК

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
    public void onSettingsClick(InventoryClickEvent event) {
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
        playClickSound(player);
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
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1f);
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

    // МЕНЮ НАСТРОЕК

    //

    //

    @EventHandler
    public void onBackButtonClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();

        // Проверяем, открыто ли нужное меню
        if (inv != null && event.getView().getTitle() != null
                && (event.getView().getTitle().equals("§6Приваты клана")
                || event.getView().getTitle().equals("§8Участники клана"))
                || event.getView().getTitle().startsWith("Войны клана: ")
                || event.getView().getTitle().startsWith("Торговые контракты клана: ")) {
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
                    playClickSound(player);
                    player.performCommand("pclans menu");
                }
            }
            event.setCancelled(true);
        }
    }


    // NEW METHODS


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

    public void onDiplomatyClick(InventoryClickEvent event) {
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




    public String getFurnaceStatus(Location loc) {
        Block block = loc.getBlock();
        if (block == null || block.getType() != Material.BLAST_FURNACE) {
            return null; // Печка была сломана — регион не отображаем
        }

        BlockState state = block.getState();
        if (!(state instanceof BlastFurnace furnace)) {
            return null;
        }

        FurnaceInventory inv = furnace.getInventory();
        ItemStack input = inv.getSmelting();
        ItemStack fuel = inv.getFuel();

        // если есть и предмет, и топливо
        if (input != null && input.getType() != Material.AIR
                && fuel != null && fuel.getType() == Material.COAL) {
            return "АКТИВЕН";
        }

        return "НЕАКТИВЕН"; // топливо или предмета нет — печка на месте, но не работает
    }

    public boolean isClanPvpEnabled(String clanName) {
        return clanPvpEnabled.getOrDefault(clanName, true);
    }

    public ChatColor getClanColor(String clanName) {
        return clanPrefixColor.getOrDefault(clanName, ChatColor.AQUA);
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

    private void playClickSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 1, 1);
    }


}

