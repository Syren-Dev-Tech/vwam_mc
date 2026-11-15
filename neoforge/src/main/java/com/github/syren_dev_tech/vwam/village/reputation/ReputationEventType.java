package com.github.syren_dev_tech.vwam.village.reputation;

/**
 * Enumeration of events that affect player reputation with villages.
 * Each event can have a configured reputation change value.
 * 
 * Reputation ranges from -1 (hostile) to 1 (friendly), with 0 being neutral.
 */
public enum ReputationEventType {
    
    // Negative events
    
    /** Player kills a villager belonging to this village */
    VILLAGER_KILLED("villager_killed", false),
    
    /** Player forces a villager out of bed */
    VILLAGER_BED_STOLEN("villager_bed_stolen", false),
    
    /** Player loots a chest within village territory */
    CHEST_LOOTED("chest_looted", false),
    
    /** Player breaks a block within village territory */
    BLOCK_BROKEN("block_broken", false),
    
    /** Player hits a villager */
    VILLAGER_HIT("villager_hit", false),
    
    /** Player destroys a workstation */
    WORKSTATION_DESTROYED("workstation_destroyed", false),
    
    /** Player causes a villager to fail their profession check */
    WORKSHOP_INVALIDATED("workshop_invalidated", false),
    
    // Positive events
    
    /** Player trades with a villager */
    TRADE_COMPLETED("trade_completed", true),
    
    /** Player defeats a raid defending this village */
    RAID_DEFEATED("raid_defeated", true),
    
    /** Player gives resources to the village */
    RESOURCE_DONATED("resource_donated", true),
    
    /** Player completes a village task/job (future feature) */
    TASK_COMPLETED("task_completed", true),
    
    /** Player repairs a damaged workstation */
    WORKSTATION_REPAIRED("workstation_repaired", true),
    
    /** Player provides emergency supplies to villagers */
    EMERGENCY_AID_PROVIDED("emergency_aid_provided", true);
    
    private final String id;
    private final boolean isPositive;
    
    ReputationEventType(String id, boolean isPositive) {
        this.id = id;
        this.isPositive = isPositive;
    }
    
    /**
     * Get the string identifier for this event type
     */
    public String getId() {
        return id;
    }
    
    /**
     * Check if this is a positive (reputation-gaining) event
     */
    public boolean isPositive() {
        return isPositive;
    }
    
    /**
     * Check if this is a negative (reputation-losing) event
     */
    public boolean isNegative() {
        return !isPositive;
    }
    
    /**
     * Get event type by ID
     */
    public static ReputationEventType fromId(String id) {
        for (ReputationEventType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
