package dev.paramountdev.zlomCore_PDev;

import dev.paramountdev.zlomCore_PDev.aviktaskmanager.AvikManager;
import dev.paramountdev.zlomCore_PDev.basecommands.home.HomeCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.home.SetHomeCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.rtp.RTPCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.spawn.SetSpawnCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.spawn.SpawnCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.tpa.TpAcceptCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.tpa.TpDenyCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.tpa.TpaCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.warp.SetWarpCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.warp.WarpCommand;
import dev.paramountdev.zlomCore_PDev.basecommands.warp.WarpManager;
import dev.paramountdev.zlomCore_PDev.boostyconnector.BoostConnector;
import dev.paramountdev.zlomCore_PDev.combatmanager.CombatManager;
import dev.paramountdev.zlomCore_PDev.combatmanager.PveOffCommand;
import dev.paramountdev.zlomCore_PDev.configchanger.BoostyConfigGUI;
import dev.paramountdev.zlomCore_PDev.configchanger.ClansConfigGUI;
import dev.paramountdev.zlomCore_PDev.configchanger.CommandConfigSettings;
import dev.paramountdev.zlomCore_PDev.configchanger.FurnaceConfigGUI;
import dev.paramountdev.zlomCore_PDev.crazyorders.ChatInputHandler;
import dev.paramountdev.zlomCore_PDev.crazyorders.ItemSelectGUI;
import dev.paramountdev.zlomCore_PDev.crazyorders.OrderBackButtonClickListener;
import dev.paramountdev.zlomCore_PDev.crazyorders.OrderManager;
import dev.paramountdev.zlomCore_PDev.crazyorders.OrdersCommand;
import dev.paramountdev.zlomCore_PDev.crazyorders.OrdersGUI;
import dev.paramountdev.zlomCore_PDev.crazyorders.PurchasedItemsManager;
import dev.paramountdev.zlomCore_PDev.crazyorders.SellAmountListener;
import dev.paramountdev.zlomCore_PDev.diamondshopper.ShopCommand;
import dev.paramountdev.zlomCore_PDev.diamondshopper.ShopMenu;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.FurnaceCommand;
import dev.paramountdev.zlomCore_PDev.furnaceprivates.FurnaceProtectionManager;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.commands.AhBlockCommand;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.commands.MoneyCommand;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.commands.PenaltyCommand;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.listeners.AhCommandBlocker;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.managers.AhBlockManager;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.managers.BalanceManager;
import dev.paramountdev.zlomCore_PDev.galaxyeconomy.vault.VaultHook;
import dev.paramountdev.zlomCore_PDev.occupation.ClaimCommand;
import dev.paramountdev.zlomCore_PDev.occupation.playerWithClan.FurnaceClickListener;
import dev.paramountdev.zlomCore_PDev.occupation.playerWithoutClan.OnJoinMessage;
import dev.paramountdev.zlomCore_PDev.occupation.playerWithoutClan.SiegeMenu;
import dev.paramountdev.zlomCore_PDev.occupation.playerWithoutClan.SiegeScheduler;
import dev.paramountdev.zlomCore_PDev.parachats.ClanChat;
import dev.paramountdev.zlomCore_PDev.parachats.GlobalChat;
import dev.paramountdev.zlomCore_PDev.parachats.JailChat;
import dev.paramountdev.zlomCore_PDev.parachats.LocalChat;
import dev.paramountdev.zlomCore_PDev.parachats.PrivateChat;
import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import dev.paramountdev.zlomCore_PDev.paraclans.ClanManager;
import dev.paramountdev.zlomCore_PDev.paraclans.ClanMenu;
import dev.paramountdev.zlomCore_PDev.paraclans.ClanRoleManager;
import dev.paramountdev.zlomCore_PDev.paraclans.PclanCommandType;
import dev.paramountdev.zlomCore_PDev.paraclans.PlayerListener;
import dev.paramountdev.zlomCore_PDev.paraclans.allies.AllyMenu;
import dev.paramountdev.zlomCore_PDev.paraclans.allies.AllyPvpListener;
import dev.paramountdev.zlomCore_PDev.paraclans.allies.MenuClickListener;
import dev.paramountdev.zlomCore_PDev.paraclans.levels.ClanLevelMenu;
import dev.paramountdev.zlomCore_PDev.paraclans.statistic.ClanStatsTracker;
import dev.paramountdev.zlomCore_PDev.paraclans.statistic.StatisticIncrementer;
import dev.paramountdev.zlomCore_PDev.worlds.AccessManager;
import dev.paramountdev.zlomCore_PDev.worlds.PWorldCommand;
import dev.paramountdev.zlomCore_PDev.worlds.RequestManager;
import dev.paramountdev.zlomCore_PDev.worlds.WorldManager;
import dev.paramountdev.zlomCore_PDev.worlds.WorldMenu;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.getMessage;
import static dev.paramountdev.zlomCore_PDev.ZlomCoreHelper.updatePlayerNames;

