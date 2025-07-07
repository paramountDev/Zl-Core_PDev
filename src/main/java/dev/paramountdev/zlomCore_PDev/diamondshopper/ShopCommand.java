package dev.paramountdev.zlomCore_PDev.diamondshopper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (label.equalsIgnoreCase("pshop") || label.equalsIgnoreCase("shop")) {
            new ShopMenu().openShop(player);
            return true;
        }

        return false;
    }

}
