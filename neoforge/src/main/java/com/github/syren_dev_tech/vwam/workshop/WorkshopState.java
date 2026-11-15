package com.github.syren_dev_tech.vwam.workshop;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Manages workshop state and bindings for villagers.
 * Stores workshop validation results and tracks villager assignments.
 */
public class WorkshopState {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsState");

    // NBT Keys for persistence
    private static final String NBT_WORKSHOP_VALID = "VWAMWorkshopValid";
    private static final String NBT_WORKSHOP_POS_X = "VWAMWorkshopPosX";
    private static final String NBT_WORKSHOP_POS_Y = "VWAMWorkshopPosY";
    private static final String NBT_WORKSHOP_POS_Z = "VWAMWorkshopPosZ";
    private static final String NBT_WORKSHOP_QUALITY = "VWAMWorkshopQuality";
    private static final String NBT_WORKSHOP_BEAUTY = "VWAMWorkshopBeauty";
    private static final String NBT_LAST_VALIDATION = "VWAMLastValidation";
    private static final String NBT_CAN_WORK = "VWAMCanWork";

    private final Villager villager;
    private WorkshopValidationResult lastValidation;
    private BlockPos workshopPosition;
    private boolean canWorkToday;
    private long lastValidationTime;

    // Validation cooldown - don't re-validate more than once per day
    private static final long VALIDATION_COOLDOWN_MS = 24 * 60 * 60 * 1000;  // 24 hours

    public WorkshopState(Villager villager) {
        this.villager = villager;
        this.canWorkToday = false;
        this.lastValidationTime = 0;
        loadFromNBT();
    }

    /**
     * Update workshop validation state
     */
    public void setValidationResult(WorkshopValidationResult result) {
        this.lastValidation = result;
        this.workshopPosition = result.getWorkstationPos();
        this.canWorkToday = result.isValid();
        this.lastValidationTime = System.currentTimeMillis();
        saveToNBT();

        if (result.isValid()) {
            LOGGER.info("Villager {} workshop validated: quality={}, beauty={}",
                    villager.getId(), result.getQuality(), result.getBeautyScore());
        } else {
            LOGGER.info("Villager {} workshop validation failed: {}",
                    villager.getId(), result.getFailureReasons());
        }
    }

    /**
     * Check if villager can work today
     */
    public boolean canWorkToday() {
        return canWorkToday;
    }

    /**
     * Get the current workshop position
     */
    public BlockPos getWorkshopPosition() {
        return workshopPosition;
    }

    /**
     * Get the last validation result
     */
    public WorkshopValidationResult getLastValidation() {
        return lastValidation;
    }

    /**
     * Get workshop quality multiplier for production
     */
    public float getQualityMultiplier() {
        if (!canWorkToday || lastValidation == null) {
            return 1.0f;  // Default to normal
        }
        return lastValidation.getQualityMultiplier();
    }

    /**
     * Get workshop beauty rating for trade discounts
     */
    public WorkshopValidationResult.WorkshopBeauty getBeautyRating() {
        if (lastValidation == null) {
            return WorkshopValidationResult.WorkshopBeauty.MODEST;
        }
        return WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(lastValidation.getBeautyScore());
    }

    /**
     * Get trade multiplier from beauty
     */
    public float getBeautyTradeMultiplier() {
        return getBeautyRating().getTradeMultiplier();
    }

    /**
     * Check if validation should be refreshed (cooldown expired)
     */
    public boolean shouldRefreshValidation() {
        long timeSinceValidation = System.currentTimeMillis() - lastValidationTime;
        return timeSinceValidation >= VALIDATION_COOLDOWN_MS;
    }

    /**
     * Save workshop state to villager NBT
     */
    private void saveToNBT() {
        CompoundTag tag = villager.getPersistentData();
        tag.putBoolean(NBT_WORKSHOP_VALID, canWorkToday);
        tag.putLong(NBT_LAST_VALIDATION, lastValidationTime);
        tag.putBoolean(NBT_CAN_WORK, canWorkToday);

        if (workshopPosition != null) {
            tag.putInt(NBT_WORKSHOP_POS_X, workshopPosition.getX());
            tag.putInt(NBT_WORKSHOP_POS_Y, workshopPosition.getY());
            tag.putInt(NBT_WORKSHOP_POS_Z, workshopPosition.getZ());
        }

        if (lastValidation != null) {
            tag.putString(NBT_WORKSHOP_QUALITY, lastValidation.getQuality().name());
            tag.putFloat(NBT_WORKSHOP_BEAUTY, lastValidation.getBeautyScore());
        }
    }

    /**
     * Load workshop state from villager NBT
     */
    private void loadFromNBT() {
        CompoundTag tag = villager.getPersistentData();

        if (tag.contains(NBT_CAN_WORK, Tag.TAG_BYTE)) {
            this.canWorkToday = tag.getBoolean(NBT_CAN_WORK);
        }

        if (tag.contains(NBT_LAST_VALIDATION, Tag.TAG_LONG)) {
            this.lastValidationTime = tag.getLong(NBT_LAST_VALIDATION);
        }

        // Try to restore workshop position
        if (tag.contains(NBT_WORKSHOP_POS_X, Tag.TAG_INT)) {
            int x = tag.getInt(NBT_WORKSHOP_POS_X);
            int y = tag.getInt(NBT_WORKSHOP_POS_Y);
            int z = tag.getInt(NBT_WORKSHOP_POS_Z);
            this.workshopPosition = new BlockPos(x, y, z);
        }

        LOGGER.debug("Loaded workshop state for villager {}: canWork={}, position={}",
                villager.getId(), canWorkToday, workshopPosition);
    }

    /**
     * Reset workshop state (for when villager changes profession or moves)
     */
    public void reset() {
        this.canWorkToday = false;
        this.lastValidation = null;
        this.workshopPosition = null;
        this.lastValidationTime = 0;
        saveToNBT();
    }

    @Override
    public String toString() {
        return String.format(
                "WorkshopState{villager=%d, canWork=%s, position=%s, quality=%s}",
                villager.getId(),
                canWorkToday,
                workshopPosition,
                lastValidation != null ? lastValidation.getQuality() : "NONE"
        );
    }
}
