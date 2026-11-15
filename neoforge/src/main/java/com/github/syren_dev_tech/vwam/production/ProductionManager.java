package com.github.syren_dev_tech.vwam.production;

import com.github.syren_dev_tech.vwam.data.CustomWorkshopDefinition;
import com.github.syren_dev_tech.vwam.data.ProductionRecipe;
import com.github.syren_dev_tech.vwam.village.VillageManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages production for all villagers in all villages.
 * Coordinates production cycles, tracks timings, and executes recipes.
 */
public class ProductionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsProduction");

    private final Map<Integer, VillagerProducer> producers;  // Villager entity ID -> Producer
    private final VillageManager villageManager;

    private long lastProductionTick = 0;
    private static final long PRODUCTION_TICK_INTERVAL = 1000;  // Check every 1 second

    public ProductionManager(VillageManager villageManager) {
        this.villageManager = villageManager;
        this.producers = new HashMap<>();
    }

    /**
     * Register a villager for production
     * @param villager The villager to register
     * @param recipes The production recipes for this villager
     * @param workshopDefinition The workshop definition (for quality multiplier)
     * @return The VillagerProducer managing this villager
     */
    public VillagerProducer registerVillager(
            Villager villager,
            ProductionRecipe[] recipes,
            CustomWorkshopDefinition workshopDefinition
    ) {
        // Get village from villager NBT
        String villageIdStr = villager.getPersistentData().getString("VillageId");
        if (villageIdStr.isEmpty()) {
            LOGGER.warn("Cannot register villager {} - not linked to village", villager.getId());
            return null;
        }

        // Get village and stockpile
        String villageId = villager.getPersistentData().getString("VillageId");
        VillageManager.Village village = villageManager.getVillage(ResourceLocation.parse(villageId))
                .orElse(null);

        if (village == null) {
            LOGGER.warn("Cannot register villager {} - village {} not found", villager.getId(), villageId);
            return null;
        }

        // Create producer
        VillagerProducer producer = new VillagerProducer(
                villager,
                village.getStockpile(),
                recipes
        );

        producers.put(villager.getId(), producer);
        LOGGER.info("Registered villager {} for production", villager.getId());
        return producer;
    }

    /**
     * Unregister a villager (usually on death)
     * @param villager The villager to unregister
     */
    public void unregisterVillager(Villager villager) {
        producers.remove(villager.getId());
        LOGGER.info("Unregistered villager {} from production", villager.getId());
    }

    /**
     * Get the producer for a villager
     * @param villager The villager
     * @return The VillagerProducer, or null if not registered
     */
    public VillagerProducer getProducer(Villager villager) {
        return producers.get(villager.getId());
    }

    /**
     * Tick production for all villagers
     * This should be called periodically (e.g., every game tick or every second)
     *
     * @param qualityMultiplierProvider A function to get workshop quality for a villager
     */
    public void tickProduction(QualityMultiplierProvider qualityMultiplierProvider) {
        long currentTime = System.currentTimeMillis();

        // Only tick every PRODUCTION_TICK_INTERVAL milliseconds
        if (currentTime - lastProductionTick < PRODUCTION_TICK_INTERVAL) {
            return;
        }
        lastProductionTick = currentTime;

        // Tick each registered producer
        List<Integer> deadVillagers = new ArrayList<>();

        for (Map.Entry<Integer, VillagerProducer> entry : producers.entrySet()) {
            VillagerProducer producer = entry.getValue();
            Villager villager = producer.getVillager();

            // Check if villager is still alive
            if (villager.isRemoved() || !villager.isAlive()) {
                deadVillagers.add(entry.getKey());
                continue;
            }

            // Get quality multiplier
            float qualityMult = qualityMultiplierProvider.getQualityMultiplier(villager);

            // Attempt production
            producer.tryProduce(currentTime, qualityMult);
        }

        // Clean up dead villagers
        for (int villagerID : deadVillagers) {
            producers.remove(villagerID);
        }
    }

    /**
     * Get production stats for a villager
     */
    public ProductionStats getStats(Villager villager) {
        VillagerProducer producer = getProducer(villager);
        if (producer == null) {
            return null;
        }

        return new ProductionStats(
                villager.getId(),
                producer.getCurrentRecipeId(),
                producer.getTimeUntilNextProduction(),
                producer.getRecipes().length
        );
    }

    /**
     * Clear all producers (e.g., on world unload)
     */
    public void clear() {
        producers.clear();
    }

    /**
     * Get total number of registered villagers
     */
    public int getProducerCount() {
        return producers.size();
    }

    /**
     * Functional interface for getting quality multiplier for a villager
     */
    @FunctionalInterface
    public interface QualityMultiplierProvider {
        /**
         * Get the workshop quality multiplier for a villager
         * @param villager The villager
         * @return The quality multiplier (0.5 to 1.5)
         */
        float getQualityMultiplier(Villager villager);
    }

    /**
     * Statistics about a villager's production
     */
    public static class ProductionStats {
        public final int villagerId;
        public final String currentRecipeId;
        public final float hoursUntilNext;
        public final int totalRecipes;

        public ProductionStats(int villagerId, String currentRecipeId, float hoursUntilNext, int totalRecipes) {
            this.villagerId = villagerId;
            this.currentRecipeId = currentRecipeId;
            this.hoursUntilNext = hoursUntilNext;
            this.totalRecipes = totalRecipes;
        }
    }
}
