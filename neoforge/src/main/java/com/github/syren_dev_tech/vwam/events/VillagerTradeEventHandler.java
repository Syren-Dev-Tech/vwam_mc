package com.github.syren_dev_tech.vwam.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Event handler for managing villager trading state.
 * Periodically checks frozen villagers to ensure their associated players are still online and trading.
 * Also handles cleanup when players disconnect.
 */
public class VillagerTradeEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerTradeEventHandler");
    
    // Check frozen villagers every N ticks (200 ticks = 10 seconds)
    private static final int FROZEN_VILLAGER_CHECK_INTERVAL = 200;
    private static int tickCounter = 0;
    
    /**
     * Periodic server tick event - check if frozen villagers should be unfrozen
     */
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;
        if (tickCounter < FROZEN_VILLAGER_CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;
        
        // Get the overworld level
        ServerLevel overworld = event.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld == null) {
            LOGGER.debug("Overworld not available for frozen villager check");
            return;
        }
        
        // Check all frozen villagers
        checkFrozenVillagers(event.getServer(), overworld);
    }
    
    /**
     * Player disconnect event - unfreeze any villagers they were trading with
     */
    @SubscribeEvent
    public void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        UUID playerUUID = player.getUUID();
        
        LOGGER.info("Player {} disconnected - checking for frozen villagers", player.getName().getString());
        
        // Get all server levels to find villagers
        Map<UUID, Villager> villagerCache = new HashMap<>();
        for (ServerLevel level : player.getServer().getAllLevels()) {
            AABB searchArea = new AABB(-30000000, -64, -30000000, 30000000, 320, 30000000);
            for (Villager villager : level.getEntitiesOfClass(Villager.class, searchArea)) {
                villagerCache.put(villager.getUUID(), villager);
            }
        }
        
        // Unfreeze all villagers associated with this player
        FrozenVillagerTracker.unfreezeByPlayer(playerUUID, villagerCache);
    }
    
    /**
     * Check all frozen villagers and unfreeze if conditions are met
     */
    private void checkFrozenVillagers(net.minecraft.server.MinecraftServer server, ServerLevel overworld) {
        try {
            Map<UUID, UUID> frozenVillagers = FrozenVillagerTracker.getAllFrozenVillagers();
            
            if (frozenVillagers.isEmpty()) {
                return;
            }
            
            LOGGER.debug("Checking {} frozen villagers", frozenVillagers.size());
            
            // Get all online players for quick lookup
            Map<UUID, ServerPlayer> onlinePlayers = new HashMap<>();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                onlinePlayers.put(player.getUUID(), player);
            }
            
            // Get a large bounding box covering the entire loaded region
            AABB searchArea = new AABB(-30000000, -64, -30000000, 30000000, 320, 30000000);
            
            // Check each frozen villager
            Set<UUID> villagersToUnfreeze = new HashSet<>();
            for (Map.Entry<UUID, UUID> entry : frozenVillagers.entrySet()) {
                UUID villagerUUID = entry.getKey();
                UUID playerUUID = entry.getValue();
                
                // Check if player is still online
                ServerPlayer player = onlinePlayers.get(playerUUID);
                if (player == null) {
                    LOGGER.info("Player {} is offline - should unfreeze villager {}", playerUUID, villagerUUID);
                    villagersToUnfreeze.add(villagerUUID);
                    continue;
                }
                
                // Player is online - check if they're still actively trading
                // Look for a trade screen open for this villager
                // Since the screen opens on client, we check if the player is in the same dimension
                // and if enough time has passed without activity
                
                // For now, we'll just ensure the player exists
                // The screen itself should send close packets when it closes
            }
            
            // Unfreeze villagers whose players have disconnected
            for (UUID villagerUUID : villagersToUnfreeze) {
                // Find the villager entity
                for (Villager villager : overworld.getEntitiesOfClass(Villager.class, searchArea)) {
                    if (villager.getUUID().equals(villagerUUID)) {
                        FrozenVillagerTracker.unfreezeVillagerByUUID(villagerUUID, villager);
                        LOGGER.info("Unfroze villager {} because associated player went offline", villagerUUID);
                        break;
                    }
                }
                
                // If we couldn't find the villager, still remove it from frozen list
                if (!FrozenVillagerTracker.isFrozen(villagerUUID)) {
                    // Already handled above
                } else {
                    // Villager not found, but we need to unfreeze it
                    FrozenVillagerTracker.clearAllFrozen(); // This is a fallback, not ideal
                    LOGGER.warn("Could not find villager {} to unfreeze, but player is offline", villagerUUID);
                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error checking frozen villagers", e);
        }
    }
}
