package com.github.syren_dev_tech.vwam.villager;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.nbt.CompoundTag;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Ignore;
import com.github.syren_dev_tech.vwam.MinecraftTestUtils;

/**
 * Unit tests for VillagerRoutineManager
 * Tests villager routine caching and daily reset management
 * 
 * NOTE: These tests require Minecraft game context (registries, static initializers).
 * They are disabled pending integration test setup.
 */
@Ignore("Requires Minecraft game context - move to integration tests")
public class VillagerRoutineManagerTest {

    private VillagerRoutineManager manager;

    @Mock
    private Villager mockVillager1;
    
    @Mock
    private Villager mockVillager2;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Reset singleton
        manager = VillagerRoutineManager.getInstance();
        manager.clear();
        
        // Setup mock villagers with test utilities to avoid Minecraft initialization
        when(mockVillager1.getId()).thenReturn(1);
        when(mockVillager1.getPersistentData()).thenReturn(MinecraftTestUtils.createMockCompoundTag());
        
        when(mockVillager2.getId()).thenReturn(2);
        when(mockVillager2.getPersistentData()).thenReturn(MinecraftTestUtils.createMockCompoundTag());
    }

    @Test
    public void testSingletonPattern() {
        VillagerRoutineManager manager1 = VillagerRoutineManager.getInstance();
        VillagerRoutineManager manager2 = VillagerRoutineManager.getInstance();
        
        assertSame("Singleton should return same instance", manager1, manager2);
    }

    @Test
    public void testGetOrCreateRoutineStatus() {
        VillagerRoutineStatus status1 = manager.getRoutineStatus(mockVillager1);
        VillagerRoutineStatus status2 = manager.getRoutineStatus(mockVillager1);
        
        assertNotNull("Should create routine status", status1);
        assertSame("Should return cached status", status1, status2);
    }

    @Test
    public void testMultipleVillagersCached() {
        VillagerRoutineStatus status1 = manager.getRoutineStatus(mockVillager1);
        VillagerRoutineStatus status2 = manager.getRoutineStatus(mockVillager2);
        
        assertNotNull("Should create status for villager 1", status1);
        assertNotNull("Should create status for villager 2", status2);
        assertNotSame("Should be different instances", status1, status2);
    }

    @Test
    public void testDailyResetOnce() {
        // Get routines for both villagers
        VillagerRoutineStatus status1 = manager.getRoutineStatus(mockVillager1);
        VillagerRoutineStatus status2 = manager.getRoutineStatus(mockVillager2);
        
        // Set statuses
        status1.setRested();
        status2.setRested();
        assertTrue("Should be set", status1.isRested());
        assertTrue("Should be set", status2.isRested());
        
        // Trigger daily reset
        manager.onServerTick(24000); // Day 1
        
        // Both should be reset
        assertFalse("Should reset villager 1", status1.isRested());
        assertFalse("Should reset villager 2", status2.isRested());
    }

    @Test
    public void testDailyResetOnlyOncePerDay() {
        VillagerRoutineStatus status = manager.getRoutineStatus(mockVillager1);
        
        // First reset
        status.setRested();
        manager.onServerTick(24000); // Day 1
        assertFalse("First reset should happen", status.isRested());
        
        // Set again within same day
        status.setRested();
        assertTrue("Should be set again", status.isRested());
        
        // Same day tick should not reset
        manager.onServerTick(24100);
        assertTrue("Should NOT reset on same day", status.isRested());
        
        // New day should reset
        manager.onServerTick(48000); // Day 2
        assertFalse("Should reset on new day", status.isRested());
    }

    @Test
    public void testRemoveRoutineStatus() {
        VillagerRoutineStatus status = manager.getRoutineStatus(mockVillager1);
        status.setRested();
        
        // Remove it
        manager.removeRoutineStatus(mockVillager1.getId());
        
        // New request should create fresh instance
        VillagerRoutineStatus newStatus = manager.getRoutineStatus(mockVillager1);
        assertFalse("New instance should not be rested", newStatus.isRested());
    }

    @Test
    public void testClear() {
        // Setup multiple villagers
        VillagerRoutineStatus status1 = manager.getRoutineStatus(mockVillager1);
        VillagerRoutineStatus status2 = manager.getRoutineStatus(mockVillager2);
        
        status1.setRested();
        status2.setSocialized();
        
        // Clear everything
        manager.clear();
        
        // New requests should create fresh instances
        VillagerRoutineStatus newStatus1 = manager.getRoutineStatus(mockVillager1);
        VillagerRoutineStatus newStatus2 = manager.getRoutineStatus(mockVillager2);
        
        assertFalse("Should be fresh instance", newStatus1.isRested());
        assertFalse("Should be fresh instance", newStatus2.isSocialized());
    }

    @Test
    public void testGetStats() {
        manager.getRoutineStatus(mockVillager1);
        manager.getRoutineStatus(mockVillager2);
        
        String stats = manager.getStats();
        
        assertNotNull("Should return stats", stats);
        assertTrue("Stats should mention total", stats.contains("Total: 2"));
    }

    @Test
    public void testDailyResetWithLargeDayGap() {
        VillagerRoutineStatus status = manager.getRoutineStatus(mockVillager1);
        
        status.setRested();
        manager.onServerTick(24000); // Day 1
        assertFalse("Day 1 reset", status.isRested());
        
        status.setRested();
        manager.onServerTick(24000 * 10); // Day 10 (large gap)
        assertFalse("Day 10 reset should happen", status.isRested());
    }

    @Test
    public void testMultipleTicksInSameDay() {
        VillagerRoutineStatus status = manager.getRoutineStatus(mockVillager1);
        status.setRested();
        
        // Multiple ticks on same day
        manager.onServerTick(12000); // Day 0 (first half)
        assertTrue("Should not reset mid-day", status.isRested());
        
        manager.onServerTick(23000); // Day 0 (end of day)
        assertTrue("Should not reset before day boundary", status.isRested());
        
        manager.onServerTick(24000); // Day 1 (new day)
        assertFalse("Should reset on new day", status.isRested());
    }

    @Test
    public void testScenarioFullDaily() {
        // Setup
        VillagerRoutineStatus v1 = manager.getRoutineStatus(mockVillager1);
        VillagerRoutineStatus v2 = manager.getRoutineStatus(mockVillager2);
        
        // Day 1 morning (tick 0)
        manager.onServerTick(24000);
        assertFalse("Day 1: Cannot produce", v1.canProduce());
        assertFalse("Day 1: Cannot trade", v1.canTrade());
        
        // Villager 1 sleeps and validates
        v1.setRested();
        v1.setAbleToWork(true);
        assertTrue("Day 1: Can produce", v1.canProduce());
        
        // Villager 1 socializes
        v1.setSocialized();
        assertTrue("Day 1: Can trade", v1.canTrade());
        
        // Villager 2 only rests
        v2.setRested();
        assertFalse("Day 1: V2 cannot produce", v2.canProduce());
        
        // Day 2 starts (tick 48000)
        manager.onServerTick(48000);
        
        // Both should be reset
        assertFalse("Day 2: V1 reset", v1.isRested());
        assertFalse("Day 2: V2 reset", v2.isRested());
        assertFalse("Day 2: V1 reset", v1.canTrade());
        assertFalse("Day 2: V2 reset", v2.canTrade());
    }
}
