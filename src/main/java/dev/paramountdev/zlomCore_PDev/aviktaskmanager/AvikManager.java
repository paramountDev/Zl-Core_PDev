package dev.paramountdev.zlomCore_PDev.aviktaskmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class AvikManager implements CommandExecutor, TabCompleter, Listener {

    private static final String MENU_TITLE = ChatColor.DARK_AQUA + "Задачи Avik";
    private final Map<UUID, List<Task>> createdTasks = new HashMap<>();
    private final Map<UUID, List<Task>> takenTasks = new HashMap<>();
    private final List<Task> allTasks = new ArrayList<>();
    private TaskStorage storage;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (label.equalsIgnoreCase("avik")) {
            List<String> parsedArgs = parseQuotedArgs(args);

            if (!parsedArgs.isEmpty()) {
                String subcommand = parsedArgs.get(0).toLowerCase();

                if (subcommand.equals("create")) {
                    if (parsedArgs.size() < 4) {
                        player.sendMessage(ChatColor.RED + "/avik create \"Название\" \"Описание\" Цена");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.7f);

                        return true;
                    }

                    String title = parsedArgs.get(1);
                    String description = parsedArgs.get(2);
                    double price;

                    try {
                        price = Double.parseDouble(parsedArgs.get(3));
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Некорректная цена");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.7f);

                        return true;
                    }

                    if (allTasks.stream().anyMatch(t -> t.title.equalsIgnoreCase(title))) {
                        player.sendMessage(ChatColor.RED + "Задача с таким названием уже существует");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.7f);

                        return true;
                    }

                    List<Task> playerTasks = createdTasks.getOrDefault(player.getUniqueId(), new ArrayList<>());
                    if (playerTasks.size() >= 3) {
                        player.sendMessage(ChatColor.RED + "Вы можете создать не более 3 задач");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.7f);

                        return true;
                    }

                    Task task = new Task(title, description, price, player.getUniqueId());
                    playerTasks.add(task);
                    createdTasks.put(player.getUniqueId(), playerTasks);
                    allTasks.add(task);

                    player.sendMessage(ChatColor.GREEN + "Задача успешно создана!");
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                    return true;
                }

                if (subcommand.equals("delete")) {
                    if (parsedArgs.size() < 2) {
                        player.sendMessage(ChatColor.RED + "/avik delete \"Название задачи\"");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.7f);

                        return true;
                    }

                    String taskTitle = parsedArgs.get(1);
                    List<Task> playerTasks = createdTasks.get(player.getUniqueId());
                    if (playerTasks == null || playerTasks.isEmpty()) {
                        player.sendMessage(ChatColor.RED + "У вас нет созданных задач");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.7f);

                        return true;
                    }

                    Optional<Task> taskOpt = playerTasks.stream()
                            .filter(t -> t.title.equalsIgnoreCase(taskTitle))
                            .findFirst();

                    if (taskOpt.isPresent()) {
                        Task task = taskOpt.get();
                        playerTasks.remove(task);
                        allTasks.remove(task);
                        player.sendMessage(ChatColor.GREEN + "Задача удалена");
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                    } else {
                        player.sendMessage(ChatColor.RED + "Задача с таким названием не найдена среди ваших");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.7f);

                    }
                    return true;
                }
                if (subcommand.equals("report")) {
                    if (parsedArgs.size() < 3) {
                        player.sendMessage(ChatColor.RED + "/avik report <Ник> \"Причина жалобы\"");
                        return true;
                    }

                    String targetName = parsedArgs.get(1);
                    String reason = parsedArgs.get(2);

                    Player targetPlayer = Bukkit.getPlayerExact(targetName);

                    String reporterName = player.getName();

                    String reportMsg = ChatColor.RED + "Жалоба от игрока: " + ChatColor.YELLOW + reporterName + "\n" +
                            ChatColor.RED + "Жалуются на: " + ChatColor.YELLOW + targetName + "\n" +
                            ChatColor.RED + "Причина: " + ChatColor.GRAY + reason;

                    boolean sentToAdmins = false;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.hasPermission("avik.admin")) {
                            p.sendMessage(reportMsg);
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            sentToAdmins = true;
                        }
                    }

                    if (sentToAdmins) {
                        player.sendMessage(ChatColor.GREEN + "Жалоба успешно отправлена администраторам. Подождите пока они обработают запрос и обратятся к вам в ближайшее время.");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "На сервере сейчас нет админов. Жалоба была заморожена к моменту их появления.");
                    }

                    return true;
                }


            }

            openTaskMenu(player);
            return true;
        }

        return false;
    }


    private void openTaskMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, MENU_TITLE);

        for (Task task : allTasks) {
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + task.title);
            meta.setLore(List.of(
                    ChatColor.GRAY + task.description,
                    ChatColor.YELLOW + "Цена: " + task.price,
                    ChatColor.AQUA + "Автор: " + Bukkit.getOfflinePlayer(task.author).getName()
            ));
            paper.setItemMeta(meta);
            inv.addItem(paper);
        }

        // Кнопка "Посмотреть свои задачи"
        ItemStack button = new ItemStack(Material.SUNFLOWER);
        ItemMeta buttonMeta = button.getItemMeta();
        buttonMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Посмотреть свои задачи");
        button.setItemMeta(buttonMeta);
        inv.setItem(53, button);

        player.openInventory(inv);
    }

    private void openConfirmMenu(Player player, Task task) {
        Inventory confirm = Bukkit.createInventory(null, 27, "Задача: " + task.title);
        ItemStack wool = new ItemStack(Material.GREEN_WOOL);
        ItemMeta meta = wool.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Взяться за эту задачу");
        wool.setItemMeta(meta);
        confirm.setItem(13, wool);
        player.openInventory(confirm);
    }

    private void openOwnTasksMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Ваши задачи");

        UUID playerId = player.getUniqueId();

        // 1. Собственные задачи
        List<Task> ownTasks = createdTasks.getOrDefault(playerId, new ArrayList<>());

        for (Task task : ownTasks) {
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + task.title);

            boolean inProcess = takenTasks.values().stream().anyMatch(list -> list.contains(task));

            meta.setLore(List.of(
                    ChatColor.GRAY + task.description,
                    ChatColor.YELLOW + "Цена: " + task.price,
                    inProcess
                            ? ChatColor.RED + "В процессе выполнения..."
                            : ChatColor.GREEN + "Активна"
            ));

            paper.setItemMeta(meta);
            inv.addItem(paper);
        }

        // 2. Задачи, которые игрок выполняет (взялся)
        List<Task> taken = takenTasks.getOrDefault(playerId, new ArrayList<>());

        for (Task task : taken) {
            ItemStack paper = new ItemStack(Material.MAP); // отличить визуально от своих
            ItemMeta meta = paper.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + task.title);

            meta.setLore(List.of(
                    ChatColor.GRAY + task.description,
                    ChatColor.YELLOW + "Цена: " + task.price,
                    ChatColor.BLUE + "Вы выполняете эту задачу",
                    ChatColor.GRAY + "Автор: " + Bukkit.getOfflinePlayer(task.author).getName()
            ));

            paper.setItemMeta(meta);
            inv.addItem(paper);
        }

        player.openInventory(inv);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();

        if (title.equals(MENU_TITLE)) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            if (clicked.getType() == Material.SUNFLOWER) {
                openOwnTasksMenu(player);
                return;
            }

            if (clicked.getType() != Material.PAPER) return;
            String taskTitle = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            Task selected = allTasks.stream()
                    .filter(task -> task.title.equals(taskTitle))
                    .findFirst().orElse(null);

            if (selected != null) {
                if (selected.author.equals(player.getUniqueId())) {
                    player.sendMessage(ChatColor.RED + "Вы не можете взяться за свою же задачу.");
                    player.closeInventory();
                    return;
                }
                openConfirmMenu(player, selected);
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);

        } else if (title.startsWith("Задача: ")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.GREEN_WOOL) return;

            String taskTitle = title.replace("Задача: ", "");
            Task task = allTasks.stream().filter(t -> t.title.equals(taskTitle)).findFirst().orElse(null);
            if (task == null) return;

            List<Task> playerTaken = takenTasks.getOrDefault(player.getUniqueId(), new ArrayList<>());
            if (playerTaken.size() >= 3) {
                player.sendMessage(ChatColor.RED + "Вы можете выполнять не более 3 задач");
                player.closeInventory();
                return;
            }

            playerTaken.add(task);
            takenTasks.put(player.getUniqueId(), playerTaken);
            allTasks.remove(task);

            player.sendMessage(ChatColor.GREEN + "Вы взялись за задачу: " + task.title);
            player.sendMessage(ChatColor.GRAY + "Описание: " + task.description);
            player.sendMessage(ChatColor.YELLOW + "Цена: " + task.price);
            player.sendMessage(ChatColor.AQUA + "Вы можете переписываться с автором в личных сообщениях");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);


            Player author = Bukkit.getPlayer(task.author);
            if (author != null && author.isOnline()) {
                author.sendMessage(ChatColor.GOLD + "Игрок " + player.getName() + " взялся за вашу задачу: " + task.title);
                author.sendMessage(ChatColor.GRAY + "Вы можете переписываться с ним в личных сообщениях");
                author.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            }

            player.closeInventory();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "report");
        }
        return Collections.emptyList();
    }


    public void initStorage(File dataFolder) {
        this.storage = new TaskStorage(dataFolder);
        this.allTasks.addAll(storage.loadTasks());
    }

    public void saveAllTasks() {
        storage.saveTasks(allTasks);
    }


    private List<String> parseQuotedArgs(String[] args) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (String arg : args) {
            if (arg.startsWith("\"")) {
                inQuotes = true;
                current.append(arg.substring(1));
            } else if (arg.endsWith("\"") && inQuotes) {
                current.append(" ").append(arg, 0, arg.length() - 1);
                result.add(current.toString());
                current.setLength(0);
                inQuotes = false;
            } else if (inQuotes) {
                current.append(" ").append(arg);
            } else {
                result.add(arg);
            }
        }

        if (inQuotes) {
            result.add(current.toString()); // на случай незакрытых кавычек
        }

        return result;
    }


}