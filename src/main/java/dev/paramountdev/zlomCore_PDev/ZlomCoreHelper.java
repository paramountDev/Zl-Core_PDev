package dev.paramountdev.zlomCore_PDev;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ZlomCoreHelper {

    private static FileConfiguration config;

    public ZlomCoreHelper(ZlomCore_PDev plugin) {
        config = plugin.getConfig();
    }

    public static String getMessage(String path, Map<String, String> placeholders) {
        String message = config.getString("messages." + path, "&c[Ошибка сообщения: " + path + "]");
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message.replace("&", "§");
    }

    public static String getMessage(String path) {
        return getMessage(path, Collections.emptyMap());
    }

    public static ItemStack getItemStack(Material material, String displayNameText, ChatColor displayNameColor,
                                         String loreText, ChatColor loreColor) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(displayNameColor + displayNameText);
        meta.setLore(Collections.singletonList(loreColor + loreText));
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ItemStack createPlusHead(String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19"));
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack createMinusHead(String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0="));
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack createCastleHead(String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
        profile.getProperties().add(new ProfileProperty("textures",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmVlZjdlNTZjZGU3NDA3NzJkZmI3NmRkZDJmNTg0YmU4OTA3Yjg1OTc2NjhlNDAyNjM0OTg2NDY5MjMwYWE0OSJ9fX0="));
        meta.setPlayerProfile(profile);
        skull.setItemMeta(meta);
        return skull;
    }

    public static ItemStack getBackButton() {
        return getItemStack(
                Material.PRISMARINE_SHARD,
                "Назад",
                ChatColor.GRAY,
                "Вернуться в главное меню",
                ChatColor.DARK_GRAY
        );
    }


    public static ItemStack createFiller() {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        return filler;
    }

    public static void fillInventory(Inventory inventory, int n) {
        ItemStack filler = createFiller();
        while(n < inventory.getSize()) {
            inventory.setItem(n, filler);
            n++;
        }
    }

    public static void updatePlayerNames(Player player,
                                         Map<UUID, String> playerClan,
                                         ClanMenu clanMenu,
                                         Scoreboard mainBoard) {
        updatePlayerChatPrefix(player, playerClan, clanMenu, mainBoard);
        updatePlayerNametag(player, playerClan, clanMenu, mainBoard);
    }

    public static void updatePlayerChatPrefix(Player player,
                                              Map<UUID, String> playerClan,
                                              ClanMenu clanMenu,
                                              Scoreboard mainBoard
    ) {
        String clanName = playerClan.get(player.getUniqueId());
        String playerName;
        if (clanName != null) {
            ChatColor color = clanMenu.getClanColor(clanName);
            String prefix = "§7[" + color + clanName + "§7] §r";
            playerName = prefix + player.getName();
        } else {
            playerName = player.getName();
        }
        player.setPlayerListName(playerName);
        player.setDisplayName(playerName);
    }

    public static void updatePlayerNametag(Player player,
                                           Map<UUID, String> playerClan,
                                           ClanMenu clanMenu,
                                           Scoreboard mainBoard
    ) {
        if (mainBoard == null) return;

        String clanName = playerClan.get(player.getUniqueId());
        if (clanName != null) {
            Team team = mainBoard.getTeam(clanName);
            if (team == null) {
                team = mainBoard.registerNewTeam(clanName);
            }

            ChatColor color = clanMenu.getClanColor(clanName);
            String prefix = "§7[" + color + clanName + "§7] ";

            if (prefix.length() > 16) {
                prefix = color + clanName + " ";
            }

            team.setPrefix(prefix);
            team.setSuffix("");

            if (!team.hasEntry(player.getName())) {
                team.addEntry(player.getName());
            }
        } else {
            for (Team team : mainBoard.getTeams()) {
                if (team.hasEntry(player.getName())) {
                    team.removeEntry(player.getName());
                }
            }
        }
        player.setScoreboard(mainBoard);
    }

    public static boolean checkClanPermission(
            Player player,
            String permissionKey,
            Map<UUID, String> playerClan,
            Map<String, Clan> clans,
            ClanRoleManager roleManager
    ) {
        UUID uuid = player.getUniqueId();
        String clanName = playerClan.get(uuid);

        if (clanName == null || !clans.containsKey(clanName)) {
            player.sendMessage("§cВы не состоите в клане.");
            return false;
        }

        Clan clan = clans.get(clanName);

        if (clan.getOwner().equals(uuid)) {
            return true;
        }

        if (!roleManager.hasClanPermission(clanName, uuid, permissionKey)) {
            player.sendMessage("§cУ вас нет прав на выполнение этой команды.");
            return false;
        }
        return true;
    }

    public static boolean isInJail(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        ZlomCore_PDev plugin = ZlomCore_PDev.getInstance();

        int xMin = plugin.getConfig().getInt("jail.xMin");
        int xMax = plugin.getConfig().getInt("jail.xMax");
        int zMin = plugin.getConfig().getInt("jail.zMin");
        int zMax = plugin.getConfig().getInt("jail.zMax");

        return x >= xMin && x <= xMax && z >= zMin && z <= zMax;
    }

    public static String formatMessage(String key, Player player, String message) {
        String format = ZlomCore_PDev.getInstance().getConfig().getString("chat.formats." + key);
        if (format == null) return message;

        return ChatColor.translateAlternateColorCodes('&',
                format.replace("{player}", player.getDisplayName()).replace("{message}", message)
        );
    }

    public static int getChatRadius(String key) {
        return ZlomCore_PDev.getInstance().getConfig().getInt("chat.radius." + key, 100);
    }


    public static void sendAuthorMessage(Player player, String pluginName) {
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GREEN + "=====[ " + ChatColor.GOLD + "Разработчик " + pluginName + ChatColor.DARK_GREEN + " ]=====");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Автор: " + ChatColor.GREEN + "ParamountDev");
        player.sendMessage("");

        // FunPay
        TextComponent funpayPrefix = new TextComponent("• ");
        funpayPrefix.setColor(net.md_5.bungee.api.ChatColor.GOLD);

        TextComponent funpayLink = new TextComponent("FunPay профиль");
        funpayLink.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        funpayLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://funpay.com/uk/users/14397429/"));
        funpayLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Открыть профиль FunPay").create()));

        funpayPrefix.addExtra(funpayLink);
        player.spigot().sendMessage(funpayPrefix);

        // Telegram
        TextComponent tgPrefix = new TextComponent("• ");
        tgPrefix.setColor(net.md_5.bungee.api.ChatColor.GOLD);

        TextComponent tgLink = new TextComponent("Telegram: @paramount1_dev");
        tgLink.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        tgLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/paramount1_dev"));
        tgLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Открыть Telegram").create()));

        tgPrefix.addExtra(tgLink);
        player.spigot().sendMessage(tgPrefix);
        player.sendMessage("");

        player.sendMessage(ChatColor.DARK_GREEN + "===============================");
        player.sendMessage("");
        player.sendMessage("");
    }
}
