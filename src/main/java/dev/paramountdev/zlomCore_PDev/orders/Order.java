package dev.paramountdev.zlomCore_PDev.orders;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.UUID;

public class Order {
    private final UUID creator;
    private final Material material;
    private int amountRemaining;
    private final double pricePerUnit;

    public Order(Player creator, Material material, int amount, double price) {
        this.creator = creator.getUniqueId();
        this.material = material;
        this.amountRemaining = amount;
        this.pricePerUnit = price;
    }

    public UUID getCreator() {
        return creator;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmountRemaining() {
        return amountRemaining;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void reduceAmount(int amount) {
        this.amountRemaining -= amount;
    }

    public boolean isCompleted() {
        return amountRemaining <= 0;
    }
}