public final class ZlomCore_PDev extends JavaPlugin implements Listener, TabCompleter {

    private static ZlomCore_PDev instance = getInstance();
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
    private StatisticIncrementer statisticIncrementer;
    private ClanStatsTracker clanStatsTracker;
    private ClanLevelMenu clanLevelMenu;
    private AvikManager avikManager;
    private OrderManager orderManager;
    private ClanManager clanManager;
    private AllyMenu allyMenu;

    private WorldManager worldManager;
    private RequestManager requestManager;

    private final Map<String, String> warpOwners = new HashMap<>();

    private FileConfiguration worldsConfig;
    private File worldsFile;
    private WorldMenu worldMenu;
    private AccessManager accessManager;

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

        new ZlomCoreHelper(this);
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
        mainBoard = manager.getMainScoreboard();

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

        clanStatsTracker = new ClanStatsTracker();
        statisticIncrementer = new StatisticIncrementer(clanStatsTracker);
        Bukkit.getPluginManager().registerEvents(statisticIncrementer, this);

        this.clanLevelMenu = new ClanLevelMenu();
        getServer().getPluginManager().registerEvents(clanLevelMenu, this);



        // PARA CLANS


        getCommand("openconfigsettings").setExecutor(new CommandConfigSettings());
        getCommand("openconfigsettings").setTabCompleter(new CommandConfigSettings());
        getServer().getPluginManager().registerEvents(new FurnaceConfigGUI(), this);
        getServer().getPluginManager().registerEvents(new BoostyConfigGUI(), this);
        getServer().getPluginManager().registerEvents(new ClansConfigGUI(), this);



        getServer().getPluginManager().registerEvents(new CombatManager(this, config), this);


        getCommand("pshop").setExecutor(new ShopCommand());
        getServer().getPluginManager().registerEvents(new ShopMenu(), this);




        avikManager = new AvikManager();
        avikManager.initStorage(getDataFolder());

        getCommand("avik").setExecutor(avikManager);
        getCommand("avik").setTabCompleter(avikManager);
        getServer().getPluginManager().registerEvents(avikManager, this);


        orderManager = new OrderManager();
        getCommand("orders").setExecutor(new OrdersCommand(orderManager));
        getServer().getPluginManager().registerEvents(new OrdersGUI(orderManager), this);
        getServer().getPluginManager().registerEvents(new SellAmountListener(), this);
        getServer().getPluginManager().registerEvents(new PurchasedItemsManager(), this);
        getServer().getPluginManager().registerEvents(new ItemSelectGUI(), this);
        getServer().getPluginManager().registerEvents(new ChatInputHandler(), this);
        getServer().getPluginManager().registerEvents(new OrderBackButtonClickListener(), this);



        getServer().getPluginManager().registerEvents(new GlobalChat(), this);
        getServer().getPluginManager().registerEvents(new LocalChat(), this);
        getServer().getPluginManager().registerEvents(new JailChat(), this);
        getServer().getPluginManager().registerEvents(new PrivateChat(), this);
        getServer().getPluginManager().registerEvents(new ClanChat(), this);



        getCommand("spawn").setExecutor(new SpawnCommand());
        getCommand("setspawn").setExecutor(new SetSpawnCommand());

        getCommand("home").setExecutor(new HomeCommand());
        getCommand("sethome").setExecutor(new SetHomeCommand());

