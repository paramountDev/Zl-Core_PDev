package dev.paramountdev.zlomCore_PDev.occupation;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.FurnaceProtectionManager;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.ProtectionRegion;
import dev.paramountdev.zlomCore_PDev.occupation.playerWithoutClan.PendingOutcome;
import dev.paramountdev.zlomCore_PDev.occupation.playerWithoutClan.SiegeScheduler;
import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ClaimCommand implements CommandExecutor {

    private static HashMap<Player, ProtectionRegion> claimedRegions = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            return false;
        }
        Location location = player.getLocation();

        FurnaceProtectionManager fpm = FurnaceProtectionManager.getFpm();
        ProtectionRegion region = fpm.getRegionAtLocation(location);

        if (region != null) {
           Player regionOwner = Bukkit.getPlayer(region.getOwner());
           if (regionOwner == null) {
               player.sendMessage("§cУ этого региона нет владельца!");
               return true;
           }

            Clan playerClan = ZlomCore_PDev.getInstance().getClanByPlayer(player.getUniqueId());
            Clan regionOwnerClan = ZlomCore_PDev.getInstance().getClanByPlayer(regionOwner.getUniqueId());

            if (playerClan == null) {
                player.sendMessage("§cВы не состоите в клане.");
                return true;
            }
            if(regionOwnerClan != null) {

                String regionOwnerNameClan = regionOwnerClan.getName();

                player.performCommand("pclans wardeclare " + regionOwnerNameClan);

                claimedRegions.put(player, region);

                player.sendMessage("");
                player.sendMessage(ChatColor.RED + "Вы заявили права на регион игрока: " + regionOwner.getName() + "!");
                player.sendMessage("");
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1, 1);

                regionOwner.sendMessage("");
                regionOwner.sendMessage(ChatColor.RED + "Игрок " + player.getName() + " заявил права на ваш регион!");
                regionOwner.sendMessage("");
                regionOwner.playSound(regionOwner.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1, 1);


                return true;

            } else {

                player.sendMessage(ChatColor.RED + "Владелец не состоит в клане! Осада будет доступна через 24 часа.");
                region.startPrivateSiegeCountdown();
                SiegeScheduler.scheduleSiege(player.getUniqueId(), new PendingOutcome(region));


                if (regionOwner != null) {
                    regionOwner.sendMessage(ChatColor.RED + "Ваш регион стал мишенью! Осада начнется через 24 часа.");
                }

            }


        } else {
            player.sendMessage(ChatColor.RED + "Вы не находитесь в регионе.");
        }
        return true;
    }

    public static HashMap<Player, ProtectionRegion> getClaimedRegions() {
        return claimedRegions;
    }

    public static Player getRegionOwner(ProtectionRegion region) {
        Player owner = Bukkit.getPlayer(region.getOwner());
        if(owner == null) {
            owner = Bukkit.getOfflinePlayer(region.getOwner()).getPlayer();
        }
        return owner;
    }

}
