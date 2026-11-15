package com.github.syren_dev_tech.vwam.village;

import net.minecraft.resources.ResourceLocation;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for VillageReputation
 * Tests core reputation tracking and threshold checks
 */
public class VillageReputationTest {

    private VillageReputation reputation;
    private ResourceLocation villageId;
    private UUID playerId;

    @Before
    public void setUp() {
        villageId = ResourceLocation.fromNamespaceAndPath("vwam", "test_village");
        playerId = UUID.randomUUID();
        
        // Standard configuration
        reputation = new VillageReputation(
            villageId,
            0.0f,      // initialReputation (neutral)
            1.0f,      // maxReputation
            -1.0f,     // minReputation
            -0.5f,     // hostileThreshold
            0.5f       // friendlyThreshold
        );
    }

    @Test
    public void testInitialReputation() {
        float rep = reputation.getReputation(playerId);
        assertEquals("Should start at 0.0", 0.0f, rep, 0.01f);
    }

    @Test
    public void testAddReputation() {
        reputation.addReputation(playerId, 0.1f);
        float rep = reputation.getReputation(playerId);
        
        assertEquals("Should be 0.1 after adding", 0.1f, rep, 0.01f);
    }

    @Test
    public void testAddNegativeReputation() {
        reputation.addReputation(playerId, -0.1f);
        float rep = reputation.getReputation(playerId);
        
        assertEquals("Should be -0.1 after negative add", -0.1f, rep, 0.01f);
    }

    @Test
    public void testReputationMaxClamped() {
        reputation.addReputation(playerId, 2.0f);
        float rep = reputation.getReputation(playerId);
        
        assertEquals("Should be clamped at 1.0", 1.0f, rep, 0.01f);
    }

    @Test
    public void testReputationMinClamped() {
        reputation.addReputation(playerId, -2.0f);
        float rep = reputation.getReputation(playerId);
        
        assertEquals("Should be clamped at -1.0", -1.0f, rep, 0.01f);
    }

    @Test
    public void testSetReputation() {
        reputation.setReputation(playerId, 0.75f);
        float rep = reputation.getReputation(playerId);
        
        assertEquals("Should be set to 0.75", 0.75f, rep, 0.01f);
    }

    @Test
    public void testSetReputationClamped() {
        reputation.setReputation(playerId, 1.5f);
        float rep = reputation.getReputation(playerId);
        
        assertEquals("Should clamp to 1.0", 1.0f, rep, 0.01f);
    }

    @Test
    public void testIsHostile() {
        reputation.setReputation(playerId, -0.6f);
        assertTrue("Should be hostile below -0.5", reputation.isHostile(playerId));
        
        reputation.setReputation(playerId, -0.5f);
        assertFalse("Should not be hostile at -0.5", reputation.isHostile(playerId));
    }

    @Test
    public void testIsFriendly() {
        reputation.setReputation(playerId, 0.4f);
        assertFalse("Should not be friendly below 0.5", reputation.isFriendly(playerId));
        
        reputation.setReputation(playerId, 0.5f);
        assertTrue("Should be friendly at 0.5", reputation.isFriendly(playerId));
        
        reputation.setReputation(playerId, 1.0f);
        assertTrue("Should be friendly at 1.0", reputation.isFriendly(playerId));
    }

    @Test
    public void testGetTradeDiscountMultiplier() {
        // No discount below friendly
        reputation.setReputation(playerId, 0.0f);
        float mult = reputation.getTradeDiscountMultiplier(playerId, 0.1f);
        assertEquals("No discount at neutral", 1.0f, mult, 0.01f);
        
        // 10% discount at 0.5
        reputation.setReputation(playerId, 0.5f);
        mult = reputation.getTradeDiscountMultiplier(playerId, 0.1f);
        assertEquals("10% discount at 0.5 rep", 0.9f, mult, 0.01f);
        
        // 20% discount at 0.6
        reputation.setReputation(playerId, 0.6f);
        mult = reputation.getTradeDiscountMultiplier(playerId, 0.1f);
        assertEquals("20% discount at 0.6 rep", 0.8f, mult, 0.01f);
        
        // 50% discount at 1.0
        reputation.setReputation(playerId, 1.0f);
        mult = reputation.getTradeDiscountMultiplier(playerId, 0.1f);
        assertEquals("50% discount at 1.0 rep", 0.5f, mult, 0.01f);
    }

    @Test
    public void testShouldVillagerFlee() {
        // Flee threshold is -0.25 (half of hostile threshold)
        reputation.setReputation(playerId, -0.24f);
        assertFalse("Should not flee at -0.24", reputation.shouldVillagerFlee(playerId));
        
        reputation.setReputation(playerId, -0.25f);
        assertTrue("Should flee at -0.25", reputation.shouldVillagerFlee(playerId));
    }

