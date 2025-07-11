package dev.paramountdev.zlomCore_PDev.furnaceprivates;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RegionUtils {

    public static int countPlayers(ProtectionRegion region, UUID attackerId, boolean attackers) {
        int count = 0;

        Clan attackerClan = ZlomCore_PDev.getInstance().getClanByPlayer(attackerId);
        Clan ownerClan = ZlomCore_PDev.getInstance().getClanByPlayer(region.getOwner());

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!region.isInside(player.getLocation())) continue;

            Clan playerClan = ZlomCore_PDev.getInstance().getClanByPlayer(player.getUniqueId());
            if (playerClan == null) continue;

            if (attackers && playerClan.equals(attackerClan)) {
                count++;
            } else if (!attackers && playerClan.equals(ownerClan)) {
                count++;
            }
        }

        return count;
    }
}
