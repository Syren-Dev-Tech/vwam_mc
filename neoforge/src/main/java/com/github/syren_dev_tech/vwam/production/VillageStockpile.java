package com.github.syren_dev_tech.vwam.production;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the shared stockpile/inventory of a village.
 * All villagers in a village contribute to and draw from this stockpile.
 * 
 * This is a virtual stockpile - items are tracked as quantities, not physical blocks.
 */
public class VillageStockpile {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillageStockpile");
    
    // NBT key prefix for village stockpile
    private static final String NBT_STOCKPILE_PREFIX = "VWAM_Stockpile_";
    private static final String NBT_STOCKPILE_TAG = "Items";
    
    private final String villageId;
    private final Map<String, Integer> inventory = new HashMap<>();
    private boolean dirty = false;
    
    /**
     * Create or retrieve village stockpile
     */
    public VillageStockpile(String villageId) {
        this.villageId = villageId;
        // Note: Load from NBT would happen in a full implementation
    }
    
    /**
     * Get the quantity of an item in stockpile
     * 
     * @param itemId The item identifier (e.g., "minecraft:wheat")
     * @return Quantity in stock, or 0 if not present
     */
    public int getStock(String itemId) {
        return inventory.getOrDefault(itemId, 0);
    }
    
    /**
     * Check if stockpile has at least quantity of an item
     */
    public boolean hasStock(String itemId, int quantity) {
        return getStock(itemId) >= quantity;
    }
    
    /**
     * Add items to stockpile
     * 
     * @param itemId The item identifier
     * @param quantity Amount to add
     * @return New total quantity
     */
    public int addStock(String itemId, int quantity) {
        if (quantity < 0) {
            LOGGER.warn("Attempted to add negative quantity {} for item {}", quantity, itemId);
            return getStock(itemId);
        }
        
        int newQuantity = getStock(itemId) + quantity;
        inventory.put(itemId, newQuantity);
        dirty = true;
        
        LOGGER.debug("Added {} of {} to village {} stockpile. New total: {}", 
            quantity, itemId, villageId, newQuantity);
        
        return newQuantity;
    }
    
    /**
     * Remove items from stockpile
     * 
     * @param itemId The item identifier
     * @param quantity Amount to remove
     * @return Actual amount removed (may be less if insufficient stock)
     */
    public int removeStock(String itemId, int quantity) {
        if (quantity < 0) {
            LOGGER.warn("Attempted to remove negative quantity {} for item {}", quantity, itemId);
            return 0;
        }
        
        int currentStock = getStock(itemId);
        int amountToRemove = Math.min(quantity, currentStock);
        
        if (amountToRemove > 0) {
            int newQuantity = currentStock - amountToRemove;
            inventory.put(itemId, newQuantity);
            dirty = true;
            
            LOGGER.debug("Removed {} of {} from village {} stockpile. New total: {}", 
                amountToRemove, itemId, villageId, newQuantity);
        }
        
        return amountToRemove;
    }
    
    /**
     * Set exact quantity of an item
     * Useful for initialization or adjustments
     */
    public void setStock(String itemId, int quantity) {
        if (quantity < 0) {
            LOGGER.warn("Attempted to set negative quantity {} for item {}", quantity, itemId);
            return;
        }
        
        if (quantity == 0) {
            inventory.remove(itemId);
        } else {
            inventory.put(itemId, quantity);
        }
        dirty = true;
    }
    
    /**
     * Check if stockpile contains ingredients needed for a recipe
     * 
     * @param ingredients Map of itemId -> quantity required
     * @return true if all ingredients are available in sufficient quantity
     */
    public boolean hasIngredientsFor(Map<String, Integer> ingredients) {
        for (Map.Entry<String, Integer> ingredient : ingredients.entrySet()) {
            if (!hasStock(ingredient.getKey(), ingredient.getValue())) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Consume ingredients from stockpile
     * 
     * @param ingredients Map of itemId -> quantity required
     * @return true if all ingredients were successfully consumed
     */
    public boolean consumeIngredients(Map<String, Integer> ingredients) {
        // First verify we have everything
        if (!hasIngredientsFor(ingredients)) {
            return false;
        }
        
        // Consume ingredients
        for (Map.Entry<String, Integer> ingredient : ingredients.entrySet()) {
            removeStock(ingredient.getKey(), ingredient.getValue());
        }
        
        LOGGER.debug("Consumed ingredients for recipe in village {}", villageId);
        return true;
    }
    
    /**
     * Get all items currently in stockpile
     */
    public Map<String, Integer> getAllStock() {
        return new HashMap<>(inventory);
    }
    
    /**
     * Get total number of unique item types in stockpile
     */
    public int getUniqueItemCount() {
        return inventory.size();
    }
    
    /**
     * Get total number of items across all types
     */
    public int getTotalItemCount() {
        return inventory.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Clear the entire stockpile
     */
    public void clear() {
        inventory.clear();
        dirty = true;
    }
    
    /**
     * Check if stockpile has been modified
     */
    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * Mark stockpile as saved
     */
    public void markSaved() {
        dirty = false;
    }
    
    /**
     * Get debug string showing all items in stockpile
     */
    public String getDebugString() {
        StringBuilder sb = new StringBuilder("VillageStockpile " + villageId + " {\n");
        if (inventory.isEmpty()) {
            sb.append("  (empty)\n");
        } else {
            for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                sb.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue()));
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