    @Test
    public void testShouldGolemAttack() {
        // Golem attacks at hostile threshold
        reputation.setReputation(playerId, -0.51f);
        assertTrue("Should attack below -0.5", reputation.shouldGolemAttack(playerId));
        
        reputation.setReputation(playerId, -0.5f);
        assertFalse("Should not attack at -0.5", reputation.shouldGolemAttack(playerId));
    }

    @Test
    public void testRecordTrade() {
        long gameTime = 1000;
        reputation.recordTrade(playerId, gameTime);
        
        // Just verify it doesn't throw
        // (recordTrade is simple, just stores time)
    }

    @Test
    public void testGetAllReputation() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        reputation.setReputation(player1, 0.5f);
        reputation.setReputation(player2, -0.3f);
        
        Map<UUID, Float> allReps = reputation.getAllReputation();
        
        assertEquals("Should have 2 players", 2, allReps.size());
        assertEquals("Player1 should be 0.5", 0.5f, (float)allReps.get(player1), 0.01f);
        assertEquals("Player2 should be -0.3", -0.3f, (float)allReps.get(player2), 0.01f);
    }

    @Test
    public void testGetVillageId() {
        assertEquals("Should return village id", villageId, reputation.getVillageId());
    }

    @Test
    public void testMultiplePlayersIndependent() {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        
        reputation.setReputation(player1, 0.8f);
        reputation.setReputation(player2, -0.6f);
        
        assertEquals("Player1 should be 0.8", 0.8f, reputation.getReputation(player1), 0.01f);
        assertEquals("Player2 should be -0.6", -0.6f, reputation.getReputation(player2), 0.01f);
    }

    @Test
    public void testDiscountPerStepVariation() {
        reputation.setReputation(playerId, 0.8f);
        
        // With 0.1 per step (default)
        float mult1 = reputation.getTradeDiscountMultiplier(playerId, 0.1f);
        assertEquals("40% discount with 0.1 step", 0.6f, mult1, 0.01f);
        
        // With 0.05 per step (more conservative)
        float mult2 = reputation.getTradeDiscountMultiplier(playerId, 0.05f);
        assertEquals("20% discount with 0.05 step", 0.8f, mult2, 0.01f);
        
        // With 0.2 per step (more aggressive)
        float mult3 = reputation.getTradeDiscountMultiplier(playerId, 0.2f);
        assertEquals("50% discount capped with 0.2 step", 0.5f, mult3, 0.01f);
    }

    @Test
    public void testScenarioFriendlyToHostile() {
        // Start neutral
        assertEquals("Start neutral", 0.0f, reputation.getReputation(playerId), 0.01f);
        assertFalse("Not hostile", reputation.isHostile(playerId));
        assertFalse("Not friendly", reputation.isFriendly(playerId));
        
        // Build up reputation
        for (int i = 0; i < 10; i++) {
            reputation.addReputation(playerId, 0.05f);
        }
        assertEquals("After gains, should be 0.5", 0.5f, reputation.getReputation(playerId), 0.01f);
        assertFalse("Not hostile yet", reputation.isHostile(playerId));
        assertTrue("Now friendly", reputation.isFriendly(playerId));
        
        // Continue building
        for (int i = 0; i < 5; i++) {
            reputation.addReputation(playerId, 0.1f);
        }
        assertEquals("After more gains, should be 1.0", 1.0f, reputation.getReputation(playerId), 0.01f);
        assertTrue("Very friendly", reputation.isFriendly(playerId));
        
        // Lose reputation
        for (int i = 0; i < 20; i++) {
            reputation.addReputation(playerId, -0.1f);
        }
        assertEquals("After losses, should be -1.0 clamped", -1.0f, reputation.getReputation(playerId), 0.01f);
        assertTrue("Now hostile", reputation.isHostile(playerId));
    }

    @Test
    public void testDiscountMultiplierEdgeCases() {
        float discountStep = 0.1f;
        
        // Edge case: exactly at friendly threshold
        reputation.setReputation(playerId, 0.5f);
        float mult = reputation.getTradeDiscountMultiplier(playerId, discountStep);
        assertEquals("Should have 10% discount exactly at threshold", 0.9f, mult, 0.01f);
        
        // Edge case: just below friendly
        reputation.setReputation(playerId, 0.49f);
        mult = reputation.getTradeDiscountMultiplier(playerId, discountStep);
        assertEquals("Should have no discount just below threshold", 1.0f, mult, 0.01f);
        
        // Edge case: exactly at max
        reputation.setReputation(playerId, 1.0f);
        mult = reputation.getTradeDiscountMultiplier(playerId, discountStep);
        assertEquals("Should have 50% discount at max", 0.5f, mult, 0.01f);
    }
}
