package com.github.syren_dev_tech.vwam.village;

import net.minecraft.resources.ResourceLocation;
import com.github.syren_dev_tech.vwam.data.VillageConfiguration;
import java.util.*;

/**
 * Central manager for all villages, their stockpiles, and reputation systems.
 */
public class VillageManager {
    private final Map<ResourceLocation, Village> villages;

    public VillageManager() {
        this.villages = new HashMap<>();
    }

    /**
     * Create or register a new village
     */
    public Village registerVillage(VillageConfiguration config) {
        Village village = new Village(
                config.getVillageId(),
                config.getDisplayName(),
                config.getBaseStockAmount(),
                config.getInitialReputation(),
                config.getMaxReputation(),
                config.getMinReputation(),
                config.getHostileThreshold(),
                config.getFriendlyThreshold()
        );

        villages.put(config.getVillageId(), village);
        return village;
    }

    /**
     * Get a village by ID
     */
    public Optional<Village> getVillage(ResourceLocation villageId) {
        return Optional.ofNullable(villages.get(villageId));
    }

    /**
     * Get all villages
     */
    public Collection<Village> getAllVillages() {
        return villages.values();
    }

    /**
     * Remove a village
     */
    public void removeVillage(ResourceLocation villageId) {
        villages.remove(villageId);
    }

    /**
     * Clear all villages (for reset/reload)
     */
    public void clear() {
        villages.clear();
    }

    /**
     * Inner class representing a complete village with all its systems
     */
    public static class Village {
        private final ResourceLocation villageId;
        private final String displayName;
        private final VillageStockpile stockpile;
        private final VillageReputation reputation;
        private final Set<UUID> linkedVillagers;  // Villagers part of this village

        public Village(
                ResourceLocation villageId,
                String displayName,
                int baseStockAmount,
                float initialRep,
                float maxRep,
                float minRep,
                float hostileThreshold,
                float friendlyThreshold
        ) {
            this.villageId = villageId;
            this.displayName = displayName;
            this.stockpile = new VillageStockpile(villageId, 24); // Restock every 24 hours
            this.reputation = new VillageReputation(
                    villageId,
                    initialRep,
                    maxRep,
                    minRep,
                    hostileThreshold,
                    friendlyThreshold
            );
            this.linkedVillagers = new HashSet<>();
        }

        // Village info
        public ResourceLocation getVillageId() { return villageId; }
        public String getDisplayName() { return displayName; }

        // Stockpile access
        public VillageStockpile getStockpile() { return stockpile; }

        // Reputation access
        public VillageReputation getReputation() { return reputation; }

        // Villager linking
        public void linkVillager(UUID villagerId) {
            linkedVillagers.add(villagerId);
        }

        public void unlinkVillager(UUID villagerId) {
            linkedVillagers.remove(villagerId);
        }

        public boolean hasVillager(UUID villagerId) {
            return linkedVillagers.contains(villagerId);
        }

        public Set<UUID> getLinkedVillagers() {
            return new HashSet<>(linkedVillagers);
        }

        public int getVillagerCount() {
            return linkedVillagers.size();
        }
    }
}
