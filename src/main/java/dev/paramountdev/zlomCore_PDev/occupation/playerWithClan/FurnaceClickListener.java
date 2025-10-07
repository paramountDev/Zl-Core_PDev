package dev.paramountdev.zlomCore_PDev.occupation.playerWithClan;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.ProtectionRegion;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.RegionUtils;
import dev.paramountdev.zlomCore_PDev.occupation.ClaimCommand;
import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FurnaceClickListener implements Listener {



    public final Map<Location, OccupationData> activeOccupations = new HashMap<>();
    private final FileConfiguration config;

    public FurnaceClickListener() {
        this.config = ZlomCore_PDev.getInstance().getConfig();
    }

    @EventHandler
    public void onFurnaceClick(PlayerInteractEvent e) {
        if (!config.getBoolean("occupation.enabled", true)) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        Block clicked = e.getClickedBlock();
        BlockState state = clicked.getState();
        if (!(state instanceof Furnace)) return;

        Player player = e.getPlayer();
        Location furnaceLocation = clicked.getLocation();
        ProtectionRegion region = ZlomCore_PDev.getInstance().getManager().getRegionAtLocation(furnaceLocation);

        if (region == null) {
            player.sendMessage(ChatColor.RED + "Это место не принадлежит ни одному региону!");
            return;
        }

        if (player.getUniqueId().equals(region.getOwner())) {
            return;
        }

        if (!ClaimCommand.getClaimedRegions().containsKey(player)) {
            return;
        }

        Clan playerClan = ZlomCore_PDev.getInstance().getClanByPlayer(player.getUniqueId());
        Clan ownerClan = ZlomCore_PDev.getInstance().getClanByPlayer(region.getOwner());

        if (playerClan != null && playerClan.equals(ownerClan)) {
            player.sendMessage(ChatColor.YELLOW + "Этот регион уже принадлежит вашему клану.");
            return;
        }

        if (activeOccupations.containsKey(furnaceLocation)) {
            player.sendMessage(ChatColor.RED + "Оккупация уже идет!");
            return;
        }

        startOccupation(furnaceLocation, player, region);
    }

    private void startOccupation(Location location, Player starter, ProtectionRegion region) {
        int duration = config.getInt("occupation.duration", 30);

        String msg = config.getString("occupation.messages.start", "&6%player% начал оккупацию региона!");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg.replace("%player%", starter.getName())));

        OccupationData data = new OccupationData(starter, region);
        activeOccupations.put(location, data);

        new BukkitRunnable() {
            int secondsLeft = duration;

            @Override
            public void run() {
                if (!data.isValid()) {
                    String cancelMsg = config.getString("occupation.messages.cancelled", "&7Оккупация отменена.");
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', cancelMsg));
                    activeOccupations.remove(location);
                    ClaimCommand.getClaimedRegions().remove(starter);
                    cancel();
                    return;
                }

                if (secondsLeft <= 0) {
                    int attackers = RegionUtils.countPlayers(region, starter.getUniqueId(), true);
                    int defenders = RegionUtils.countPlayers(region, starter.getUniqueId(), false);

                    if (attackers > defenders) {
                        region.setOwner(starter.getUniqueId());
                        String success = config.getString("occupation.messages.success", "&cРегион перешел к %player%!");
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', success.replace("%player%", starter.getName())));
                    } else {
                        String fail = config.getString("occupation.messages.failure", "&eОккупация не удалась. Защитников было больше.");
                        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', fail));
                    }

                    ClaimCommand.getClaimedRegions().remove(starter);
                    activeOccupations.remove(location);
                    cancel();
                }

                secondsLeft--;
            }
        }.runTaskTimer(ZlomCore_PDev.getInstance(), 0, 20);
    }

    public static class OccupationData {
        private final Player starter;
        private final ProtectionRegion region;

        OccupationData(Player starter, ProtectionRegion region) {
            this.starter = starter;
            this.region = region;
        }

        public ProtectionRegion getRegion() {
            return region;
        }

        public boolean isValid() {
            return starter.isOnline();
        }
    }

    public static Map<Location, OccupationData> getActiveOccupations() {
        return ZlomCore_PDev.getInstance().getFurnaceClickListener().activeOccupations;
    }

}
