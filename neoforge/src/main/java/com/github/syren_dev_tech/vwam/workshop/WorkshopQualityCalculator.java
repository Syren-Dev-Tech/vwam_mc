package com.github.syren_dev_tech.vwam.workshop;

import net.minecraft.core.BlockPos;
import java.util.List;

/**
 * Calculates workshop quality based on support station proximity.
 * Quality affects villager production rates.
 *
 * Quality ratings (based on average distance to support stations):
 * - Cramped: <= 3 blocks average → 0.5x production
 * - Tight: <= 4 blocks average → 0.75x production
 * - Normal: <= 5 blocks average → 1.0x production
 * - Roomy: <= 6 blocks average → 1.25x production
 * - Spacious: > 6 blocks average → 1.5x production
 */
public class WorkshopQualityCalculator {

    /**
     * Calculate quality rating from list of support station distances
     */
    public static WorkshopValidationResult.WorkshopQuality calculateQuality(List<BlockPos> supportStations, BlockPos workstationPos) {
        if (supportStations == null || supportStations.isEmpty()) {
            // No support stations means cramped quality
            return WorkshopValidationResult.WorkshopQuality.CRAMPED;
        }

        double averageDistance = calculateAverageDistance(supportStations, workstationPos);
        return WorkshopValidationResult.WorkshopQuality.fromAverageDistance(averageDistance);
    }

    /**
     * Calculate average distance from workstation to all support stations
     */
    public static double calculateAverageDistance(List<BlockPos> supportStations, BlockPos workstationPos) {
        if (supportStations == null || supportStations.isEmpty()) {
            return 0.0;
        }

        double totalDistance = 0.0;
        for (BlockPos supportPos : supportStations) {
            double distance = Math.sqrt(workstationPos.distSqr(supportPos));
            totalDistance += distance;
        }

        return totalDistance / supportStations.size();
    }

    /**
     * Get quality rating label
     */
    public static String getQualityLabel(WorkshopValidationResult.WorkshopQuality quality) {
        return switch (quality) {
            case CRAMPED -> "Cramped";
            case TIGHT -> "Tight";
            case NORMAL -> "Normal";
            case ROOMY -> "Roomy";
            case SPACIOUS -> "Spacious";
        };
    }

    /**
     * Get production multiplier for quality level
     */
    public static float getProductionMultiplier(WorkshopValidationResult.WorkshopQuality quality) {
        return quality.getProductionMultiplier();
    }
}
