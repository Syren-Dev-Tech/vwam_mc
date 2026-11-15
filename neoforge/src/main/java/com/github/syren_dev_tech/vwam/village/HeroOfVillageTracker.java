package com.github.syren_dev_tech.vwam.village;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages Hero of the Village buff effects for players
 * - 2x productivity multiplier for villagers
 * - 24 hour duration
 * - Only applies to village that was part of raid
 */
public class HeroOfVillageTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger("HeroOfVillageTracker");
    
    private static final HeroOfVillageTracker INSTANCE = new HeroOfVillageTracker();
    
    // Track when player received hero buff and which village it applies to
    private static class HeroBuff {
        long startTime;
        ResourceLocation villageId;
        
        HeroBuff(long startTime, ResourceLocation villageId) {
            this.startTime = startTime;
            this.villageId = villageId;
        }
        
        boolean isExpired(long currentTime, long durationTicks) {
            return currentTime - startTime >= durationTicks;
        }
    }
    
    private final Map<UUID, HeroBuff> activeBuffs = new HashMap<>();
    private static final long BUFF_DURATION_TICKS = 24 * 60 * 60 * 20L; // 24 hours in ticks
    
    private HeroOfVillageTracker() {}
    
    public static HeroOfVillageTracker getInstance() {
        return INSTANCE;
    }
    
    /**
     * Apply hero of the village buff to a player for a specific village
     */
    public void applyHeroBuff(UUID playerId, ResourceLocation villageId, long gameTime) {
        activeBuffs.put(playerId, new HeroBuff(gameTime, villageId));
        LOGGER.info("Applied hero buff to player {} for village {} at time {}", 
            playerId, villageId, gameTime);
    }
    
    /**
     * Check if player has active hero buff for this village
     */
    public boolean hasActiveBuff(UUID playerId, ResourceLocation villageId, long gameTime) {
        HeroBuff buff = activeBuffs.get(playerId);
        if (buff == null) {
            return false;
        }
        
        // Check if buff is for this village and hasn't expired
        if (!buff.villageId.equals(villageId)) {
            return false;
        }
        
        if (buff.isExpired(gameTime, BUFF_DURATION_TICKS)) {
            activeBuffs.remove(playerId);
            LOGGER.info("Hero buff expired for player {} on village {}", playerId, villageId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get productivity multiplier for this player/village (1.0 or 2.0)
     */
    public float getProductivityMultiplier(UUID playerId, ResourceLocation villageId, long gameTime) {
        return hasActiveBuff(playerId, villageId, gameTime) ? 2.0f : 1.0f;
    }
    
    /**
     * Remove buff when it expires
     */
    public void removeBuff(UUID playerId) {
        HeroBuff removed = activeBuffs.remove(playerId);
        if (removed != null) {
            LOGGER.debug("Removed hero buff for player {}", playerId);
        }
    }
    
    /**
     * Get remaining time on buff in ticks
     */
    public long getRemainingTime(UUID playerId, long gameTime) {
        HeroBuff buff = activeBuffs.get(playerId);
        if (buff == null) {
            return 0;
        }
        
        long elapsed = gameTime - buff.startTime;
        long remaining = BUFF_DURATION_TICKS - elapsed;
        return Math.max(0, remaining);
    }
    
    /**
     * Clear all buffs (server shutdown, etc.)
     */
    public void clear() {
        activeBuffs.clear();
    }
    
    /**
     * Get debug stats
     */
    public String getStats() {
        return String.format("HeroOfVillageTracker - Active buffs: %d", activeBuffs.size());
    }
}
