package com.github.syren_dev_tech.vwam.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Village detection wrapper for backward compatibility.
 * 
 * This class provides the same interface as before but now uses the improved
 * VillageLocationRegistry which properly handles large villages and provides
 * human-friendly names.
 * 
 * IMPROVED FEATURES:
 * - Large villages are no longer split into multiple entries
 * - Villages have human-friendly display names
 * - Primary village center location is tracked
 * - Secondary locations within range are associated with the primary village
 */
public class VillageDetector {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsVillageDetector");
    
    /**
     * Detect if a villager is part of a village and tag it with village information.
     * Uses the improved VillageLocationRegistry system.
     */
    public static void detectAndTagVillager(Villager villager, ServerLevel level) {
        VillageLocationRegistry registry = VillageLocationRegistry.getInstance();
        registry.detectAndTagVillager(villager, level);
    }
    
    /**
     * Get the village ID for a villager
     */
    public static String getVillageId(Villager villager) {
        VillageLocationRegistry registry = VillageLocationRegistry.getInstance();
        String villageId = registry.getVillageId(villager);
        return villageId != null && !villageId.isEmpty() ? villageId : null;
    }
    
    /**
     * Get the display name of a villager's village
     */
    public static String getVillageName(Villager villager) {
        VillageLocationRegistry registry = VillageLocationRegistry.getInstance();
        String villageName = registry.getVillageName(villager);
        return villageName != null && !villageName.isEmpty() ? villageName : "Unknown Village";
    }
    
    /**
     * Check if a villager is part of any village
     */
    public static boolean isPartOfVillage(Villager villager) {
        VillageLocationRegistry registry = VillageLocationRegistry.getInstance();
        return registry.isPartOfVillage(villager);
    }
    
    /**
     * Set a custom name for a village
     */
    public static void setVillageName(String villageId, String displayName) {
        VillageLocationRegistry registry = VillageLocationRegistry.getInstance();
        registry.setVillageName(villageId, displayName);
        LOGGER.info("Village {} renamed to: {}", villageId, displayName);
    }
    
    /**
     * Get the village location registry instance (for advanced usage)
     */
    public static VillageLocationRegistry getRegistry() {
        return VillageLocationRegistry.getInstance();
    }
}
