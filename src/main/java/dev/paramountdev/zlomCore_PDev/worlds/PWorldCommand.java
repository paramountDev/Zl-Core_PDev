package dev.paramountdev.zlomCore_PDev.worlds;


import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PWorldCommand implements CommandExecutor, TabCompleter {

    private final ZlomCore_PDev plugin;
    private final WorldManager worldManager;
    private final RequestManager requestManager;
    private final Map<UUID, Location> lastLocation = new HashMap<>();
    private final List<String> subCommands = Arrays.asList(
            "create", "join", "accept", "deny", "author", "tp", "back", "menu"
    );



    public PWorldCommand(ZlomCore_PDev plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
        this.requestManager = plugin.getRequestManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Команда доступна только игрокам.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage("§cИспользование: /pworld <create|join|accept|deny|author>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage("§cУкажите имя мира.");
                    return true;
                }
                lastLocation.put(player.getUniqueId(), player.getLocation());
                worldManager.createWorld(player, args[1]);
            }
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage("§cУкажите имя мира.");
                    return true;
                }
                worldManager.requestJoin(player, args[1]);
            }
            case "accept" -> {
                if (args.length < 2) {
                    player.sendMessage("§cУкажите ник игрока.");
                    return true;
                }
                requestManager.acceptRequest(player, args[1]);
            }
            case "deny" -> {
                if (args.length < 2) {
                    player.sendMessage("§cУкажите ник игрока.");
                    return true;
                }
                requestManager.denyRequest(player, args[1]);
            }
            case "author" -> {
                player.sendMessage("§eКоманда /pworld author пока не реализована. Добавьте логику в PWorldCommand.java");
                sendAuthorMessage(player);
            }
            case "tp" -> {
                lastLocation.put(player.getUniqueId(), player.getLocation());
                plugin.getWorldManager().teleportToOwnWorld(player, lastLocation);
            }
            case "back" -> {
                Location loc = lastLocation.get(player.getUniqueId());
                plugin.getWorldManager().returnBack(player, loc);
                lastLocation.remove(player.getUniqueId());
            }

            case "menu" -> {
                plugin.getWorldMenu().openWorldMenu(player);
            }

            default -> player.sendMessage("§cНеизвестная подкоманда.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(subCommands, args[0]);
        }

        // Подсказки для второго аргумента
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "accept", "deny" -> {
                    if (sender instanceof Player player) {
                        return requestManager.getPendingRequestNames(player);
                    }
                }
                case "join" -> {
                    return worldManager.getAvailableWorldNames();
                }
                case "create" -> {
                    return Collections.singletonList("<имя_мира>");
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        List<String> result = new ArrayList<>();
        for (String item : list) {
            if (item.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(item);
            }
        }
        return result;
    }




    private void sendAuthorMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage(ChatColor.DARK_GREEN + "=====[ " + ChatColor.GOLD + "Разработчик ParaWorlds" + ChatColor.DARK_GREEN + " ]=====");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Автор: " + ChatColor.GREEN + "ParamountDev");
        player.sendMessage("");

        // FunPay
        TextComponent funpayPrefix = new TextComponent("• ");
        funpayPrefix.setColor(net.md_5.bungee.api.ChatColor.GOLD);

        TextComponent funpayLink = new TextComponent("FunPay профиль");
        funpayLink.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        funpayLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://funpay.com/uk/users/14397429/"));
        funpayLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Открыть профиль FunPay").create()));

        funpayPrefix.addExtra(funpayLink);
        player.spigot().sendMessage(funpayPrefix);

        // Telegram
        TextComponent tgPrefix = new TextComponent("• ");
        tgPrefix.setColor(net.md_5.bungee.api.ChatColor.GOLD);

        TextComponent tgLink = new TextComponent("Telegram: @paramount1_dev");
        tgLink.setColor(net.md_5.bungee.api.ChatColor.AQUA);
        tgLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://t.me/paramount1_dev"));
        tgLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Открыть Telegram").create()));

        tgPrefix.addExtra(tgLink);
        player.spigot().sendMessage(tgPrefix);
        player.sendMessage("");

        player.sendMessage(ChatColor.DARK_GREEN + "===============================");
        player.sendMessage("");
        player.sendMessage("");


    }
}
