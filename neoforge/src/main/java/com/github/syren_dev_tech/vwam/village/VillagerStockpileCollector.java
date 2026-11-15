package com.github.syren_dev_tech.vwam.village;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages automatic stockpile collection from villager inventories.
 * Monitors villager inventory changes and automatically transfers collected items to the village stockpile.
 * 
 * This allows villagers to automatically contribute items they pick up (e.g., harvested crops)
 * to the village stockpile without manual intervention.
 */
public class VillagerStockpileCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerStockpileCollector");
    
    // NBT key for storing the last known inventory state (serialized item counts)
    private static final String NBT_INVENTORY_TRACKING = "VWAM_InventoryTracking";
    
    /**
     * Check a villager's inventory for items that should be added to the stockpile.
     * Compares current inventory with tracked inventory state to detect new items.
     * Automatically transfers new items to the stockpile and removes them from the villager.
     * 
     * @param villager The villager to check
     * @param stockpile The stockpile to add items to
     * @return True if items were transferred, false otherwise
     */
    public static boolean collectItemsFromVillager(Villager villager, VillageStockpile stockpile) {
        boolean itemsTransferred = false;
        CompoundTag villagerTag = villager.getPersistentData();
        
        // Get or create inventory tracking data
        CompoundTag inventoryTracking;
        if (villagerTag.contains(NBT_INVENTORY_TRACKING, Tag.TAG_COMPOUND)) {
            inventoryTracking = villagerTag.getCompound(NBT_INVENTORY_TRACKING);
        } else {
            inventoryTracking = new CompoundTag();
        }
        
        // Check each slot in the villager's inventory
        for (int slot = 0; slot < villager.getInventory().getContainerSize(); slot++) {
            ItemStack stack = villager.getInventory().getItem(slot);
            
            if (stack.isEmpty()) {
                // Clear tracking for this slot if it's now empty
                inventoryTracking.remove("slot_" + slot);
                continue;
            }
            
            // Get the item's registry name
            ResourceLocation itemRegistry = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (itemRegistry == null) {
                LOGGER.debug("Could not find registry name for item {}", stack.getItem());
                continue;
            }
            String itemId = itemRegistry.toString();
            
            int currentCount = stack.getCount();
            int trackedCount = inventoryTracking.getInt("slot_" + slot + "_count");
            
            // Check if this item is new or increased
            if (currentCount > trackedCount) {
                int quantityAdded = currentCount - trackedCount;
                
                // Add to stockpile
                stockpile.addItems(itemId, quantityAdded);
                
                LOGGER.debug("Villager {} picked up {} x{} - added to stockpile", 
                    villager.getId(), itemId, quantityAdded);
                
                // Remove from villager inventory
                stack.shrink(quantityAdded);
                if (stack.isEmpty()) {
                    villager.getInventory().setItem(slot, ItemStack.EMPTY);
                }
                
                itemsTransferred = true;
            }
            
            // Update tracked count for next check
            inventoryTracking.putInt("slot_" + slot + "_count", stack.getCount());
            inventoryTracking.putString("slot_" + slot + "_item", itemId);
        }
        
        // Save updated tracking state
        villagerTag.put(NBT_INVENTORY_TRACKING, inventoryTracking);
        
        return itemsTransferred;
    }
    
    /**
     * Clear inventory tracking for a villager (useful when changing villagers or resetting)
     */
    public static void clearInventoryTracking(Villager villager) {
        villager.getPersistentData().remove(NBT_INVENTORY_TRACKING);
        LOGGER.debug("Cleared inventory tracking for villager {}", villager.getId());
    }
    
    /**
     * Get a summary of what items the villager currently has
     */
    public static Map<String, Integer> getCurrentInventorySummary(Villager villager) {
        Map<String, Integer> summary = new HashMap<>();
        
        for (int slot = 0; slot < villager.getInventory().getContainerSize(); slot++) {
            ItemStack stack = villager.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
                // Get the item's registry name
                ResourceLocation itemRegistry = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (itemRegistry != null) {
                    String itemId = itemRegistry.toString();
                    summary.put(itemId, summary.getOrDefault(itemId, 0) + stack.getCount());
                }
            }
        }
        
        return summary;
    }
    
    /**
     * Check if a villager's inventory has changed since the last check
     */
    public static boolean hasInventoryChanged(Villager villager) {
        CompoundTag villagerTag = villager.getPersistentData();
        
        if (!villagerTag.contains(NBT_INVENTORY_TRACKING, Tag.TAG_COMPOUND)) {
            // No tracking data yet, so it's a first check
            return villager.getInventory().getContainerSize() > 0;
        }
        
        CompoundTag inventoryTracking = villagerTag.getCompound(NBT_INVENTORY_TRACKING);
        
        // Check each slot for changes
        for (int slot = 0; slot < villager.getInventory().getContainerSize(); slot++) {
            ItemStack stack = villager.getInventory().getItem(slot);
            int trackedCount = inventoryTracking.getInt("slot_" + slot + "_count");
            
            if (stack.isEmpty() && trackedCount > 0) {
                return true;  // Item was removed
            }
            
            if (!stack.isEmpty() && stack.getCount() != trackedCount) {
                return true;  // Item count changed
            }
        }
        
        return false;
    }
}
