package com.github.syren_dev_tech.vwam.village;

import net.minecraft.resources.ResourceLocation;
import java.util.*;

/**
 * Manages player reputation with villages and applies reputation-based effects.
 */
public class VillageReputation {
    private final ResourceLocation villageId;
    private final Map<UUID, Float> playerReputation;  // Player UUID -> reputation score
    private final Map<UUID, Long> lastTradeTime;      // Track trade timing for cooldowns

    private final float initialReputation;
    private final float maxReputation;
    private final float minReputation;
    private final float hostileThreshold;
    private final float friendlyThreshold;

    public VillageReputation(
            ResourceLocation villageId,
            float initialReputation,
            float maxReputation,
            float minReputation,
            float hostileThreshold,
            float friendlyThreshold
    ) {
        this.villageId = villageId;
        this.playerReputation = new HashMap<>();
        this.lastTradeTime = new HashMap<>();
        this.initialReputation = initialReputation;
        this.maxReputation = maxReputation;
        this.minReputation = minReputation;
        this.hostileThreshold = hostileThreshold;
        this.friendlyThreshold = friendlyThreshold;
    }

    /**
     * Get or initialize a player's reputation
     */
    public float getReputation(UUID playerId) {
        return playerReputation.getOrDefault(playerId, initialReputation);
    }

    /**
     * Add or subtract reputation
     */
    public void addReputation(UUID playerId, float amount) {
        float current = getReputation(playerId);
        float newRep = Math.max(minReputation, Math.min(maxReputation, current + amount));
        playerReputation.put(playerId, newRep);
    }

    /**
     * Set reputation directly
     */
    public void setReputation(UUID playerId, float amount) {
        float clamped = Math.max(minReputation, Math.min(maxReputation, amount));
        playerReputation.put(playerId, clamped);
    }

    /**
     * Check if player is hostile to village
     */
    public boolean isHostile(UUID playerId) {
        return getReputation(playerId) < hostileThreshold;
    }

    /**
     * Check if player is friendly to village
     */
    public boolean isFriendly(UUID playerId) {
        return getReputation(playerId) >= friendlyThreshold;
    }

    /**
     * Calculate trade price discount based on reputation
     * Returns a multiplier (0.5 to 1.0)
     * Discount tier increases by 10% for each 0.1 reputation
     * At exactly friendly threshold (0.5): 10% discount
     * At 0.6 or higher in [0.6,0.7): 20% discount
     * At 0.7 or higher in [0.7,0.8): 30% discount
     * etc., capped at 50% maximum
     */
    public float getTradeDiscountMultiplier(UUID playerId, float discountPerStep) {
        float rep = getReputation(playerId);

        if (rep < friendlyThreshold) {
            return 1.0f; // No discount below friendly threshold
        }

        // Calculate steps: use rounding to handle floating point precision
        // This gives us: 0.5→1 step, 0.6→2 steps, 0.7→3 steps, etc.
        float distanceAbove = rep - friendlyThreshold;
        float stepsAboveThreshold = 1.0f + Math.round(distanceAbove * 10);
        
        float discountPercentage = stepsAboveThreshold * discountPerStep;

        // Clamp at 50% max discount
        discountPercentage = Math.min(0.5f, discountPercentage);

        return 1.0f - discountPercentage;
    }

    /**
     * Check if villager should flee from player
     */
    public boolean shouldVillagerFlee(UUID playerId) {
        return getReputation(playerId) <= (hostileThreshold / 2);
    }

    /**
     * Check if iron golem should attack player
     */
    public boolean shouldGolemAttack(UUID playerId) {
        return getReputation(playerId) < hostileThreshold;
    }

    /**
     * Record a trade with this player
     */
    public void recordTrade(UUID playerId, long gameTime) {
        lastTradeTime.put(playerId, gameTime);
    }

    /**
     * Get all players with their reputation
     */
    public Map<UUID, Float> getAllReputation() {
        return new HashMap<>(playerReputation);
    }

    public ResourceLocation getVillageId() {
        return villageId;
    }
}
