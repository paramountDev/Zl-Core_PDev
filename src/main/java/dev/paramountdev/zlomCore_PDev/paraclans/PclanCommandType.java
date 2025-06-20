package dev.paramountdev.zlomCore_PDev.paraclans;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.checkClanPermission;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.getMessage;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.updatePlayerNames;

public enum PclanCommandType {
    CREATE("create"),
    JOIN("join"),
    ACCEPT("accept"),
    DENY("deny"),
    LEAVE("leave"),
    REMOVE("remove"),
    REQUESTS("requests"),
    MY("my"),
    WARDECLARE("wardeclare"),
    WAREND("warend"),
    TRADECONTRACT("tradecontract"),
    TRADECONTRACTEND("tradecontractend"),
    MENU("menu"),
    SETTINGS("settings");

    private final String name;

    PclanCommandType(String name) {
        this.name = name;
    }

    public static void proccessCommand(
            CommandSender sender,
            String[] args,
            Player player,
            Map<UUID, String> playerClan,
            Map<String, Clan> clans,
            FileConfiguration config,
            Economy economy,
            ClanMenu clanMenu,
            Scoreboard mainBoard,
            Map<String, List<UUID>> joinRequests,
            ClanRoleManager roleManager
    ) {
        String action = args[0];
        UUID playerId = player.getUniqueId();
        PclanCommandType type = PclanCommandType.valueOf(action.toUpperCase());

        String targetName;
        OfflinePlayer targetPlayer;
        UUID targetUUID;

        switch (type) {
            case CREATE:
                if (args.length != 2) {
                    player.sendMessage("§c/pclans create <clan-name>");
                    return;
                }

                String clanName = args[1].toLowerCase();
                if (playerClan.containsKey(playerId)) {
                    player.sendMessage(getMessage("already-in-clan"));
                    return;
                }

                if (clans.containsKey(clanName)) {
                    player.sendMessage(getMessage("clan-exists"));
                    return;
                }

                List<String> blacklist = config.getStringList("blacklist");
                if (blacklist.contains(clanName)) {
                    player.sendMessage(getMessage("clan-name-blacklisted"));
                    return;
                }

                double price = config.getDouble("create-price", 0.0);
                if (price > 0.0) {
                    if (!economy.has(player, price)) {
                        player.sendMessage(getMessage("not-enough-money", Map.of("price", String.valueOf(price))));
                        return;
                    }
                    economy.withdrawPlayer(player, price);
                    player.sendMessage(getMessage("money-withdrawn", Map.of("price", String.valueOf(price))));
                }

                Clan newClan = new Clan(clanName, playerId, new HashSet<>(List.of(playerId.toString())));
                clans.put(clanName, newClan);
                playerClan.put(playerId, clanName);
                player.sendMessage(getMessage("clan-created", Map.of("clan", clanName)));
                updatePlayerNames(player, playerClan, clanMenu, mainBoard);
                break;
            case JOIN:
                if (args.length != 2) {
                    player.sendMessage("§c/pclans join <clan-name>");
                    return;
                }
                if (playerClan.containsKey(playerId)) {
                    player.sendMessage(getMessage("already-in-clan"));
                    return;
                }
                clanName = args[1].toLowerCase();
                Clan targetClan = clans.get(clanName);
                if (targetClan == null) {
                    player.sendMessage(getMessage("clan-not-found"));
                    return;
                }
                if (targetClan.getMembers().size() >= config.getInt("max-members", 10)) {
                    player.sendMessage(getMessage("clan-full"));
                    return;
                }
                joinRequests.computeIfAbsent(clanName, k -> new ArrayList<>()).add(playerId);
                Player owner = Bukkit.getPlayer(targetClan.getOwner());
                if (owner != null) {
                    owner.sendMessage("§eИгрок \"" + player.getName() + "\" хочет вступить в ваш клан. Используйте /pclans accept " + player.getName() + " или /pclans deny " + player.getName());
                }
                player.sendMessage(getMessage("clan-joined-request"));
                break;
            case ACCEPT:
                if (args.length < 2) {
                    player.sendMessage("§c/pclans accept <player-name>");
                    return;
                }
                String ownerClanName = playerClan.get(playerId);
                if (ownerClanName == null || !clans.containsKey(ownerClanName)) {
                    player.sendMessage("§cТолько владелец клана может принимать игроков.");
                    return;
                }

                if (!checkClanPermission(player, "can-accept-requests", playerClan, clans, roleManager)) {
                    return;
                }

                targetName = args[1];
                targetPlayer = Bukkit.getOfflinePlayer(targetName);
                targetUUID = targetPlayer.getUniqueId();
                List<UUID> requests = joinRequests.getOrDefault(ownerClanName, new ArrayList<>());

                if (requests.contains(targetUUID)) {
                    Clan ownerClan = clans.get(ownerClanName);
                    ownerClan.getMembers().add(targetUUID.toString());

                    playerClan.put(targetUUID, ownerClanName);
                    requests.remove(targetUUID);
                    player.sendMessage("§aИгрок " + targetName + " принят в клан.");
                    Player joined = Bukkit.getPlayer(targetUUID);
                    if (joined != null) {
                        joined.sendMessage("§aВы были приняты в клан " + ownerClanName);
                        updatePlayerNames(joined, playerClan, clanMenu, mainBoard);
                    }
                } else {
                    player.sendMessage("§cУ игрока нет заявки в ваш клан.");
                }
                break;
            case DENY:
                if (args.length != 2) {
                    player.sendMessage("§c/pclans deny <player-name>");
                    return;
                }
                targetName = args[1];
                targetPlayer = Bukkit.getOfflinePlayer(targetName);
                targetUUID = targetPlayer.getUniqueId();
                ownerClanName = playerClan.get(playerId);
                if (ownerClanName == null || !clans.containsKey(ownerClanName)) {
                    player.sendMessage("§cТолько владелец клана может отклонять заявки.");
                    return;
                }

                if (!checkClanPermission(player, "can-deny-requests", playerClan, clans, roleManager)) return;

                requests = joinRequests.getOrDefault(ownerClanName, new ArrayList<>());
                if (requests.remove(targetUUID)) {
                    player.sendMessage("§eЗаявка от " + targetName + " отклонена.");
                    Player denied = Bukkit.getPlayer(targetUUID);
                    if (denied != null) denied.sendMessage("§cВаша заявка в клан была отклонена.");
                } else {
                    player.sendMessage("§cУ игрока нет заявки в ваш клан.");
                }
                break;
            case LEAVE:
                String currentClanName = playerClan.get(playerId);
                if (currentClanName == null) {
                    player.sendMessage("§cВы не состоите в клане.");
                    return;
                }
                Clan currentClan = clans.get(currentClanName);
                if (currentClan.getOwner().equals(playerId)) {
                    player.sendMessage("§cВы не можете выйти из собственного клана. Чтобы удалить клан, используйте /pclans remove " + currentClanName);
                    return;
                }
                currentClan.getMembers().remove(playerId.toString());
                playerClan.remove(playerId);
                player.sendMessage("§aВы вышли из клана " + currentClanName);
                updatePlayerNames(player, playerClan, clanMenu, mainBoard);
                break;
            case REMOVE:
                if (args.length != 2) {
                    player.sendMessage("§c/pclans remove <clan-name>");
                    return;
                }
                clanName = args[1].toLowerCase();
                if (!clans.containsKey(clanName)) {
                    player.sendMessage(getMessage("clan-not-found"));
                    return;
                }

                if (!checkClanPermission(player, "can-remove-clan", playerClan, clans, roleManager)) {
                    return;
                }

                Clan clan = clans.get(clanName);
                for (String member : clan.getMembers()) {
                    playerClan.remove(UUID.fromString(member));
                }
                clans.remove(clanName);
                joinRequests.remove(clanName);
                player.sendMessage(getMessage("clan-removed", Map.of("clan", clanName)));

                clan.getMembers().stream()
                        .map(memberUuid -> Bukkit.getPlayer(UUID.fromString(memberUuid)))
                        .forEach(member -> ZlomCoreHelper.updatePlayerNames(member, playerClan, clanMenu, mainBoard));
                updatePlayerNames(player, playerClan, clanMenu, mainBoard);
                break;
            default:
                player.sendMessage(getMessage("unknown-command"));
                break;
            case REQUESTS:
                ownerClanName = playerClan.get(playerId);
                if (ownerClanName == null || !clans.containsKey(ownerClanName)) {
                    player.sendMessage("§cТолько владелец клана может просматривать заявки.");
                    return;
                }
                Clan ownerClan = clans.get(ownerClanName);
                if (!ownerClan.getOwner().equals(playerId)) {
                    player.sendMessage("§cТолько владелец клана может просматривать заявки.");
                    return;
                }
                requests = joinRequests.getOrDefault(ownerClanName, new ArrayList<>());
                if (requests.isEmpty()) {
                    player.sendMessage("§eНет активных заявок в клан.");
                } else {
                    player.sendMessage("§aЗаявки в клан:");
                    requests.forEach(reqUUID -> {
                        OfflinePlayer reqPlayer = Bukkit.getOfflinePlayer(reqUUID);
                        player.sendMessage("§7- " + reqPlayer.getName());
                    });
                }
                break;
            case MY:
                UUID uuidPlayer = player.getUniqueId();
                String clanNameMy = playerClan.get(uuidPlayer);
                Clan clanMy = clans.get(clanNameMy);
                if (clanMy == null) {
                    sender.sendMessage("§cКлан не найден.");
                    return;
                }

                // Убираем несуществующие кланы из списка войн
                List<Clan> validWars = new ArrayList<>();
                for (Clan warClan : clanMy.getWars()) {
                    if (clans.containsKey(warClan.getName().toLowerCase())) {
                        validWars.add(warClan);
                    }
                }
                clanMy.setWars(validWars);

                StringBuilder myWarsString = new StringBuilder();
                validWars.forEach(warClan -> myWarsString.append(warClan.getName()).append(" "));

                String ownerName = Objects.requireNonNull(Bukkit.getPlayer(clanMy.getOwner())).getName();

                player.sendMessage("§5-----------------------------------------------------");
                player.sendMessage("");
                player.sendMessage("   §a§lИнформация про клан: §5§l" + clanNameMy);
                player.sendMessage("");
                player.sendMessage(" - §aВладелец клана: §3§l" + ownerName);
                player.sendMessage(" - §aТекущее кол-во участников: §3§l" + clanMy.getMembers().size());
                player.sendMessage("");
                player.sendMessage(" - §aВраждующие кланы: §3§l" + (!myWarsString.isEmpty() ? myWarsString : "Отсутствуют"));
                player.sendMessage("§5-----------------------------------------------------");
                break;
            case WARDECLARE:
                if (args.length < 2) {
                    player.sendMessage("§c/pclans wardeclare <clan-name>");
                    return;
                }

                String targetClanName = args[1].toLowerCase();
                if (!clans.containsKey(targetClanName)) {
                    player.sendMessage(getMessage("clan-not-found"));
                    return;
                }

                UUID playerUUID = player.getUniqueId();
                String myClanName = playerClan.get(playerUUID);

                if (!checkClanPermission(player, "can-declare-war", playerClan, clans, roleManager)) {
                    return;
                }

                Clan targetClan1 = clans.get(targetClanName);
                Clan myClan = clans.get(myClanName);

                if (myClan.getWars().contains(targetClan1)) {
                    player.sendMessage(getMessage("war-already-declared"));
                    return;
                }
                myClan.addWar(targetClan1);
                targetClan1.addWar(myClan);

                Bukkit.broadcastMessage(
                        getMessage("wardeclare")
                                .replace("{clan1}", clanMenu.getClanColor(myClanName) + myClanName)
                                .replace("{clan2}", clanMenu.getClanColor(targetClanName) +  targetClanName)
                );
                break;
            case WAREND:
                if (args.length != 2) {
                    player.sendMessage("§c/pclans wardend <clan-name>");
                    return;
                }

                String targetEndClanName = args[1].toLowerCase();
                if (!clans.containsKey(targetEndClanName)) {
                    player.sendMessage(getMessage("clan-not-found"));
                    return;
                }

                UUID playerIdEnd = player.getUniqueId();
                String myClanNameEnd = playerClan.get(playerIdEnd);
                Clan myClanEnd = clans.get(myClanNameEnd);

                if (!checkClanPermission(player, "can-end-war", playerClan, clans, roleManager)) {
                    return;
                }

                Clan targetClan2 = clans.get(targetEndClanName);
                if(!myClanEnd.getWars().contains(targetClan2)) {
                    player.sendMessage(getMessage("clan-not-war-with"));
                    return;
                }

                myClanEnd.removeWar(targetClan2);
                targetClan2.removeWar(myClanEnd);

                Bukkit.broadcastMessage(
                        getMessage("endwar")
                                .replace("{clan1}", clanMenu.getClanColor(myClanNameEnd) + myClanNameEnd)
                                .replace("{clan2}", clanMenu.getClanColor(targetEndClanName) + targetEndClanName)
                );
                break;
            case TRADECONTRACT:
                if (args.length != 2) {
                    player.sendMessage("§c/pclans tradecontract <clan-name>");
                    return;
                }

                String targetClanNameTrade = args[1].toLowerCase();
                if (!clans.containsKey(targetClanNameTrade)) {
                    player.sendMessage(getMessage("clan-not-found"));
                    return;
                }

                UUID playerUUIDTrade = player.getUniqueId();
                String myClanNameTrade = playerClan.get(playerUUIDTrade);
                Clan myClanTrade = clans.get(myClanNameTrade);

                if (!checkClanPermission(player, "can-trade-contract", playerClan, clans, roleManager)) return;

                Clan targetClanTrade = clans.get(targetClanNameTrade);
                if (myClanTrade.getTradecontrats().contains(targetClanTrade)) {
                    player.sendMessage(getMessage("clan-not-trade-with"));
                    return;
                }
                targetClanTrade.addTradecontrat(myClanTrade);
                myClanTrade.addTradecontrat(targetClanTrade);

                Bukkit.broadcastMessage(
                        getMessage("tradecontract")
                                .replace( "{clan1}", clanMenu.getClanColor(myClanNameTrade) +  myClanNameTrade)
                                .replace("{clan2}", clanMenu.getClanColor(targetClanNameTrade) + targetClanNameTrade)
                );
                break;
            case TRADECONTRACTEND:
                if (args.length != 2) {
                    player.sendMessage("§c/pclans tradecontractend <clan-name>");
                    return;
                }

                String targetClanNameTradeEnd = args[1].toLowerCase();
                if (!clans.containsKey(targetClanNameTradeEnd)) {
                    player.sendMessage(getMessage("clan-not-found"));
                    return;
                }

                UUID playerUUIDTradeEnd = player.getUniqueId();
                String myClanNameTradeEnd = playerClan.get(playerUUIDTradeEnd);
                Clan myClanTradeEnd = clans.get(myClanNameTradeEnd);

                if (!checkClanPermission(player, "can-trade-end", playerClan, clans, roleManager)) {
                    return;
                }

                Clan targetClanTradeEnd = clans.get(targetClanNameTradeEnd);
                if (!myClanTradeEnd.getTradecontrats().contains(targetClanTradeEnd)) {
                    player.sendMessage(getMessage("clan-not-trade-withend"));
                    return;
                }
                targetClanTradeEnd.removeTradecontrat(myClanTradeEnd);
                myClanTradeEnd.removeTradecontrat(targetClanTradeEnd);

                Bukkit.broadcastMessage(
                        getMessage("tradecontractend")
                                .replace( "{clan1}",clanMenu.getClanColor(myClanNameTradeEnd) +  myClanNameTradeEnd)
                                .replace("{clan2}", clanMenu.getClanColor(targetClanNameTradeEnd) + targetClanNameTradeEnd)
                );
                break;
            case MENU:
                UUID playerUUIDMenu = player.getUniqueId();
                String myClanNameMenu = playerClan.get(playerUUIDMenu);
                if (myClanNameMenu == null || !clans.containsKey(myClanNameMenu)) {
                    player.sendMessage("§cВы не состоите в клане.");
                    return;
                }
                Clan clanMenu1 = clans.get(myClanNameMenu);
                clanMenu.openClanMenu(player, clanMenu1);
                break;
            case SETTINGS:
                UUID playerUUIDSettings = player.getUniqueId();
                String clanNameSettings = playerClan.get(playerUUIDSettings);
                Clan clanSettings = clans.get(clanNameSettings);
                if (!checkClanPermission(player, "can-open-settings", playerClan, clans, roleManager)) {
                    return;
                }
                clanMenu.openSettingsMenu(player, clanSettings);
                break;
        }

    }
}
