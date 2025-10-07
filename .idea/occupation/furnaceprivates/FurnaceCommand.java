package dev.paramountdev.zlomCore_PDev.LOL.occupation.furnaceprivates;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class FurnaceCommand implements CommandExecutor, TabCompleter {

    private final ZlomCore_PDev plugin;

    public FurnaceCommand(ZlomCore_PDev plugin) {
        this.plugin = plugin;
        plugin.getCommand("fp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length != 2) {
                sender.sendMessage(ChatColor.RED + "Используй: /fp give <ник>");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Игрок не найден!");
                return true;
            }

            ItemStack furnace = new ItemStack(Material.BLAST_FURNACE);
            ItemMeta meta = furnace.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        plugin.getConfig().getString("custom_furnace.name", "&6[Приватная Печь]")));
                List<String> lore = plugin.getConfig().getStringList("custom_furnace.lore");
                meta.setLore(ChatColor.translateAlternateColorCodes('&', String.join("\n", lore)).lines().toList());
                furnace.setItemMeta(meta);
            }

            target.getInventory().addItem(furnace);
            sender.sendMessage(ChatColor.GREEN + "Выдана приватная печь игроку " + target.getName());
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Только игрок может использовать эту команду.");
                return true;
            }

            Player player = (Player) sender;
            List<ProtectionRegion> list = plugin.getManager().getRegionsFor(player.getUniqueId());

            if (list.isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "У тебя нет приватных печек.");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Твои приваты от печек:");
                for (ProtectionRegion region : list) {
                    player.sendMessage(ChatColor.GOLD + "- " + region.getCenter().getBlockX() + ", "
                            + region.getCenter().getBlockY() + ", "
                            + region.getCenter().getBlockZ() + " | Радиус: " + region.getSize());
                }
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("author")) {
            Player player = (Player) sender;
            ZlomCoreHelper.sendAuthorMessage(player, "FurnaceGuards");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("give", "list", "author").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                names.add(online.getName());
            }
            return names.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}

