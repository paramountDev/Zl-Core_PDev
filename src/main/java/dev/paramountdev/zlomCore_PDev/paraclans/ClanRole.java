package dev.paramountdev.zlomCore_PDev.paraclans;

import java.util.HashMap;
import java.util.Map;

public class ClanRole {
    private String name;
    private Map<String, Boolean> permissions = new HashMap<>();

    public ClanRole(String name) {
        this.name = name;

        // Все флаги по умолчанию false
        permissions.put("can-open-settings", false);
        permissions.put("can-accept-requests", false);
        permissions.put("can-deny-requests", false);
        permissions.put("can-declare-war", false);
        permissions.put("can-end-war", false);
        permissions.put("can-trade-contract", false);
        permissions.put("can-trade-end", false);
        permissions.put("can-remove-clan", false);
        permissions.put("place-private-furnace", false);
        permissions.put("break-private-furnace", false);
    }

    public String getName() {
        return name;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void togglePermission(String key) {
        if (permissions.containsKey(key)) {
            permissions.put(key, !permissions.get(key));
        }
    }

    public boolean getPermission(String key) {
        return permissions.getOrDefault(key, false);
    }
}
