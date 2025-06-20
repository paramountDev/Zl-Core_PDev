package dev.paramountdev.zlomCore_PDev;

import dev.paramountdev.zlomCore_PDev.boostyconnector.BoostConnector;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.FurnaceCommand;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.FurnaceProtectionManager;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.commands.AhBlockCommand;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.commands.MoneyCommand;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.commands.PenaltyCommand;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.listeners.AhCommandBlocker;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.managers.AhBlockManager;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.managers.BalanceManager;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.vault.VaultHook;
import dev.paramountdev.zlomCore_PDev.paraclans.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class ZlomCore_PDev extends JavaPlugin implements Listener, TabCompleter {

    private static ZlomCore_PDev instance;
    private static BoostConnector boostConnector;
    private static ClanRoleManager roleManager;

    private Economy economy;
    private FurnaceProtectionManager manager;
    private BalanceManager balanceManager;
    private AhBlockManager ahBlockManager;
    private Scoreboard mainBoard;
    private File clansFile;
    private FileConfiguration clansConfig;
    public Map<String, Clan> clans = new HashMap<>();
    public Map<UUID, String> playerClan = new HashMap<>();
    private FileConfiguration config;
    private Map<String, List<UUID>> joinRequests = new HashMap<>();
    private ClanMenu clanMenu;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault не найден! Ядро может работать некоректно!");
        }

        Economy provider = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        getLogger().info("Провайдер Vault: " + (provider != null ? provider.getName() : "не найден."));
        if (provider != null) {
            getLogger().info("Vault успешно интегрирован. Плагин на экономику запущен.");
        }

        //

        // BOOSTY CONNECTOR

        boostConnector = new BoostConnector();
        boostConnector.loadSettings();
        getServer().getPluginManager().registerEvents(boostConnector, this);
        this.getCommand("boostysetlimit").setExecutor(boostConnector);

        // BOOSTY CONNECTOR

        //

        // FURNACE PRIVATES

        manager = new FurnaceProtectionManager(this);
        getServer().getPluginManager().registerEvents(manager, this);
        manager.startBurnTask();
        FurnaceCommand fp = new FurnaceCommand(this);
        this.getCommand("fp").setExecutor(fp);

        // FURNACE PRIVATES

        //

        // GALAXY ECONOMY

        balanceManager = new BalanceManager(this);
        ahBlockManager = new AhBlockManager(this);
        getCommand("money").setExecutor(new MoneyCommand(this));
        getCommand("penalty").setExecutor(new PenaltyCommand(this));
        getCommand("ahblock").setExecutor(new AhBlockCommand(this));
        getServer().getPluginManager().registerEvents(new AhCommandBlocker(this), this);

        // GALAXY ECONOMY

        //

        // PARA CLANS

        PluginCommand pclansCommand = getCommand("pclans");
        if (pclansCommand != null) {
            pclansCommand.setExecutor(this);
            pclansCommand.setTabCompleter(this);
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager != null) {
            mainBoard = manager.getMainScoreboard();
        }

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        config = getConfig();

        try {
            loadClans();
        } catch (Exception e) {
            getLogger().severe("Ошибка при загрузке кланов: " + e.getMessage());
            e.printStackTrace();
        }

        clanMenu = new ClanMenu(this);
        getServer().getPluginManager().registerEvents(clanMenu, this);

        roleManager = new ClanRoleManager(config);

        // PARA CLANS

        getLogger().log(Level.INFO, "\n");
        getLogger().info("\u001B[35m!---------------ZlomCore Plugin enabled---------------!\u001B[0m");
        getLogger().info("\u001B[35m!---------------Made by Paramount_Dev---------------!\u001B[0m");
        getLogger().info("\u001B[35m!FunPay Link: https://funpay.com/uk/users/14397429/ !\u001B[0m");
        getLogger().log(Level.INFO, "\n");
    }

    @Override
    public void onDisable() {
        manager.saveProtections();
        manager.clearProtections();

        getLogger().log(Level.INFO, "\n");
        getLogger().info("\u001B[35m!---------------ZlomCore Plugin disabled---------------!\u001B[0m");
        getLogger().info("\u001B[35m!---------------Made by Paramount_Dev---------------!\u001B[0m");
        getLogger().info("\u001B[35m!FunPay Link: https://funpay.com/uk/users/14397429/ !\u001B[0m");
        getLogger().log(Level.INFO, "\n");
    }

    private void loadClans() {
        clansFile = new File(getDataFolder(), "clans.yml");
        if (!clansFile.exists()) {
            saveResource("clans.yml", false);
        }

        clansConfig = YamlConfiguration.loadConfiguration(clansFile);
        if (clansConfig == null) {
            getLogger().severe("Не удалось загрузить clans.yml.");
            return;
        }

        for (String key : clansConfig.getKeys(false)) {
            String owner = clansConfig.getString(key + ".owner");
            List<String> members = clansConfig.getStringList(key + ".members");
            clans.put(key.toLowerCase(), new Clan(key, UUID.fromString(owner), new HashSet<>(members)));
            for (String uuid : members) {
                playerClan.put(UUID.fromString(uuid), key.toLowerCase());
            }
        }
    }

    private void saveClans() {
        if (clansConfig == null || clansFile == null) {
            getLogger().warning("clansConfig или clansFile не инициализированы. Пропускаем сохранение кланов.");
            return;
        }

        for (Map.Entry<String, Clan> entry : clans.entrySet()) {
            Clan clan = entry.getValue();
            clansConfig.set(entry.getKey() + ".owner", clan.owner.toString());
            List<String> memberUUIDs = new ArrayList<>(clan.getMembers());
            clansConfig.set(entry.getKey() + ".members", memberUUIDs);
        }

        try {
            clansConfig.save(clansFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getMessage(String path, Map<String, String> placeholders) {
        String message = config.getString("messages." + path, "&c[Ошибка сообщения: " + path + "]");
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message.replace("&", "§");
    }

    private String getMessage(String path) {
        return getMessage(path, Collections.emptyMap());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        onPclansCommand(sender, args);
        return true;
    }

    public void onPclansCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();
        if (args.length < 1) {
            return;
        }

        String action = args[0].toLowerCase();
        String targetName;
        OfflinePlayer targetPlayer;
        UUID targetUUID;

        switch (action) {
            case "create":
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
                    if (!getEconomy().has(player, price)) {
                        player.sendMessage(getMessage("not-enough-money", Map.of("price", String.valueOf(price))));
                        return;
                    }
                    getEconomy().withdrawPlayer(player, price);
                    player.sendMessage(getMessage("money-withdrawn", Map.of("price", String.valueOf(price))));
                }

                Clan newClan = new Clan(clanName, playerId, new HashSet<>(List.of(playerId.toString())));
                clans.put(clanName, newClan);
                playerClan.put(playerId, clanName);
                player.sendMessage(getMessage("clan-created", Map.of("clan", clanName)));
                updatePlayerNames(player);
                break;
            case "leave":
                String currentClanName = playerClan.get(playerId);
                if (currentClanName == null) {
                    player.sendMessage("§cВы не состоите в клане.");
                    return;
                }
                Clan currentClan = clans.get(currentClanName);
                if (currentClan.owner.equals(playerId)) {
                    player.sendMessage("§cВы не можете выйти из собственного клана. Чтобы удалить клан, используйте /pclans remove " + currentClanName);
                    return;
                }
                currentClan.getMembers().remove(playerId.toString());
                playerClan.remove(playerId);
                player.sendMessage("§aВы вышли из клана " + currentClanName);
                updatePlayerNames(player);
                break;
            case "join":
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
                Player owner = Bukkit.getPlayer(targetClan.owner);
                if (owner != null) {
                    owner.sendMessage("§eИгрок \"" + player.getName() + "\" хочет вступить в ваш клан. Используйте /pclans accept " + player.getName() + " или /pclans deny " + player.getName());
                }
                player.sendMessage(getMessage("clan-joined-request"));
                break;
            case "accept":
                if (args.length < 2) {
                    player.sendMessage("§c/pclans accept <player-name>");
                    return;
                }
                String ownerClanName = playerClan.get(playerId);
                if (ownerClanName == null || !clans.containsKey(ownerClanName)) {
                    player.sendMessage("§cТолько владелец клана может принимать игроков.");
                    return;
                }

                if (!checkClanPermission(player, "can-accept-requests")) {
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
                        updatePlayerNames(joined);
                    }
                } else {
                    player.sendMessage("§cУ игрока нет заявки в ваш клан.");
                }
                break;
            case "deny":
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

                if (!checkClanPermission(player, "can-deny-requests")) return;

                requests = joinRequests.getOrDefault(ownerClanName, new ArrayList<>());
                if (requests.remove(targetUUID)) {
                    player.sendMessage("§eЗаявка от " + targetName + " отклонена.");
                    Player denied = Bukkit.getPlayer(targetUUID);
                    if (denied != null) denied.sendMessage("§cВаша заявка в клан была отклонена.");
                } else {
                    player.sendMessage("§cУ игрока нет заявки в ваш клан.");
                }
                break;
            case "requests":
                ownerClanName = playerClan.get(playerId);
                if (ownerClanName == null || !clans.containsKey(ownerClanName)) {
                    player.sendMessage("§cТолько владелец клана может просматривать заявки.");
                    return;
                }
                Clan ownerClan = clans.get(ownerClanName);
                if (!ownerClan.owner.equals(playerId)) {
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
            case "my":
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

                String ownerName = Bukkit.getPlayer(clanMy.owner).getName();

                player.sendMessage("§5-----------------------------------------------------");
                player.sendMessage("");
                player.sendMessage("   §a§lИнформация про клан: §5§l" + clanNameMy);
                player.sendMessage("");
                player.sendMessage(" - §aВладелец клана: §3§l" + ownerName);
                player.sendMessage(" - §aТекущее кол-во участников: §3§l" + clanMy.members.size());
                player.sendMessage("");
                player.sendMessage(" - §aВраждующие кланы: §3§l" + (myWarsString.length() > 0 ? myWarsString : "Отсутствуют"));
                player.sendMessage("§5-----------------------------------------------------");
                break;
            case "warend":
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

                if (!checkClanPermission(player, "can-end-war")) {
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
            case "wardeclare":
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

                if (!checkClanPermission(player, "can-declare-war")) {
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
            case "tradecontract":
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

                if (!checkClanPermission(player, "can-trade-contract")) return;

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
            case "tradecontractend":
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

                if (!checkClanPermission(player, "can-trade-end")) {
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
            case "menu":
                UUID playerUUIDMenu = player.getUniqueId();
                String myClanNameMenu = playerClan.get(playerUUIDMenu);
                if (myClanNameMenu == null || !clans.containsKey(myClanNameMenu)) {
                    player.sendMessage("§cВы не состоите в клане.");
                    return;
                }
                Clan clanMenu1 = clans.get(myClanNameMenu);
                clanMenu.openClanMenu(player, clanMenu1);
                break;
            case "settings":
                UUID playerUUIDSettings = player.getUniqueId();
                String clanNameSettings = playerClan.get(playerUUIDSettings);
                Clan clanSettings = clans.get(clanNameSettings);
                if (!checkClanPermission(player, "can-open-settings")) {
                    return;
                }
                clanMenu.openSettingsMenu(player, clanSettings);
                break;
            case "remove":
                if (args.length != 2) {
                    player.sendMessage("§c/pclans remove <clan-name>");
                    return;
                }
                clanName = args[1].toLowerCase();
                if (!clans.containsKey(clanName)) {
                    player.sendMessage(getMessage("clan-not-found"));
                    return;
                }

                if (!checkClanPermission(player, "can-remove-clan")) {
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
                        .forEach(this::updatePlayerNames);
                updatePlayerNames(player);
                break;
            default:
                player.sendMessage(getMessage("unknown-command"));
                break;
        }
    }

    public void updatePlayerNames(Player player) {
        updatePlayerChatPrefix(player);
        updatePlayerNametag(player);
    }

    public void updatePlayerChatPrefix(Player player) {
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

    public void updatePlayerNametag(Player player) {
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
            // Удаляем из всех команд, если игрок больше не в клане
//            mainBoard.getTeams().removeIf(team -> team.hasEntry(player.getName()));
            for (Team team : mainBoard.getTeams()) {
                if (team.hasEntry(player.getName())) {
                    team.removeEntry(player.getName());
                }
            }
        }
        player.setScoreboard(mainBoard);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length == 1) {
            return Stream.of("create", "join", "accept", "deny", "requests", "remove", "leave", "my", "menu", "settings",  "tradecontractend", "tradecontract", "wardeclare", "warend")
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "accept":
                case "deny":
                    String clanName = playerClan.get(uuid);
                    if (clanName != null && clans.containsKey(clanName)) {
                        Clan clan = clans.get(clanName);
                        if (clan.owner.equals(uuid)) {
                            List<UUID> requests = joinRequests.getOrDefault(clanName, new ArrayList<>());
                            return requests.stream()
                                    .map(Bukkit::getOfflinePlayer)
                                    .map(OfflinePlayer::getName)
                                    .filter(Objects::nonNull)
                                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                                    .toList();
                        }
                    }
                    break;
                case "remove":
                    if (playerClan.get(uuid) != null) {
                        return List.of(playerClan.get(uuid));
                    }
                    break;
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void onLoad() {
        getServer().getServicesManager().register(Economy.class, new VaultHook(this), this, ServicePriority.Highest);
        getLogger().info("VaultHook зарегистрирован в onLoad().");
    }

    public Map<String, Clan> getClans() {
        return clans;
    }

    public Map<UUID, String> getPlayerClan() {
        return playerClan;
    }

    public static ZlomCore_PDev getInstance() {
        return instance;
    }

    public Economy getEconomy() {
        return economy;
    }

    public FurnaceProtectionManager getManager() {
        return manager;
    }

    public BalanceManager getBalanceManager() {
        return balanceManager;
    }

    public AhBlockManager getAhBlockManager() {
        return ahBlockManager;
    }

    public ClanMenu getClanMenu() {
        return clanMenu;
    }

    public static ClanRoleManager getRoleManager() {
        return roleManager;
    }

    private boolean checkClanPermission(Player player, String permissionKey) {
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

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }
}
