package com.github.syren_dev_tech.vwam.villager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.npc.Villager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages villager daily routine status including:
 * - Rested: Whether villager slept in a bed (required for production)
 * - Socialized: Whether villager participated in social gathering (required for trading)
 * - Able to Work: Whether villager passed workshop validation (required for production)
 * 
 * These statuses reset at dawn (tick 0, which is 6:00 AM in-game)
 */
public class VillagerRoutineStatus {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerRoutineStatus");
    
    // NBT Keys for persistence
    private static final String NBT_RESTED = "VWAM_Rested";
    private static final String NBT_SOCIALIZED = "VWAM_Socialized";
    private static final String NBT_ABLE_TO_WORK = "VWAM_AbleToWork";
    private static final String NBT_LAST_RESET = "VWAM_LastResetDay";
    
    private final Villager villager;
    private boolean rested = false;
    private boolean socialized = false;
    private boolean ableToWork = false;
    private long lastResetDay = -1;
    
    /**
     * Create or retrieve routine status for a villager
     */
    public VillagerRoutineStatus(Villager villager) {
        this.villager = villager;
        loadFromNBT();
    }
    
    /**
     * Load status from NBT
     */
    private void loadFromNBT() {
        CompoundTag tag = villager.getPersistentData();
        this.rested = tag.getBoolean(NBT_RESTED);
        this.socialized = tag.getBoolean(NBT_SOCIALIZED);
        this.ableToWork = tag.getBoolean(NBT_ABLE_TO_WORK);
        this.lastResetDay = tag.getLong(NBT_LAST_RESET);
    }
    
    /**
     * Save status to NBT
     */
    private void saveToNBT() {
        CompoundTag tag = villager.getPersistentData();
        tag.putBoolean(NBT_RESTED, rested);
        tag.putBoolean(NBT_SOCIALIZED, socialized);
        tag.putBoolean(NBT_ABLE_TO_WORK, ableToWork);
        tag.putLong(NBT_LAST_RESET, lastResetDay);
    }
    
    /**
     * Check and reset status if it's a new day
     * Called at dawn (tick 0, 6:00 AM)
     */
    public void checkAndResetDaily(long currentDay) {
        // Only reset once per day
        if (lastResetDay != currentDay) {
            LOGGER.debug("Resetting daily routine for villager {} at day {}", villager.getId(), currentDay);
            rested = false;
            socialized = false;
            ableToWork = false;
            lastResetDay = currentDay;
            saveToNBT();
        }
    }
    
    /**
     * Set villager as rested (called when villager sleeps in a bed)
     */
    public void setRested() {
        if (!rested) {
            LOGGER.debug("Villager {} is now rested", villager.getId());
            rested = true;
            saveToNBT();
        }
    }
    
    /**
     * Set villager as socialized (called when villager participates in social gathering)
     */
    public void setSocialized() {
        if (!socialized) {
            LOGGER.debug("Villager {} is now socialized", villager.getId());
            socialized = true;
            saveToNBT();
        }
    }
    
    /**
     * Set whether villager is able to work
     * This is determined by workshop validation
     */
    public void setAbleToWork(boolean able) {
        if (ableToWork != able) {
            LOGGER.debug("Villager {} able to work: {}", villager.getId(), able);
            ableToWork = able;
            saveToNBT();
        }
    }
    
    /**
     * Check if villager can produce items
     * Requires: Rested + Able to Work
     */
    public boolean canProduce() {
        boolean can = rested && ableToWork;
        if (!can) {
            LOGGER.debug("Villager {} cannot produce - rested: {}, ableToWork: {}", 
                villager.getId(), rested, ableToWork);
        }
        return can;
    }
    
    /**
     * Check if villager can trade
     * Requires: Socialized (no other requirements for trading)
     */
    public boolean canTrade() {
        boolean can = socialized;
        if (!can) {
            LOGGER.debug("Villager {} cannot trade - not socialized", villager.getId());
        }
        return can;
    }
    
    // Getters for individual statuses
    public boolean isRested() {
        return rested;
    }
    
    public boolean isSocialized() {
        return socialized;
    }
    
    public boolean isAbleToWork() {
        return ableToWork;
    }
    
    /**
     * Get status string for debugging
     */
    public String getStatusString() {
        return String.format("Rested: %s, Socialized: %s, AbleToWork: %s",
            rested, socialized, ableToWork);
    }
}
