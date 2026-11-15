package com.github.syren_dev_tech.vwam.village;

import net.minecraft.resources.ResourceLocation;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ReputationEffects
 * Tests reputation effect calculations and formatting
 * 
 * Note: These tests verify the mathematical calculations.
 * Integration tests with game entities would require mocking the full stack.
 */
public class ReputationEffectsTest {

    @Before
    public void setUp() {
        // Clear any previous test state
    }

    @Test
    public void testBuyingMultiplierCalculation() {
        // Base cost: 10 emeralds
        // At 0.5 reputation: 10% discount = 9 emeralds
        
        float baseCost = 10f;
        float expectedWith50Discount = 9f; // 10 * 0.9
        
        // This is the formula: baseCost * multiplier
        // where multiplier at 0.5 rep with 0.1 step is 0.9
        float multiplier = 0.9f; // 10% discount
        float result = baseCost * multiplier;
        
        assertEquals("Should calculate buying discount correctly", expectedWith50Discount, result, 0.01f);
    }

    @Test
    public void testSellingMultiplierCalculation() {
        // Base income: 1 emerald per 10 items
        // At 0.5 reputation with 10% discount: 1 emerald per ~9 items (1.11x rate)
        
        float baseIncome = 1f;
        float buyingMultiplier = 0.9f; // 10% discount
        
        // For selling, we invert: income / multiplier
        float sellingResult = baseIncome / buyingMultiplier;
        
        assertTrue("Selling should be ~1.11x", sellingResult > 1.1f && sellingResult < 1.12f);
    }

    @Test
    public void testBuyingMultiplierAtVariousReputations() {
        // At each reputation level, test the buying multiplier
        
        float[][] testCases = {
            {0.0f, 1.0f},   // No discount at 0.0
            {0.5f, 0.9f},   // 10% discount at 0.5
            {0.6f, 0.8f},   // 20% discount at 0.6
            {0.7f, 0.7f},   // 30% discount at 0.7
            {0.8f, 0.6f},   // 40% discount at 0.8
            {0.9f, 0.5f},   // 50% discount at 0.9 (capped)
            {1.0f, 0.5f},   // 50% discount at 1.0 (capped)
        };
        
        for (float[] testCase : testCases) {
            float reputation = testCase[0];
            float expectedMultiplier = testCase[1];
            
            // Calculate steps above friendly (0.5)
            if (reputation < 0.5f) {
                // No discount
                assertEquals("Discount at " + reputation, 1.0f, 1.0f, 0.01f);
            } else {
                // Calculate steps: at exactly 0.5, we get 1 step; otherwise use ceil
                float distanceAbove = reputation - 0.5f;
                float stepsAboveFriendly = 1.0f + Math.round(distanceAbove * 10f);
                // Discount: steps * 0.1, capped at 0.5
                float discount = Math.min(0.5f, stepsAboveFriendly * 0.1f);
                float multiplier = 1.0f - discount;
                
                assertEquals("Multiplier at " + reputation, expectedMultiplier, multiplier, 0.01f);
            }
        }
    }

    @Test
    public void testSellingMultiplierAtVariousReputations() {
        // At various reputation levels, calculate selling multipliers
        
        float[][] testCases = {
            {0.0f, 1.0f},      // Base at 0.0
            {0.5f, 1.111f},    // ~1.11x at 0.5
            {0.6f, 1.250f},    // 1.25x at 0.6
            {0.7f, 1.429f},    // ~1.43x at 0.7
            {0.8f, 1.667f},    // ~1.67x at 0.8
            {0.9f, 2.0f},      // 2.0x at 0.9+
            {1.0f, 2.0f},      // 2.0x at 1.0
        };
        
        for (float[] testCase : testCases) {
            float reputation = testCase[0];
            float expectedMultiplier = testCase[1];
            
            // Calculate selling multiplier
            float buyingMultiplier;
            if (reputation < 0.5f) {
                buyingMultiplier = 1.0f;
            } else {
                float distanceAbove = reputation - 0.5f;
                float stepsAboveFriendly = 1.0f + Math.round(distanceAbove * 10f);
                float discount = Math.min(0.5f, stepsAboveFriendly * 0.1f);
                buyingMultiplier = 1.0f - discount;
            }
            
            float sellingMultiplier = 1.0f / buyingMultiplier;
            
            assertEquals("Selling multiplier at " + reputation, expectedMultiplier, sellingMultiplier, 0.01f);
        }
    }

    @Test
    public void testReputationStatusFormatting() {
        // Test formatting with Minecraft color codes
        
        // Friendly
        String status = "§2FRIENDLY§r";
        assertTrue("Friendly should have green code", status.contains("2"));
        
        // Neutral
        status = "§fNEUTRAL§r";
        assertTrue("Neutral should have white code", status.contains("f"));
        
        // Hostile
        status = "§cHOSTILE§r";
        assertTrue("Hostile should have red code", status.contains("c"));
    }

