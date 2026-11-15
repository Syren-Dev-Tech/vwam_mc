package com.github.syren_dev_tech.vwam.events;

import com.github.syren_dev_tech.vwam.data.RegistryHandler;
import com.github.syren_dev_tech.vwam.village.VillageDetector;
import com.github.syren_dev_tech.vwam.village.VillageManager;
import com.github.syren_dev_tech.vwam.village.VillagerStockpileCollector;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler for managing villager stockpile collection.
 * Periodically checks villager inventories and automatically transfers items to the village stockpile.
 */
public class VillagerStockpileEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerStockpileEventHandler");
    
    // Check inventory every N ticks (20 ticks = 1 second)
    // Using 100 ticks = 5 seconds to avoid too frequent checks
    private static final int CHECK_INTERVAL_TICKS = 100;
    private static int tickCounter = 0;
    
    /**
     * Server tick event - check villager inventories periodically
     */
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        // Only check every N ticks to avoid performance impact
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        
        // Get the server and overworld
        ServerLevel overworld = event.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld == null) {
            return;
        }
        
        // Get village manager from registry handler
        VillageManager villageManager = RegistryHandler.getVillageManager();
        if (villageManager == null) {
            LOGGER.debug("VillageManager not initialized yet");
            return;
        }
        
        // Check all villagers in the world
        // Get a large bounding box covering the entire loaded region
        AABB searchArea = new AABB(-30000000, -64, -30000000, 30000000, 320, 30000000);
        for (Villager villager : overworld.getEntitiesOfClass(Villager.class, searchArea)) {
            if (!VillageDetector.isPartOfVillage(villager)) {
                continue;
            }
            
            try {
                checkVillagerInventory(villager, villageManager);
            } catch (Exception e) {
                LOGGER.error("Error checking villager {} inventory", villager.getId(), e);
            }
        }
    }
    
    /**
     * Check a single villager's inventory and transfer items to stockpile
     */
    private void checkVillagerInventory(Villager villager, VillageManager villageManager) {
        // Get the villager's village
        String villageIdStr = VillageDetector.getVillageId(villager);
        if (villageIdStr == null) {
            LOGGER.debug("Villager {} has no village assigned", villager.getId());
            return;
        }
        
        // Convert village ID string to ResourceLocation
        ResourceLocation villageId = ResourceLocation.fromNamespaceAndPath("vwam", villageIdStr);
        
        // Get the village from the manager
        var villageOptional = villageManager.getVillage(villageId);
        if (villageOptional.isEmpty()) {
            LOGGER.debug("Village {} not found in manager for villager {}", villageId, villager.getId());
            return;
        }
        
        // Collect items from villager inventory
        var stockpile = villageOptional.get().getStockpile();
        boolean itemsTransferred = VillagerStockpileCollector.collectItemsFromVillager(villager, stockpile);
        
        if (itemsTransferred) {
            LOGGER.debug("Villager {} contributed items to village {} stockpile", 
                villager.getId(), villageId);
        }
    }
}
