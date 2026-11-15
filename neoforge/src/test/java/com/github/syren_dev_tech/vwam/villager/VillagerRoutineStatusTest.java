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
 * Unit tests for VillagerRoutineStatus
 * Tests villager daily routine tracking and status management
 * 
 * NOTE: These tests require Minecraft game context (registries, static initializers).
 * They are disabled pending integration test setup.
 */
@Ignore("Requires Minecraft game context - move to integration tests")
public class VillagerRoutineStatusTest {

    @Mock
    private Villager mockVillager;

    private VillagerRoutineStatus routineStatus;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mock villager with persistent data using test utilities
        mockVillager = MinecraftTestUtils.createMockVillager();
        CompoundTag persistentData = MinecraftTestUtils.createMockCompoundTag();
        when(mockVillager.getPersistentData()).thenReturn(persistentData);
        
        routineStatus = new VillagerRoutineStatus(mockVillager);
    }

    @Test
    public void testInitialStatus() {
        // Initially, all statuses should be false
        assertFalse("Should start not rested", routineStatus.isRested());
        assertFalse("Should start not socialized", routineStatus.isSocialized());
        assertFalse("Should start not able to work", routineStatus.isAbleToWork());
    }

    @Test
    public void testSetRested() {
        // Set rested status
        routineStatus.setRested();
        
        assertTrue("Should be marked as rested", routineStatus.isRested());
        assertFalse("Should not be marked as socialized", routineStatus.isSocialized());
    }

    @Test
    public void testSetSocialized() {
        // Set socialized status
        routineStatus.setSocialized();
        
        assertFalse("Should not be marked as rested", routineStatus.isRested());
        assertTrue("Should be marked as socialized", routineStatus.isSocialized());
    }

    @Test
    public void testSetAbleToWork() {
        // Set able to work status
        routineStatus.setAbleToWork(true);
        
        assertTrue("Should be marked as able to work", routineStatus.isAbleToWork());
    }

    @Test
    public void testCanProduceRequiresRestedAndAbleToWork() {
        // Can't produce without rested
        assertFalse("Cannot produce if not rested", routineStatus.canProduce());
        
        // Still can't produce with only one status
        routineStatus.setRested();
        assertFalse("Cannot produce if not able to work", routineStatus.canProduce());
        
        // Now can produce with both
        routineStatus.setAbleToWork(true);
        assertTrue("Can produce when both rested and able to work", routineStatus.canProduce());
    }

    @Test
    public void testCanTradeRequiresSocialized() {
        // Can't trade without socialized
        assertFalse("Cannot trade if not socialized", routineStatus.canTrade());
        
        // Can trade when socialized
        routineStatus.setSocialized();
        assertTrue("Can trade when socialized", routineStatus.canTrade());
    }

    @Test
    public void testCanProduceAndCanTradeAreIndependent() {
        // Trade requires socialized, production requires rested + able to work
        routineStatus.setRested();
        routineStatus.setAbleToWork(true);
        
        // Can produce but cannot trade
        assertTrue("Should be able to produce", routineStatus.canProduce());
        assertFalse("Should not be able to trade", routineStatus.canTrade());
        
        // Add socialization
        routineStatus.setSocialized();
        
        // Now can do both
        assertTrue("Should be able to produce", routineStatus.canProduce());
        assertTrue("Should be able to trade", routineStatus.canTrade());
    }

    @Test
    public void testDailyReset() {
        // Set all statuses
        routineStatus.setRested();
        routineStatus.setSocialized();
        routineStatus.setAbleToWork(true);
        
        // Reset for new day
        routineStatus.checkAndResetDaily(1);
        
        // All should be false after reset
        assertFalse("Should be reset after new day", routineStatus.isRested());
        assertFalse("Should be reset after new day", routineStatus.isSocialized());
        assertFalse("Should be reset after new day", routineStatus.isAbleToWork());
    }

    @Test
    public void testDailyResetOnlyOncePerDay() {
        // Set statuses
        routineStatus.setRested();
        routineStatus.setSocialized();
        routineStatus.setAbleToWork(true);
        
        // First reset
        routineStatus.checkAndResetDaily(1);
        assertTrue("Should be reset", !routineStatus.isRested());
        
        // Set again
        routineStatus.setRested();
        assertTrue("Should be set", routineStatus.isRested());
        
        // Second reset on same day should not happen
        routineStatus.checkAndResetDaily(1);
        assertTrue("Should NOT reset again on same day", routineStatus.isRested());
        
        // But new day should reset
        routineStatus.checkAndResetDaily(2);
        assertFalse("Should reset on new day", routineStatus.isRested());
    }

    @Test
    public void testStatusNBTPersistence() {
        // Set statuses
        routineStatus.setRested();
        routineStatus.setSocialized();
        routineStatus.setAbleToWork(true);
        
        // Create new instance with same villager mock
        // (Should load from NBT)
        VillagerRoutineStatus routineStatus2 = new VillagerRoutineStatus(mockVillager);
        
        // Should have loaded persisted statuses
        assertTrue("Should persist rested status", routineStatus2.isRested());
        assertTrue("Should persist socialized status", routineStatus2.isSocialized());
        assertTrue("Should persist able to work status", routineStatus2.isAbleToWork());
    }

    @Test
    public void testGetStatusString() {
        routineStatus.setRested();
        // Don't set socialized or able to work
        
        String status = routineStatus.getStatusString();
        
        assertTrue("Status string should mention Rested", status.contains("Rested: true"));
        assertTrue("Status string should mention Socialized", status.contains("Socialized: false"));
        assertTrue("Status string should mention AbleToWork", status.contains("AbleToWork: false"));
    }

    @Test
    public void testMultipleRepeatedStatusSets() {
        // Setting same status multiple times should be idempotent
        routineStatus.setRested();
        routineStatus.setRested();
        routineStatus.setRested();
        
        assertTrue("Should be rested after multiple sets", routineStatus.isRested());
    }

    @Test
    public void testComplexScenario() {
        // Scenario: Villager goes through full daily cycle
        
        // Morning: Nothing set yet
        assertFalse("Morning: Cannot produce", routineStatus.canProduce());
        assertFalse("Morning: Cannot trade", routineStatus.canTrade());
        
        // Villager sleeps
        routineStatus.setRested();
        assertTrue("After sleep: Can produce if validated", routineStatus.isRested());
        
        // Workshop validates
        routineStatus.setAbleToWork(true);
        assertTrue("After validation: Can produce", routineStatus.canProduce());
        
        // Villager socializes at bell
        routineStatus.setSocialized();
        assertTrue("After socializing: Can trade", routineStatus.canTrade());
        
        // Player interacts
        assertTrue("Full day: Can produce AND trade", 
            routineStatus.canProduce() && routineStatus.canTrade());
        
        // New day arrives
        routineStatus.checkAndResetDaily(2);
        assertFalse("New day: Cannot produce", routineStatus.canProduce());
        assertFalse("New day: Cannot trade", routineStatus.canTrade());
    }
}
