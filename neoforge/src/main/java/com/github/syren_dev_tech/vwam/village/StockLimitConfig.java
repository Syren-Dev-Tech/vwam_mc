package com.github.syren_dev_tech.vwam.village;

import net.minecraft.resources.ResourceLocation;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.*;

/**
 * Represents stock limit configuration for a village.
 * Tracks maximum amounts for items in the village stockpile.
 */
public class StockLimitConfig {
    private final ResourceLocation villageId;
    private final int defaultMaxStock;              // Default max for all items
    private final Map<String, Integer> itemLimits; // Per-item overrides
    private final boolean autoRestockOnLowStock;   // Whether to produce items when stock is low

    public StockLimitConfig(
            ResourceLocation villageId,
            int defaultMaxStock,
            Map<String, Integer> itemLimits,
            boolean autoRestockOnLowStock
    ) {
        this.villageId = villageId;
        this.defaultMaxStock = defaultMaxStock;
        this.itemLimits = new HashMap<>(itemLimits);
        this.autoRestockOnLowStock = autoRestockOnLowStock;
    }

    public static StockLimitConfig fromJson(ResourceLocation villageId, JsonObject json) {
        int defaultMax = json.has("default_max_stock")
                ? json.get("default_max_stock").getAsInt()
                : 64;

        boolean autoRestock = json.has("auto_restock_on_low")
                ? json.get("auto_restock_on_low").getAsBoolean()
                : true;

        Map<String, Integer> itemLimits = parseItemLimits(
                json.has("item_stock_limits") ? json.getAsJsonObject("item_stock_limits") : null
        );

        return new StockLimitConfig(villageId, defaultMax, itemLimits, autoRestock);
    }

    private static Map<String, Integer> parseItemLimits(JsonObject json) {
        Map<String, Integer> limits = new HashMap<>();

        if (json == null) {
            return limits;
        }

        for (String key : json.keySet()) {
            int limit = json.get(key).getAsInt();
            limits.put(key, limit);
        }

        return limits;
    }

    /**
     * Get the maximum stock amount for a specific item
     * @param itemId The item ResourceLocation
     * @return Maximum stock for that item
     */
    public int getMaxStockForItem(String itemId) {
        return itemLimits.getOrDefault(itemId, defaultMaxStock);
    }

    /**
     * Check if an item is below its maximum stock
     * @param itemId The item to check
     * @param currentStock Current amount in stockpile
     * @return True if below max, false if at or above max
     */
    public boolean isBelowMaxStock(String itemId, int currentStock) {
        int maxStock = getMaxStockForItem(itemId);
        return currentStock < maxStock;
    }

    /**
     * Check if an item is critically low (below 25% of max)
     * @param itemId The item to check
     * @param currentStock Current amount in stockpile
     * @return True if below 25% of max stock
     */
    public boolean isCriticallyLow(String itemId, int currentStock) {
        int maxStock = getMaxStockForItem(itemId);
        return currentStock < (maxStock * 0.25f);
    }

    /**
     * Check if an item is low (below 50% of max)
     * @param itemId The item to check
     * @param currentStock Current amount in stockpile
     * @return True if below 50% of max stock
     */
    public boolean isLowStock(String itemId, int currentStock) {
        int maxStock = getMaxStockForItem(itemId);
        return currentStock < (maxStock * 0.5f);
    }

    // Getters
    public ResourceLocation getVillageId() { return villageId; }
    public int getDefaultMaxStock() { return defaultMaxStock; }
    public boolean isAutoRestockEnabled() { return autoRestockOnLowStock; }
    public Map<String, Integer> getItemLimits() { return new HashMap<>(itemLimits); }

    /**
     * Override the max stock for a specific item
     * @param itemId The item
     * @param maxStock The new maximum
     */
    public void setItemMaxStock(String itemId, int maxStock) {
        itemLimits.put(itemId, maxStock);
    }

    /**
     * Get all items with custom limits
     */
    public Set<String> getItemsWithCustomLimits() {
        return new HashSet<>(itemLimits.keySet());
    }
}
