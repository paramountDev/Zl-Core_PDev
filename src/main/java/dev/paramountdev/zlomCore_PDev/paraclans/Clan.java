package dev.paramountdev.zlomCore_PDev.paraclans;


import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.paraclans.allies.AllyPermissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Clan {
    public String name;
    public UUID owner;
    public Set<String> members;
    public List<Clan> wars = new ArrayList<>();
    List<Clan> tradecontrats = new ArrayList<>();
    public Map<String, AllyPermissions> allies;
    private int level = 1;

    public Clan(String name, UUID owner, Set<String> members, List<Clan> wars, List<Clan> tradecontrats) {
        this.name = name;
        this.owner = owner;
        this.members = members;
        this.wars = wars;
        this.tradecontrats = tradecontrats;
        this.allies = new HashMap<>();
    }

    public Clan(String name, UUID owner, Set<String> members) {
        this.name = name;
        this.owner = owner;
        this.members = members;
    }

    public void setName(String newName, Player player) {
        ZlomCore_PDev plugin = ZlomCore_PDev.getInstance();
        if (plugin == null) {
            Bukkit.getLogger().severe("ParaClans_PDev.getInstance() вернул null в setName().");
            return;
        }

        // Создание нового клана
        Clan newClan = new Clan(newName, player.getUniqueId(), members, wars, tradecontrats);
        plugin.clans.remove(this.getName());
        plugin.clans.put(newName, newClan);

        // Обновление карты playerClan
        plugin.playerClan.remove(this.getOwner());
        plugin.playerClan.put(player.getUniqueId(), newName);

        // Обновление имени
        this.name = newName;
    }

    public void addTradecontrat(Clan tradecontrat) {
        this.tradecontrats.add(tradecontrat);
    }

    public void removeTradecontrat(Clan tradecontrat) {
        this.tradecontrats.remove(tradecontrat);
    }

    public void addWar(Clan war) {
        this.wars.add(war);
    }

    public void removeWar(Clan war) {
        this.wars.remove(war);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Set<String> getMembers() {
        return members;
    }

    public void setMembers(Set<String> members) {
        this.members = members;
    }

    public List<Clan> getWars() {
        return wars;
    }

    public void setWars(List<Clan> wars) {
        this.wars = wars;
    }

    public List<Clan> getTradecontrats() {
        return tradecontrats;
    }

    public int getLevel() { return level; }

    public void setLevel(int level) { this.level = level; }


    public Map<String, AllyPermissions> getAllies() {
        if (allies == null) {
            allies = new HashMap<>();
        }
        return allies;
    }

    public void addAlly(String otherClanName, AllyPermissions perms) {
        allies.put(otherClanName, perms);
    }

    public void removeAlly(String otherClanName) {
        allies.remove(otherClanName);
    }

    public boolean isAlly(String otherClanName) {
        return allies.containsKey(otherClanName);
    }

    public AllyPermissions getPermissionsForAlly(String otherClanName) {
        return allies.getOrDefault(otherClanName, new AllyPermissions(false, false));
    }
}