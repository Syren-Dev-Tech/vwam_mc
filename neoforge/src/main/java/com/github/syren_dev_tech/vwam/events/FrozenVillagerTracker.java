package com.github.syren_dev_tech.vwam.events;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks villagers whose AI has been disabled for trading.
 * Also tracks which player is trading with each villager to prevent multiple player trades.
 * Allows re-enabling when trade screen closes.
 */
public class FrozenVillagerTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsFrozen");
    
    // Set of UUIDs of villagers that have frozen AI
    private static final Set<UUID> frozenVillagers = new HashSet<>();
    
    // Map of villager UUID -> player UUID currently trading with them
    // This prevents multiple players from trading with the same villager
    private static final Map<UUID, UUID> villagerToPlayerMap = new HashMap<>();
    
    /**
     * Mark a villager as frozen (AI disabled) and associate it with a player
     * @return true if freeze was successful, false if another player is already trading
     */
    public static boolean freezeVillager(Villager villager, Player player) {
        UUID villagerUUID = villager.getUUID();
        UUID playerUUID = player.getUUID();
        
        // Check if another player is already trading with this villager
        if (villagerToPlayerMap.containsKey(villagerUUID)) {
            UUID existingPlayer = villagerToPlayerMap.get(villagerUUID);
            if (!existingPlayer.equals(playerUUID)) {
                LOGGER.warn("Cannot freeze villager {} for player {} - already trading with player {}", 
                    villager.getId(), playerUUID, existingPlayer);
                return false;
            }
        }
        
        frozenVillagers.add(villagerUUID);
        villagerToPlayerMap.put(villagerUUID, playerUUID);
        LOGGER.debug("Froze villager {} for player {} (total frozen: {})", 
            villager.getId(), player.getName().getString(), frozenVillagers.size());
        return true;
    }
    
    /**
     * Unfreeze a villager (re-enable AI) and remove player association
     */
    public static void unfreezeVillager(Villager villager) {
        UUID villagerUUID = villager.getUUID();
        if (frozenVillagers.remove(villagerUUID)) {
            villager.setNoAi(false);
            UUID playerUUID = villagerToPlayerMap.remove(villagerUUID);
            String playerName = playerUUID != null ? playerUUID.toString() : "unknown";
            LOGGER.info("Unfroze villager {} (was trading with player {}, NoAi now: {})", 
                villager.getId(), playerName, villager.isNoAi());
        }
    }
    
    /**
     * Unfreeze a villager by UUID (for when we don't have the entity reference)
     */
    public static void unfreezeVillagerByUUID(UUID villagerUUID, Villager villager) {
        if (frozenVillagers.remove(villagerUUID)) {
            villager.setNoAi(false);
            UUID playerUUID = villagerToPlayerMap.remove(villagerUUID);
            String playerName = playerUUID != null ? playerUUID.toString() : "unknown";
            LOGGER.info("Unfroze villager {} via UUID (was trading with player {}, NoAi now: {})", 
                villager.getId(), playerName, villager.isNoAi());
        }
    }
    
    /**
     * Check if a villager is frozen
     */
    public static boolean isFrozen(Villager villager) {
        return frozenVillagers.contains(villager.getUUID());
    }
    
    /**
     * Check if a villager UUID is frozen
     */
    public static boolean isFrozen(UUID villagerUUID) {
        return frozenVillagers.contains(villagerUUID);
    }
    
    /**
     * Get the player currently trading with a villager, if any
     */
    public static UUID getPlayerTradingWith(UUID villagerUUID) {
        return villagerToPlayerMap.get(villagerUUID);
    }
    
    /**
     * Check if a specific player is trading with a villager
     */
    public static boolean isPlayerTradingWith(UUID villagerUUID, UUID playerUUID) {
        UUID tradingPlayer = villagerToPlayerMap.get(villagerUUID);
        return tradingPlayer != null && tradingPlayer.equals(playerUUID);
    }
    
    /**
     * Unfreeze all villagers associated with a disconnected player
     */
    public static void unfreezeByPlayer(UUID playerUUID, Map<UUID, Villager> villagerCache) {
        // Find all frozen villagers associated with this player
        Set<UUID> villagersToUnfreeze = new HashSet<>();
        for (Map.Entry<UUID, UUID> entry : villagerToPlayerMap.entrySet()) {
            if (entry.getValue().equals(playerUUID)) {
                villagersToUnfreeze.add(entry.getKey());
            }
        }
        
        // Unfreeze them
        for (UUID villagerUUID : villagersToUnfreeze) {
            Villager villager = villagerCache.get(villagerUUID);
            if (villager != null) {
                unfreezeVillagerByUUID(villagerUUID, villager);
                LOGGER.info("Unfroze villager {} because player {} disconnected", villagerUUID, playerUUID);
            } else {
                // Even if we don't have the entity, remove the association
                frozenVillagers.remove(villagerUUID);
                villagerToPlayerMap.remove(villagerUUID);
                LOGGER.info("Removed frozen status for villager {} (player {} disconnected, entity not found)", 
                    villagerUUID, playerUUID);
            }
        }
    }
    
    /**
     * Clear all frozen villagers (for server shutdown, etc.)
     */
    public static void clearAllFrozen() {
        LOGGER.info("Clearing {} frozen villagers", frozenVillagers.size());
        frozenVillagers.clear();
        villagerToPlayerMap.clear();
    }
    
    /**
     * Get count of frozen villagers (for debugging)
     */
    public static int getFrozenCount() {
        return frozenVillagers.size();
    }
    
    /**
     * Get all frozen villagers and their associated players (for debugging)
     */
    public static Map<UUID, UUID> getAllFrozenVillagers() {
        return new HashMap<>(villagerToPlayerMap);
    }
}