        getCommand("rtp").setExecutor(new RTPCommand());

        WarpManager.loadWarps();
        getCommand("warp").setExecutor(new WarpCommand());
        getCommand("setwarp").setExecutor(new SetWarpCommand());


        getCommand("tpa").setExecutor(new TpaCommand());
        getCommand("tpaccept").setExecutor(new TpAcceptCommand());
        getCommand("tpdeny").setExecutor(new TpDenyCommand());



        clanManager = new ClanManager();
        allyMenu = new AllyMenu(this, clanManager);

        getServer().getPluginManager().registerEvents(new MenuClickListener(clanManager, allyMenu), this);
        getServer().getPluginManager().registerEvents(new AllyPvpListener(clanManager), this);


        getCommand("claim").setExecutor(new ClaimCommand());
        getServer().getPluginManager().registerEvents(new FurnaceClickListener(), this);
        getServer().getPluginManager().registerEvents(new OnJoinMessage(), this);
        getServer().getPluginManager().registerEvents(new SiegeMenu(), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                SiegeScheduler.checkExpirations();
            }
        }.runTaskTimer(ZlomCore_PDev.getInstance(), 0L, 20L * 300); // каждые 5 минут

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, SiegeScheduler::checkOutcomeExecutions, 20L * 60 * 5, 20L * 60 * 5); // каждые 5 минут


        getCommand("pveoff").setExecutor(new PveOffCommand());



        this.worldManager = new WorldManager(this);
        this.requestManager = new RequestManager();

        PWorldCommand pWorldCommand = new PWorldCommand(this);
        getCommand("pworld").setExecutor(pWorldCommand);
        getCommand("pworld").setTabCompleter(pWorldCommand);

        worldMenu = new WorldMenu(this);
        getServer().getPluginManager().registerEvents(worldMenu, this);

        loadWorlds();

        this.accessManager = new AccessManager(this);

        getLogger().log(Level.INFO, "\n");
        getLogger().log(Level.INFO, "\n");
        getLogger().info("\u001B[35m!---------------ZlomCore Plugin enabled---------------!\u001B[0m");
        getLogger().info("\u001B[35m!---------------Made by Paramount_Dev---------------!\u001B[0m");
        getLogger().info("\u001B[35m!FunPay Link: https://funpay.com/uk/users/14397429/ !\u001B[0m");
        getLogger().log(Level.INFO, "\n");
        getLogger().info("\u001B[35m!BoosyConnector enabled!\u001B[0m");
        getLogger().info("\u001B[35m!FurnacePrivates enabled!\u001B[0m");
        getLogger().info("\u001B[35m!GalaxyEconomy enabled!\u001B[0m");
        getLogger().info("\u001B[35m!ParaClans enabled!\u001B[0m");
        getLogger().info("\u001B[35m!ConfigChanger enabled!\u001B[0m");
        getLogger().info("\u001B[35m!CombatManager enabled!\u001B[0m");
        getLogger().info("\u001B[35m!DiamondShopper enabled!\u001B[0m");
        getLogger().info("\u001B[35m!AvikTaskManager enabled!\u001B[0m");
        getLogger().info("\u001B[35m!CrazyOrders enabled!\u001B[0m");
        getLogger().info("\u001B[35m!ParaChats enabled!\u001B[0m");
        getLogger().info("\u001B[35m!TeleportCore enabled!\u001B[0m");
        getLogger().log(Level.INFO, "\n");
        getLogger().log(Level.INFO, "\n");
    }

    @Override
    public void onDisable() {

        manager.saveProtections();
        manager.clearProtections();

        saveClans();

        balanceManager.saveBalances();
        ahBlockManager.saveBlocks();

        if (avikManager != null) {
            avikManager.saveAllTasks();
        }

        getLogger().log(Level.INFO, "\n");
        getLogger().log(Level.INFO, "\n");
        getLogger().info("\u001B[35m!---------------ZlomCore Plugin disabled---------------!\u001B[0m");
        getLogger().info("\u001B[35m!---------------Made by Paramount_Dev---------------!\u001B[0m");
        getLogger().info("\u001B[35m!FunPay Link: https://funpay.com/uk/users/14397429/ !\u001B[0m");
        getLogger().log(Level.INFO, "\n");
        getLogger().info("\u001B[35m!BoosyConnector disabled!\u001B[0m");
        getLogger().info("\u001B[35m!FurnacePrivates disabled!\u001B[0m");
        getLogger().info("\u001B[35m!GalaxyEconomy disabled!\u001B[0m");
        getLogger().info("\u001B[35m!ParaClans disabled!\u001B[0m");
        getLogger().info("\u001B[35m!ConfigChanger disabled!\u001B[0m");
        getLogger().info("\u001B[35m!CombatManager disabled!\u001B[0m");
        getLogger().info("\u001B[35m!DiamondShopper disabled!\u001B[0m");
        getLogger().info("\u001B[35m!AvikTaskManager disabled!\u001B[0m");
        getLogger().info("\u001B[35m!CrazyOrders disabled!\u001B[0m");
        getLogger().info("\u001B[35m!ParaChats disabled!\u001B[0m");
        getLogger().info("\u001B[35m!TeleportCore enabled!\u001B[0m");
        getLogger().log(Level.INFO, "\n");
        getLogger().log(Level.INFO, "\n");
    }


    public WorldMenu getWorldMenu() {
        return worldMenu;
    }

    public AccessManager getAccessManager() {
        return accessManager;
    }

    private void loadWorlds() {
        this.worldsFile = new File(getDataFolder(), "worlds.yml");
        if (!worldsFile.exists()) saveResource("worlds.yml", false);
        this.worldsConfig = YamlConfiguration.loadConfiguration(worldsFile);
    }

    public void saveWorldsConfig() {
        try {
            worldsConfig.save(worldsFile);
        } catch (Exception e) {
            getLogger().severe("Ошибка при сохранении worlds.yml");
        }
    }

    public FileConfiguration getWorldsConfig() {
        return this.worldsConfig;
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

        // Первая фаза — загрузка базовых кланов
        for (String key : clansConfig.getKeys(false)) {
            String owner = clansConfig.getString(key + ".owner");
            List<String> members = clansConfig.getStringList(key + ".members");
            int level = clansConfig.getInt(key + ".level", 1); // <= загрузка уровня клана

            Clan clan = new Clan(key, UUID.fromString(owner), new HashSet<>(members));
            clan.setLevel(level); // <= установка уровня в объекте

            clans.put(key.toLowerCase(), clan);

            for (String uuid : members) {
                playerClan.put(UUID.fromString(uuid), key.toLowerCase());
            }
        }

        // Вторая фаза — привязка войн и торговых контрактов по имени клана
        for (String key : clansConfig.getKeys(false)) {
            Clan clan = clans.get(key.toLowerCase());
            if (clan == null) continue;

            // Привязка войн
            List<String> warNames = clansConfig.getStringList(key + ".wars");
            for (String warName : warNames) {
                Clan warClan = clans.get(warName.toLowerCase());
                if (warClan != null) {
                    clan.addWar(warClan);
                }
            }

            // Привязка торговых контрактов
            List<String> tradeNames = clansConfig.getStringList(key + ".tradecontracts");
            for (String tradeName : tradeNames) {
                Clan tradeClan = clans.get(tradeName.toLowerCase());
                if (tradeClan != null) {
                    clan.addTradecontrat(tradeClan);
                }
            }
        }
    }

    private void saveClans() {
        if (clansConfig == null || clansFile == null) {
            getLogger().warning("clansConfig или clansFile не инициализированы. Пропускаем сохранение кланов.");
            return;
        }

        for (Map.Entry<String, Clan> entry : clans.entrySet()) {
            String clanKey = entry.getKey();
            Clan clan = entry.getValue();

            clansConfig.set(clanKey + ".owner", clan.owner.toString());
            clansConfig.set(clanKey + ".members", new ArrayList<>(clan.getMembers()));

            // Сохраняем имена кланов, с которыми война
            List<String> warNames = new ArrayList<>();
            for (Clan war : clan.getWars()) {
                warNames.add(war.getName());
            }
            clansConfig.set(clanKey + ".wars", warNames);

            // Сохраняем имена кланов с торговыми контрактами
            List<String> tradeNames = new ArrayList<>();
            for (Clan trade : clan.getTradecontrats()) {
                tradeNames.add(trade.getName());
            }
            clansConfig.set(clanKey + ".tradecontracts", tradeNames);

            clansConfig.set(clanKey + ".level", clan.getLevel());
        }

        try {
            clansConfig.save(clansFile);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        onPclansCommand(sender, args);
        return true;
    }

    private void onPclansCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return;
        }

        Player player = (Player) sender;
        if (args.length < 1) {
            UUID playerUUIDMenu = player.getUniqueId();
            String myClanNameMenu = playerClan.get(playerUUIDMenu);
            if (myClanNameMenu == null || !clans.containsKey(myClanNameMenu)) {
                clanMenu.openCreateMenu(player);
                return;
            }
            Clan clanMenu1 = clans.get(myClanNameMenu);
            clanMenu.openMainClanMenu(player, clanMenu1);
            return;
        }
        if (!playerClan.containsKey(player.getUniqueId())) {
            clanMenu.openCreateMenu(player);
            return;
        }

        PclanCommandType.proccessCommand(
                sender,
                args,
                player,
                playerClan,
                clans,
                config,
                getEconomy(),
                clanMenu,
                mainBoard,
                joinRequests,
                roleManager
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length == 1) {
            return Stream.of("author", "create", "join", "accept", "deny", "requests", "remove", "leave", "my", "menu", "settings", "tradecontractend", "tradecontract", "wardeclare", "warend")
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
                        if (clan.getOwner().equals(uuid)) {
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

    public void addPlayerToClan(Player player, String clanName) {
        if (playerClan.containsKey(player.getUniqueId())) {
            player.sendMessage(getMessage("already-in-clan"));
            return;
        }
        Clan targetClan = clans.get(clanName);
        if (targetClan == null) {
            player.sendMessage(getMessage("clan-not-found"));
            return;
        }
        if (targetClan.getMembers().size() >= config.getInt("max-members", 10)) {
            player.sendMessage(getMessage("clan-full"));
            return;
        }
        joinRequests.computeIfAbsent(clanName, k -> new ArrayList<>()).add(player.getUniqueId());
        Player owner = Bukkit.getPlayer(targetClan.getOwner());
        if (owner != null) {
            owner.sendMessage("§eИгрок \"" + player.getName() + "\" хочет вступить в ваш клан. Используйте /pclans accept " + player.getName() + " или /pclans deny " + player.getName());
        }
        player.sendMessage(getMessage("clan-joined-request"));
    }

    public void createClan(Player player, String clanName) {

        UUID playerId = player.getUniqueId();

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
    }

    public Map<String, Clan> getClans() {
        return clans;
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

    public Map<UUID, String> getPlayerClan() {
        return playerClan;
    }

    public Scoreboard getMainBoard() {
        return mainBoard;
    }

    public static ClanRoleManager getRoleManager() {
        return roleManager;
    }

    public StatisticIncrementer getStatisticIncrementer() { return statisticIncrementer; }

    public ClanStatsTracker getStatsTracker() { return clanStatsTracker; }


    public Clan getClanByPlayer(UUID playerUUID) {
        String clanName = playerClan.get(playerUUID);
        if (clanName == null) return null;
        return clans.get(clanName.toLowerCase());
    }

    public OrderManager getOrderManager() {
        return orderManager;
    }


    public ClanLevelMenu getClanLevelMenu() {
        return clanLevelMenu;
    }

    public void setWarpOwner(String warp, String playerName) {
        warpOwners.put(warp.toLowerCase(), playerName);
    }

    public String getWarpOwner(String warp) {
        return warpOwners.getOrDefault(warp.toLowerCase(), "");
    }


    public WorldManager getWorldManager() {
        return worldManager;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public Map<String, Set<String>> getAllClansAsMap() {
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Clan> entry : clans.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getMembers());
        }
        return result;
    }

    public AllyMenu getAllyMenu() {
        return allyMenu;
    }

    public ClanManager getClanManager() {
        return clanManager;
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
