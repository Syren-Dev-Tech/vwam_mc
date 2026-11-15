package com.github.syren_dev_tech.vwam.production;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import com.github.syren_dev_tech.vwam.villager.VillagerExperienceManager;
import com.github.syren_dev_tech.vwam.trading.TradingTimeRestriction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Manages villager production.
 * Handles selection of what to produce, ingredient checking, and stockpile updates.
 * 
 * Production flow:
 * 1. Check if villager is working
 * 2. Check if enough time has passed since last production
 * 3. Select an item to produce (round-robin, checking stock quota)
 * 4. Check if ingredients are available
 * 5. Produce item and add to stockpile
 */
public class VillagerProductionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerProductionManager");
    
    /**
     * Attempt to produce an item for a villager
     * 
     * @param villager The villager that will produce
     * @param level The game level
     * @param villageId The ID of the village
     * @param productionItems List of items this profession can produce
     * @param stockpile The village stockpile to add produced items to
     * @return The produced item, or null if nothing could be produced
     */
    public static ProductionItem attemptProduction(
        Villager villager,
        Level level,
        String villageId,
        List<ProductionItem> productionItems,
        VillageStockpile stockpile
    ) {
        // Check if villager is currently working
        if (!TradingTimeRestriction.isVillagerWorking(villager, level)) {
            return null; // Not working hours
        }
        
        // Get villager's current level
        VillagerExperienceManager xp = new VillagerExperienceManager(villager);
        int villagerLevel = xp.getLevel();
        
        // Try to produce each item in the list (round-robin order)
        for (ProductionItem prodItem : productionItems) {
            ProductionItem result = tryProduceItem(villager, level, villageId, prodItem, stockpile, villagerLevel);
            if (result != null) {
                return result; // Successfully produced
            }
        }
        
        // Nothing could be produced
        LOGGER.debug("Villager {} could not produce anything - all quotas met or no ingredients", villager.getId());
        return null;
    }
    
    /**
     * Attempt to produce a specific item
     * 
     * @return The ProductionItem if successfully produced, null otherwise
     */
    private static ProductionItem tryProduceItem(
        Villager villager,
        Level level,
        String villageId,
        ProductionItem prodItem,
        VillageStockpile stockpile,
        int villagerLevel
    ) {
        // Check if villager is high enough level
        if (villagerLevel < prodItem.getRequiredLevel()) {
            LOGGER.debug("Villager {} level {} too low for {} (requires level {})",
                villager.getId(), villagerLevel, prodItem.getItemId(), prodItem.getRequiredLevel());
            return null;
        }
        
        // Check if stock is already at or above quota (skip if it is)
        int currentStock = stockpile.getStock(prodItem.getItemId());
        if (currentStock >= prodItem.getStockQuota()) {
            LOGGER.debug("Skipping {} production - stock {} >= quota {}",
                prodItem.getItemId(), currentStock, prodItem.getStockQuota());
            return null;
        }
        
        // Get recipe ingredients
        Map<String, Integer> ingredients = RecipeManager.getRecipeIngredients(level, prodItem.getItemId());
        
        // If no recipe found, try default recipes
        if (ingredients.isEmpty()) {
            ingredients = RecipeManager.getDefaultRecipeIngredients(prodItem.getItemId());
        }
        
        // Check if stockpile has all required ingredients
        if (!stockpile.hasIngredientsFor(ingredients)) {
            LOGGER.debug("Villager {} cannot produce {} - missing ingredients",
                villager.getId(), prodItem.getItemId());
            return null;
        }
        
        // Consume ingredients and produce item
        if (stockpile.consumeIngredients(ingredients)) {
            // Successfully consumed ingredients - add produced item to stockpile
            int produced = stockpile.addStock(prodItem.getItemId(), prodItem.getProductQuantity());
            
            // Award XP to villager for production
            VillagerExperienceManager xpManager = new VillagerExperienceManager(villager);
            xpManager.addExperience(10); // 10 XP per successful production
            
            LOGGER.debug("Villager {} produced {} (now have {})",
                villager.getId(), prodItem.getItemId(), produced);
            
            return prodItem;
        }
        
        return null;
    }
    
    /**
     * Check if a villager should attempt production
     * Takes into account time since last production
     * 
     * @param villager The villager
     * @param level The level
     * @return true if enough time has passed for production attempt
     */
    public static boolean shouldAttemptProduction(Villager villager, Level level) {
        // In a full implementation, this would check:
        // 1. Is it working hours?
        // 2. Is it time for the next production cycle?
        
        return TradingTimeRestriction.isVillagerWorking(villager, level);
    }
    
    /**
     * Get production status for debugging
     */
    public static String getProductionStatusString(
        Villager villager,
        List<ProductionItem> productionItems,
        VillageStockpile stockpile
    ) {
        StringBuilder sb = new StringBuilder("Production Status {\n");
        sb.append("  Villager: ").append(villager.getId()).append("\n");
        sb.append("  Can produce:\n");
        
        for (ProductionItem item : productionItems) {
            int stock = stockpile.getStock(item.getItemId());
            boolean canProduce = stock < item.getStockQuota();
            String status = canProduce ? "✓" : "✗";
            sb.append(String.format("    %s %s (stock: %d, quota: %d)\n",
                status, item.getItemId(), stock, item.getStockQuota()));
        }
        
        sb.append("}");
        return sb.toString();
    }
}
