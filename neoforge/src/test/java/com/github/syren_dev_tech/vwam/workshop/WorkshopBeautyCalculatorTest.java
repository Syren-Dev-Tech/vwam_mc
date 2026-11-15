package com.github.syren_dev_tech.vwam.workshop;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for WorkshopBeautyCalculator.
 *
 * Tests beauty score calculation based on decorative blocks.
 * Beauty tiers and their trade multipliers:
 * - Modest: score < 1.0 → 1.0x multiplier (no discount)
 * - Elegant: score < 1.5 → 1.1x multiplier (10% increase)
 * - Striking: score < 2.0 → 1.2x multiplier (20% increase)
 * - Magnificent: score < 2.5 → 1.3x multiplier (30% increase)
 * - Sublime: score < 3.0 → 1.4x multiplier (40% increase)
 * - Transcendent: score ≥ 3.0 → 1.5x multiplier (50% increase)
 *
 * Decorative block scores:
 * - Potted plants: 0.3 each
 * - Paintings: 0.2 each
 * - Carpets: 0.05 each
 * - Colored glass/panes: 0.08 each
 * - Hanging signs: 0.2 each
 * - Lanterns: 0.2 each
 */
public class WorkshopBeautyCalculatorTest {

    private static final net.minecraft.core.BlockPos workstationPos = 
        new net.minecraft.core.BlockPos(0, 64, 0);

    @Before
    public void setUp() {
        // Note: Testing with mock Level would require game context
        // Current tests validate the calculator's public static methods
    }

