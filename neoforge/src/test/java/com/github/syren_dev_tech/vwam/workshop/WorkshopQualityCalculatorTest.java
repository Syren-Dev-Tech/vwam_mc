package com.github.syren_dev_tech.vwam.workshop;

import net.minecraft.core.BlockPos;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for WorkshopQualityCalculator.
 *
 * Tests quality rating calculation based on average distance to support stations.
 * Quality tiers and their production multipliers:
 * - Cramped: average ≤ 3 blocks → 0.5x production
 * - Tight: average ≤ 4 blocks → 0.75x production
 * - Normal: average ≤ 5 blocks → 1.0x production
 * - Roomy: average ≤ 6 blocks → 1.25x production
 * - Spacious: average > 6 blocks → 1.5x production
 */
public class WorkshopQualityCalculatorTest {

    private BlockPos workstationPos;
    private List<BlockPos> supportStations;

    @Before
    public void setUp() {
        workstationPos = new BlockPos(0, 64, 0);
        supportStations = new ArrayList<>();
    }

    // ════════════════════════════════════════════════════════════════════════════
    // QUALITY TIER TESTS (All 5 tiers)
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test CRAMPED tier: Average distance ≤ 3 blocks
     * Production multiplier: 0.5x
     */
    @Test
    public void testCrampedQualityTier() {
        // Place 3 support stations very close (all within 3 blocks)
        // Positions: (1,64,0), (2,64,0), (3,64,0) from workstation
        supportStations.add(new BlockPos(1, 64, 0));    // distance: 1.0
        supportStations.add(new BlockPos(2, 64, 0));    // distance: 2.0
        supportStations.add(new BlockPos(3, 64, 0));    // distance: 3.0
        // Average: (1.0 + 2.0 + 3.0) / 3 = 2.0

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("Should be CRAMPED with avg distance 2.0", 
                WorkshopValidationResult.WorkshopQuality.CRAMPED, quality);
        assertEquals("CRAMPED multiplier should be 0.5x", 
                0.5f, quality.getProductionMultiplier(), 0.001f);
    }

    /**
     * Test TIGHT tier: Average distance ≤ 4 blocks
     * Production multiplier: 0.75x
     */
    @Test
    public void testTightQualityTier() {
        // Place 3 support stations at average distance of ~3.5 blocks
        supportStations.add(new BlockPos(2, 64, 0));    // distance: 2.0
        supportStations.add(new BlockPos(4, 64, 0));    // distance: 4.0
        supportStations.add(new BlockPos(4, 64, 0));    // distance: 4.0
        // Average: (2.0 + 4.0 + 4.0) / 3 = 3.33

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("Should be TIGHT with avg distance ~3.33", 
                WorkshopValidationResult.WorkshopQuality.TIGHT, quality);
        assertEquals("TIGHT multiplier should be 0.75x", 
                0.75f, quality.getProductionMultiplier(), 0.001f);
    }

    /**
     * Test NORMAL tier: Average distance ≤ 5 blocks
     * Production multiplier: 1.0x (baseline)
     */
    @Test
    public void testNormalQualityTier() {
        // Place 3 support stations at average distance of ~4.7 blocks
        supportStations.add(new BlockPos(3, 64, 0));    // distance: 3.0
        supportStations.add(new BlockPos(5, 64, 0));    // distance: 5.0
        supportStations.add(new BlockPos(6, 64, 0));    // distance: 6.0
        // Average: (3.0 + 5.0 + 6.0) / 3 = 4.67

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("Should be NORMAL with avg distance ~4.67", 
                WorkshopValidationResult.WorkshopQuality.NORMAL, quality);
        assertEquals("NORMAL multiplier should be 1.0x (baseline)", 
                1.0f, quality.getProductionMultiplier(), 0.001f);
    }

