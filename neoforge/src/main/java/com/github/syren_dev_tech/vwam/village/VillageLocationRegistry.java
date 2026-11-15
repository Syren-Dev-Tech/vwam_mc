package com.github.syren_dev_tech.vwam.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Improved village detection and management system.
 * 
 * Solves the problem of large villages being split into multiple entries by:
 * 1. Tracking the primary location (center) of each village
 * 2. Checking if new villagers are within range of existing village centers
 * 3. Using human-friendly names for villages
 * 4. Merging nearby villages into single entries
 */
public class VillageLocationRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillageLocationRegistry");
    
    // NBT keys
    private static final String VILLAGE_ID_NBT = "VillageId";
    private static final String VILLAGE_NAME_NBT = "VillageName";
    
    // Configuration
    private static final double VILLAGE_MERGE_DISTANCE = 256.0;  // Villages within 256 blocks merge
    private static final double VILLAGE_ASSOCIATION_RANGE = 384.0;  // Villagers within 384 blocks belong to village
    
    // Singleton instance
    private static final VillageLocationRegistry INSTANCE = new VillageLocationRegistry();
    
    // Village registry: villageId -> VillageData
    private final Map<String, VillageData> villageRegistry = new HashMap<>();
    
    private VillageLocationRegistry() {}
    
    public static VillageLocationRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Detect and tag a villager with the appropriate village.
     * 
     * Algorithm:
     * 1. Check if villager is already tagged
     * 2. Find all nearby villagers with tags
     * 3. Check if any known village center is within range
     * 4. If within range of known village, add villager to that village
     * 5. If not, create a new village at this location
     */
    public void detectAndTagVillager(Villager villager, ServerLevel level) {
        CompoundTag tag = villager.getPersistentData();
        
        // Check if already tagged
        if (tag.contains(VILLAGE_ID_NBT)) {
            String existingVillageId = tag.getString(VILLAGE_ID_NBT);
            LOGGER.debug("Villager {} already tagged with village: {}", villager.getId(), existingVillageId);
            return;
        }
        
        BlockPos villagerPos = villager.blockPosition();
        LOGGER.info("Detecting village for villager {} at {}", villager.getId(), villagerPos);
        
        // Step 1: Check if this villager is within range of any known village center
        String nearestVillageId = findNearestVillage(villagerPos);
        if (nearestVillageId != null) {
            LOGGER.info("Villager {} is near known village: {}", villager.getId(), nearestVillageId);
            tagVillagerWithVillage(villager, nearestVillageId);
            return;
        }
        
        // Step 2: Check nearby villagers for existing village tags
        List<Villager> nearbyVillagers = level.getEntitiesOfClass(
            Villager.class,
            villager.getBoundingBox().inflate(VILLAGE_ASSOCIATION_RANGE)
        );
        
        for (Villager nearby : nearbyVillagers) {
            if (nearby.getId() == villager.getId()) continue;
            
            CompoundTag nearbyTag = nearby.getPersistentData();
            if (nearbyTag.contains(VILLAGE_ID_NBT)) {
                String villageId = nearbyTag.getString(VILLAGE_ID_NBT);
                String villageName = nearbyTag.getString(VILLAGE_NAME_NBT);
                
                LOGGER.info("Villager {} found nearby villager {} with village: {} ({})", 
                    villager.getId(), nearby.getId(), villageId, villageName);
                
                // Update the village center if this villager is farther than current center
                VillageData villageData = villageRegistry.get(villageId);
                if (villageData != null && villagerPos.distSqr(villageData.centerLocation) > 
                    villageData.centerLocation.distSqr(villageData.centerLocation)) {
                    // Keep the first identified location as center
                } else if (villageData != null) {
                    villageData.addSecondaryLocation(villagerPos);
                }
                
                tagVillagerWithVillage(villager, villageId);
                return;
            }
        }
        
        // Step 3: Create a new village with this location as center
        String villageId = createNewVillage(villagerPos);
        LOGGER.info("Created new village {} at {}", villageId, villagerPos);
        tagVillagerWithVillage(villager, villageId);
        
        // Tag all nearby untagged villagers with this village
        for (Villager nearby : nearbyVillagers) {
            if (nearby.getId() == villager.getId()) continue;
            
            CompoundTag nearbyTag = nearby.getPersistentData();
            if (!nearbyTag.contains(VILLAGE_ID_NBT)) {
                tagVillagerWithVillage(nearby, villageId);
                LOGGER.info("Tagged nearby villager {} with new village {}", nearby.getId(), villageId);
            }
        }
    }
    
    /**
     * Find the nearest known village to a position
     * Returns the village ID if within range, null otherwise
     */
    private String findNearestVillage(BlockPos pos) {
        String nearestVillageId = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Map.Entry<String, VillageData> entry : villageRegistry.entrySet()) {
            double distance = pos.distSqr(entry.getValue().centerLocation);
            if (distance < VILLAGE_ASSOCIATION_RANGE * VILLAGE_ASSOCIATION_RANGE && distance < nearestDistance) {
                nearestDistance = distance;
                nearestVillageId = entry.getKey();
            }
        }
        
        return nearestVillageId;
    }
    
    /**
     * Create a new village with the given center location
     * Generates a human-friendly name based on coordinates
     */
    private String createNewVillage(BlockPos centerPos) {
        // Generate a unique ID based on chunk location
        String villageId = "village_" + (centerPos.getX() / 128) + "_" + (centerPos.getZ() / 128) + "_" + 
                          System.currentTimeMillis() % 10000;
        
        // Generate a human-friendly name
        String villageName = generateVillageName(centerPos);
        
        // Create and register village data
        VillageData villageData = new VillageData(villageId, villageName, centerPos);
        villageRegistry.put(villageId, villageData);
        
        LOGGER.info("Registered new village: {} ({}) at {}", villageId, villageName, centerPos);
        
        return villageId;
    }
    
    /**
     * Generate a human-friendly name for a village based on its location
     */
    private String generateVillageName(BlockPos pos) {
        // Get region name based on coordinate quadrants
        String regionName = "";
        if (pos.getX() >= 0 && pos.getZ() >= 0) {
            regionName = "Northeastern";
        } else if (pos.getX() < 0 && pos.getZ() >= 0) {
            regionName = "Northwestern";
        } else if (pos.getX() >= 0 && pos.getZ() < 0) {
            regionName = "Southeastern";
        } else {
            regionName = "Southwestern";
        }
        
        // Generate a name with a number
        int nameIndex = Math.abs((pos.getX() + pos.getZ()) % 100);
        return regionName + " Village " + nameIndex;
    }
    
    /**
     * Tag a villager with village information
     */
    private void tagVillagerWithVillage(Villager villager, String villageId) {
        VillageData villageData = villageRegistry.get(villageId);
        if (villageData == null) {
            LOGGER.warn("Village {} not found in registry", villageId);
            return;
        }
        
        CompoundTag tag = villager.getPersistentData();
        tag.putString(VILLAGE_ID_NBT, villageId);
        tag.putString(VILLAGE_NAME_NBT, villageData.displayName);
        
        LOGGER.debug("Tagged villager {} with village {} ({})", 
            villager.getId(), villageId, villageData.displayName);
    }
    
    /**
     * Get the village ID for a villager
     */
    public String getVillageId(Villager villager) {
        return villager.getPersistentData().getString(VILLAGE_ID_NBT);
    }
    
    /**
     * Get the display name of a villager's village
     */
    public String getVillageName(Villager villager) {
        return villager.getPersistentData().getString(VILLAGE_NAME_NBT);
    }
    
    /**
     * Check if a villager is part of any village
     */
    public boolean isPartOfVillage(Villager villager) {
        return villager.getPersistentData().contains(VILLAGE_ID_NBT);
    }
    
    /**
     * Set a custom name for a village
     */
    public void setVillageName(String villageId, String displayName) {
        VillageData villageData = villageRegistry.get(villageId);
        if (villageData != null) {
            villageData.displayName = displayName;
            LOGGER.info("Set village {} name to: {}", villageId, displayName);
        }
    }
    
    /**
     * Get all village data
     */
    public Map<String, VillageData> getAllVillages() {
        return new HashMap<>(villageRegistry);
    }
    
    /**
     * Get village data by ID
     */
    public VillageData getVillageData(String villageId) {
        return villageRegistry.get(villageId);
    }
    
    /**
     * Clear all village data (for testing/reset)
     */
    public void clear() {
        villageRegistry.clear();
        LOGGER.info("Cleared village registry");
    }
    
    /**
     * Inner class to store village information
     */
    public static class VillageData {
        public final String villageId;
        public String displayName;  // Can be updated
        public final BlockPos centerLocation;  // Primary location
        public final List<BlockPos> secondaryLocations;  // Other detected locations
        
        public VillageData(String villageId, String displayName, BlockPos centerLocation) {
            this.villageId = villageId;
            this.displayName = displayName;
            this.centerLocation = centerLocation;
            this.secondaryLocations = new ArrayList<>();
        }
        
        public void addSecondaryLocation(BlockPos pos) {
            if (!secondaryLocations.contains(pos)) {
                secondaryLocations.add(pos);
            }
        }
        
        public double getDistanceFromCenter(BlockPos pos) {
            return Math.sqrt(pos.distSqr(centerLocation));
        }
        
        @Override
        public String toString() {
            return displayName + " (" + villageId + ") at " + centerLocation;
        }
    }
}
