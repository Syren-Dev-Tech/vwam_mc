package com.github.syren_dev_tech.vwam.villager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for villager XP progression system.
 * Loaded from the xp_progression section of workshop JSON definitions.
 */
public class VillagerXPConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerXPConfig");
    
    // Default values
    public static final int DEFAULT_BASE_XP = 100;
    public static final double DEFAULT_EXPONENT = 1.5;
    public static final int DEFAULT_MAX_LEVEL = 5;
    
    private final int baseXP;
    private final double exponent;
    private final int maxLevel;
    private final String profession;
    
    /**
     * Create XP config with explicit values
     */
    public VillagerXPConfig(String profession, int baseXP, double exponent, int maxLevel) {
        this.profession = profession;
        this.baseXP = Math.max(1, baseXP);
        this.exponent = Math.max(1.0, exponent);
        this.maxLevel = Math.max(1, maxLevel);
    }
    
    /**
     * Create XP config with default values for a profession
     */
    public VillagerXPConfig(String profession) {
        this(profession, DEFAULT_BASE_XP, DEFAULT_EXPONENT, DEFAULT_MAX_LEVEL);
    }
    
    /**
     * Load XP config from JSON object (xp_progression section)
     * @param profession The profession name (for logging)
     * @param jsonElement The xp_progression JSON element
     * @return VillagerXPConfig with loaded or default values
     */
    public static VillagerXPConfig fromJson(String profession, JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            LOGGER.debug("No XP progression config found for profession '{}', using defaults", profession);
            return new VillagerXPConfig(profession);
        }
        
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        
        int baseXP = DEFAULT_BASE_XP;
        double exponent = DEFAULT_EXPONENT;
        int maxLevel = DEFAULT_MAX_LEVEL;
        
        if (jsonObject.has("base_xp")) {
            try {
                baseXP = jsonObject.get("base_xp").getAsInt();
                if (baseXP < 1) {
                    LOGGER.warn("Invalid base_xp value {} for profession '{}', using default", baseXP, profession);
                    baseXP = DEFAULT_BASE_XP;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to parse base_xp for profession '{}': {}", profession, e.getMessage());
            }
        }
        
        if (jsonObject.has("exponent")) {
            try {
                exponent = jsonObject.get("exponent").getAsDouble();
                if (exponent < 1.0) {
                    LOGGER.warn("Invalid exponent value {} for profession '{}', using default", exponent, profession);
                    exponent = DEFAULT_EXPONENT;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to parse exponent for profession '{}': {}", profession, e.getMessage());
            }
        }
        
        if (jsonObject.has("max_level")) {
            try {
                maxLevel = jsonObject.get("max_level").getAsInt();
                if (maxLevel < 1) {
                    LOGGER.warn("Invalid max_level value {} for profession '{}', using default", maxLevel, profession);
                    maxLevel = DEFAULT_MAX_LEVEL;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to parse max_level for profession '{}': {}", profession, e.getMessage());
            }
        }
        
        LOGGER.debug("Loaded XP progression for profession '{}': baseXP={}, exponent={}, maxLevel={}", 
            profession, baseXP, exponent, maxLevel);
        
        return new VillagerXPConfig(profession, baseXP, exponent, maxLevel);
    }
    
    // Getters
    public String getProfession() {
        return profession;
    }
    
    public int getBaseXP() {
        return baseXP;
    }
    
    public double getExponent() {
        return exponent;
    }
    
    public int getMaxLevel() {
        return maxLevel;
    }
    
    /**
     * Get a debug string representation
     */
    public String getConfigString() {
        return String.format("XPConfig{profession='%s', baseXP=%d, exponent=%.2f, maxLevel=%d}", 
            profession, baseXP, exponent, maxLevel);
    }
}
