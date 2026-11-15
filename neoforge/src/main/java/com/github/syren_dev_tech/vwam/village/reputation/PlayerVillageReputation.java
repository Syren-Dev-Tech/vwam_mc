package com.github.syren_dev_tech.vwam.village.reputation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks a player's reputation with a specific village.
 * 
 * Reputation ranges from -1.0 (hostile) to 1.0 (friendly):
 * - -1.0 to -0.5: Hostile - villagers flee, iron golems attack
 * - -0.5 to 0.0: Unfriendly - reduced trades, no services
 * - 0.0: Neutral - normal trading
 * - 0.0 to 0.5: Friendly - basic services available
 * - 0.5 to 1.0: Very Friendly - trade discounts up to 50%
 * - 1.0: Beloved - maximum benefits and discounts
 * 
 * Reputation is stored in player NBT data with a village-specific key.
 */
public class PlayerVillageReputation {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerVillageReputation");
    
    // NBT key prefix for village reputation
    private static final String NBT_PREFIX = "VWAM_Rep_";
    
    private final Player player;
    private final String villageId;
    private double reputation = 0.0; // Default: neutral
    private long lastChanged = 0;
    
    /**
     * Create or load reputation tracker for player with village
     */
    public PlayerVillageReputation(Player player, String villageId) {
        this.player = player;
        this.villageId = villageId;
        loadFromNBT();
    }
    
    /**
     * Load reputation from player NBT data
     */
    private void loadFromNBT() {
        CompoundTag tag = player.getPersistentData();
        String key = getNBTKey();
        
        if (tag.contains(key)) {
            this.reputation = tag.getDouble(key);
            this.lastChanged = tag.getLong(getNBTKeyLastChanged());
            
            // Validate and clamp
            this.reputation = clampReputation(this.reputation);
        } else {
            this.reputation = 0.0;
            this.lastChanged = 0;
        }
    }
    
    /**
     * Save reputation to player NBT data
     */
    private void saveToNBT() {
        CompoundTag tag = player.getPersistentData();
        tag.putDouble(getNBTKey(), reputation);
        tag.putLong(getNBTKeyLastChanged(), lastChanged);
    }
    
    /**
     * Get the NBT key for this village's reputation
     */
    private String getNBTKey() {
        return NBT_PREFIX + villageId;
    }
    
    /**
     * Get the NBT key for last changed timestamp
     */
    private String getNBTKeyLastChanged() {
        return NBT_PREFIX + villageId + "_LastChanged";
    }
    
    /**
     * Clamp reputation to valid range [-1, 1]
     */
    private static double clampReputation(double value) {
        return Math.max(-1.0, Math.min(1.0, value));
    }
    
    /**
     * Apply a reputation event and update player reputation
     * Returns the actual change applied (after clamping)
     */
    public double applyEvent(ReputationEventType eventType, double customChange) {
        double change = customChange;
        double newReputation = clampReputation(reputation + change);
        double actualChange = newReputation - reputation;
        
        reputation = newReputation;
        lastChanged = System.currentTimeMillis();
        saveToNBT();
        
        LOGGER.debug("Applied {} to player {}: {} → {} (change: {})", 
            eventType.getId(), player.getName().getString(), 
            reputation - actualChange, reputation, actualChange);
        
        return actualChange;
    }
    
    /**
     * Apply a reputation event with configured value
     */
    public double applyEvent(ReputationEventType eventType, ReputationEventConfig config) {
        double change = config.getEventChange(eventType);
        return applyEvent(eventType, change);
    }
    
    /**
     * Get current reputation value
     */
    public double getReputation() {
        return reputation;
    }
    
    /**
     * Set reputation directly
     */
    public void setReputation(double value) {
        reputation = clampReputation(value);
        lastChanged = System.currentTimeMillis();
        saveToNBT();
        
        LOGGER.debug("Set reputation for player {} with village {}: {}", 
            player.getName().getString(), villageId, reputation);
    }
    
    /**
     * Check if player has positive reputation
     */
    public boolean isPositive() {
        return reputation > 0.0;
    }
    
    /**
     * Check if player has negative reputation
     */
    public boolean isNegative() {
        return reputation < 0.0;
    }
    
    /**
     * Check if player is neutral
     */
    public boolean isNeutral() {
        return reputation == 0.0;
    }
    
    /**
     * Check if player is hostile (should be attacked by golems)
     */
    public boolean isHostile() {
        return reputation < -0.5;
    }
    
    /**
     * Check if player can trade with this village
     */
    public boolean canTrade() {
        return reputation >= -0.5;
    }
    
    /**
     * Check if player can access special services (heal, repair, etc.)
     */
    public boolean canAccessServices() {
        return reputation > 0.0;
    }
    
    /**
     * Get trade price modifier based on reputation
     * Returns multiplier: 1.0 = normal, 0.5 = 50% discount, 1.5 = 50% markup
     * 
     * Formula: For positive reputation above 0.5, each 0.1 gives 10% discount
     * At 1.0: 50% discount (0.5x multiplier)
     * At 0.9: 40% discount (0.6x multiplier)
     * etc.
     */
    public double getTradePriceModifier() {
        if (reputation <= 0.5) {
            return 1.0; // No discount below 0.5
        }
        
        // For each 0.1 above 0.5, apply 10% discount
        // Reputation 0.6 = 10% discount = 0.9x multiplier
        // Reputation 0.7 = 20% discount = 0.8x multiplier
        // Reputation 1.0 = 50% discount = 0.5x multiplier
        double discountSteps = (reputation - 0.5) / 0.1;
        double discountPercent = Math.min(0.5, discountSteps * 0.1);
        return 1.0 - discountPercent;
    }
    
    /**
     * Get reputation level as a readable string
     */
    public String getReputationLevel() {
        if (reputation >= 0.9) {
            return "Beloved";
        } else if (reputation >= 0.7) {
            return "Very Friendly";
        } else if (reputation >= 0.5) {
            return "Friendly";
        } else if (reputation > 0.0) {
            return "Agreeable";
        } else if (reputation == 0.0) {
            return "Neutral";
        } else if (reputation > -0.5) {
            return "Unfriendly";
        } else {
            return "Hostile";
        }
    }
    
    /**
     * Get time since reputation was last changed (in milliseconds)
     */
    public long getTimeSinceLastChange() {
        if (lastChanged == 0) {
            return Long.MAX_VALUE;
        }
        return System.currentTimeMillis() - lastChanged;
    }
    
    /**
     * Get debug string with reputation info
     */
    public String getStatusString() {
        return String.format("Village %s: %.2f (%s) - Modifier: %.2fx - Can Trade: %s", 
            villageId, reputation, getReputationLevel(), getTradePriceModifier(), canTrade());
    }
}