    /**
     * Test ROOMY tier: Average distance ≤ 6 blocks
     * Production multiplier: 1.25x
     */
    @Test
    public void testRoomyQualityTier() {
        // Place 3 support stations at average distance of ~5.3 blocks
        supportStations.add(new BlockPos(4, 64, 0));    // distance: 4.0
        supportStations.add(new BlockPos(6, 64, 0));    // distance: 6.0
        supportStations.add(new BlockPos(6, 64, 0));    // distance: 6.0
        // Average: (4.0 + 6.0 + 6.0) / 3 = 5.33

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("Should be ROOMY with avg distance ~5.33", 
                WorkshopValidationResult.WorkshopQuality.ROOMY, quality);
        assertEquals("ROOMY multiplier should be 1.25x", 
                1.25f, quality.getProductionMultiplier(), 0.001f);
    }

    /**
     * Test SPACIOUS tier: Average distance > 6 blocks
     * Production multiplier: 1.5x (maximum)
     */
    @Test
    public void testSpaciousQualityTier() {
        // Place 3 support stations at average distance of ~7.0 blocks
        supportStations.add(new BlockPos(6, 64, 0));    // distance: 6.0
        supportStations.add(new BlockPos(7, 64, 0));    // distance: 7.0
        supportStations.add(new BlockPos(8, 64, 0));    // distance: 8.0
        // Average: (6.0 + 7.0 + 8.0) / 3 = 7.0

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("Should be SPACIOUS with avg distance ~7.0", 
                WorkshopValidationResult.WorkshopQuality.SPACIOUS, quality);
        assertEquals("SPACIOUS multiplier should be 1.5x", 
                1.5f, quality.getProductionMultiplier(), 0.001f);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // BOUNDARY CONDITION TESTS (Tier transitions)
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test boundary: Exactly 3.0 average (CRAMPED/TIGHT boundary)
     * At 3.0, should be CRAMPED (≤ 3)
     */
    @Test
    public void testBoundaryExactly3_0() {
        // Create stations with exactly 3.0 average distance
        supportStations.add(new BlockPos(0, 64, 3));    // distance: 3.0
        supportStations.add(new BlockPos(3, 64, 0));    // distance: 3.0
        supportStations.add(new BlockPos(3, 64, 0));    // distance: 3.0
        // Average: 3.0

        double avg = WorkshopQualityCalculator.calculateAverageDistance(supportStations, workstationPos);
        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("Average should be 3.0", 3.0, avg, 0.01);
        assertEquals("At 3.0 average, should be CRAMPED (≤3)", 
                WorkshopValidationResult.WorkshopQuality.CRAMPED, quality);
    }

    /**
     * Test boundary: Just above 3.0 (approaching TIGHT)
     * At 3.01, should be TIGHT (> 3, ≤ 4)
     */
    @Test
    public void testBoundaryJustAbove3_0() {
        // Create stations with ~3.01 average distance
        supportStations.add(new BlockPos(0, 64, 3));    // distance: 3.0
        supportStations.add(new BlockPos(3, 64, 0));    // distance: 3.0
        supportStations.add(new BlockPos(4, 64, 0));    // distance: 4.0
        // Average: (3.0 + 3.0 + 4.0) / 3 = 3.33

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("Just above 3.0 average, should be TIGHT", 
                WorkshopValidationResult.WorkshopQuality.TIGHT, quality);
    }

    /**
     * Test boundary: Exactly 4.0 average (TIGHT/NORMAL boundary)
     * At 4.0, should be TIGHT (≤ 4)
     */
    @Test
    public void testBoundaryExactly4_0() {
        // Create stations with exactly 4.0 average distance
        supportStations.add(new BlockPos(4, 64, 0));    // distance: 4.0
        supportStations.add(new BlockPos(4, 64, 0));    // distance: 4.0
        supportStations.add(new BlockPos(4, 64, 0));    // distance: 4.0
        // Average: 4.0

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("At 4.0 average, should be TIGHT (≤4)", 
                WorkshopValidationResult.WorkshopQuality.TIGHT, quality);
    }

    /**
     * Test boundary: Exactly 5.0 average (NORMAL/ROOMY boundary)
     * At 5.0, should be NORMAL (≤ 5)
     */
    @Test
    public void testBoundaryExactly5_0() {
        // Create stations with exactly 5.0 average distance
        supportStations.add(new BlockPos(5, 64, 0));    // distance: 5.0
        supportStations.add(new BlockPos(5, 64, 0));    // distance: 5.0
        supportStations.add(new BlockPos(5, 64, 0));    // distance: 5.0
        // Average: 5.0

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("At 5.0 average, should be NORMAL (≤5)", 
                WorkshopValidationResult.WorkshopQuality.NORMAL, quality);
    }

    /**
     * Test boundary: Exactly 6.0 average (ROOMY/SPACIOUS boundary)
     * At 6.0, should be ROOMY (≤ 6)
     */
    @Test
    public void testBoundaryExactly6_0() {
        // Create stations with exactly 6.0 average distance
        supportStations.add(new BlockPos(6, 64, 0));    // distance: 6.0
        supportStations.add(new BlockPos(6, 64, 0));    // distance: 6.0
        supportStations.add(new BlockPos(6, 64, 0));    // distance: 6.0
        // Average: 6.0

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("At 6.0 average, should be ROOMY (≤6)", 
                WorkshopValidationResult.WorkshopQuality.ROOMY, quality);
    }

    /**
     * Test boundary: Just above 6.0 (SPACIOUS tier)
     * At 6.01, should be SPACIOUS (> 6)
     */
    @Test
    public void testBoundaryJustAbove6_0() {
        // Create stations with ~6.33 average distance
        supportStations.add(new BlockPos(6, 64, 0));    // distance: 6.0
        supportStations.add(new BlockPos(6, 64, 0));    // distance: 6.0
        supportStations.add(new BlockPos(7, 64, 0));    // distance: 7.0
        // Average: (6.0 + 6.0 + 7.0) / 3 = 6.33

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("Just above 6.0 average, should be SPACIOUS", 
                WorkshopValidationResult.WorkshopQuality.SPACIOUS, quality);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // DISTANCE CALCULATION TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test average distance calculation with single support station
     */
    @Test
    public void testAverageDistanceSingleStation() {
        supportStations.add(new BlockPos(3, 64, 4));    // distance: sqrt(9+16) = 5.0

        double average = WorkshopQualityCalculator.calculateAverageDistance(supportStations, workstationPos);

        assertEquals("Average with one station should be its distance", 5.0, average, 0.01);
    }

    /**
     * Test average distance calculation with multiple stations
     */
    @Test
    public void testAverageDistanceMultipleStations() {
        supportStations.add(new BlockPos(1, 64, 0));    // distance: 1.0
        supportStations.add(new BlockPos(2, 64, 0));    // distance: 2.0
        supportStations.add(new BlockPos(3, 64, 0));    // distance: 3.0
        supportStations.add(new BlockPos(4, 64, 0));    // distance: 4.0
        // Average: (1.0 + 2.0 + 3.0 + 4.0) / 4 = 2.5

        double average = WorkshopQualityCalculator.calculateAverageDistance(supportStations, workstationPos);

        assertEquals("Average should be 2.5", 2.5, average, 0.01);
    }

    /**
     * Test average distance with vertical separation
     * Euclidean distance in 3D space
     */
    @Test
    public void testAverageDistanceWithVerticalSeparation() {
        // Support stations with vertical offset
        supportStations.add(new BlockPos(3, 65, 0));    // distance: sqrt(9+1) ≈ 3.16
        supportStations.add(new BlockPos(3, 63, 0));    // distance: sqrt(9+1) ≈ 3.16
        supportStations.add(new BlockPos(0, 64, 4));    // distance: 4.0

        double average = WorkshopQualityCalculator.calculateAverageDistance(supportStations, workstationPos);

        assertTrue("Average should be ~3.44", average > 3.3 && average < 3.5);
    }

    /**
     * Test distance calculation with diagonal placement
     */
    @Test
    public void testAverageDistanceDiagonalPlacement() {
        // Stations placed diagonally from workstation
        supportStations.add(new BlockPos(2, 64, 2));    // distance: sqrt(8) ≈ 2.83
        supportStations.add(new BlockPos(3, 64, 3));    // distance: sqrt(18) ≈ 4.24
        supportStations.add(new BlockPos(4, 64, 4));    // distance: sqrt(32) ≈ 5.66

        double average = WorkshopQualityCalculator.calculateAverageDistance(supportStations, workstationPos);

        assertTrue("Average should be ~4.24", average > 4.0 && average < 4.5);
    }

    /**
     * Test empty support stations list
     */
    @Test
    public void testAverageDistanceEmptyList() {
        double average = WorkshopQualityCalculator.calculateAverageDistance(supportStations, workstationPos);

        assertEquals("Empty list should return 0.0", 0.0, average, 0.001);
    }

    /**
     * Test null support stations list
     */
    @Test
    public void testAverageDistanceNullList() {
        double average = WorkshopQualityCalculator.calculateAverageDistance(null, workstationPos);

        assertEquals("Null list should return 0.0", 0.0, average, 0.001);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test empty support stations list returns CRAMPED
     */
    @Test
    public void testEmptyStationsReturnsCramped() {
        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertEquals("Empty stations list should return CRAMPED", 
                WorkshopValidationResult.WorkshopQuality.CRAMPED, quality);
    }

    /**
     * Test null support stations list returns CRAMPED
     */
    @Test
    public void testNullStationsReturnsCramped() {
        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(null, workstationPos);

        assertEquals("Null stations list should return CRAMPED", 
                WorkshopValidationResult.WorkshopQuality.CRAMPED, quality);
    }

    /**
     * Test workstation at origin (0, 0, 0)
     */
    @Test
    public void testWorkstationAtOrigin() {
        BlockPos origin = new BlockPos(0, 0, 0);
        supportStations.add(new BlockPos(3, 0, 0));
        supportStations.add(new BlockPos(0, 3, 0));
        supportStations.add(new BlockPos(0, 0, 3));
        // All distance: 3.0

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, origin);

        assertEquals("Should work with origin position", 
                WorkshopValidationResult.WorkshopQuality.CRAMPED, quality);
    }

    /**
     * Test very large workstation position
     */
    @Test
    public void testLargeWorkstationCoordinates() {
        BlockPos largePos = new BlockPos(1000, 64, 1000);
        supportStations.add(new BlockPos(1003, 64, 1000));
        supportStations.add(new BlockPos(1000, 64, 1003));
        supportStations.add(new BlockPos(1003, 64, 1003));
        // All distance: ~3 to 4.24

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, largePos);

        assertNotNull("Should handle large coordinates", quality);
        assertTrue("Quality should be CRAMPED or TIGHT", 
                quality == WorkshopValidationResult.WorkshopQuality.CRAMPED ||
                quality == WorkshopValidationResult.WorkshopQuality.TIGHT);
    }

    /**
     * Test many support stations (>5)
     */
    @Test
    public void testManySupportStations() {
        // Add 10 support stations
        for (int i = 1; i <= 10; i++) {
            supportStations.add(new BlockPos(i, 64, 0));
        }
        // Average: (1+2+3+...+10)/10 = 5.5

        double average = WorkshopQualityCalculator.calculateAverageDistance(supportStations, workstationPos);
        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        assertTrue("Average should be ~5.5", average > 5.0 && average < 6.0);
        assertEquals("Should be ROOMY with 10 stations", 
                WorkshopValidationResult.WorkshopQuality.ROOMY, quality);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // MULTIPLIER TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test CRAMPED multiplier: 0.5x
     */
    @Test
    public void testCrampedMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopQuality.CRAMPED.getProductionMultiplier();
        assertEquals("CRAMPED multiplier", 0.5f, multiplier, 0.001f);
    }

    /**
     * Test TIGHT multiplier: 0.75x
     */
    @Test
    public void testTightMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopQuality.TIGHT.getProductionMultiplier();
        assertEquals("TIGHT multiplier", 0.75f, multiplier, 0.001f);
    }

