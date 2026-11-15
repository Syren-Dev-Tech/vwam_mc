package com.github.syren_dev_tech.vwam.village.reputation;

import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager for player reputations across all villages.
 * Caches reputation objects to avoid repeated NBT lookups.
 */
public class PlayerReputationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("PlayerReputationManager");
    
    // Cache: Player UUID -> (Village ID -> Reputation)
    private static final Map<String, Map<String, PlayerVillageReputation>> cache = new HashMap<>();
    
    /**
     * Get or create reputation tracker for player with village
     */
    public static PlayerVillageReputation getOrCreateReputation(Player player, String villageId) {
        String playerId = player.getUUID().toString();
        
        Map<String, PlayerVillageReputation> playerReputations = 
            cache.computeIfAbsent(playerId, k -> new HashMap<>());
        
        return playerReputations.computeIfAbsent(villageId, k -> 
            new PlayerVillageReputation(player, villageId));
    }
    
    /**
     * Apply a reputation event to player for a village
     */
    public static double applyReputationEvent(
        Player player, 
        String villageId, 
        ReputationEventType eventType, 
        ReputationEventConfig config) {
        
        PlayerVillageReputation reputation = getOrCreateReputation(player, villageId);
        double change = reputation.applyEvent(eventType, config);
        
        LOGGER.debug("Applied {} to player {} (village {}): change={}", 
            eventType.getId(), player.getName().getString(), villageId, change);
        
        return change;
    }
    
    /**
     * Apply a reputation event with custom change value
     */
    public static double applyReputationEvent(
        Player player, 
        String villageId, 
        ReputationEventType eventType, 
        double customChange) {
        
        PlayerVillageReputation reputation = getOrCreateReputation(player, villageId);
        double change = reputation.applyEvent(eventType, customChange);
        
        return change;
    }
    
    /**
     * Get player's reputation with a village
     */
    public static double getReputation(Player player, String villageId) {
        PlayerVillageReputation reputation = getOrCreateReputation(player, villageId);
        return reputation.getReputation();
    }
    
    /**
     * Set player's reputation with a village
     */
    public static void setReputation(Player player, String villageId, double value) {
        PlayerVillageReputation reputation = getOrCreateReputation(player, villageId);
        reputation.setReputation(value);
    }
    
    /**
     * Check if player can trade with a village
     */
    public static boolean canTrade(Player player, String villageId) {
        PlayerVillageReputation reputation = getOrCreateReputation(player, villageId);
        return reputation.canTrade();
    }
    
    /**
     * Get trade price modifier for player with village
     */
    public static double getTradePriceModifier(Player player, String villageId) {
        PlayerVillageReputation reputation = getOrCreateReputation(player, villageId);
        return reputation.getTradePriceModifier();
    }
    
    /**
     * Check if player is hostile to a village
     */
    public static boolean isHostile(Player player, String villageId) {
        PlayerVillageReputation reputation = getOrCreateReputation(player, villageId);
        return reputation.isHostile();
    }
    
    /**
     * Clear cache for a player (e.g., on logout)
     */
    public static void clearPlayerCache(Player player) {
        String playerId = player.getUUID().toString();
        cache.remove(playerId);
        LOGGER.debug("Cleared reputation cache for player {}", player.getName().getString());
    }
    
    /**
     * Get all reputations for a player (for debugging/display)
     */
    public static Map<String, PlayerVillageReputation> getAllReputations(Player player) {
        String playerId = player.getUUID().toString();
        return new HashMap<>(cache.getOrDefault(playerId, new HashMap<>()));
    }
    
    /**
     * Get debug string with all player reputations
     */
    public static String getPlayerReputationDebug(Player player) {
        StringBuilder sb = new StringBuilder("Player Reputations: " + player.getName().getString() + "\n");
        Map<String, PlayerVillageReputation> reputations = getAllReputations(player);
        
        if (reputations.isEmpty()) {
            sb.append("  No tracked villages");
        } else {
            for (Map.Entry<String, PlayerVillageReputation> entry : reputations.entrySet()) {
                sb.append("  ").append(entry.getValue().getStatusString()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Clear all cache (for testing or reload)
     */
    public static void clearAllCache() {
        cache.clear();
        LOGGER.info("Cleared all reputation caches");
    }
}
