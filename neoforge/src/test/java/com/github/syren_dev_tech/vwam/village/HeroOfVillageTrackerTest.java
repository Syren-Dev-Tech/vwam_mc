package com.github.syren_dev_tech.vwam.village;

import net.minecraft.resources.ResourceLocation;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Unit tests for HeroOfVillageTracker
 * Tests hero of the village buff tracking and expiration
 */
public class HeroOfVillageTrackerTest {

    private HeroOfVillageTracker tracker;
    private UUID playerId;
    private ResourceLocation villageId;

    @Before
    public void setUp() {
        tracker = HeroOfVillageTracker.getInstance();
        tracker.clear();
        
        playerId = UUID.randomUUID();
        villageId = ResourceLocation.fromNamespaceAndPath("vwam", "test_village");
    }

    @Test
    public void testApplyHeroBuff() {
        tracker.applyHeroBuff(playerId, villageId, 0);
        
        assertTrue("Should have active buff", 
            tracker.hasActiveBuff(playerId, villageId, 1000));
    }

    @Test
    public void testBuffWrongVillageNotActive() {
        tracker.applyHeroBuff(playerId, villageId, 0);
        
        ResourceLocation differentVillage = ResourceLocation.fromNamespaceAndPath("vwam", "other_village");
        assertFalse("Should not have buff for different village", 
            tracker.hasActiveBuff(playerId, differentVillage, 1000));
    }

    @Test
    public void testGetProductivityMultiplierWithBuff() {
        tracker.applyHeroBuff(playerId, villageId, 0);
        
        float multiplier = tracker.getProductivityMultiplier(playerId, villageId, 1000);
        assertEquals("Should be 2x with buff", 2.0f, multiplier, 0.01f);
    }

    @Test
    public void testGetProductivityMultiplierWithoutBuff() {
        float multiplier = tracker.getProductivityMultiplier(playerId, villageId, 1000);
        assertEquals("Should be 1x without buff", 1.0f, multiplier, 0.01f);
    }

    @Test
    public void testBuffExpiration() {
        long startTime = 0;
        long durationTicks = 24 * 60 * 60 * 20L; // 24 hours
        
        tracker.applyHeroBuff(playerId, villageId, startTime);
        
        // Before expiration
        assertTrue("Should be active before expiration", 
            tracker.hasActiveBuff(playerId, villageId, startTime + durationTicks - 1));
        
        // At expiration
        assertFalse("Should be expired at duration", 
            tracker.hasActiveBuff(playerId, villageId, startTime + durationTicks));
        
        // After expiration
        assertFalse("Should be expired after duration", 
            tracker.hasActiveBuff(playerId, villageId, startTime + durationTicks + 1000));
    }

    @Test
    public void testGetRemainingTime() {
        long startTime = 0;
        long durationTicks = 24 * 60 * 60 * 20L;
        
        tracker.applyHeroBuff(playerId, villageId, startTime);
        
        // At start
        long remaining = tracker.getRemainingTime(playerId, startTime);
        assertEquals("Should have full duration at start", durationTicks, remaining, 100);
        
        // Halfway through
        remaining = tracker.getRemainingTime(playerId, startTime + durationTicks / 2);
        assertTrue("Should have roughly half time remaining", 
            remaining > durationTicks / 2 - 100 && remaining < durationTicks / 2 + 100);
        
        // After expiration
        remaining = tracker.getRemainingTime(playerId, startTime + durationTicks + 1000);
        assertEquals("Should be 0 after expiration", 0, remaining);
    }

    @Test
    public void testRemoveBuff() {
        tracker.applyHeroBuff(playerId, villageId, 0);
        assertTrue("Should have buff", tracker.hasActiveBuff(playerId, villageId, 1000));
        
        tracker.removeBuff(playerId);
        assertFalse("Should not have buff after removal", 
            tracker.hasActiveBuff(playerId, villageId, 1000));
    }

    @Test
    public void testMultiplePlayersIndependent() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        tracker.applyHeroBuff(player1, villageId, 0);
        
        assertTrue("Player 1 should have buff", 
            tracker.hasActiveBuff(player1, villageId, 1000));
        assertFalse("Player 2 should not have buff", 
            tracker.hasActiveBuff(player2, villageId, 1000));
    }

    @Test
    public void testMultipleVillagesIndependent() {
        ResourceLocation village1 = ResourceLocation.fromNamespaceAndPath("vwam", "village1");
        ResourceLocation village2 = ResourceLocation.fromNamespaceAndPath("vwam", "village2");
        
        tracker.applyHeroBuff(playerId, village1, 0);
        
        assertTrue("Should have buff for village1", 
            tracker.hasActiveBuff(playerId, village1, 1000));
        assertFalse("Should not have buff for village2", 
            tracker.hasActiveBuff(playerId, village2, 1000));
    }

    @Test
    public void testClear() {
        tracker.applyHeroBuff(playerId, villageId, 0);
        tracker.clear();
        
        assertFalse("Should be cleared", 
            tracker.hasActiveBuff(playerId, villageId, 1000));
    }

    @Test
    public void testGetStats() {
        tracker.applyHeroBuff(playerId, villageId, 0);
        String stats = tracker.getStats();
        
        assertNotNull("Should return stats", stats);
        assertTrue("Stats should mention active buffs", stats.contains("Active buffs: 1"));
    }

    @Test
    public void testBuffReplacement() {
        // Apply buff
        tracker.applyHeroBuff(playerId, villageId, 0);
        
        // Apply same buff again (replaces)
        tracker.applyHeroBuff(playerId, villageId, 10000);
        
        // Should still be active but with new start time
        long remaining = tracker.getRemainingTime(playerId, 10000);
        long durationTicks = 24 * 60 * 60 * 20L;
        assertEquals("Should have full duration from new start time", durationTicks, remaining, 100);
    }

    @Test
    public void testScenarioFullRaidToExpiration() {
        long raidTime = 0;
        
        // Player wins raid
        tracker.applyHeroBuff(playerId, villageId, raidTime);
        assertEquals("Should be 2x right after raid", 2.0f, 
            tracker.getProductivityMultiplier(playerId, villageId, raidTime), 0.01f);
        
        // 12 hours later (halfway)
        long halfDay = 12 * 60 * 60 * 20L;
        assertEquals("Should still be 2x at 12h", 2.0f,
            tracker.getProductivityMultiplier(playerId, villageId, raidTime + halfDay), 0.01f);
        
        // 23.5 hours later (almost expired)
        long almostFull = (long)(23.5 * 60 * 60 * 20);
        assertEquals("Should still be 2x at 23.5h", 2.0f,
            tracker.getProductivityMultiplier(playerId, villageId, raidTime + almostFull), 0.01f);
        
        // 24+ hours later (expired)
        long fullDay = 24 * 60 * 60 * 20L;
        assertEquals("Should be 1x after 24h", 1.0f,
            tracker.getProductivityMultiplier(playerId, villageId, raidTime + fullDay + 1000), 0.01f);
    }
}
