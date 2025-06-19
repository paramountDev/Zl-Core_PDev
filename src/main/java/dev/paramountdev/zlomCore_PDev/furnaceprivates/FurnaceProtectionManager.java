package dev.paramountdev.zlomCore_PDev.furnaceprivates;

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
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FurnaceProtectionManager implements Listener {

    private final Plugin plugin;
    private final Map<Location, ProtectionRegion> protections = new HashMap<>();
    private final Map<Material, Integer> itemValue;
    private final int burnMultiplier;
    private FileConfiguration dataFile;
    private File dataFilePath;
    private final Set<UUID> processingPlayers = new HashSet<>();
    private final boolean adminBypass;

    public static FurnaceProtectionManager getFpm() {
        return fpm;
    }

    private static FurnaceProtectionManager fpm;

    public FurnaceProtectionManager(Plugin plugin) {
        this.plugin = plugin;
        this.adminBypass = plugin.getConfig().getBoolean("admin_bypass", true);
        this.itemValue = new HashMap<>();
        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("items");
        if (itemsSection != null) {
            itemsSection.getValues(false).forEach((key, val) ->
                    itemValue.put(Material.valueOf(key), (Integer) val)
            );
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

                if (fuelItem == null || fuelItem.getType() != Material.COAL) {
                    iterator.remove();
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
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, 1f, 1f);
                player.sendMessage(ChatColor.RED + "Приват удалён — вы забрали все предметы из печки.");
            }

            return;
        }

        // 🔁 Создание или обновление привата
        if (input != null && itemValue.containsKey(input.getType()) && fuel != null && fuel.getType() == Material.COAL) {
            int addedSize = itemValue.get(input.getType());
            int inputAmount = input.getAmount();

            if (!protections.containsKey(loc)) {
                ProtectionRegion region = new ProtectionRegion(player.getUniqueId(), loc);
                int newSize = addedSize * inputAmount;
                region.setSize(newSize);
                region.setLastInputAmount(inputAmount);
                protections.put(loc, region);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
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

}

