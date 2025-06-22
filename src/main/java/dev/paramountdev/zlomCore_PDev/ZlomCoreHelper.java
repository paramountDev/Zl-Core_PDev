package dev.paramountdev.zlomCore_PDev;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import dev.paramountdev.zlomCore_PDev.paraclans.ClanMenu;
import dev.paramountdev.zlomCore_PDev.paraclans.ClanRoleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.Arrays;
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


    public static ItemStack createFiller() {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);
        return filler;
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
                prefix = color + clanName;
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


}
