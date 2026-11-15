package com.github.syren_dev_tech.vwam.village;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for reputation-based effects on trading and interactions
 * Centralizes reputation checks and applies appropriate multipliers/restrictions
 */
public class ReputationEffects {
    private static final Logger LOGGER = LoggerFactory.getLogger("ReputationEffects");
    
    /**
     * Check if player can trade with a village
     * Returns false if reputation is too hostile
     */
    public static boolean canPlayerTrade(ServerPlayer player, ResourceLocation villageId) {
        var villageManager = com.github.syren_dev_tech.vwam.data.RegistryHandler.getVillageManager();
        var villageOpt = villageManager.getVillage(villageId);
        
        if (villageOpt.isEmpty()) {
            return true; // Allow if village doesn't exist
        }
        
        VillageReputation reputation = villageOpt.get().getReputation();
        float playerRep = reputation.getReputation(player.getUUID());
        
        // Can't trade if hostile
        if (playerRep < -0.5f) {
            LOGGER.debug("Player {} cannot trade - reputation {:.2f} is below hostile threshold", 
                player.getUUID(), playerRep);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get the trade discount multiplier for a player at a village
     * Applied to both buying (cost) and selling (income)
     * 
     * Examples:
     * - Reputation 1.0: 50% discount (cost 50% of normal, sell for 200% of normal)
     * - Reputation 0.7: 20% discount (cost 80% of normal, sell for 125% of normal)
     * - Reputation 0.5: 10% discount (cost 90% of normal, sell for ~111% of normal)
     * - Reputation 0.0: 0% discount (cost 100%, sell for 100%)
     */
    public static float getTradeDiscountMultiplier(ServerPlayer player, ResourceLocation villageId) {
        var villageManager = com.github.syren_dev_tech.vwam.data.RegistryHandler.getVillageManager();
        var villageOpt = villageManager.getVillage(villageId);
        
        if (villageOpt.isEmpty()) {
            return 1.0f; // No discount if village doesn't exist
        }
        
        var configOpt = com.github.syren_dev_tech.vwam.data.RegistryHandler.getInstance()
            .getVillageConfiguration(villageId);
        float discountPerStep = 0.1f; // 10% discount per 0.1 reputation
        if (configOpt.isPresent()) {
            discountPerStep = configOpt.get().getTradeDiscountPerStep();
        }
        
        VillageReputation reputation = villageOpt.get().getReputation();
        return reputation.getTradeDiscountMultiplier(player.getUUID(), discountPerStep);
    }
    
    /**
     * Get the productivity multiplier for villagers in a village
     * Applied based on Hero of the Village buff
     */
    public static float getProductivityMultiplier(ServerPlayer player, ResourceLocation villageId, 
                                                  long gameTime) {
        HeroOfVillageTracker heroTracker = HeroOfVillageTracker.getInstance();
        return heroTracker.getProductivityMultiplier(player.getUUID(), villageId, gameTime);
    }
    
    /**
     * Apply reputation gain from a trade
     */
    public static void applyTradeReputation(ServerPlayer player, ResourceLocation villageId, 
                                           long gameTime) {
        ReputationEventHandler.onPlayerTrade(player, null, villageId, gameTime);
    }
    
    /**
     * Apply reputation loss from killing a villager
     */
    public static void applyKillReputation(net.minecraft.world.entity.npc.Villager villager, 
                                          ServerPlayer player) {
        ReputationEventHandler.onVillagerKilledByPlayer(villager, player);
    }
    
    /**
     * Apply reputation gain and hero buff from raid victory
     */
    public static void applyRaidVictoryReputation(ServerPlayer player, ResourceLocation villageId, 
                                                 long gameTime) {
        ReputationEventHandler.onPlayerRaidVictory(player, villageId, gameTime);
    }
    
    /**
     * Check if villager should flee from player
     */
    public static boolean shouldVillagerFlee(net.minecraft.world.entity.npc.Villager villager, 
                                            ServerPlayer player) {
        return ReputationEventHandler.shouldVillagerFlee(villager, player);
    }
    
    /**
     * Check if iron golem should attack player
     */
    public static boolean shouldGolemAttack(ServerPlayer player, ResourceLocation villageId) {
        return ReputationEventHandler.shouldGolemAttack(player, villageId);
    }
    
    /**
     * Get human-readable reputation status for UI/commands
     */
    public static String getReputationStatus(ServerPlayer player, ResourceLocation villageId) {
        return ReputationEventHandler.getReputationInfo(player, villageId);
    }
    
    /**
     * Apply buying price multiplier to item cost
     * Lower cost with good reputation
     * 
     * Example: 10 emeralds with 50% discount = 5 emeralds
     */
    public static float applyBuyingMultiplier(float baseCost, ServerPlayer player, 
                                             ResourceLocation villageId) {
        float multiplier = getTradeDiscountMultiplier(player, villageId);
        return baseCost * multiplier;
    }
    
    /**
     * Apply selling price multiplier to item income
     * Higher income with good reputation
     * 
     * Example: 1 emerald per 10 items with 50% discount = 1 emerald per 5 items
     */
    public static float applySellingMultiplier(float baseIncome, ServerPlayer player, 
                                              ResourceLocation villageId) {
        float multiplier = getTradeDiscountMultiplier(player, villageId);
        // For selling, we want the inverse - better reputation = better deals for player
        // If buy multiplier is 0.5 (50% discount), sell multiplier should be 2.0 (sell at 2x rate)
        return baseIncome / multiplier;
    }
    
    /**
     * Format reputation value for display (-1.0 to 1.0)
     */
    public static String formatReputation(float rep) {
        if (rep >= 0.5f) {
            return String.format("§2FRIENDLY (%.2f)§r", rep);
        } else if (rep >= -0.5f) {
            return String.format("§fNEUTRAL (%.2f)§r", rep);
        } else {
            return String.format("§cHOSTILE (%.2f)§r", rep);
        }
    }
}
