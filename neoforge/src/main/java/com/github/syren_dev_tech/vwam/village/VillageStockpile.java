package com.github.syren_dev_tech.vwam.village;

import net.minecraft.resources.ResourceLocation;
import java.util.*;

/**
 * Manages the village stockpile and economy system.
 * Tracks inventory quantities, calculates price multipliers based on supply/demand.
 */
public class VillageStockpile {
    private final ResourceLocation villageId;
    private final Map<String, Integer> inventory;        // Current stock amounts
    private final Map<String, Integer> baselineAmounts;  // Target amounts
    private final Map<String, Long> lastRestockTime;     // Track when items were last restocked

    private final int restockIntervalHours; // How often items restock

    public VillageStockpile(ResourceLocation villageId, int restockIntervalHours) {
        this.villageId = villageId;
        this.inventory = new HashMap<>();
        this.baselineAmounts = new HashMap<>();
        this.lastRestockTime = new HashMap<>();
        this.restockIntervalHours = restockIntervalHours;
    }

    /**
     * Initialize an item in the stockpile at its baseline amount
     */
    public void initializeItem(String itemId, int baselineAmount) {
        baselineAmounts.put(itemId, baselineAmount);
        inventory.put(itemId, baselineAmount);
        lastRestockTime.put(itemId, System.currentTimeMillis());
    }

    /**
     * Get current quantity of an item
     */
    public int getQuantity(String itemId) {
        return inventory.getOrDefault(itemId, 0);
    }

    /**
     * Add items to stockpile (villager production or player sales)
     */
    public void addItems(String itemId, int quantity) {
        int current = getQuantity(itemId);
        inventory.put(itemId, current + quantity);
    }

    /**
     * Remove items from stockpile (player purchases)
     */
    public boolean removeItems(String itemId, int quantity) {
        int current = getQuantity(itemId);
        if (current < quantity) return false;

        inventory.put(itemId, current - quantity);
        return true;
    }

    /**
     * Calculate price multiplier based on supply/demand
     * Returns a float that modifies the base price
     * > 1.0 means more expensive (demand/shortage)
     * < 1.0 means cheaper (surplus)
     *
     * @param itemId The item to check
     * @param demandThreshold Percentage below which demand multiplier applies (e.g., 0.25)
     * @param surplusThreshold Percentage above which surplus multiplier applies (e.g., 0.75)
     * @param demandMultiplier How much to multiply when in demand (e.g., 1.5)
     * @param surplusMultiplier How much to multiply when surplus (e.g., 0.5)
     */
    public float getPriceMultiplier(
            String itemId,
            float demandThreshold,
            float surplusThreshold,
            float demandMultiplier,
            float surplusMultiplier
    ) {
        int baseline = baselineAmounts.getOrDefault(itemId, 64);
        int current = getQuantity(itemId);

        float percentageOfBaseline = (float) current / baseline;

        if (percentageOfBaseline <= demandThreshold) {
            // Below demand threshold - apply demand multiplier
            return demandMultiplier;
        } else if (percentageOfBaseline >= surplusThreshold) {
            // Above surplus threshold - apply surplus multiplier
            return surplusMultiplier;
        } else {
            // Normal range - no modifier
            return 1.0f;
        }
    }

    /**
     * Check if item needs restocking (production consideration)
     */
    public boolean needsRestocking(String itemId, float surplusThreshold) {
        int baseline = baselineAmounts.getOrDefault(itemId, 64);
        int current = getQuantity(itemId);

        float percentageOfBaseline = (float) current / baseline;
        return percentageOfBaseline < surplusThreshold;
    }

    /**
     * Get all items in stockpile with their current quantities
     */
    public Map<String, Integer> getAllItems() {
        return new HashMap<>(inventory);
    }

    /**
     * Clear stockpile (for testing or reset)
     */
    public void clear() {
        inventory.clear();
        lastRestockTime.clear();
    }

    public ResourceLocation getVillageId() {
        return villageId;
    }
}
