package com.github.syren_dev_tech.vwam.villager;

import com.github.syren_dev_tech.vwam.workshop.WorkshopManager;
import com.github.syren_dev_tech.vwam.workshop.WorkshopValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.BedBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler for villager routine mechanics
 * Detects sleep, social gathering, and validates workshops
 */
public class VillagerRoutineEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerRoutineEvents");
    
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        // Trigger daily reset check and routine updates
        VillagerRoutineManager manager = VillagerRoutineManager.getInstance();
        manager.onServerTick(event.getServer().getTickCount());
    }
    
    /**
     * Called when a villager enters a bed
     * Detects via block position checks in villager AI
     * Note: This is called from VillagerEvents when we detect sleep
     */
    public static void onVillagerSleep(Villager villager) {
        VillagerRoutineManager manager = VillagerRoutineManager.getInstance();
        VillagerRoutineStatus status = manager.getRoutineStatus(villager);
        status.setRested();
        LOGGER.info("Villager {} detected sleeping in bed", villager.getId());
    }
    
    /**
     * Called when a villager participates in social gathering
     * Note: Vanilla villagers naturally do this at certain times
     * For our system, we hook into when they gather and interact
     */
    public static void onVillagerSocialize(Villager villager) {
        VillagerRoutineManager manager = VillagerRoutineManager.getInstance();
        VillagerRoutineStatus status = manager.getRoutineStatus(villager);
        status.setSocialized();
        LOGGER.info("Villager {} participated in social gathering", villager.getId());
    }
    
    /**
     * Called when a villager attempts to work for the first time in the day
     * Validates their workshop and sets their "able to work" status
     */
    public static void onVillagerFirstWorkAttempt(Villager villager) {
        VillagerRoutineManager manager = VillagerRoutineManager.getInstance();
        VillagerRoutineStatus status = manager.getRoutineStatus(villager);
        
        // Only validate once per day
        if (!status.isAbleToWork() && !status.isSocialized()) {
            LOGGER.info("Villager {} attempting first work of day, validating workshop", villager.getId());
            
            // Get workshop manager and validate
            WorkshopManager workshopManager = WorkshopManager.getInstance();
            WorkshopValidationResult validationResult = workshopManager.validateCurrentWorkshop(villager);
            
            if (validationResult != null && validationResult.isValid()) {
                status.setAbleToWork(true);
                LOGGER.info("Villager {} workshop validated successfully - quality: {}, beauty: {}",
                    villager.getId(), validationResult.getQuality(), validationResult.getBeautyScore());
            } else {
                status.setAbleToWork(false);
                String reason = validationResult != null ? 
                    String.join(", ", validationResult.getFailureReasons()) : "Unknown reason";
                LOGGER.info("Villager {} workshop validation failed: {}", villager.getId(), reason);
            }
        }
    }
    
    /**
     * Called when a player attempts to interact with a villager for trading
     * Checks if villager can trade based on routine status
     */
    public static boolean canVillagerTrade(Villager villager) {
        VillagerRoutineManager manager = VillagerRoutineManager.getInstance();
        VillagerRoutineStatus status = manager.getRoutineStatus(villager);
        
        boolean canTrade = status.canTrade();
        if (!canTrade) {
            LOGGER.debug("Villager {} cannot trade - status: {}", villager.getId(), status.getStatusString());
        }
        return canTrade;
    }
    
    /**
     * Called when checking if a villager can produce items
     */
    public static boolean canVillagerProduce(Villager villager) {
        VillagerRoutineManager manager = VillagerRoutineManager.getInstance();
        VillagerRoutineStatus status = manager.getRoutineStatus(villager);
        
        boolean canProduce = status.canProduce();
        if (!canProduce) {
            LOGGER.debug("Villager {} cannot produce - status: {}", villager.getId(), status.getStatusString());
        }
        return canProduce;
    }
}