    /**
     * Test NORMAL multiplier: 1.0x
     */
    @Test
    public void testNormalMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopQuality.NORMAL.getProductionMultiplier();
        assertEquals("NORMAL multiplier", 1.0f, multiplier, 0.001f);
    }

    /**
     * Test ROOMY multiplier: 1.25x
     */
    @Test
    public void testRoomyMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopQuality.ROOMY.getProductionMultiplier();
        assertEquals("ROOMY multiplier", 1.25f, multiplier, 0.001f);
    }

    /**
     * Test SPACIOUS multiplier: 1.5x
     */
    @Test
    public void testSpaciousMultiplier() {
        float multiplier = WorkshopValidationResult.WorkshopQuality.SPACIOUS.getProductionMultiplier();
        assertEquals("SPACIOUS multiplier", 1.5f, multiplier, 0.001f);
    }

    /**
     * Test multiplier progression (should increase with quality)
     */
    @Test
    public void testMultiplierProgression() {
        float cramped = WorkshopValidationResult.WorkshopQuality.CRAMPED.getProductionMultiplier();
        float tight = WorkshopValidationResult.WorkshopQuality.TIGHT.getProductionMultiplier();
        float normal = WorkshopValidationResult.WorkshopQuality.NORMAL.getProductionMultiplier();
        float roomy = WorkshopValidationResult.WorkshopQuality.ROOMY.getProductionMultiplier();
        float spacious = WorkshopValidationResult.WorkshopQuality.SPACIOUS.getProductionMultiplier();

        assertTrue("CRAMPED < TIGHT", cramped < tight);
        assertTrue("TIGHT < NORMAL", tight < normal);
        assertTrue("NORMAL < ROOMY", normal < roomy);
        assertTrue("ROOMY < SPACIOUS", roomy < spacious);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // LABEL TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test quality labels for all tiers
     */
    @Test
    public void testQualityLabels() {
        assertEquals("CRAMPED label", "Cramped", 
                WorkshopQualityCalculator.getQualityLabel(WorkshopValidationResult.WorkshopQuality.CRAMPED));
        assertEquals("TIGHT label", "Tight", 
                WorkshopQualityCalculator.getQualityLabel(WorkshopValidationResult.WorkshopQuality.TIGHT));
        assertEquals("NORMAL label", "Normal", 
                WorkshopQualityCalculator.getQualityLabel(WorkshopValidationResult.WorkshopQuality.NORMAL));
        assertEquals("ROOMY label", "Roomy", 
                WorkshopQualityCalculator.getQualityLabel(WorkshopValidationResult.WorkshopQuality.ROOMY));
        assertEquals("SPACIOUS label", "Spacious", 
                WorkshopQualityCalculator.getQualityLabel(WorkshopValidationResult.WorkshopQuality.SPACIOUS));
    }

    /**
     * Test multiplier method with all tiers
     */
    @Test
    public void testProductionMultiplierMethod() {
        assertEquals("CRAMPED multiplier via method", 0.5f, 
                WorkshopQualityCalculator.getProductionMultiplier(WorkshopValidationResult.WorkshopQuality.CRAMPED), 0.001f);
        assertEquals("TIGHT multiplier via method", 0.75f, 
                WorkshopQualityCalculator.getProductionMultiplier(WorkshopValidationResult.WorkshopQuality.TIGHT), 0.001f);
        assertEquals("NORMAL multiplier via method", 1.0f, 
                WorkshopQualityCalculator.getProductionMultiplier(WorkshopValidationResult.WorkshopQuality.NORMAL), 0.001f);
        assertEquals("ROOMY multiplier via method", 1.25f, 
                WorkshopQualityCalculator.getProductionMultiplier(WorkshopValidationResult.WorkshopQuality.ROOMY), 0.001f);
        assertEquals("SPACIOUS multiplier via method", 1.5f, 
                WorkshopQualityCalculator.getProductionMultiplier(WorkshopValidationResult.WorkshopQuality.SPACIOUS), 0.001f);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // PRODUCTION OUTPUT TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test production output calculation: CRAMPED workshop
     * Base: 1 item/day × 0.5 = 0.5 items/day
     */
    @Test
    public void testProductionOutputCramped() {
        supportStations.add(new BlockPos(1, 64, 0));
        supportStations.add(new BlockPos(2, 64, 0));
        supportStations.add(new BlockPos(3, 64, 0));

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);
        float baseProduction = 1.0f;  // 1 item per day
        float actualProduction = baseProduction * quality.getProductionMultiplier();

        assertEquals("CRAMPED production: 1.0 × 0.5 = 0.5", 0.5f, actualProduction, 0.001f);
    }

    /**
     * Test production output calculation: NORMAL workshop
     * Base: 1 item/day × 1.0 = 1 item/day
     */
    @Test
    public void testProductionOutputNormal() {
        supportStations.add(new BlockPos(3, 64, 0));
        supportStations.add(new BlockPos(5, 64, 0));
        supportStations.add(new BlockPos(5, 64, 0));

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);
        float baseProduction = 1.0f;
        float actualProduction = baseProduction * quality.getProductionMultiplier();

        assertEquals("NORMAL production: 1.0 × 1.0 = 1.0", 1.0f, actualProduction, 0.001f);
    }

    /**
     * Test production output calculation: SPACIOUS workshop
     * Base: 1 item/day × 1.5 = 1.5 items/day
     */
    @Test
    public void testProductionOutputSpacious() {
        supportStations.add(new BlockPos(6, 64, 0));
        supportStations.add(new BlockPos(7, 64, 0));
        supportStations.add(new BlockPos(8, 64, 0));

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);
        float baseProduction = 1.0f;
        float actualProduction = baseProduction * quality.getProductionMultiplier();

        assertEquals("SPACIOUS production: 1.0 × 1.5 = 1.5", 1.5f, actualProduction, 0.001f);
    }

    /**
     * Test production scaling: Verify multiplier effect on different base rates
     */
    @Test
    public void testProductionScaling() {
        // Setup a TIGHT workshop
        supportStations.add(new BlockPos(2, 64, 0));
        supportStations.add(new BlockPos(4, 64, 0));
        supportStations.add(new BlockPos(4, 64, 0));

        WorkshopValidationResult.WorkshopQuality quality =
                WorkshopQualityCalculator.calculateQuality(supportStations, workstationPos);

        // Test with different base production rates
        float multiplier = quality.getProductionMultiplier();
        
        float baseA = 2.0f;
        float outputA = baseA * multiplier;
        assertEquals("Base 2.0: 2.0 × 0.75 = 1.5", 1.5f, outputA, 0.001f);

        float baseB = 4.0f;
        float outputB = baseB * multiplier;
        assertEquals("Base 4.0: 4.0 × 0.75 = 3.0", 3.0f, outputB, 0.001f);
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ENUM CONSISTENCY TESTS
    // ════════════════════════════════════════════════════════════════════════════

    /**
     * Test fromAverageDistance enum method with all ranges
     */
    @Test
    public void testFromAverageDistanceMethod() {
        assertEquals("2.5 avg → CRAMPED", 
                WorkshopValidationResult.WorkshopQuality.CRAMPED,
                WorkshopValidationResult.WorkshopQuality.fromAverageDistance(2.5));

        assertEquals("3.5 avg → TIGHT", 
                WorkshopValidationResult.WorkshopQuality.TIGHT,
                WorkshopValidationResult.WorkshopQuality.fromAverageDistance(3.5));

        assertEquals("4.5 avg → NORMAL", 
                WorkshopValidationResult.WorkshopQuality.NORMAL,
                WorkshopValidationResult.WorkshopQuality.fromAverageDistance(4.5));

        assertEquals("5.5 avg → ROOMY", 
                WorkshopValidationResult.WorkshopQuality.ROOMY,
                WorkshopValidationResult.WorkshopQuality.fromAverageDistance(5.5));

        assertEquals("7.0 avg → SPACIOUS", 
                WorkshopValidationResult.WorkshopQuality.SPACIOUS,
                WorkshopValidationResult.WorkshopQuality.fromAverageDistance(7.0));
    }

    /**
     * Test all enum values are accessible and valid
     */
    @Test
    public void testAllEnumValuesValid() {
        WorkshopValidationResult.WorkshopQuality[] qualities = 
            WorkshopValidationResult.WorkshopQuality.values();

        assertEquals("Should have 5 quality tiers", 5, qualities.length);
        
        for (WorkshopValidationResult.WorkshopQuality quality : qualities) {
            assertNotNull("Quality should not be null", quality);
            assertTrue("Multiplier should be > 0", quality.getProductionMultiplier() > 0);
            assertTrue("Multiplier should be <= 1.5", quality.getProductionMultiplier() <= 1.5f);
        }
    }
}
