package com.github.syren_dev_tech.vwam;

import net.minecraft.nbt.CompoundTag;
import org.mockito.Mockito;

/**
 * Support utilities for testing Minecraft mod code
 * Handles mocking and setup of Minecraft classes
 */
public class MinecraftTestUtils {
    
    /**
     * Create a safe mock CompoundTag for testing
     * Avoids Minecraft static initializer issues
     */
    public static CompoundTag createMockCompoundTag() {
        CompoundTag tag = Mockito.mock(CompoundTag.class);
        
        // Set up default behavior for common operations
        Mockito.when(tag.isEmpty()).thenReturn(true);
        Mockito.when(tag.size()).thenReturn(0);
        
        return tag;
    }
    
    /**
     * Create a safe mock Villager for testing
     */
    public static net.minecraft.world.entity.npc.Villager createMockVillager() {
        net.minecraft.world.entity.npc.Villager villager = Mockito.mock(net.minecraft.world.entity.npc.Villager.class);
        
        // Set up default persistent data
        CompoundTag persistentData = createMockCompoundTag();
        Mockito.when(villager.getPersistentData()).thenReturn(persistentData);
        
        return villager;
    }
    
    /**
     * Check if Minecraft bootstrap is initialized
     */
    public static boolean isMinecraftInitialized() {
        try {
            Class<?> bootstrapClass = Class.forName("net.minecraft.Bootstrap");
            bootstrapClass.getMethod("bootStrap").invoke(null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
