package com.github.syren_dev_tech.vwam.workshop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates workshop beauty score based on decorative blocks.
 * Beauty score affects trade multipliers and villager morale.
 *
 * Scoring system:
 * - Potted plants: 0.3 each
 * - Paintings: 0.2 each
 * - Carpets: 0.05 each
 * - Colored glass/panes: 0.08 each
 * - Hanging signs: 0.2 each
 * - Lanterns: 0.2 each
 */
public class WorkshopBeautyCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsBeauty");

    private static final float POTTED_PLANT_SCORE = 0.3f;
    private static final float PAINTING_SCORE = 0.2f;
    private static final float CARPET_SCORE = 0.05f;
    private static final float COLORED_GLASS_SCORE = 0.08f;
    private static final float HANGING_SIGN_SCORE = 0.2f;
    private static final float LANTERN_SCORE = 0.2f;

    private static final int WORKSHOP_BEAUTY_RADIUS = 16;  // Search radius around workshop

    /**
     * Calculate beauty score for a workshop centered at the given workstation position
     */
    public static float calculateBeautyScore(Level level, BlockPos workstationPos) {
        float score = 0.0f;

        // Search in a cube around the workstation
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = -WORKSHOP_BEAUTY_RADIUS; x <= WORKSHOP_BEAUTY_RADIUS; x++) {
            for (int y = -WORKSHOP_BEAUTY_RADIUS; y <= WORKSHOP_BEAUTY_RADIUS; y++) {
                for (int z = -WORKSHOP_BEAUTY_RADIUS; z <= WORKSHOP_BEAUTY_RADIUS; z++) {
                    mutablePos.set(workstationPos.getX() + x, workstationPos.getY() + y, workstationPos.getZ() + z);

                    Block block = level.getBlockState(mutablePos).getBlock();
                    float blockScore = getBlockBeautyScore(block);
                    score += blockScore;
                }
            }
        }

        LOGGER.debug("Workshop at {} has beauty score: {}", workstationPos, score);
        return score;
    }

    /**
     * Calculate beauty score for a workshop in a specific search radius
     */
    public static float calculateBeautyScore(Level level, BlockPos workstationPos, int searchRadius) {
        float score = 0.0f;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    mutablePos.set(workstationPos.getX() + x, workstationPos.getY() + y, workstationPos.getZ() + z);

                    Block block = level.getBlockState(mutablePos).getBlock();
                    float blockScore = getBlockBeautyScore(block);
                    score += blockScore;
                }
            }
        }

        LOGGER.debug("Workshop at {} has beauty score: {} (radius: {})", workstationPos, score, searchRadius);
        return score;
    }

    /**
     * Get beauty score contribution for a single block
     */
    private static float getBlockBeautyScore(Block block) {
        if (block instanceof FlowerPotBlock) {
            return POTTED_PLANT_SCORE;
        } else if (block instanceof CarpetBlock) {
            return CARPET_SCORE;
        } else if (block instanceof StainedGlassBlock || block instanceof StainedGlassPaneBlock) {
            return COLORED_GLASS_SCORE;
        } else if (block instanceof SignBlock) {
            return HANGING_SIGN_SCORE;
        } else if (block instanceof LanternBlock) {
            return LANTERN_SCORE;
        } else if (isPainting(block)) {
            return PAINTING_SCORE;
        }
        return 0.0f;
    }

    /**
     * Check if block is a painting block (includes item frames with paintings)
     */
    private static boolean isPainting(Block block) {
        String name = block.getClass().getSimpleName().toLowerCase();
        return name.contains("painting") || name.contains("frame");
    }


}
