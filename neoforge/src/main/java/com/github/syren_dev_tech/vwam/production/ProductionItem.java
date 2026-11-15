package com.github.syren_dev_tech.vwam.production;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single production item that a villager profession can produce.
 * Includes information about the item, required ingredients, and production settings.
 */
public class ProductionItem {
    private static final Logger LOGGER = LoggerFactory.getLogger("ProductionItem");
    
    private final String itemId;           // minecraft:item_name
    private final int requiredLevel;       // Villager level needed to produce
    private final int productQuantity;     // How many items produced per cycle
    private final long productionTicksPerCycle; // Ticks between production cycles
    private final int stockQuota;          // Target stock level (skip if >= this)
    private final String recipeName;       // Recipe identifier (optional, for custom recipes)
    
    /**
     * Create a production item definition
     */
    public ProductionItem(
        String itemId,
        int requiredLevel,
        int productQuantity,
        long productionTicksPerCycle,
        int stockQuota,
        String recipeName
    ) {
        this.itemId = itemId;
        this.requiredLevel = requiredLevel;
        this.productQuantity = productQuantity;
        this.productionTicksPerCycle = productionTicksPerCycle;
        this.stockQuota = stockQuota;
        this.recipeName = recipeName;
    }
    
    // Getters
    public String getItemId() {
        return itemId;
    }
    
    public int getRequiredLevel() {
        return requiredLevel;
    }
    
    public int getProductQuantity() {
        return productQuantity;
    }
    
    public long getProductionTicksPerCycle() {
        return productionTicksPerCycle;
    }
    
    public int getStockQuota() {
        return stockQuota;
    }
    
    public String getRecipeName() {
        return recipeName;
    }
    
    /**
     * Check if this item has a custom recipe name
     */
    public boolean hasCustomRecipe() {
        return recipeName != null && !recipeName.isEmpty();
    }
    
    /**
     * Get production item info string
     */
    public String getInfoString() {
        return String.format(
            "ProductionItem{item=%s, level=%d, qty=%d, quota=%d}",
            itemId, requiredLevel, productQuantity, stockQuota
        );
    }
}
