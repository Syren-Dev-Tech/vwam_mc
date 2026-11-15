package com.github.syren_dev_tech.vwam.workshop;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a workshop validation check.
 * Contains validation status, failure reasons, and workshop metrics.
 */
public class WorkshopValidationResult {
    private final boolean valid;
    private final List<String> failureReasons;
    private final BlockPos workstationPos;
    private final WorkshopQuality quality;
    private final float beautyScore;
    private final List<BlockPos> supportStations;
    private final List<BlockPos> storageBlocks;

    public WorkshopValidationResult(
            boolean valid,
            BlockPos workstationPos,
            WorkshopQuality quality,
            float beautyScore,
            List<BlockPos> supportStations,
            List<BlockPos> storageBlocks
    ) {
        this.valid = valid;
        this.workstationPos = workstationPos;
        this.quality = quality;
        this.beautyScore = beautyScore;
        this.supportStations = supportStations != null ? supportStations : new ArrayList<>();
        this.storageBlocks = storageBlocks != null ? storageBlocks : new ArrayList<>();
        this.failureReasons = new ArrayList<>();
    }

    public WorkshopValidationResult(
            boolean valid,
            BlockPos workstationPos,
            List<String> failureReasons
    ) {
        this.valid = valid;
        this.workstationPos = workstationPos;
        this.failureReasons = failureReasons != null ? failureReasons : new ArrayList<>();
        this.quality = WorkshopQuality.NORMAL;  // Default if invalid
        this.beautyScore = 0.0f;
        this.supportStations = new ArrayList<>();
        this.storageBlocks = new ArrayList<>();
    }

    public boolean isValid() {
        return valid;
    }

    public BlockPos getWorkstationPos() {
        return workstationPos;
    }

    public WorkshopQuality getQuality() {
        return quality;
    }

    public float getQualityMultiplier() {
        return quality.getProductionMultiplier();
    }

    public float getBeautyScore() {
        return beautyScore;
    }

    public List<BlockPos> getSupportStations() {
        return new ArrayList<>(supportStations);
    }

    public List<BlockPos> getStorageBlocks() {
        return new ArrayList<>(storageBlocks);
    }

    public List<String> getFailureReasons() {
        return new ArrayList<>(failureReasons);
    }

    public void addFailureReason(String reason) {
        this.failureReasons.add(reason);
    }

    @Override
    public String toString() {
        if (valid) {
            return String.format(
                    "WorkshopValidationResult{valid=true, quality=%s, beautyScore=%.2f, "
                            + "supportStations=%d, storageBlocks=%d}",
                    quality, beautyScore, supportStations.size(), storageBlocks.size()
            );
        } else {
            return String.format(
                    "WorkshopValidationResult{valid=false, reasons=%s}",
                    failureReasons
            );
        }
    }

    /**
     * Workshop quality ratings
     */
    public enum WorkshopQuality {
        CRAMPED(0.5f),
        TIGHT(0.75f),
        NORMAL(1.0f),
        ROOMY(1.25f),
        SPACIOUS(1.5f);

        private final float productionMultiplier;

        WorkshopQuality(float productionMultiplier) {
            this.productionMultiplier = productionMultiplier;
        }

        public float getProductionMultiplier() {
            return productionMultiplier;
        }

        public static WorkshopQuality fromAverageDistance(double avgDistance) {
            if (avgDistance <= 3) return CRAMPED;
            if (avgDistance <= 4) return TIGHT;
            if (avgDistance <= 5) return NORMAL;
            if (avgDistance <= 6) return ROOMY;
            return SPACIOUS;
        }
    }

    /**
     * Workshop beauty ratings
     */
    public enum WorkshopBeauty {
        MODEST(0.0f, 1.0f),      // < 1
        ELEGANT(1.0f, 1.1f),     // < 1.5
        STRIKING(1.5f, 1.2f),    // < 2
        MAGNIFICENT(2.0f, 1.3f), // < 2.5
        SUBLIME(2.5f, 1.4f),     // < 3
        TRANSCENDENT(3.0f, 1.5f);  // >= 3

        private final float minScore;
        private final float tradeMultiplier;

        WorkshopBeauty(float minScore, float tradeMultiplier) {
            this.minScore = minScore;
            this.tradeMultiplier = tradeMultiplier;
        }

        public float getTradeMultiplier() {
            return tradeMultiplier;
        }

        public static WorkshopBeauty fromBeautyScore(float score) {
            if (score < 1.0f) return MODEST;
            if (score < 1.5f) return ELEGANT;
            if (score < 2.0f) return STRIKING;
            if (score < 2.5f) return MAGNIFICENT;
            if (score < 3.0f) return SUBLIME;
            return TRANSCENDENT;
        }
    }
}
