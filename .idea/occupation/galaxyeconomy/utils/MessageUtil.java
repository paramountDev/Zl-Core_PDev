package dev.paramountdev.zlomCore_PDev.LOL.occupation.galaxyeconomy.utils;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.ChatColor;

public class MessageUtil {

    public static String format(String path) {
        String message = ZlomCore_PDev.getInstance().getConfig().getString("messages." + path, "&cСообщение не найдено: " + path);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
