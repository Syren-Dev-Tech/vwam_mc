package com.github.syren_dev_tech.vwam.village.reputation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for player reputation system
 * 
 * NOTE: These tests require full Minecraft game context for entity initialization.
 * Marked as @Ignore to skip in normal test runs. Move to integration tests for proper execution.
 */
@Ignore("Requires Minecraft game context - move to integration tests")
public class PlayerReputationTest {
    
    @Mock
    private Player player;
    private PlayerVillageReputation reputation;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        CompoundTag tag = new CompoundTag();
        when(player.getPersistentData()).thenReturn(tag);
        when(player.getUUID()).thenReturn(java.util.UUID.randomUUID());
        when(player.getName()).thenReturn(mock(net.minecraft.network.chat.Component.class));
        when(player.getName().getString()).thenReturn("TestPlayer");
        
        reputation = new PlayerVillageReputation(player, "village_1");
    }
    
    @Test
    public void testDefaultReputationIsNeutral() {
        assertEquals(0.0, reputation.getReputation(), 0.001);
        assertTrue(reputation.isNeutral());
        assertFalse(reputation.isPositive());
        assertFalse(reputation.isNegative());
    }
    
    @Test
    public void testPositiveReputation() {
        reputation.setReputation(0.5);
        assertEquals(0.5, reputation.getReputation(), 0.001);
        assertTrue(reputation.isPositive());
        assertFalse(reputation.isNegative());
    }
    
    @Test
    public void testNegativeReputation() {
        reputation.setReputation(-0.5);
        assertEquals(-0.5, reputation.getReputation(), 0.001);
        assertTrue(reputation.isNegative());
        assertFalse(reputation.isPositive());
    }
    
    @Test
    public void testReputationClamping() {
        reputation.setReputation(2.0); // Above max
        assertEquals(1.0, reputation.getReputation(), 0.001);
        
        reputation.setReputation(-2.0); // Below min
        assertEquals(-1.0, reputation.getReputation(), 0.001);
    }
    
    @Test
    public void testApplyPositiveEvent() {
        ReputationEventConfig config = new ReputationEventConfig();
        double change = reputation.applyEvent(ReputationEventType.TRADE_COMPLETED, config);
        
        assertTrue(change > 0);
        assertEquals(change, reputation.getReputation(), 0.001);
    }
    
    @Test
    public void testApplyNegativeEvent() {
        ReputationEventConfig config = new ReputationEventConfig();
        double change = reputation.applyEvent(ReputationEventType.VILLAGER_KILLED, config);
        
        assertTrue(change < 0);
        assertEquals(change, reputation.getReputation(), 0.001);
    }
    
    @Test
    public void testMultipleEvents() {
        ReputationEventConfig config = new ReputationEventConfig();
        
        // Trade 5 times
        for (int i = 0; i < 5; i++) {
            reputation.applyEvent(ReputationEventType.TRADE_COMPLETED, config);
        }
        
        double afterTrades = reputation.getReputation();
        assertTrue(afterTrades > 0);
        
        // Kill a villager
        reputation.applyEvent(ReputationEventType.VILLAGER_KILLED, config);
        
        double afterKill = reputation.getReputation();
        assertTrue(afterKill < afterTrades);
    }
    
    @Test
    public void testHostileThreshold() {
        reputation.setReputation(-0.6);
        assertTrue(reputation.isHostile());
        
        reputation.setReputation(-0.4);
        assertFalse(reputation.isHostile());
    }
    
    @Test
    public void testCanTrade() {
        reputation.setReputation(-0.6);
        assertFalse(reputation.canTrade());
        
        reputation.setReputation(-0.4);
        assertTrue(reputation.canTrade());
        
        reputation.setReputation(0.5);
        assertTrue(reputation.canTrade());
    }
    
    @Test
    public void testCanAccessServices() {
        reputation.setReputation(0.0);
        assertFalse(reputation.canAccessServices());
        
        reputation.setReputation(0.1);
        assertTrue(reputation.canAccessServices());
        
        reputation.setReputation(-0.1);
        assertFalse(reputation.canAccessServices());
    }
    
    @Test
    public void testTradePriceModifier() {
        // Below 0.5: No discount
        reputation.setReputation(0.4);
        assertEquals(1.0, reputation.getTradePriceModifier(), 0.001);
        
        // At 0.5: No discount
        reputation.setReputation(0.5);
        assertEquals(1.0, reputation.getTradePriceModifier(), 0.001);
        
        // At 0.6: 10% discount (0.9x multiplier)
        reputation.setReputation(0.6);
        assertEquals(0.9, reputation.getTradePriceModifier(), 0.001);
        
        // At 0.7: 20% discount (0.8x multiplier)
        reputation.setReputation(0.7);
        assertEquals(0.8, reputation.getTradePriceModifier(), 0.001);
        
        // At 0.8: 30% discount (0.7x multiplier)
        reputation.setReputation(0.8);
        assertEquals(0.7, reputation.getTradePriceModifier(), 0.001);
        
        // At 0.9: 40% discount (0.6x multiplier)
        reputation.setReputation(0.9);
        assertEquals(0.6, reputation.getTradePriceModifier(), 0.001);
        
        // At 1.0: 50% discount (0.5x multiplier)
        reputation.setReputation(1.0);
        assertEquals(0.5, reputation.getTradePriceModifier(), 0.001);
    }
    
    @Test
    public void testReputationLevels() {
        reputation.setReputation(-1.0);
        assertEquals("Hostile", reputation.getReputationLevel());
        
        reputation.setReputation(-0.7);
        assertEquals("Hostile", reputation.getReputationLevel());
        
        reputation.setReputation(-0.3);
        assertEquals("Unfriendly", reputation.getReputationLevel());
        
        reputation.setReputation(0.0);
        assertEquals("Neutral", reputation.getReputationLevel());
        
        reputation.setReputation(0.2);
        assertEquals("Agreeable", reputation.getReputationLevel());
        
        reputation.setReputation(0.5);
        assertEquals("Friendly", reputation.getReputationLevel());
        
        reputation.setReputation(0.7);
        assertEquals("Very Friendly", reputation.getReputationLevel());
        
        reputation.setReputation(0.95);
        assertEquals("Beloved", reputation.getReputationLevel());
    }
    
    @Test
    public void testCustomEventChange() {
        double change = reputation.applyEvent(ReputationEventType.TRADE_COMPLETED, 0.25);
        assertEquals(0.25, reputation.getReputation(), 0.001);
        assertEquals(0.25, change, 0.001);
    }
    
    @Test
    public void testCustomEventChangeWithClamping() {
        reputation.setReputation(0.9);
        double change = reputation.applyEvent(ReputationEventType.RAID_DEFEATED, 0.3);
        
        // Should clamp to 1.0
        assertEquals(1.0, reputation.getReputation(), 0.001);
        // Actual change should be less than requested
        assertEquals(0.1, change, 0.001);
    }
    
    @Test
    public void testStatusString() {
        reputation.setReputation(0.7);
        String status = reputation.getStatusString();
        
        assertNotNull(status);
        assertTrue(status.contains("village_1"));
        assertTrue(status.contains("0.70"));
    }
}
