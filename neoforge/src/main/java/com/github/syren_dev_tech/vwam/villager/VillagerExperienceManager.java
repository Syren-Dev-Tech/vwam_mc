package com.github.syren_dev_tech.vwam.villager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages villager experience and leveling using exponential progression.
 * 
 * XP requirements per level follow this formula:
 * xp_required_for_level_n = base_xp * (exponent ^ (n - 2))
 * 
 * Where:
 * - level 1 is the starting level (0 XP required)
 * - level 2 requires base_xp
 * - each subsequent level multiplies by the exponent
 */
public class VillagerExperienceManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerExperienceManager");
    
    // NBT Keys for persistence
    private static final String NBT_EXPERIENCE = "VWAM_Experience";
    private static final String NBT_LEVEL = "VWAM_Level";
    private static final String NBT_BASE_XP = "VWAM_BaseXP";
    private static final String NBT_EXPONENT = "VWAM_Exponent";
    private static final String NBT_MAX_LEVEL = "VWAM_MaxLevel";
    
    // Default values
    private static final int DEFAULT_BASE_XP = 100;
    private static final double DEFAULT_EXPONENT = 1.5;
    private static final int DEFAULT_MAX_LEVEL = 5;
    private static final int MIN_LEVEL = 1;
    
    private final Villager villager;
    private long currentExperience = 0;
    private int currentLevel = 1;
    private int baseXP = DEFAULT_BASE_XP;
    private double exponent = DEFAULT_EXPONENT;
    private int maxLevel = DEFAULT_MAX_LEVEL;
    
    /**
     * Create or retrieve experience manager for a villager
     */
    public VillagerExperienceManager(Villager villager) {
        this.villager = villager;
        loadFromNBT();
    }
    
    /**
     * Load experience data from NBT
     */
    private void loadFromNBT() {
        CompoundTag tag = villager.getPersistentData();
        this.currentExperience = tag.getLong(NBT_EXPERIENCE);
        this.currentLevel = tag.getInt(NBT_LEVEL);
        this.baseXP = tag.getInt(NBT_BASE_XP);
        this.exponent = tag.getDouble(NBT_EXPONENT);
        this.maxLevel = tag.getInt(NBT_MAX_LEVEL);
        
        // Set defaults if not present
        if (this.currentLevel == 0) {
            this.currentLevel = MIN_LEVEL;
        }
        if (this.baseXP == 0) {
            this.baseXP = DEFAULT_BASE_XP;
        }
        if (this.exponent == 0.0) {
            this.exponent = DEFAULT_EXPONENT;
        }
        if (this.maxLevel == 0) {
            this.maxLevel = DEFAULT_MAX_LEVEL;
        }
    }
    
    /**
     * Save experience data to NBT
     */
    private void saveToNBT() {
        CompoundTag tag = villager.getPersistentData();
        tag.putLong(NBT_EXPERIENCE, currentExperience);
        tag.putInt(NBT_LEVEL, currentLevel);
        tag.putInt(NBT_BASE_XP, baseXP);
        tag.putDouble(NBT_EXPONENT, exponent);
        tag.putInt(NBT_MAX_LEVEL, maxLevel);
    }
    
    /**
     * Initialize XP progression with custom values
     */
    public void initializeProgression(int baseXP, double exponent, int maxLevel) {
        this.baseXP = Math.max(1, baseXP);
        this.exponent = Math.max(1.0, exponent);
        this.maxLevel = Math.max(1, maxLevel);
        
        // Validate current level doesn't exceed max
        if (this.currentLevel > this.maxLevel) {
            this.currentLevel = this.maxLevel;
        }
        
        saveToNBT();
        LOGGER.debug("Initialized XP progression for villager {}: baseXP={}, exponent={}, maxLevel={}", 
            villager.getId(), this.baseXP, this.exponent, this.maxLevel);
    }
    
    /**
     * Calculate XP required to reach the next level from the specified level
     */
    private long calculateXPForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        if (level > maxLevel) {
            return Long.MAX_VALUE;
        }
        
        // Formula: base_xp * (exponent ^ (level - 2))
        double xpRequired = baseXP * Math.pow(exponent, level - 2);
        return Math.round(xpRequired);
    }
    
    /**
     * Calculate total XP required to reach a specific level from level 1
     */
    private long calculateTotalXPForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        
        long totalXP = 0;
        for (int i = 2; i <= level; i++) {
            totalXP += calculateXPForLevel(i);
        }
        return totalXP;
    }
    
    /**
     * Add experience and handle level-ups
     * @param amount The amount of experience to add
     * @return The number of levels gained (0 if no level-up occurred)
     */
    public int addExperience(long amount) {
        if (currentLevel >= maxLevel) {
            LOGGER.debug("Villager {} is already at max level {}", villager.getId(), maxLevel);
            return 0;
        }
        
        if (amount <= 0) {
            return 0;
        }
        
        int levelsBefore = currentLevel;
        currentExperience += amount;
        
        // Check for level ups
        while (currentLevel < maxLevel) {
            long xpForNextLevel = calculateXPForLevel(currentLevel + 1);
            if (currentExperience >= xpForNextLevel) {
                currentExperience -= xpForNextLevel;
                currentLevel++;
                LOGGER.debug("Villager {} leveled up to level {} (XP progress: {}/{})", 
                    villager.getId(), currentLevel, currentExperience, 
                    calculateXPForLevel(currentLevel + 1));
            } else {
                break;
            }
        }
        
        int levelsGained = currentLevel - levelsBefore;
        if (levelsGained > 0) {
            saveToNBT();
        }
        
        return levelsGained;
    }
    
    /**
     * Get current level
     */
    public int getLevel() {
        return currentLevel;
    }
    
    /**
     * Get current experience points
     */
    public long getExperience() {
        return currentExperience;
    }
    
    /**
     * Get XP required to reach the next level
     */
    public long getXPForNextLevel() {
        if (currentLevel >= maxLevel) {
            return 0;
        }
        return calculateXPForLevel(currentLevel + 1);
    }
    
    /**
     * Get progress towards next level as a percentage (0.0 to 1.0)
     */
    public double getProgressToNextLevel() {
        if (currentLevel >= maxLevel) {
            return 1.0;
        }
        
        long xpForNext = getXPForNextLevel();
        if (xpForNext <= 0) {
            return 1.0;
        }
        
        return Math.min(1.0, (double) currentExperience / xpForNext);
    }
    
    /**
     * Check if villager can produce items at a specific level requirement
     */
    public boolean canProduceAtLevel(int requiredLevel) {
        return currentLevel >= requiredLevel;
    }
    
    /**
     * Check if villager is at max level
     */
    public boolean isMaxLevel() {
        return currentLevel >= maxLevel;
    }
    
    /**
     * Get max level
     */
    public int getMaxLevel() {
        return maxLevel;
    }
    
    /**
     * Get base XP value
     */
    public int getBaseXP() {
        return baseXP;
    }
    
    /**
     * Get exponent value
     */
    public double getExponent() {
        return exponent;
    }
    
    /**
     * Get a debug string with current status
     */
    public String getStatusString() {
        long xpForNext = getXPForNextLevel();
        if (currentLevel >= maxLevel) {
            return String.format("Level: %d/%d (MAX)", currentLevel, maxLevel);
        }
        return String.format("Level: %d/%d | XP: %d/%d (%.1f%%)", 
            currentLevel, maxLevel, currentExperience, xpForNext, 
            getProgressToNextLevel() * 100);
    }
}