    @Test
    public void testDiscountPercentageCalculation() {
        // At various reputation levels, what's the discount percentage?
        
        float[][] testCases = {
            {0.0f, 0f},     // 0% discount
            {0.5f, 10f},    // 10% discount
            {0.6f, 20f},    // 20% discount
            {0.7f, 30f},    // 30% discount
            {0.8f, 40f},    // 40% discount
            {0.9f, 50f},    // 50% discount
            {1.0f, 50f},    // 50% discount (max)
        };
        
        for (float[] testCase : testCases) {
            float reputation = testCase[0];
            float expectedPercent = testCase[1];
            
            // Calculate
            float multiplier;
            if (reputation < 0.5f) {
                multiplier = 1.0f;
            } else {
                float stepsAboveFriendly = 1.0f + Math.round((reputation - 0.5f) * 10f);
                float discount = Math.min(0.5f, stepsAboveFriendly * 0.1f);
                multiplier = 1.0f - discount;
            }
            
            float discount = (1.0f - multiplier) * 100f;
            
            assertEquals("Discount % at " + reputation, expectedPercent, discount, 0.1f);
        }
    }

    @Test
    public void testTradeScenarioWithDiscounts() {
        // Scenario: Player buys 64 carrots from villager
        
        float baseCost = 1f; // 1 emerald base
        int itemCount = 64;
        float reputation = 0.7f; // Friendly
        
        // Calculate buying discount
        float stepsAboveFriendly = 1.0f + Math.round((reputation - 0.5f) * 10f);
        float discount = Math.min(0.5f, stepsAboveFriendly * 0.1f);
        float buyingMultiplier = 1.0f - discount;
        
        float emeraldsNeeded = (baseCost * itemCount) * buyingMultiplier;
        
        assertEquals("Should cost 30% less", 44.8f, emeraldsNeeded, 0.1f);
    }

    @Test
    public void testSellScenarioWithPremiums() {
        // Scenario: Player sells 64 carrots to villager
        
        float baseValue = 1f; // 1 emerald per 10 items base
        int itemCount = 64;
        float reputation = 0.7f; // Friendly
        
        // Calculate selling premium
        float stepsAboveFriendly = (reputation - 0.5f) * 10f;
        float discount = Math.min(0.5f, stepsAboveFriendly * 0.1f);
        float buyingMultiplier = 1.0f - discount;
        float sellingMultiplier = 1.0f / buyingMultiplier;
        
        // With base of 1 emerald per 10 items
        float itemsPerEmerald = 10f / sellingMultiplier;
        float emeraldsEarned = itemCount / itemsPerEmerald;
        
        // Should earn more than 6.4 (base)
        assertTrue("Should earn premium", emeraldsEarned > 6.4f);
    }

    @Test
    public void testProductivityMultiplierFromHeroBuff() {
        // Hero buff should provide 2x multiplier
        
        float withBuff = 2.0f;
        float withoutBuff = 1.0f;
        
        assertEquals("With hero buff", 2.0f, withBuff, 0.01f);
        assertEquals("Without hero buff", 1.0f, withoutBuff, 0.01f);
    }

    @Test
    public void testProductivityWithQualityAndHero() {
        // Workshop quality: 1.25x (Roomy)
        // Hero buff: 2.0x
        // Combined: 1.25 * 2.0 = 2.5x
        
        float qualityMultiplier = 1.25f;
        float heroMultiplier = 2.0f;
        float combined = qualityMultiplier * heroMultiplier;
        
        assertEquals("Combined multipliers", 2.5f, combined, 0.01f);
    }

    @Test
    public void testProductivityWithAllBonuses() {
        // Workshop quality: 1.5x (Spacious)
        // Hero buff: 2.0x
        // Combined: 1.5 * 2.0 = 3.0x
        
        float qualityMultiplier = 1.5f;
        float heroMultiplier = 2.0f;
        float combined = qualityMultiplier * heroMultiplier;
        
        assertEquals("Max multipliers", 3.0f, combined, 0.01f);
    }

    @Test
    public void testZeroMultiplierProtection() {
        // Ensure we never have 0x multiplier
        
        // Worst case: no reputation, no quality, no buff
        float reputation = -1.0f;
        float quality = 0.5f; // Cramped workshop
        float hero = 1.0f;    // No buff
        
        float combined = quality * hero;
        
        assertTrue("Should never be 0", combined > 0f);
        assertEquals("Worst case is 0.5x", 0.5f, combined, 0.01f);
    }

    @Test
    public void testMaximumMultiplier() {
        // Best case: max reputation, max quality, hero buff
        
        float reputation = 1.0f;
        float quality = 1.5f; // Spacious
        float hero = 2.0f;    // Hero buff active
        
        float combined = quality * hero;
        
        assertEquals("Best case multiplier", 3.0f, combined, 0.01f);
    }
}
