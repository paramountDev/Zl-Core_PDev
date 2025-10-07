package dev.paramountdev.zlomCore_PDev.LOL.occupation.paraclans.allies;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.LOL.occupation.paraclans.Clan;
import dev.paramountdev.zlomCore_PDev.LOL.occupation.paraclans.ClanManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /pclans allies <accept|deny> <клан>");
            return true;
        }

        ClanManager clanManager = ZlomCore_PDev.getInstance().getClanManager();
        String action = args[0].toLowerCase();
        String targetClanName = args[1];
        Clan playerClan = ZlomCore_PDev.getInstance().getClanByPlayer(player.getUniqueId());

        if (playerClan == null) {
            player.sendMessage("§cВы не состоите в клане.");
            return true;
        }

        if (!playerClan.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("§cТолько владелец клана может принимать или отклонять союзы.");
            return true;
        }

        if (action.equals("accept")) {
            if (!clanManager.hasRequest(targetClanName, playerClan.getName())) {
                player.sendMessage("§cНет запроса на союз от " + targetClanName);
                return true;
            }
            clanManager.acceptAllyRequest(playerClan.getName(), targetClanName);
        } else if (action.equals("deny")) {
            if (!clanManager.hasRequest(targetClanName, playerClan.getName())) {
                player.sendMessage("§cНет запроса на союз от " + targetClanName);
                return true;
            }
            clanManager.denyAllyRequest(playerClan.getName(), targetClanName);
        } else {
            player.sendMessage("§cНеизвестная подкоманда: " + action);
        }

        return true;
    }

}
