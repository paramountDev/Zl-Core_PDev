package dev.paramountdev.zlomCore_PDev.paraclans.allies;

public class AllyRequest {
    private final String fromClan;
    private final String toClan;

    public AllyRequest(String fromClan, String toClan) {
        this.fromClan = fromClan;
        this.toClan = toClan;
    }

    public String getFromClan() { return fromClan; }
    public String getToClan() { return toClan; }
}

