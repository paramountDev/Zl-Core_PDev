package dev.paramountdev.zlomCore_PDev.LOL.occupation.configchanger;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandConfigSettings implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игрок может использовать эту команду.");
            return true;
        }

        if (!player.hasPermission("configeditor.use")) {
            player.sendMessage("§cНет прав.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§eИспользование: /openconfigsettings <furnace|boosty|clans|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "furnace" -> FurnaceConfigGUI.open(player);
            case "boosty" -> BoostyConfigGUI.open(player);
            case "clans" -> ClansConfigGUI.open(player);
            case "reload" -> {
                ZlomCore_PDev.getInstance().reloadConfig(); // Загружаем config.yml заново
                player.sendMessage("§aКонфиг успешно перезагружен.");
            }
            default -> player.sendMessage("§cНеизвестный аргумент. Используй: furnace, boosty, clans или reload.");
        }

        return true;
    }

    private final List<String> options = Arrays.asList("furnace", "boosty", "clans", "reload");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();
            for (String option : options) {
                if (option.startsWith(input)) {
                    completions.add(option);
                }
            }
            return completions;
        }
        return List.of();
    }
}
