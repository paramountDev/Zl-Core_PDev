package dev.paramountdev.zlomCore_PDev.LOL.occupation.crazyorders;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrdersCommand implements CommandExecutor, TabCompleter {

    private final OrderManager manager;

    public OrdersCommand(OrderManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }

        if (args.length == 0) {
            new OrdersGUI(manager).openOrdersMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("author")) {
            ZlomCoreHelper.sendAuthorMessage(player, "CrazyOrders");
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("author").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }

}

