package com.github.syren_dev_tech.vwam.village.reputation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for reputation events loaded from data pack JSON files.
 * Defines how much reputation each event type should add or subtract.
 * 
 * Reputation values can be:
 * - Positive (0.0 to 1.0): Increase player reputation with village
 * - Negative (-1.0 to 0.0): Decrease player reputation with village
 * - Clamped to [-1.0, 1.0] range when applied
 */
public class ReputationEventConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("ReputationEventConfig");
    
    // Default reputation changes for each event
    public static final double DEFAULT_REPUTATION_CHANGE = 0.05; // 5% of reputation range
    
    // Default values for specific events
    private static final Map<ReputationEventType, Double> DEFAULTS = new HashMap<>();
    
    static {
        // Negative events (reputation loss)
        DEFAULTS.put(ReputationEventType.VILLAGER_KILLED, -0.20);      // Significant penalty
        DEFAULTS.put(ReputationEventType.VILLAGER_BED_STOLEN, -0.10);  // Moderate penalty
        DEFAULTS.put(ReputationEventType.CHEST_LOOTED, -0.05);         // Minor penalty
        DEFAULTS.put(ReputationEventType.BLOCK_BROKEN, -0.02);         // Very minor penalty
        DEFAULTS.put(ReputationEventType.VILLAGER_HIT, -0.03);         // Minor penalty
        DEFAULTS.put(ReputationEventType.WORKSTATION_DESTROYED, -0.10); // Moderate penalty
        DEFAULTS.put(ReputationEventType.WORKSHOP_INVALIDATED, -0.05); // Minor penalty
        
        // Positive events (reputation gain)
        DEFAULTS.put(ReputationEventType.TRADE_COMPLETED, 0.01);       // Very small per trade
        DEFAULTS.put(ReputationEventType.RAID_DEFEATED, 0.20);         // Significant bonus
        DEFAULTS.put(ReputationEventType.RESOURCE_DONATED, 0.10);      // Good bonus
        DEFAULTS.put(ReputationEventType.TASK_COMPLETED, 0.15);        // Good bonus
        DEFAULTS.put(ReputationEventType.WORKSTATION_REPAIRED, 0.10);  // Good bonus
        DEFAULTS.put(ReputationEventType.EMERGENCY_AID_PROVIDED, 0.15); // Good bonus
    }
    
    private final Map<ReputationEventType, Double> eventChanges = new HashMap<>();
    
    /**
     * Create configuration with default values
     */
    public ReputationEventConfig() {
        eventChanges.putAll(DEFAULTS);
    }
    
    /**
     * Load reputation event configuration from JSON object
     * 
     * Expected format:
     * {
     *   "reputation_events": {
     *     "villager_killed": -0.25,
     *     "trade_completed": 0.02,
     *     ...
     *   }
     * }
     */
    public static ReputationEventConfig fromJson(JsonElement jsonElement) {
        ReputationEventConfig config = new ReputationEventConfig();
        
        if (jsonElement == null || !jsonElement.isJsonObject()) {
            LOGGER.debug("No reputation event config found, using defaults");
            return config;
        }
        
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        
        if (!jsonObject.has("reputation_events")) {
            LOGGER.debug("No 'reputation_events' section found, using defaults");
            return config;
        }
        
        JsonObject eventsObj = jsonObject.get("reputation_events").getAsJsonObject();
        
        for (String eventId : eventsObj.keySet()) {
            try {
                ReputationEventType eventType = ReputationEventType.fromId(eventId);
                if (eventType == null) {
                    LOGGER.warn("Unknown reputation event type: '{}', skipping", eventId);
                    continue;
                }
                
                double value = eventsObj.get(eventId).getAsDouble();
                
                // Validate value is in reasonable range
                if (value < -1.0 || value > 1.0) {
                    LOGGER.warn("Reputation change for '{}' is out of range: {}, clamping to [-1.0, 1.0]", 
                        eventId, value);
                    value = Math.max(-1.0, Math.min(1.0, value));
                }
                
                config.eventChanges.put(eventType, value);
                LOGGER.debug("Loaded reputation event: {} = {}", eventId, value);
                
            } catch (Exception e) {
                LOGGER.warn("Failed to parse reputation event '{}': {}", eventId, e.getMessage());
            }
        }
        
        return config;
    }
    
    /**
     * Set reputation change for an event type
     */
    public void setEventChange(ReputationEventType eventType, double change) {
        double clamped = Math.max(-1.0, Math.min(1.0, change));
        eventChanges.put(eventType, clamped);
    }
    
    /**
     * Get reputation change for an event type
     */
    public double getEventChange(ReputationEventType eventType) {
        return eventChanges.getOrDefault(eventType, 0.0);
    }
    
    /**
     * Get all configured event types
     */
    public Map<ReputationEventType, Double> getAllEventChanges() {
        return new HashMap<>(eventChanges);
    }
    
    /**
     * Check if an event type has a custom configuration
     */
    public boolean hasCustomConfig(ReputationEventType eventType) {
        Double defaultValue = DEFAULTS.get(eventType);
        Double currentValue = eventChanges.get(eventType);
        
        if (defaultValue == null || currentValue == null) {
            return false;
        }
        
        return !defaultValue.equals(currentValue);
    }
    
    /**
     * Get configuration string for debugging
     */
    public String getConfigString() {
        StringBuilder sb = new StringBuilder("ReputationEventConfig{\n");
        for (ReputationEventType eventType : ReputationEventType.values()) {
            double change = getEventChange(eventType);
            String marker = hasCustomConfig(eventType) ? "[CUSTOM]" : "";
            sb.append(String.format("  %s: %.2f %s\n", eventType.getId(), change, marker));
        }
        sb.append("}");
        return sb.toString();
    }
}
