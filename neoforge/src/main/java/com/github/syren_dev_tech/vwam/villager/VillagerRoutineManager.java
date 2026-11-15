package com.github.syren_dev_tech.vwam.villager;

import net.minecraft.world.entity.npc.Villager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manager for villager daily routine statuses
 * Handles creation, updates, and daily resets
 */
public class VillagerRoutineManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerRoutineManager");
    
    private static final VillagerRoutineManager INSTANCE = new VillagerRoutineManager();
    
    // Cache of villager routine statuses
    private final Map<Integer, VillagerRoutineStatus> routineCache = new HashMap<>();
    
    // Track last reset day to avoid duplicate resets
    private long lastResetDay = -1;
    
    private VillagerRoutineManager() {}
    
    public static VillagerRoutineManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get or create routine status for a villager
     */
    public VillagerRoutineStatus getRoutineStatus(Villager villager) {
        return routineCache.computeIfAbsent(
            villager.getId(),
            id -> new VillagerRoutineStatus(villager)
        );
    }
    
    /**
     * Called on server tick to handle daily resets
     * Should be called once per tick with the current in-game day
     * 
     * In-game time calculation:
     * - Ticks per day: 24000
     * - Current day: worldTime / 24000
     * - Time of day: worldTime % 24000
     * - Dawn (reset time): 0 (which is 6:00 AM)
     */
    public void onServerTick(long worldTime) {
        // Calculate current in-game day
        long currentDay = worldTime / 24000L;
        
        // Only reset once per day
        if (currentDay != lastResetDay) {
            LOGGER.debug("Daily reset triggered for all villagers at day {}", currentDay);
            lastResetDay = currentDay;
            
            // Reset all cached villager statuses
            for (VillagerRoutineStatus status : routineCache.values()) {
                status.checkAndResetDaily(currentDay);
            }
        }
    }
    
    /**
     * Clean up routine status for a villager (called when villager is removed)
     */
    public void removeRoutineStatus(int villagerId) {
        VillagerRoutineStatus removed = routineCache.remove(villagerId);
        if (removed != null) {
            LOGGER.debug("Removed routine status for villager {}", villagerId);
        }
    }
    
    /**
     * Clear all cached routine statuses
     */
    public void clear() {
        routineCache.clear();
        lastResetDay = -1;
        LOGGER.info("Cleared all cached routine statuses");
    }
    
    /**
     * Get stats for debugging
     */
    public String getStats() {
        int totalVillagers = routineCache.size();
        int rested = (int) routineCache.values().stream().filter(VillagerRoutineStatus::isRested).count();
        int socialized = (int) routineCache.values().stream().filter(VillagerRoutineStatus::isSocialized).count();
        int ableToWork = (int) routineCache.values().stream().filter(VillagerRoutineStatus::isAbleToWork).count();
        
        return String.format(
            "VillagerRoutineManager - Total: %d, Rested: %d, Socialized: %d, AbleToWork: %d",
            totalVillagers, rested, socialized, ableToWork
        );
    }
}
