package com.github.syren_dev_tech.vwam.village;

import com.github.syren_dev_tech.vwam.data.VillageConfiguration;
import com.github.syren_dev_tech.vwam.data.RegistryHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler for reputation changes
 * Tracks:
 * - Killing villagers
 * - Trading with villagers  
 * - Defeating raids (hero of the village)
 */
public class ReputationEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("ReputationEventHandler");
    
    /**
     * Called when a villager dies
     * Updates reputation based on whether it was part of a village
     */
    @SubscribeEvent
    public void onVillagerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Villager villager)) {
            return;
        }
        
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            onVillagerKilledByPlayer(villager, player);
        }
    }
    
    /**
     * Static helper for when a player kills a villager
     */
    public static void onVillagerKilledByPlayer(Villager villager, ServerPlayer player) {
        // Check if villager is part of a village
        if (!VillageDetector.isPartOfVillage(villager)) {
            LOGGER.info("Player {} killed unaffiliated villager, no reputation change", 
                player.getUUID());
            return;
        }
        
        String villageIdStr = VillageDetector.getVillageId(villager);
        ResourceLocation villageId = ResourceLocation.fromNamespaceAndPath("vwam", villageIdStr);
        
        VillageManager villageManager = RegistryHandler.getVillageManager();
        var villageOpt = villageManager.getVillage(villageId);
        
        if (villageOpt.isEmpty()) {
            LOGGER.warn("Village {} not found in manager", villageId);
            return;
        }
        
        VillageManager.Village village = villageOpt.get();
        VillageReputation reputation = village.getReputation();
        
        // Get configuration for reputation loss amount
        var configOpt = RegistryHandler.getInstance().getVillageConfiguration(villageId);
        float reputationLoss = -0.1f; // Default penalty
        if (configOpt.isPresent()) {
            // Negative reputation gain for killing
            reputationLoss = -0.1f; // Could be configurable
        }
        
        reputation.addReputation(player.getUUID(), reputationLoss);
        float newRep = reputation.getReputation(player.getUUID());
        
        LOGGER.info("Player {} killed villager from village {}. Reputation: {} -> {}", 
            player.getName().getString(), villageId, newRep + reputationLoss, newRep);
        
        // Check if reputation is now hostile
        if (reputation.isHostile(player.getUUID())) {
            LOGGER.info("Player {} is now HOSTILE to village {}", 
                player.getName().getString(), villageId);
        }
    }
    
    /**
     * Called when a player trades with a villager
     * Increases reputation
     */
    public static void onPlayerTrade(ServerPlayer player, Villager villager, 
                                     ResourceLocation villageId, long gameTime) {
        VillageManager villageManager = RegistryHandler.getVillageManager();
        var villageOpt = villageManager.getVillage(villageId);
        
        if (villageOpt.isEmpty()) {
            LOGGER.warn("Village {} not found in manager when processing trade", villageId);
            return;
        }
        
        VillageManager.Village village = villageOpt.get();
        VillageReputation reputation = village.getReputation();
        
        // Get configuration for reputation gain
        var configOpt = RegistryHandler.getInstance().getVillageConfiguration(villageId);
        float reputationGain = 0.05f; // Default reputation per trade
        if (configOpt.isPresent()) {
            reputationGain = configOpt.get().getTradeReputationGain();
        }
        
        float oldRep = reputation.getReputation(player.getUUID());
        reputation.addReputation(player.getUUID(), reputationGain);
        reputation.recordTrade(player.getUUID(), gameTime);
        float newRep = reputation.getReputation(player.getUUID());
        
        LOGGER.info("Player {} traded with villager from village {}. Reputation: {} -> {}", 
            player.getName().getString(), villageId, oldRep, newRep);
        
        // Check if player reached friendly threshold
        if (oldRep < 0.5f && newRep >= 0.5f) {
            LOGGER.info("Player {} reached FRIENDLY status with village {}", 
                player.getName().getString(), villageId);
        }
    }
    
    /**
     * Called when player completes a raid and gets "Hero of the Village" buff
     * Applies productivity boost for this village
     */
    public static void onPlayerRaidVictory(ServerPlayer player, ResourceLocation villageId, 
                                          long gameTime) {
        VillageManager villageManager = RegistryHandler.getVillageManager();
        var villageOpt = villageManager.getVillage(villageId);
        
        if (villageOpt.isEmpty()) {
            LOGGER.warn("Village {} not found when processing raid victory", villageId);
            return;
        }
        
        VillageManager.Village village = villageOpt.get();
        VillageReputation reputation = village.getReputation();
        
        // Get configuration for raid reputation gain
        var configOpt = RegistryHandler.getInstance().getVillageConfiguration(villageId);
        float raidReputationGain = 0.2f; // Default reputation per successful raid
        if (configOpt.isPresent()) {
            // Use trade reputation gain as proxy for raid gains
            raidReputationGain = configOpt.get().getTradeReputationGain() * 4; // 4x trade gain
        }
        
        float oldRep = reputation.getReputation(player.getUUID());
        reputation.addReputation(player.getUUID(), raidReputationGain);
        float newRep = reputation.getReputation(player.getUUID());
        
        // Apply hero of the village buff
        HeroOfVillageTracker heroTracker = HeroOfVillageTracker.getInstance();
        heroTracker.applyHeroBuff(player.getUUID(), villageId, gameTime);
        
        LOGGER.info("Player {} defeated raid in village {}. Reputation: {} -> {}. Hero buff applied for 24h", 
            player.getName().getString(), villageId, oldRep, newRep);
    }
    
    /**
     * Check if villager should flee from player based on reputation
     */
    public static boolean shouldVillagerFlee(Villager villager, ServerPlayer player) {
        if (!VillageDetector.isPartOfVillage(villager)) {
            return false;
        }
        
        String villageIdStr = VillageDetector.getVillageId(villager);
        ResourceLocation villageId = ResourceLocation.fromNamespaceAndPath("vwam", villageIdStr);
        
        VillageManager villageManager = RegistryHandler.getVillageManager();
        var villageOpt = villageManager.getVillage(villageId);
        
        if (villageOpt.isEmpty()) {
            return false;
        }
        
        VillageManager.Village village = villageOpt.get();
        VillageReputation reputation = village.getReputation();
        
        return reputation.shouldVillagerFlee(player.getUUID());
    }
    
    /**
     * Check if iron golem should attack player based on reputation
     */
    public static boolean shouldGolemAttack(ServerPlayer player, ResourceLocation villageId) {
        VillageManager villageManager = RegistryHandler.getVillageManager();
        var villageOpt = villageManager.getVillage(villageId);
        
        if (villageOpt.isEmpty()) {
            return false;
        }
        
        VillageManager.Village village = villageOpt.get();
        VillageReputation reputation = village.getReputation();
        
        return reputation.shouldGolemAttack(player.getUUID());
    }
    
    /**
     * Get reputation info for player/village
     */
    public static String getReputationInfo(ServerPlayer player, ResourceLocation villageId) {
        VillageManager villageManager = RegistryHandler.getVillageManager();
        var villageOpt = villageManager.getVillage(villageId);
        
        if (villageOpt.isEmpty()) {
            return "No village data";
        }
        
        VillageManager.Village village = villageOpt.get();
        VillageReputation reputation = village.getReputation();
        float rep = reputation.getReputation(player.getUUID());
        
        String status = "HOSTILE";
        if (reputation.isFriendly(player.getUUID())) {
            status = "FRIENDLY";
        } else if (rep >= -0.5f) {
            status = "NEUTRAL";
        }
        
        float discount = 1.0f - reputation.getTradeDiscountMultiplier(player.getUUID(), 0.1f);
        
        return String.format("Village: %s | Reputation: %.2f | Status: %s | Trade Discount: %.0f%%", 
            villageId, rep, status, discount * 100);
    }
}
