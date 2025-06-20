package dev.paramountdev.zlomCore_PDev.paraclans;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

public class ClanRoleManager {

    private final NavigableMap<Integer, ClanRole> roles = new TreeMap<>();
    private final Map<String, Integer> playerLevels = new HashMap<>(); // key: clanName:uuid
    private final FileConfiguration config;

    public ClanRoleManager(FileConfiguration config) {
        this.config = config;
        loadRoles();
    }

    private void loadRoles() {
        roles.clear();
        ConfigurationSection roleSection = config.getConfigurationSection("roles");
        if (roleSection == null) return;

        for (String key : roleSection.getKeys(false)) {
            if (!key.startsWith("level")) continue;

            try {
                int level = Integer.parseInt(key.replace("level", ""));
                ConfigurationSection section = roleSection.getConfigurationSection(key);

                String name = section.getString("name", "Без имени");
                Map<String, Boolean> permissions = new HashMap<>();
                for (String permKey : section.getKeys(false)) {
                    if (permKey.equalsIgnoreCase("name")) continue;
                    permissions.put(permKey, section.getBoolean(permKey));
                }

                roles.put(level, new ClanRole(level, name, permissions));
            } catch (NumberFormatException e) {
                System.out.println("[ClanRoles] Неверный формат уровня: " + key);
            }
        }
    }

    public ClanRole getRole(int level) {
        return roles.get(level);
    }

    public Integer getNextLevel(int currentLevel) {
        return roles.higherKey(currentLevel);
    }

    public Integer getPreviousLevel(int currentLevel) {
        return roles.lowerKey(currentLevel);
    }

    public boolean hasPermission(int level, String permissionKey) {
        ClanRole role = roles.get(level);
        return role != null && role.getPermissions().getOrDefault(permissionKey, false);
    }

    public boolean hasClanPermission(String clanName, UUID playerUUID, String permissionKey) {
        int level = getMemberLevel(clanName, playerUUID);
        return hasPermission(level, permissionKey);
    }

    public int getMemberLevel(String clanName, UUID uuid) {
        return playerLevels.getOrDefault(clanName + ":" + uuid.toString(), 1); // 1 = новичок по умолчанию
    }

    public void setMemberLevel(String clanName, UUID uuid, int level) {
        playerLevels.put(clanName + ":" + uuid.toString(), level);
    }

    public static class ClanRole {
        private final int level;
        private final String name;
        private final Map<String, Boolean> permissions;

        public ClanRole(int level, String name, Map<String, Boolean> permissions) {
            this.level = level;
            this.name = name;
            this.permissions = permissions;
        }

        public String getName() {
            return name;
        }

        public Map<String, Boolean> getPermissions() {
            return permissions;
        }
    }
}

