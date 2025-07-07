package dev.paramountdev.zlomCore_PDev.parachats;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.isInJail;


public class ClanChat implements Listener {

    @EventHandler
    public void onClanChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        String message = event.getMessage();

        if (isInJail(sender.getLocation())) {
            return;
        }

        if (!message.startsWith("#")) return;

        ZlomCore_PDev plugin = ZlomCore_PDev.getInstance();
        if (plugin == null) {
            sender.sendMessage(ChatColor.RED + "Клановый плагин недоступен.");
            event.setCancelled(true);
            return;
        }


        String clanName = plugin.getPlayerClan().get(sender.getUniqueId());
        if (clanName == null || !plugin.getClans().containsKey(clanName)) {
            sender.sendMessage(ChatColor.RED + "Вы не состоите в клане.");
            event.setCancelled(true);
            return;
        }

        Clan clan = plugin.getClans().get(clanName);

        String formatTemplate = plugin.getConfig().getString("chat.formats.clan", "&f[&9Clan&f] &r{player}: {message}");
        String formatted = ChatColor.translateAlternateColorCodes('&',
                formatTemplate.replace("{player}", sender.getDisplayName())
                        .replace("{message}", message.substring(1).trim()));

        event.setCancelled(true);


        for (String memberName : clan.getMembers()) {
            Player member = Bukkit.getPlayerExact(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage(formatted);
            }
        }


        Player owner = Bukkit.getPlayer(clan.getOwner());
        if (owner != null && owner.isOnline() && !clan.getMembers().contains(owner.getName())) {
            owner.sendMessage(formatted);
        }
    }

}
