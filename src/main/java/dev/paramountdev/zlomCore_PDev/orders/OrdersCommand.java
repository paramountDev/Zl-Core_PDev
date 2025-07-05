package dev.paramountdev.zlomCore_PDev.orders;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class OrdersCommand implements CommandExecutor {

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
        return true;
    }
}

