package dev.paramountdev.zlomCore_PDev.furnaceprivates;

import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ProtectionRegion {
    private final Location center;
    private UUID owner;
    private int size;
    private int fuelTicks = 200;
    private int burnTime = 0;
    private int lastInputAmount = -1;
    private long siegeAvailableAt = 0L;
    private final UUID regionId = UUID.randomUUID();

    public ProtectionRegion(UUID ownerID, Location center) {
        this.owner = ownerID;
        this.center = center;
    }

    public boolean isSiegeAvailable() {
        return System.currentTimeMillis() >= siegeAvailableAt;
    }

    public void startPrivateSiegeCountdown() {
        this.siegeAvailableAt = System.currentTimeMillis() + + 24 * 60 * 60 * 1000; // 24ч
    }

    public long getSiegeAvailableAt() {
        return siegeAvailableAt;
    }


    public void expand(int amount) {
        this.size += amount;
        this.fuelTicks = 200; // сбросить время
    }

    public boolean isInside(Location loc) {
        return loc.getWorld().equals(center.getWorld())
                && Math.abs(loc.getBlockX() - center.getBlockX()) <= size
                && Math.abs(loc.getBlockY() - center.getBlockY()) <= size
                && Math.abs(loc.getBlockZ() - center.getBlockZ()) <= size;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID ownerID) {
        owner = ownerID;
    }

    public boolean tickBurn() {
        fuelTicks--;
        return fuelTicks > 0;
    }

    public void unprotect() {
        // можно добавить визуальные эффекты/оповещение
        Player p = Bukkit.getPlayer(owner);
        if (p != null) {
            p.sendMessage(ChatColor.GRAY +
                    "Приват возле печки на X: " + center.getBlockX() + ", Y: " + center.getBlockY() +
                    ", Z: " + center.getBlockZ() + " пропал");
        }
    }
    public void setFuelTicks(int fuelTicks) {
        this.fuelTicks = fuelTicks;
    }

    public int getSize() {
        return size;
    }

    public int getFuelTicks() {
        return fuelTicks;
    }

    public Location getCenter() {
        return center;
    }

    public void incrementBurnTime() {
        burnTime++;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public void resetBurnTime() {
        burnTime = 0;
    }

    public int getLastInputAmount() {
        return lastInputAmount;
    }

    public void setLastInputAmount(int amount) {
        this.lastInputAmount = amount;
    }

    public void setSize(int newSize) {
        this.size = newSize;
        this.fuelTicks = 200; // сбросим топливо на новое
    }
    public UUID getRegionId() {
        return regionId;
    }

}

