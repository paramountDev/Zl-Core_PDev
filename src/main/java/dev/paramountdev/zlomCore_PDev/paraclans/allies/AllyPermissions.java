package dev.paramountdev.zlomCore_PDev.paraclans.allies;

public class AllyPermissions {
    private boolean canAccessClaims;
    private boolean pvpDisabled;

    public AllyPermissions(boolean canAccessClaims, boolean pvpDisabled) {
        this.canAccessClaims = canAccessClaims;
        this.pvpDisabled = pvpDisabled;
    }

    public boolean canAccessClaims() {
        return canAccessClaims;
    }

    public void setAccessClaims(boolean access) {
        this.canAccessClaims = access;
    }

    public boolean isPvpDisabled() {
        return pvpDisabled;
    }

    public void setPvpDisabled(boolean pvpDisabled) {
        this.pvpDisabled = pvpDisabled;
    }
}

