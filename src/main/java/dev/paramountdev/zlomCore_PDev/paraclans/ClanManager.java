package dev.paramountdev.zlomCore_PDev.paraclans;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.paraclans.allies.AllyPermissions;
import dev.paramountdev.zlomCore_PDev.paraclans.allies.AllyRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClanManager {
    private final Map<String, Clan> clans = ZlomCore_PDev.getInstance().getClans();
    private final List<AllyRequest> pendingRequests = new ArrayList<>();
    private final Map<String, List<String>> allyRequests = new HashMap<>();

    public Clan getClan(String name) {
        return clans.get(name.toLowerCase());
    }

    public void addClan(Clan clan) {
        clans.put(clan.getName().toLowerCase(), clan);
    }

    public Collection<Clan> getAllClans() {
        return clans.values();
    }

    public void sendAllyRequest(String fromClan, String toClan) {
        Clan target = getClan(toClan);
        Clan sender = getClan(fromClan);

        if (target == null || sender == null) return;

        // Сохраняем входящий запрос
        allyRequests.computeIfAbsent(toClan, k -> new ArrayList<>());
        if (!allyRequests.get(toClan).contains(fromClan)) {
            allyRequests.get(toClan).add(fromClan);
        }

        // Уведомляем владельца
        Player owner = Bukkit.getPlayer(target.getOwner());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§aВы получили союзный запрос от клана §e" + fromClan + "§a.");
            owner.sendMessage("§7Используйте §f/pclans allies accept " + fromClan + " §7для принятия.");
            owner.sendMessage("§7Или §f/pclans allies deny " + fromClan + " §7для отклонения.");
        }
    }

    public boolean hasRequest(String fromClan, String toClan) {
        return allyRequests.getOrDefault(toClan, List.of()).contains(fromClan);
    }

    public void acceptAllyRequest(String receiverClanName, String senderClanName) {
        Clan receiver = getClan(receiverClanName);
        Clan sender = getClan(senderClanName);
        if (receiver == null || sender == null) return;

        // Удаляем запрос
        List<String> requests = allyRequests.get(receiverClanName);
        if (requests != null) {
            requests.remove(senderClanName);
            if (requests.isEmpty()) allyRequests.remove(receiverClanName);
        }

        // Добавляем в союзники
        receiver.getAllies().put(senderClanName, new AllyPermissions(false, true));
        sender.getAllies().put(receiverClanName, new AllyPermissions(false, true));

        // Уведомляем
        Player receiverOwner = Bukkit.getPlayer(receiver.getOwner());
        Player senderOwner = Bukkit.getPlayer(sender.getOwner());

        if (receiverOwner != null) receiverOwner.sendMessage("§aВы приняли союз с кланом §e" + senderClanName);
        if (senderOwner != null) senderOwner.sendMessage("§aКлан §e" + receiverClanName + " §aпринял ваш союз.");
    }

    public void denyAllyRequest(String receiverClanName, String senderClanName) {
        List<String> requests = allyRequests.get(receiverClanName);
        if (requests != null) {
            requests.remove(senderClanName);
            if (requests.isEmpty()) allyRequests.remove(receiverClanName);
        }

        Player senderOwner = Bukkit.getPlayer(getClan(senderClanName).getOwner());
        if (senderOwner != null) {
            senderOwner.sendMessage("§cКлан §e" + receiverClanName + " §cотклонил ваш запрос на союз.");
        }
    }

    public List<AllyRequest> getRequestsTo(String clanName) {
        return pendingRequests.stream()
                .filter(req -> req.getToClan().equalsIgnoreCase(clanName))
                .collect(Collectors.toList());
    }

    public void acceptRequest(String from, String to) {
        pendingRequests.removeIf(req -> req.getFromClan().equalsIgnoreCase(from) &&
                req.getToClan().equalsIgnoreCase(to));
        Clan fromClan = getClan(from);
        Clan toClan = getClan(to);
        if (fromClan != null && toClan != null) {
            fromClan.addAlly(to, new AllyPermissions(false, false));
            toClan.addAlly(from, new AllyPermissions(false, false));
        }
    }

    public Clan getClanByPlayer(Player player) {
        return clans.values().stream()
                .filter(clan -> clan.getMembers().contains(player.getName()))
                .findFirst()
                .orElse(null);
    }


}

