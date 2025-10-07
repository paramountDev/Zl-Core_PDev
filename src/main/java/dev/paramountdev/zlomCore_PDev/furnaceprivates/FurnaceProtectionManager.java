package dev.paramountdev.zlomCore_PDev.furnaceprivates;

import dev.paramountdev.zlomCore_PDev.ZlomCoreHelper;
import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.paraclans.Clan;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlastFurnace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FurnaceProtectionManager implements Listener {

    private final ZlomCore_PDev plugin;
    private final Map<Location, ProtectionRegion> protections = new HashMap<>();
    private final Map<Material, Integer> itemValue;
    private final int burnMultiplier;
    private FileConfiguration dataFile;
    private File dataFilePath;
    private final Set<UUID> processingPlayers = new HashSet<>();
    private final boolean adminBypass;
    private final Map<UUID, String> playerClan;
    private final Map<Location, ProtectionRegion> archivedProtections = new HashMap<>();
    private final Map<UUID, Long> lastCombatTime = new HashMap<>();
    private final List<FuelItemData> fuelItems = new ArrayList<>();




    public static FurnaceProtectionManager getFpm() {
        return fpm;
    }

    private static FurnaceProtectionManager fpm;

    public FurnaceProtectionManager(ZlomCore_PDev plugin) {
        this.plugin = plugin;
        this.adminBypass = plugin.getConfig().getBoolean("admin_bypass", true);
        this.itemValue = new HashMap<>();
        this.playerClan = plugin.getPlayerClan();
        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("items");
        if (itemsSection != null) {
            itemsSection.getValues(false).forEach((key, val) ->
                    itemValue.put(Material.valueOf(key), (Integer) val)
            );
            ConfigurationSection fuelsSection = plugin.getConfig().getConfigurationSection("fuels");
            if (fuelsSection != null) {
                for (String key : fuelsSection.getKeys(false)) {
                    try {
                        Material mat = Material.valueOf(key);
                        ConfigurationSection fuelInfo = fuelsSection.getConfigurationSection(key);
                        int value = fuelInfo.getInt("burn_value", 1);
                        Integer customModelData = fuelInfo.contains("custom_model_data") ?
                                fuelInfo.getInt("custom_model_data") : null;

                        fuelItems.add(new FuelItemData(mat, value, customModelData));
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material in fuels: " + key);
                    }
                }
            } else {
                plugin.getLogger().warning("Config section 'fuels' is missing!");
            }

        } else {
            plugin.getLogger().warning("Config section 'items' is missing!");
        }

        this.burnMultiplier = plugin.getConfig().getInt("burn_multiplier", 30);

        dataFilePath = new File(plugin.getDataFolder(), "furnaces.yml");
        if (!dataFilePath.exists()) {
            plugin.saveResource("furnaces.yml", false);
        }
        dataFile = YamlConfiguration.loadConfiguration(dataFilePath);
        loadProtections();
        this.fpm = this;
    }

    public void startBurnTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Iterator<Map.Entry<Location, ProtectionRegion>> iterator = protections.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<Location, ProtectionRegion> entry = iterator.next();
                Location loc = entry.getKey();
                ProtectionRegion region = entry.getValue();

                if (!(loc.getBlock().getState() instanceof BlastFurnace furnace)) {
                    iterator.remove();
                    continue;
                }

                FurnaceInventory inv = furnace.getInventory();
                ItemStack fuelItem = inv.getItem(1); // ✅ ПРАВИЛЬНЫЙ способ
                furnace.getLocation().getWorld().spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        furnace.getLocation().add(0.5, 0.5, 0.5), // центр блока
                        1,      // count
                        0,      // offsetX
                        0.05,   // offsetY — чтобы чуть тянулось вверх
                        0,      // offsetZ
                        0.01    // speed — очень низкая
                );

                FuelItemData fuelType = getMatchingFuel(fuelItem);
                if (fuelType == null) {
                    iterator.remove();
                    archivedProtections.put(loc, region);
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                    }
                    Bukkit.broadcastMessage(ChatColor.RED +
                            "Приват возле печки на X: " + loc.getBlockX() + ", Y: " + loc.getBlockY() +
                            ", Z: " + loc.getBlockZ() + " пропал — закончился уголь.");
                    continue;
                }
                region.incrementBurnTime();

                if (region.getBurnTime() >= burnMultiplier * 20) {
                    region.resetBurnTime();

                    int burnValue = fuelType.getBurnValue();

                    int newAmount = fuelItem.getAmount() - 1;
                    if (newAmount <= 0) {
                        inv.setItem(1,new ItemStack(Material.AIR)); // удалить уголь полностью
                        furnace.getInventory().setContents(inv.getContents());
                        furnace.update();
                    } else {
                        fuelItem.setAmount(newAmount);
                        inv.setItem(1, fuelItem); // Обновить топливо
                        furnace.getInventory().setContents(inv.getContents());
                        furnace.update();
                    }
                    furnace.setBurnTime((short) 1600);
                    furnace.setCookTime((short) 1600);
                    furnace.update();

                    for (HumanEntity viewer : inv.getViewers()) {
                        if (viewer instanceof Player player) {
                            player.updateInventory();
                        }
                    }

                }
            }
        }, 0L, 20L);
    }



    @EventHandler
    public void onFurnaceInventoryClick(InventoryClickEvent event) {
        // Проверяем, что это печка
        if (!(event.getInventory().getHolder() instanceof BlastFurnace)) return;

        // Запретить Shift-клик любого типа
        if (event.isShiftClick()) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage(ChatColor.RED + "Перемещение предметов с помощью Shift запрещено внутри печки.");
            }
            return;
        }

        // Разрешить только ЛКМ (PICKUP_ALL = обычный клик)
        InventoryAction action = event.getAction();
        if (action != InventoryAction.PICKUP_ALL && action != InventoryAction.PLACE_ALL) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage(ChatColor.RED + "Разрешено только обычное перемещение предметов внутри печки.");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof BlastFurnace BlastFurnace)) return;

        FurnaceInventory inv = BlastFurnace.getInventory();
        ItemStack input = inv.getSmelting();
        ItemStack fuel = inv.getFuel();

        Player player = (Player) event.getPlayer();
        Location loc = BlastFurnace.getLocation();

        // 🔴 Если предметов в input слоте больше нет — удалить приват
        if ((input == null || input.getType() == Material.AIR) && protections.containsKey(loc)) {
            ProtectionRegion region = protections.get(loc);

            // Только если владелец сам забрал предмет
            if (region.getOwner().equals(player.getUniqueId())) {
                protections.remove(loc);
                archivedProtections.put(loc, region);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, 1f, 1f);
                player.sendMessage(ChatColor.RED + "Приват удалён — вы забрали все предметы из печки.");
            }

            return;
        }

        // 🔁 Создание или обновление привата
        FuelItemData fuelType = getMatchingFuel(fuel);
        if (input != null && itemValue.containsKey(input.getType()) && fuelType != null) {
            int addedSize = itemValue.get(input.getType());
            int inputAmount = input.getAmount();

            UUID playerId = player.getUniqueId();
            if (!playerClan.containsKey(playerId)) {
                player.sendMessage(ChatColor.RED + "Вы должны участвовать в клане, чтобы использовать приватную печку!");
                event.getInventory().clear(0);
                player.getInventory().addItem(input);
                return;
            }

            if (!protections.containsKey(loc)) {

                ProtectionRegion testRegion = new ProtectionRegion(player.getUniqueId(), loc);
                int testSize = addedSize * inputAmount;
                testRegion.setSize(testSize);

                if (!canActivateFurnace(player, loc, testRegion)) {
                    event.getInventory().clear(0);
                    player.getInventory().addItem(input);
                    return;
                }

                ProtectionRegion region = new ProtectionRegion(player.getUniqueId(), loc);
                int newSize = addedSize * inputAmount;
                region.setSize(newSize);
                region.setLastInputAmount(inputAmount);
                protections.put(loc, region);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                showProtectionBorder(region, 7);
                player.sendMessage(ChatColor.GREEN + "Приват успешно создан на " +
                        loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
            } else {
                ProtectionRegion region = protections.get(loc);
                if (region.getOwner().equals(player.getUniqueId())) {
                    if (region.getLastInputAmount() != inputAmount) {
                        int newSize = addedSize * inputAmount;
                        region.setSize(newSize);
                        region.setLastInputAmount(inputAmount);
                        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1f);
                        showProtectionBorder(region, 5);
                        player.sendMessage(ChatColor.YELLOW + "Приватная зона обновлена.");
                    }
                }
            }
        }
    }



    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();

        if (protections.containsKey(loc)) {
            ProtectionRegion region = protections.get(loc);

            if (!region.getOwner().equals(event.getPlayer().getUniqueId())) {
                if (!adminBypass || !event.getPlayer().isOp()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "Вы не можете ломать чужой приват.");
                    return;
                }
            }

            region.unprotect();
            protections.remove(loc);
        }
    }


    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        Location loc = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : null;
        if (loc == null) return;

        for (ProtectionRegion region : protections.values()) {
            if (region.isInside(loc) && !region.getOwner().equals(event.getPlayer().getUniqueId())) {
                // ✅ Разрешить админам при включённом bypass
                if (adminBypass && event.getPlayer().isOp()) return;

                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "Это приватная зона!");
            }
        }
    }

    public List<ProtectionRegion> getAllRegions() {
        return new ArrayList<>(protections.values());
    }

    public void clearProtections() {
        for (ProtectionRegion region : protections.values()) {
            region.unprotect();
        }
        protections.clear();
    }
    public void saveProtections() {
        dataFile.set("furnaces", null);
        int i = 0;
        for (Map.Entry<Location, ProtectionRegion> entry : protections.entrySet()) {
            Location loc = entry.getKey();
            ProtectionRegion region = entry.getValue();
            String path = "furnaces." + i;
            dataFile.set(path + ".world", loc.getWorld().getName());
            dataFile.set(path + ".x", loc.getBlockX());
            dataFile.set(path + ".y", loc.getBlockY());
            dataFile.set(path + ".z", loc.getBlockZ());
            dataFile.set(path + ".owner", region.getOwner().toString());
            dataFile.set(path + ".size", region.getSize());
            dataFile.set(path + ".fuel", region.getFuelTicks());
            i++;
        }

        try {
            dataFile.save(dataFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadProtections() {
        if (!dataFile.contains("furnaces")) return;

        for (String key : dataFile.getConfigurationSection("furnaces").getKeys(false)) {
            String path = "furnaces." + key;
            World world = Bukkit.getWorld(dataFile.getString(path + ".world"));
            if (world == null) continue;

            int x = dataFile.getInt(path + ".x");
            int y = dataFile.getInt(path + ".y");
            int z = dataFile.getInt(path + ".z");

            UUID owner = UUID.fromString(dataFile.getString(path + ".owner"));
            int size = dataFile.getInt(path + ".size");
            int fuel = dataFile.getInt(path + ".fuel");

            ProtectionRegion region = new ProtectionRegion(owner, new Location(world, x, y, z));
            region.expand(size);
            region.setFuelTicks(fuel);
            protections.put(region.getCenter(), region);
        }
    }

    public List<ProtectionRegion> getRegionsFor(UUID uuid) {
        return protections.values().stream()
                .filter(region -> region.getOwner().equals(uuid))
                .toList();
    }

    public void showProtectionBorder(ProtectionRegion region, int durationSeconds) {
        Location center = region.getCenter();
        int radius = region.getSize();
        World world = center.getWorld();
        if (world == null) return;

        int x0 = center.getBlockX();
        int y = center.getBlockY();
        int z0 = center.getBlockZ();

        Particle.DustOptions redDust = new Particle.DustOptions(Color.RED, 1.5f);
        double yOffset = 1;

        int intervalTicks = 5; // каждые 5 тиков (0.25 сек)
        int maxTicks = durationSeconds * 20;

        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (elapsed >= maxTicks) {
                    cancel();
                    return;
                }

                for (int i = -radius; i <= radius; i++) {
                    spawnParticle(world, x0 + i, y, z0 - radius, yOffset, redDust);
                    spawnParticle(world, x0 + i, y, z0 + radius, yOffset, redDust);
                    spawnParticle(world, x0 - radius, y, z0 + i, yOffset, redDust);
                    spawnParticle(world, x0 + radius, y, z0 + i, yOffset, redDust);
                }

                elapsed += intervalTicks;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);
    }

    private void spawnParticle(World world, int x, int y, int z, double yOffset, Particle.DustOptions dust) {
        Location loc = new Location(world, x + 0.5, y + yOffset, z + 0.5);
        world.spawnParticle(Particle.DUST, loc, 5, 0, 0, 0, 0, dust);
    }

    public List<ProtectionRegion> getRegionsForClanMembersIncludingArchived(Set<UUID> clanMembers) {
        Map<Location, ProtectionRegion> result = new HashMap<>();

        for (ProtectionRegion region : archivedProtections.values()) {
            if (clanMembers.contains(region.getOwner())) {
                result.put(region.getCenter(), region);
            }
        }

        for (ProtectionRegion region : protections.values()) {
            if (clanMembers.contains(region.getOwner())) {
                result.put(region.getCenter(), region);
            }
        }

        return new ArrayList<>(result.values());
    }




    public boolean canActivateFurnace(Player player, Location loc, ProtectionRegion region) {
        int radius = plugin.getConfig().getInt("protection_from_player_radius", 7);

        if (isInCombat(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Вы не можете активировать приват во время боя!");
            return false;
        }

        for (ProtectionRegion existing : fpm.getAllRegions()) {
            if (overlaps(region, existing)) {
                player.sendMessage(ChatColor.RED + "Печь не может быть активирована — её зона пересекается с чужим приватом.");
                return false;
            }
        }

        if (isPlayerNearby(loc, radius, player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Рядом находятся другие игроки.");
            return false;
        }

        return true;
    }


    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) return;
        if (!(event.getEntity() instanceof Player player)) return;

        lastCombatTime.put(damager.getUniqueId(), System.currentTimeMillis());
        lastCombatTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public boolean isInCombat(UUID uuid) {
        long cooldown = plugin.getConfig().getLong("combat_cooldown_seconds", 10) * 1000;
        return lastCombatTime.containsKey(uuid)
                && (System.currentTimeMillis() - lastCombatTime.get(uuid)) < cooldown;
    }

    public boolean isPlayerNearby(Location center, int radius, UUID owner) {
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getUniqueId().equals(owner)) continue;
            if (player.getLocation().distance(center) <= radius) {
                return true;
            }
        }
        return false;
    }

    public boolean overlaps(ProtectionRegion myRegion, ProtectionRegion other) {
        Location center = myRegion.getCenter();
        Location otherCenter = other.getCenter();

        int size = myRegion.getSize();
        int otherSize = other.getSize();

        if (!center.getWorld().equals(other.getCenter().getWorld())) return false;

        int minX1 = center.getBlockX() - size;
        int maxX1 = center.getBlockX() + size;
        int minY1 = center.getBlockY() - size;
        int maxY1 = center.getBlockY() + size;
        int minZ1 = center.getBlockZ() - size;
        int maxZ1 = center.getBlockZ() + size;

        int minX2 = otherCenter.getBlockX() - otherSize;
        int maxX2 = otherCenter.getBlockX() + otherSize;
        int minY2 = otherCenter.getBlockY() - otherSize;
        int maxY2 = otherCenter.getBlockY() + otherSize;
        int minZ2 = otherCenter.getBlockZ() - otherSize;
        int maxZ2 = otherCenter.getBlockZ() + otherSize;

        return (minX1 <= maxX2 && maxX1 >= minX2) &&
                (minY1 <= maxY2 && maxY1 >= minY2) &&
                (minZ1 <= maxZ2 && maxZ1 >= minZ2);
    }

    public ProtectionRegion getRegionAtLocation(Location location) {
        for (ProtectionRegion region : protections.values()) {
            if (region.isInside(location)) {
                return region;
            }
        }
        return null; // Нет подходящего региона
    }

    public ProtectionRegion getRegionByOwner(UUID uuid) {
        for (ProtectionRegion region : protections.values()) {
            if (region.getOwner().equals(uuid)) {
                return region;
            }
        }
        return null;
    }


    private FuelItemData getMatchingFuel(ItemStack item) {
        for (FuelItemData fuel : fuelItems) {
            if (fuel.matches(item)) {
                return fuel;
            }
        }
        return null;
    }


}