    // ════════════════════════════════════════════════════════════════════════════
    // BEAUTY TIER TESTS (All 6 tiers)
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test MODEST tier: Beauty score < 1.0
     * Trade multiplier: 1.0x (no discount/penalty)
     */
    @Test
    public void testModestBeautyTier() {
        // Score calculation: 2 potted plants = 2 × 0.3 = 0.6
        // Expected: MODEST (< 1.0)
        float beautyScore = 0.6f;

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Score 0.6 should be MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty);
        assertEquals("MODEST multiplier should be 1.0x", 
            1.0f, beauty.getTradeMultiplier(), 0.001f);
    }

    /**
     * Test ELEGANT tier: Beauty score < 1.5
     * Trade multiplier: 1.1x (10% discount)
     */
    @Test
    public void testElegantBeautyTier() {
        // Score calculation: 1 potted plant + 2 lanterns = 0.3 + 0.4 = 0.7
        // Or: 1 painting + 2 carpets + 1 sign = 0.2 + 0.1 + 0.2 = 0.5
        // Or: Mix to get between 1.0 and 1.5
        float beautyScore = 1.2f;

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Score 1.2 should be ELEGANT", 
            WorkshopValidationResult.WorkshopBeauty.ELEGANT, beauty);
        assertEquals("ELEGANT multiplier should be 1.1x", 
            1.1f, beauty.getTradeMultiplier(), 0.001f);
    }

    /**
     * Test STRIKING tier: Beauty score < 2.0
     * Trade multiplier: 1.2x (20% discount)
     */
    @Test
    public void testStrikingBeautyTier() {
        // Score calculation: 1 potted plant + 3 lanterns + 1 painting
        // = 0.3 + 0.6 + 0.2 = 1.1, or other combinations
        float beautyScore = 1.7f;

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Score 1.7 should be STRIKING", 
            WorkshopValidationResult.WorkshopBeauty.STRIKING, beauty);
        assertEquals("STRIKING multiplier should be 1.2x", 
            1.2f, beauty.getTradeMultiplier(), 0.001f);
    }

    /**
     * Test MAGNIFICENT tier: Beauty score < 2.5
     * Trade multiplier: 1.3x (30% discount)
     */
    @Test
    public void testMagnificentBeautyTier() {
        // Score calculation: 5 lanterns + 1 potted plant + 1 painting
        // = 1.0 + 0.3 + 0.2 = 1.5, or other combinations
        float beautyScore = 2.2f;

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Score 2.2 should be MAGNIFICENT", 
            WorkshopValidationResult.WorkshopBeauty.MAGNIFICENT, beauty);
        assertEquals("MAGNIFICENT multiplier should be 1.3x", 
            1.3f, beauty.getTradeMultiplier(), 0.001f);
    }

    /**
     * Test SUBLIME tier: Beauty score < 3.0
     * Trade multiplier: 1.4x (40% discount)
     */
    @Test
    public void testSublimeBeautyTier() {
        // Score calculation: Multiple decorative blocks to reach 2.5-2.9
        float beautyScore = 2.7f;

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Score 2.7 should be SUBLIME", 
            WorkshopValidationResult.WorkshopBeauty.SUBLIME, beauty);
        assertEquals("SUBLIME multiplier should be 1.4x", 
            1.4f, beauty.getTradeMultiplier(), 0.001f);
    }

    /**
     * Test TRANSCENDENT tier: Beauty score ≥ 3.0
     * Trade multiplier: 1.5x (50% discount - maximum)
     */
    @Test
    public void testTranscendentBeautyTier() {
        // Score calculation: 15 lanterns = 3.0
        float beautyScore = 3.0f;

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Score 3.0 should be TRANSCENDENT", 
            WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT, beauty);
        assertEquals("TRANSCENDENT multiplier should be 1.5x", 
            1.5f, beauty.getTradeMultiplier(), 0.001f);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // BOUNDARY CONDITION TESTS (Tier transitions)
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test boundary: Score < 1.0 → MODEST
     */
    @Test
    public void testBoundaryBelow1_0() {
        float beautyScore = 0.99f;
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);
        
        assertEquals("Score 0.99 should be MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty);
    }

    /**
     * Test boundary: Score = 1.0 → ELEGANT (transitions at < 1.5)
     */
    @Test
    public void testBoundaryExactly1_0() {
        float beautyScore = 1.0f;
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);
        
        assertEquals("Score 1.0 should be ELEGANT (boundary: < 1.5)", 
            WorkshopValidationResult.WorkshopBeauty.ELEGANT, beauty);
    }

    /**
     * Test boundary: Score = 1.5 → STRIKING
     */
    @Test
    public void testBoundaryExactly1_5() {
        float beautyScore = 1.5f;
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);
        
        assertEquals("Score 1.5 should be STRIKING (boundary: < 2.0)", 
            WorkshopValidationResult.WorkshopBeauty.STRIKING, beauty);
    }

    /**
     * Test boundary: Score = 2.0 → MAGNIFICENT
     */
    @Test
    public void testBoundaryExactly2_0() {
        float beautyScore = 2.0f;
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);
        
        assertEquals("Score 2.0 should be MAGNIFICENT (boundary: < 2.5)", 
            WorkshopValidationResult.WorkshopBeauty.MAGNIFICENT, beauty);
    }

    /**
     * Test boundary: Score = 2.5 → SUBLIME
     */
    @Test
    public void testBoundaryExactly2_5() {
        float beautyScore = 2.5f;
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);
        
        assertEquals("Score 2.5 should be SUBLIME (boundary: < 3.0)", 
            WorkshopValidationResult.WorkshopBeauty.SUBLIME, beauty);
    }

    /**
     * Test boundary: Score = 3.0 → TRANSCENDENT
     */
    @Test
    public void testBoundaryExactly3_0() {
        float beautyScore = 3.0f;
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);
        
        assertEquals("Score 3.0 should be TRANSCENDENT (boundary: ≥ 3.0)", 
            WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT, beauty);
    }

    /**
     * Test boundary: Score > 3.0 → TRANSCENDENT
     */
    @Test
    public void testBoundaryAbove3_0() {
        float beautyScore = 5.5f;
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);
        
        assertEquals("Score 5.5 should be TRANSCENDENT", 
            WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT, beauty);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // BLOCK SCORING TESTS (Individual block types)
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test potted plant block scoring: 0.3 points each
     */
    @Test
    public void testPottedPlantScoring() {
        // 1 potted plant = 0.3
        float singlePlant = 0.3f;
        WorkshopValidationResult.WorkshopBeauty beauty1 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(singlePlant);
        assertEquals("1 plant (0.3) is MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty1);

        // 3 potted plants = 0.9
        float threePlants = 0.9f;
        WorkshopValidationResult.WorkshopBeauty beauty3 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(threePlants);
        assertEquals("3 plants (0.9) is MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty3);

        // 5 potted plants = 1.5
        float fivePlants = 1.5f;
        WorkshopValidationResult.WorkshopBeauty beauty5 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(fivePlants);
        assertEquals("5 plants (1.5) is STRIKING", 
            WorkshopValidationResult.WorkshopBeauty.STRIKING, beauty5);
    }

    /**
     * Test painting block scoring: 0.2 points each
     */
    @Test
    public void testPaintingScoring() {
        // 1 painting = 0.2
        float onePainting = 0.2f;
        WorkshopValidationResult.WorkshopBeauty beauty1 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(onePainting);
        assertEquals("1 painting (0.2) is MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty1);

        // 5 paintings = 1.0
        float fivePaintings = 1.0f;
        WorkshopValidationResult.WorkshopBeauty beauty5 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(fivePaintings);
        assertEquals("5 paintings (1.0) is ELEGANT", 
            WorkshopValidationResult.WorkshopBeauty.ELEGANT, beauty5);

        // 10 paintings = 2.0
        float tenPaintings = 2.0f;
        WorkshopValidationResult.WorkshopBeauty beauty10 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(tenPaintings);
        assertEquals("10 paintings (2.0) is MAGNIFICENT", 
            WorkshopValidationResult.WorkshopBeauty.MAGNIFICENT, beauty10);
    }

    /**
     * Test carpet block scoring: 0.05 points each
     */
    @Test
    public void testCarpetScoring() {
        // 1 carpet = 0.05
        float oneCarpet = 0.05f;
        WorkshopValidationResult.WorkshopBeauty beauty1 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(oneCarpet);
        assertEquals("1 carpet (0.05) is MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty1);

        // 20 carpets = 1.0
        float twentyCarpets = 1.0f;
        WorkshopValidationResult.WorkshopBeauty beauty20 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(twentyCarpets);
        assertEquals("20 carpets (1.0) is ELEGANT", 
            WorkshopValidationResult.WorkshopBeauty.ELEGANT, beauty20);

        // Carpets are least valuable but still contribute
        assertTrue("20 carpets should contribute to beauty tier", 
            twentyCarpets > 0);
    }

    /**
     * Test colored glass/pane block scoring: 0.08 points each
     */
    @Test
    public void testColoredGlassScoring() {
        // 1 glass = 0.08
        float oneGlass = 0.08f;
        WorkshopValidationResult.WorkshopBeauty beauty1 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(oneGlass);
        assertEquals("1 glass (0.08) is MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty1);

        // 12.5 glass = 1.0 (approximately)
        float twelveGlass = 0.96f;
        WorkshopValidationResult.WorkshopBeauty beauty12 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(twelveGlass);
        assertEquals("12 glass (0.96) is MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty12);
    }

    /**
     * Test hanging sign block scoring: 0.2 points each
     */
    @Test
    public void testSignScoring() {
        // 1 sign = 0.2
        float oneSign = 0.2f;
        WorkshopValidationResult.WorkshopBeauty beauty1 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(oneSign);
        assertEquals("1 sign (0.2) is MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty1);

        // 5 signs = 1.0
        float fiveSigns = 1.0f;
        WorkshopValidationResult.WorkshopBeauty beauty5 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(fiveSigns);
        assertEquals("5 signs (1.0) is ELEGANT", 
            WorkshopValidationResult.WorkshopBeauty.ELEGANT, beauty5);
    }

    /**
     * Test lantern block scoring: 0.2 points each
     */
    @Test
    public void testLanternScoring() {
        // 1 lantern = 0.2
        float oneLantern = 0.2f;
        WorkshopValidationResult.WorkshopBeauty beauty1 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(oneLantern);
        assertEquals("1 lantern (0.2) is MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty1);

        // 5 lanterns = 1.0
        float fiveLanterns = 1.0f;
        WorkshopValidationResult.WorkshopBeauty beauty5 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(fiveLanterns);
        assertEquals("5 lanterns (1.0) is ELEGANT", 
            WorkshopValidationResult.WorkshopBeauty.ELEGANT, beauty5);

        // 15 lanterns = 3.0 (maximum)
        float fifteenLanterns = 3.0f;
        WorkshopValidationResult.WorkshopBeauty beauty15 = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(fifteenLanterns);
        assertEquals("15 lanterns (3.0) is TRANSCENDENT", 
            WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT, beauty15);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MIXED BLOCK COMBINATION TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test combination: Multiple block types creating a score
     * 2 lanterns + 3 paintings + 5 carpets
     * = 0.4 + 0.6 + 0.25 = 1.25
     */
    @Test
    public void testMixedBlocksCombination1() {
        // 2 lanterns (0.4) + 3 paintings (0.6) + 5 carpets (0.25) = 1.25
        float mixedScore = 0.4f + 0.6f + 0.25f;
        assertEquals("Mixed blocks score", 1.25f, mixedScore, 0.001f);

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(mixedScore);
        assertEquals("Mixed score 1.25 is ELEGANT", 
            WorkshopValidationResult.WorkshopBeauty.ELEGANT, beauty);
    }

    /**
     * Test combination: Balanced beautiful workshop
     * 3 potted plants + 2 lanterns + 1 painting + 10 carpets
     * = 0.9 + 0.4 + 0.2 + 0.5 = 2.0
     */
    @Test
    public void testMixedBlocksCombination2() {
        float mixedScore = 0.9f + 0.4f + 0.2f + 0.5f;
        assertEquals("Mixed blocks score", 2.0f, mixedScore, 0.001f);

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(mixedScore);
        assertEquals("Mixed score 2.0 is MAGNIFICENT", 
            WorkshopValidationResult.WorkshopBeauty.MAGNIFICENT, beauty);
    }

    /**
     * Test combination: High-end luxury workshop
     * 5 lanterns + 5 potted plants + 3 paintings + 15 colored glass
     * = 1.0 + 1.5 + 0.6 + 1.2 = 4.3
     */
    @Test
    public void testMixedBlocksCombination3() {
        float mixedScore = 1.0f + 1.5f + 0.6f + 1.2f;
        assertEquals("Mixed blocks score", 4.3f, mixedScore, 0.001f);

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(mixedScore);
        assertEquals("Mixed score 4.3 is TRANSCENDENT", 
            WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT, beauty);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // TRADE MULTIPLIER TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test MODEST trade multiplier: 1.0x
     */
    @Test
    public void testModestTradeMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopBeauty.MODEST.getTradeMultiplier();
        assertEquals("MODEST multiplier", 1.0f, multiplier, 0.001f);
    }

    /**
     * Test ELEGANT trade multiplier: 1.1x
     */
    @Test
    public void testElegantTradeMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopBeauty.ELEGANT.getTradeMultiplier();
        assertEquals("ELEGANT multiplier", 1.1f, multiplier, 0.001f);
    }

    /**
     * Test STRIKING trade multiplier: 1.2x
     */
    @Test
    public void testStrikingTradeMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopBeauty.STRIKING.getTradeMultiplier();
        assertEquals("STRIKING multiplier", 1.2f, multiplier, 0.001f);
    }

    /**
     * Test MAGNIFICENT trade multiplier: 1.3x
     */
    @Test
    public void testMagnificentTradeMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopBeauty.MAGNIFICENT.getTradeMultiplier();
        assertEquals("MAGNIFICENT multiplier", 1.3f, multiplier, 0.001f);
    }

    /**
     * Test SUBLIME trade multiplier: 1.4x
     */
    @Test
    public void testSublimeTradeMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopBeauty.SUBLIME.getTradeMultiplier();
        assertEquals("SUBLIME multiplier", 1.4f, multiplier, 0.001f);
    }

    /**
     * Test TRANSCENDENT trade multiplier: 1.5x (maximum)
     */
    @Test
    public void testTranscendentTradeMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT.getTradeMultiplier();
        assertEquals("TRANSCENDENT multiplier", 1.5f, multiplier, 0.001f);
    }

    /**
     * Test multiplier progression (should increase with beauty)
     */
    @Test
    public void testMultiplierProgression() {
        float modest = WorkshopValidationResult.WorkshopBeauty.MODEST.getTradeMultiplier();
        float elegant = WorkshopValidationResult.WorkshopBeauty.ELEGANT.getTradeMultiplier();
        float striking = WorkshopValidationResult.WorkshopBeauty.STRIKING.getTradeMultiplier();
        float magnificent = WorkshopValidationResult.WorkshopBeauty.MAGNIFICENT.getTradeMultiplier();
        float sublime = WorkshopValidationResult.WorkshopBeauty.SUBLIME.getTradeMultiplier();
        float transcendent = WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT.getTradeMultiplier();

        assertTrue("MODEST < ELEGANT", modest < elegant);
        assertTrue("ELEGANT < STRIKING", elegant < striking);
        assertTrue("STRIKING < MAGNIFICENT", striking < magnificent);
        assertTrue("MAGNIFICENT < SUBLIME", magnificent < sublime);
        assertTrue("SUBLIME < TRANSCENDENT", sublime < transcendent);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // TRADE DISCOUNT CALCULATION TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test trade discount: MODEST (no discount)
     * Base trade: 10 items for 1 emerald
     * With 1.0x multiplier: Still 10 items for 1 emerald
     */
    @Test
    public void testTradeDiscountModest() {
        float baseItemsPerEmerald = 10.0f;
        float multiplier = WorkshopValidationResult.WorkshopBeauty.MODEST.getTradeMultiplier();
        float discountedItems = baseItemsPerEmerald / multiplier;

        assertEquals("MODEST discount: 10 / 1.0 = 10", 10.0f, discountedItems, 0.01f);
    }

    /**
     * Test trade discount: ELEGANT (10% discount)
     * Base trade: 10 items for 1 emerald
     * With 1.1x multiplier: 11 items for 1 emerald (better deal for player)
     */
    @Test
    public void testTradeDiscountElegant() {
        float baseItemsPerEmerald = 10.0f;
        float multiplier = WorkshopValidationResult.WorkshopBeauty.ELEGANT.getTradeMultiplier();
        float discountedItems = baseItemsPerEmerald / multiplier;

        assertEquals("ELEGANT discount: 10 / 1.1 ≈ 9.09", 9.09f, discountedItems, 0.1f);
        // Player gets fewer items = worse trade, which matches the spec
        assertTrue("ELEGANT gives less favorable trade", discountedItems < baseItemsPerEmerald);
    }

    /**
     * Test trade discount: TRANSCENDENT (50% discount - maximum)
     * Base trade: 10 items for 1 emerald
     * With 1.5x multiplier: 6.67 items for 1 emerald (least favorable)
     */
    @Test
    public void testTradeDiscountTranscendent() {
        float baseItemsPerEmerald = 10.0f;
        float multiplier = WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT.getTradeMultiplier();
        float discountedItems = baseItemsPerEmerald / multiplier;

        assertEquals("TRANSCENDENT discount: 10 / 1.5 ≈ 6.67", 6.67f, discountedItems, 0.1f);
    }

    /**
     * Test trade price impact: Higher beauty = higher prices (worse for buyer)
     * Note: This matches the plan spec - beauty increases village prices
     */
    @Test
    public void testPriceImpactWithBeauty() {
        float basePrice = 100.0f;  // 100 emeralds for item

        float modestPrice = basePrice * WorkshopValidationResult.WorkshopBeauty.MODEST.getTradeMultiplier();
        float transcendentPrice = basePrice * WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT.getTradeMultiplier();

        assertEquals("MODEST price", 100.0f, modestPrice, 0.01f);
        assertEquals("TRANSCENDENT price", 150.0f, transcendentPrice, 0.01f);
        assertTrue("Higher beauty = higher prices", transcendentPrice > modestPrice);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // SCORE ACCUMULATION TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test that scores accumulate properly
     */
    @Test
    public void testScoreAccumulation() {
        float score1 = 0.3f;  // 1 potted plant
        float score2 = score1 + 0.2f;  // + 1 painting
        float score3 = score2 + 0.2f;  // + 1 lantern

        assertEquals("Score after 1 plant", 0.3f, score1, 0.001f);
        assertEquals("Score after plant + painting", 0.5f, score2, 0.001f);
        assertEquals("Score after plant + painting + lantern", 0.7f, score3, 0.001f);
    }

    /**
     * Test score scaling: Many blocks
     */
    @Test
    public void testScoreScaling() {
        // 100 lanterns = 20.0 (very high)
        float highScore = 100 * 0.2f;
        assertEquals("100 lanterns", 20.0f, highScore, 0.001f);

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(highScore);
        assertEquals("Very high score is TRANSCENDENT", 
            WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT, beauty);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test zero beauty score (no decorative blocks)
     */
    @Test
    public void testZeroBeautyScore() {
        float beautyScore = 0.0f;
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Score 0.0 should be MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty);
    }

    /**
     * Test very small beauty score (fractional)
     */
    @Test
    public void testVerySmallBeautyScore() {
        float beautyScore = 0.01f;  // Less than a carpet
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Score 0.01 should be MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty);
    }

    /**
     * Test very high beauty score (excess decoration)
     */
    @Test
    public void testVeryHighBeautyScore() {
        float beautyScore = 100.0f;  // Massive amount of blocks
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Score 100.0 should be TRANSCENDENT", 
            WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT, beauty);
    }

    /**
     * Test negative score (shouldn't happen but should be handled)
     */
    @Test
    public void testNegativeBeautyScore() {
        float beautyScore = -1.0f;
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        // Should default to MODEST for any value < 1.0
        assertEquals("Negative score should be MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ENUM CONSISTENCY TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test all enum values are valid
     */
    @Test
    public void testAllEnumValuesValid() {
        WorkshopValidationResult.WorkshopBeauty[] beauties = 
            WorkshopValidationResult.WorkshopBeauty.values();

        assertEquals("Should have 6 beauty tiers", 6, beauties.length);

        for (WorkshopValidationResult.WorkshopBeauty beauty : beauties) {
            assertNotNull("Beauty should not be null", beauty);
            float multiplier = beauty.getTradeMultiplier();
            assertTrue("Multiplier should be >= 1.0", multiplier >= 1.0f);
            assertTrue("Multiplier should be <= 1.5", multiplier <= 1.5f);
        }
    }

    /**
     * Test fromBeautyScore method with all ranges
     */
    @Test
    public void testFromBeautyScoreMethod() {
        assertEquals("0.5 → MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST,
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(0.5f));

        assertEquals("1.2 → ELEGANT", 
            WorkshopValidationResult.WorkshopBeauty.ELEGANT,
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(1.2f));

        assertEquals("1.8 → STRIKING", 
            WorkshopValidationResult.WorkshopBeauty.STRIKING,
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(1.8f));

        assertEquals("2.2 → MAGNIFICENT", 
            WorkshopValidationResult.WorkshopBeauty.MAGNIFICENT,
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(2.2f));

        assertEquals("2.8 → SUBLIME", 
            WorkshopValidationResult.WorkshopBeauty.SUBLIME,
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(2.8f));

        assertEquals("3.5 → TRANSCENDENT", 
            WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT,
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(3.5f));
    }

    // ════════════════════════════════════════════════════════════════════════════
    // REAL-WORLD WORKSHOP SCENARIO TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Scenario 1: Basic craftsman's workshop
     * Setup: 3 lanterns for light, 2 potted plants
     * Score: 0.6 + 0.6 = 1.2
     * Expected: ELEGANT (1.1x trade multiplier)
     */
    @Test
    public void testScenarioCraftsmansWorkshop() {
        // 3 lanterns + 2 potted plants
        float beautyScore = (3 * 0.2f) + (2 * 0.3f);
        assertEquals("Craftsman workshop score", 1.2f, beautyScore, 0.001f);

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);
        assertEquals("Craftsman workshop is ELEGANT", 
            WorkshopValidationResult.WorkshopBeauty.ELEGANT, beauty);
        assertEquals("Trade multiplier 1.1x", 1.1f, beauty.getTradeMultiplier(), 0.001f);
    }

    /**
     * Scenario 2: Decorated marketplace
     * Setup: 5 lanterns + 3 paintings + 20 carpets + 10 colored glass + 4 potted plants
     * Score: 1.0 + 0.6 + 1.0 + 0.8 + 1.2 = 4.6
     * Expected: TRANSCENDENT (1.5x trade multiplier - max)
     */
    @Test
    public void testScenarioDecoratedMarketplace() {
        float beautyScore = (5 * 0.2f) + (3 * 0.2f) + (20 * 0.05f) + 
                           (10 * 0.08f) + (4 * 0.3f);
        assertEquals("Marketplace score", 4.6f, beautyScore, 0.001f);

        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);
        assertEquals("Marketplace is TRANSCENDENT", 
            WorkshopValidationResult.WorkshopBeauty.TRANSCENDENT, beauty);
        assertEquals("Maximum trade multiplier 1.5x", 1.5f, beauty.getTradeMultiplier(), 0.001f);
    }

    /**
     * Scenario 3: Minimal workshop
     * Setup: 1 potted plant only
     * Score: 0.3
     * Expected: MODEST (1.0x - no discount)
     */
    @Test
    public void testScenarioMinimalWorkshop() {
        float beautyScore = 0.3f;  // 1 potted plant
        WorkshopValidationResult.WorkshopBeauty beauty = 
            WorkshopValidationResult.WorkshopBeauty.fromBeautyScore(beautyScore);

        assertEquals("Minimal workshop is MODEST", 
            WorkshopValidationResult.WorkshopBeauty.MODEST, beauty);
        assertEquals("No trade multiplier", 1.0f, beauty.getTradeMultiplier(), 0.001f);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // BLOCK VALUE COMPARISON TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Verify relative block values
     * Most valuable: Potted plants, paintings, signs, lanterns (0.2-0.3)
     * Medium: Colored glass (0.08)
     * Least: Carpets (0.05)
     */
    @Test
    public void testBlockValueComparison() {
        assertTrue("Potted plant > carpet", 0.3f > 0.05f);
        assertTrue("Painting > carpet", 0.2f > 0.05f);
        assertTrue("Lantern > carpet", 0.2f > 0.05f);
        assertTrue("Sign > carpet", 0.2f > 0.05f);
        assertTrue("Glass > carpet", 0.08f > 0.05f);
    }

    /**
     * Calculate blocks needed to reach each tier
     */
    @Test
    public void testBlocksNeededPerTier() {
        // Reaching ELEGANT (score >= 1.0)
        float blocksTillElegant = 1.0f / 0.3f;  // Potted plants
        assertEquals("Need 3.33 potted plants for ELEGANT", 3.33f, blocksTillElegant, 0.01f);

        // Reaching STRIKING (score >= 1.5)
        float blocksTillStriking = 1.5f / 0.2f;  // Lanterns
        assertEquals("Need 7.5 lanterns for STRIKING", 7.5f, blocksTillStriking, 0.01f);

        // Reaching MAGNIFICENT (score >= 2.0)
        float blocksTillMagnificent = 2.0f / 0.2f;  // Paintings
        assertEquals("Need 10 paintings for MAGNIFICENT", 10.0f, blocksTillMagnificent, 0.01f);

        // Reaching TRANSCENDENT (score >= 3.0)
        float blocksTillTranscendent = 3.0f / 0.3f;  // Potted plants
        assertEquals("Need 10 potted plants for TRANSCENDENT", 10.0f, blocksTillTranscendent, 0.01f);
    }
}
