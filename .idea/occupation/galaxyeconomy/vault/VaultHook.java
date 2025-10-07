package dev.paramountdev.zlomCore_PDev.LOL.occupation.galaxyeconomy.vault;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;


public class VaultHook implements Economy {

    private final ZlomCore_PDev plugin;

    public VaultHook(ZlomCore_PDev plugin) {
        this.plugin = plugin;
    }

    @Override public boolean isEnabled() {
        return plugin != null && plugin.isEnabled();
    }

    @Override public String getName() {
        return "GalaxyEconomy_PDev";
    }

    @Override public boolean hasBankSupport() {
        return false;
    }

    @Override public int fractionalDigits() {
        return 2;
    }

    @Override public String format(double amount) {
        return String.format("%.2f", amount);
    }

    @Override public String currencyNamePlural() {
        return "монет";
    }

    @Override public String currencyNameSingular() {
        return "монета";
    }

    @Override public boolean hasAccount(String playerName) {
        return true;
    }

    @Override public boolean hasAccount(OfflinePlayer player) {
        return true;
    }

    @Override public boolean hasAccount(String playerName, String worldName) {
        return true;
    }

    @Override public boolean hasAccount(OfflinePlayer player, String worldName) {
        return true;
    }

    @Override public double getBalance(String playerName) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return plugin.getBalanceManager().getBalance(player.getUniqueId());
    }

    @Override public double getBalance(OfflinePlayer player) {
        return plugin.getBalanceManager().getBalance(player.getUniqueId());
    }

    @Override public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override public boolean has(OfflinePlayer player, String worldName, double amount) {
        return has(player, amount);
    }

    @Override public EconomyResponse withdrawPlayer(String playerName, double amount) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return withdrawPlayer(player, amount);
    }

    @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        boolean success = plugin.getBalanceManager().subtractBalance(player.getUniqueId(), amount);
        return new EconomyResponse(amount, getBalance(player),
                success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE,
                success ? "OK" : "Недостаточно средств");
    }

    @Override public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override public EconomyResponse depositPlayer(String playerName, double amount) {
        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerName);
        return depositPlayer(player, amount);
    }

    @Override public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        plugin.getBalanceManager().addBalance(player.getUniqueId(), amount);
        return new EconomyResponse(amount, getBalance(player),
                EconomyResponse.ResponseType.SUCCESS, "OK");
    }

    @Override public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    // Банки не поддерживаются
    @Override public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available.");
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available.");
    }

    @Override public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available.");
    }

    @Override public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available.");
    }

    @Override public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available.");
    }

    @Override public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available.");
    }

    @Override public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available.");
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank support not available.");
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override public List<String> getBanks() {
        return null;
    }

    @Override public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }
}
