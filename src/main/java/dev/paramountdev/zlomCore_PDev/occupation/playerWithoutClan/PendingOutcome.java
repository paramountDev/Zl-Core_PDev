package dev.paramountdev.zlomCore_PDev.occupation.playerWithoutClan;


import dev.paramountdev.zlomCore_PDev.furnaceprivates.ProtectionRegion;
import org.bukkit.Location;

public class PendingOutcome {
    private final Location location;
    private final ProtectionRegion region;

    public PendingOutcome(ProtectionRegion region) {
        this.location = region.getCenter();
        this.region = region;
    }

    public Location getLocation() {
        return location;
    }

    public ProtectionRegion getRegion() {
        return region;
    }
}

