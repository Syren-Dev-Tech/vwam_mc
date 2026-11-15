package com.github.syren_dev_tech.vwam.production;

import com.github.syren_dev_tech.vwam.data.ProductionRecipe;
import com.github.syren_dev_tech.vwam.village.VillageStockpile;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages production for a single villager.
 * Tracks production state, timings, and recipe execution.
 */
public class VillagerProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsProduction");

    private final Villager villager;
    private final VillageStockpile stockpile;
    private final ProductionRecipe[] recipes;

    // Production state
    private int currentRecipeIndex = 0;           // Current recipe in round-robin
    private long lastProductionTime = 0;          // Last time production occurred
    private long nextProductionTime = 0;          // When next production should occur
    private String currentRecipeId = "";          // ID of currently-executing recipe

    // NBT keys for persistence
    private static final String NBT_LAST_PRODUCTION = "LastProductionTime";
    private static final String NBT_CURRENT_RECIPE = "CurrentRecipeId";
    private static final String NBT_NEXT_PRODUCTION = "NextProductionTime";

    public VillagerProducer(
            Villager villager,
            VillageStockpile stockpile,
            ProductionRecipe[] recipes
    ) {
        this.villager = villager;
        this.stockpile = stockpile;
        this.recipes = recipes;

        // Load state from NBT
        loadStateFromNBT();
    }

    /**
     * Attempt to produce items if enough time has passed
     * @param currentTime The current game time in milliseconds
     * @param qualityMultiplier The workshop quality multiplier (affects production speed)
     * @return True if production occurred, false if still on cooldown
     */
    public boolean tryProduce(long currentTime, float qualityMultiplier) {
        // Check if enough time has passed
        if (currentTime < nextProductionTime) {
            return false;  // Not ready yet
        }

        // Try recipes in round-robin order
        for (int i = 0; i < recipes.length; i++) {
            int recipeIndex = (currentRecipeIndex + i) % recipes.length;
            ProductionRecipe recipe = recipes[recipeIndex];

            // Check if recipe can execute
            if (recipe.canProduce(stockpile, villager.getVillagerData().getLevel())) {
                // Execute production
                if (recipe.executeProd(stockpile)) {
                    // Schedule next production
                    float productionTime = recipe.getBaseProductionTime() / qualityMultiplier;
                    long productionMs = (long) (productionTime * 1000);  // Convert hours to ms

                    lastProductionTime = currentTime;
                    nextProductionTime = currentTime + productionMs;
                    currentRecipeId = recipe.getRecipeId();
                    currentRecipeIndex = (recipeIndex + 1) % recipes.length;

                    // Save state to NBT
                    saveStateToNBT();

                    LOGGER.debug("Villager {} produced {} (recipe: {})",
                            villager.getId(), recipe.getOutputItem(), recipe.getRecipeId());
                    return true;
                }
            }
        }

        // No recipes could execute, reschedule for later
        nextProductionTime = currentTime + 60000;  // Try again in 1 minute
        return false;
    }

    /**
     * Schedule the next production attempt
     * @param qualityMultiplier The workshop quality multiplier
     */
    public void scheduleNextProduction(float qualityMultiplier) {
        // Default: try again in 1 hour (real time = actual time)
        nextProductionTime = System.currentTimeMillis() + 3600000;
    }

    /**
     * Get estimated time until next production (in game hours)
     */
    public float getTimeUntilNextProduction() {
        long remaining = Math.max(0, nextProductionTime - System.currentTimeMillis());
        return remaining / 3600000.0f;  // Convert ms to hours
    }

    /**
     * Get the ID of the currently-executing recipe
     */
    public String getCurrentRecipeId() {
        return currentRecipeId;
    }

    /**
     * Get the current recipe being executed
     */
    public ProductionRecipe getCurrentRecipe() {
        for (ProductionRecipe recipe : recipes) {
            if (recipe.getRecipeId().equals(currentRecipeId)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Load production state from villager NBT
     */
    private void loadStateFromNBT() {
        if (villager.getPersistentData().contains(NBT_LAST_PRODUCTION, Tag.TAG_LONG)) {
            lastProductionTime = villager.getPersistentData().getLong(NBT_LAST_PRODUCTION);
        }

        if (villager.getPersistentData().contains(NBT_CURRENT_RECIPE, Tag.TAG_STRING)) {
            currentRecipeId = villager.getPersistentData().getString(NBT_CURRENT_RECIPE);
        }

        if (villager.getPersistentData().contains(NBT_NEXT_PRODUCTION, Tag.TAG_LONG)) {
            nextProductionTime = villager.getPersistentData().getLong(NBT_NEXT_PRODUCTION);
        }
    }

    /**
     * Save production state to villager NBT
     */
    private void saveStateToNBT() {
        villager.getPersistentData().putLong(NBT_LAST_PRODUCTION, lastProductionTime);
        villager.getPersistentData().putString(NBT_CURRENT_RECIPE, currentRecipeId);
        villager.getPersistentData().putLong(NBT_NEXT_PRODUCTION, nextProductionTime);
    }

    /**
     * Reset production state
     */
    public void reset() {
        currentRecipeIndex = 0;
        lastProductionTime = 0;
        nextProductionTime = 0;
        currentRecipeId = "";
        villager.getPersistentData().remove(NBT_LAST_PRODUCTION);
        villager.getPersistentData().remove(NBT_CURRENT_RECIPE);
        villager.getPersistentData().remove(NBT_NEXT_PRODUCTION);
    }

    public Villager getVillager() { return villager; }
    public VillageStockpile getStockpile() { return stockpile; }
    public ProductionRecipe[] getRecipes() { return recipes; }
}
