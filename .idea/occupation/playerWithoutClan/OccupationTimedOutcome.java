package dev.paramountdev.zlomCore_PDev.LOL.occupation.playerWithoutClan;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import dev.paramountdev.zlomCore_PDev.LOL.occupation.furnaceprivates.ProtectionRegion;
import org.bukkit.Location;

public class OccupationTimedOutcome {
    private final long executionTime;
    private final String outcomeType;
    private final Location location;
    private final ProtectionRegion region;

    public OccupationTimedOutcome(String outcomeType, Location location, ProtectionRegion region) {
        int delayDays = ZlomCore_PDev.getInstance().getConfig().getInt("occupation.delay-days", 3);
        this.executionTime = System.currentTimeMillis() + delayDays * 24L * 60 * 60 * 1000; // в миллисекундах
        this.outcomeType = outcomeType;
        this.location = location;
        this.region = region;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getOutcomeType() {
        return outcomeType;
    }

    public Location getLocation() {
        return location;
    }

    public ProtectionRegion getRegion() {
        return region;
    }
}
