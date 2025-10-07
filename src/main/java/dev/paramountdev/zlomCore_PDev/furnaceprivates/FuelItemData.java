package dev.paramountdev.zlomCore_PDev.furnaceprivates;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class FuelItemData {
    private final Material material;
    private final int burnValue;
    private final Integer customModelData; // nullable

    public FuelItemData(Material material, int burnValue, Integer customModelData) {
        this.material = material;
        this.burnValue = burnValue;
        this.customModelData = customModelData;
    }

    public Material getMaterial() {
        return material;
    }

    public int getBurnValue() {
        return burnValue;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }

    public boolean matches(ItemStack item) {
        if (item == null || item.getType() != material) return false;

        if (customModelData != null) {
            if (!item.hasItemMeta() || !item.getItemMeta().hasCustomModelData()) return false;
            return item.getItemMeta().getCustomModelData() == customModelData;
        }

        return true;
    }
}

