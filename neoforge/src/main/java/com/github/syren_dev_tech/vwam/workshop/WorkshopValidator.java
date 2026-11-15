package com.github.syren_dev_tech.vwam.workshop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.CarpetBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates whether a workstation meets all workshop requirements.
 *
 * Workshop requirements (8 rules):
 * 1. Accessible workstation available (vanilla requirement)
 * 2. 3+ "support" workstations available (crafting tables, furnaces, campfires)
 * 3. Nearby storage available (chests, barrels)
 * 4. Distance from workstation to support stations < 9 blocks
 * 5. Distance from workstation to storage < 9 blocks
 * 6. 3x3 area of free space around workstation (1 block radius)
 * 7. All spots in 3x3 area have light level > 9
 * 8. No identical workstation within 9 blocks
 */
public class WorkshopValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsValidator");

    // Configuration constants
    private static final int MAX_SUPPORT_DISTANCE = 9;
    private static final int MAX_STORAGE_DISTANCE = 9;
    private static final int WORKSTATION_CONFLICT_DISTANCE = 9;
    private static final int MIN_LIGHT_LEVEL = 9;
    private static final int MIN_SUPPORT_STATIONS = 3;

    // Block type checks
    private static final Set<Class<?>> SUPPORT_BLOCK_TYPES = new HashSet<>();

    static {
        SUPPORT_BLOCK_TYPES.add(CraftingTableBlock.class);
        SUPPORT_BLOCK_TYPES.add(FurnaceBlock.class);
        SUPPORT_BLOCK_TYPES.add(CampfireBlock.class);
    }

    /**
     * Validate a complete workshop
     */
    public static WorkshopValidationResult validateWorkshop(
            Level level,
            BlockPos workstationPos,
            Block workstationType
    ) {
        List<String> failureReasons = new ArrayList<>();

        // Rule 1: Workstation is accessible (basic check - already has a workstation)
        if (workstationType == null) {
            failureReasons.add("Workstation block is missing or invalid");
            return new WorkshopValidationResult(false, workstationPos, failureReasons);
        }

        // Check if the workstation exists at the position
        if (level.getBlockState(workstationPos).getBlock() != workstationType) {
            failureReasons.add("No valid workstation block found at position");
            return new WorkshopValidationResult(false, workstationPos, failureReasons);
        }

        // Rule 7: Check light levels in 3x3 area (do this early as it's a disqualifier)
        List<BlockPos> darkBlocks = checkLighting(level, workstationPos);
        if (!darkBlocks.isEmpty()) {
            failureReasons.add(String.format(
                    "Insufficient lighting: %d blocks have light level <= %d",
                    darkBlocks.size(),
                    MIN_LIGHT_LEVEL
            ));
            return new WorkshopValidationResult(false, workstationPos, failureReasons);
        }

        // Rule 6: Check for 3x3 free space around workstation
        List<BlockPos> blockedSpaces = checkFreeSpace(level, workstationPos);
        if (!blockedSpaces.isEmpty()) {
            failureReasons.add(String.format(
                    "Insufficient free space: %d blocks are occupied in the 3x3 area",
                    blockedSpaces.size()
            ));
            return new WorkshopValidationResult(false, workstationPos, failureReasons);
        }

        // Rule 2 & 4: Find support stations
        List<BlockPos> supportStations = findSupportStations(level, workstationPos);
        if (supportStations.size() < MIN_SUPPORT_STATIONS) {
            failureReasons.add(String.format(
                    "Insufficient support stations: found %d, need %d (crafting tables, furnaces, campfires)",
                    supportStations.size(),
                    MIN_SUPPORT_STATIONS
            ));
            return new WorkshopValidationResult(false, workstationPos, failureReasons);
        }

        // Rule 3 & 5: Find storage
        List<BlockPos> storageBlocks = findStorageBlocks(level, workstationPos);
        if (storageBlocks.isEmpty()) {
            failureReasons.add("No storage found within range (chests or barrels)");
            return new WorkshopValidationResult(false, workstationPos, failureReasons);
        }

        // Rule 8: Check for duplicate workstations
        List<BlockPos> duplicates = findDuplicateWorkstations(level, workstationPos, workstationType);
        if (!duplicates.isEmpty()) {
            failureReasons.add(String.format(
                    "Duplicate workstations found within %d blocks: %d conflicts",
                    WORKSTATION_CONFLICT_DISTANCE,
                    duplicates.size()
            ));
            return new WorkshopValidationResult(false, workstationPos, failureReasons);
        }

        // All checks passed - calculate quality and beauty
        WorkshopValidationResult.WorkshopQuality quality = WorkshopQualityCalculator.calculateQuality(
                supportStations,
                workstationPos
        );

        float beautyScore = WorkshopBeautyCalculator.calculateBeautyScore(level, workstationPos);

        LOGGER.info(
                "Workshop validated at {}: quality={}, beauty={}, support={}, storage={}",
                workstationPos,
                quality,
                beautyScore,
                supportStations.size(),
                storageBlocks.size()
        );

        return new WorkshopValidationResult(
                true,
                workstationPos,
                quality,
                beautyScore,
                supportStations,
                storageBlocks
        );
    }

    /**
     * Rule 7: Check lighting levels in 3x3 area around workstation
     * Returns list of blocks with insufficient light
     */
    private static List<BlockPos> checkLighting(Level level, BlockPos workstationPos) {
        List<BlockPos> darkBlocks = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos checkPos = workstationPos.offset(x, 0, z);
                int lightLevel = level.getMaxLocalRawBrightness(checkPos);

                if (lightLevel <= MIN_LIGHT_LEVEL) {
                    darkBlocks.add(checkPos);
                }
            }
        }

        return darkBlocks;
    }

    /**
     * Rule 6: Check for free space in 3x3 area around workstation
     * The workstation itself can be occupied, but the surrounding area should be free
     * Returns list of occupied blocks
     */
    private static List<BlockPos> checkFreeSpace(Level level, BlockPos workstationPos) {
        List<BlockPos> blockedSpaces = new ArrayList<>();

        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {  // Check above and at workstation level
                for (int z = -1; z <= 1; z++) {
                    // Skip the workstation block itself
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    BlockPos checkPos = workstationPos.offset(x, y, z);
                    Block block = level.getBlockState(checkPos).getBlock();
                    
                    // Check if block is solid (not replaceable blocks like tall grass, flowers, etc.)
                    if (isSolidBlock(block)) {
                        blockedSpaces.add(checkPos);
                    }
                }
            }
        }

        return blockedSpaces;
    }

    /**
     * Check if a block is solid (not replaceable or passable)
     */
    private static boolean isSolidBlock(Block block) {
        // Check if it's air or carpet (known passable blocks)
        if (block == Blocks.AIR || block instanceof CarpetBlock) {
            return false;  // Not solid
        }
        // All other blocks are considered solid for our purposes
        return true;
    }

    /**
     * Rule 2 & 4: Find support stations within range
     */
    private static List<BlockPos> findSupportStations(Level level, BlockPos workstationPos) {
        List<BlockPos> supportStations = new ArrayList<>();

        // Search in a cube around the workstation
        for (int x = -MAX_SUPPORT_DISTANCE; x <= MAX_SUPPORT_DISTANCE; x++) {
            for (int y = -MAX_SUPPORT_DISTANCE; y <= MAX_SUPPORT_DISTANCE; y++) {
                for (int z = -MAX_SUPPORT_DISTANCE; z <= MAX_SUPPORT_DISTANCE; z++) {
                    // Skip the workstation itself
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    BlockPos checkPos = workstationPos.offset(x, y, z);
                    Block block = level.getBlockState(checkPos).getBlock();

                    if (isSupportBlock(block)) {
                        double distance = Math.sqrt(workstationPos.distSqr(checkPos));
                        if (distance < MAX_SUPPORT_DISTANCE) {
                            supportStations.add(checkPos);
                        }
                    }
                }
            }
        }

        return supportStations;
    }

    /**
     * Rule 3 & 5: Find storage blocks within range
     */
    private static List<BlockPos> findStorageBlocks(Level level, BlockPos workstationPos) {
        List<BlockPos> storageBlocks = new ArrayList<>();

        // Search in a cube around the workstation
        for (int x = -MAX_STORAGE_DISTANCE; x <= MAX_STORAGE_DISTANCE; x++) {
            for (int y = -MAX_STORAGE_DISTANCE; y <= MAX_STORAGE_DISTANCE; y++) {
                for (int z = -MAX_STORAGE_DISTANCE; z <= MAX_STORAGE_DISTANCE; z++) {
                    BlockPos checkPos = workstationPos.offset(x, y, z);
                    Block block = level.getBlockState(checkPos).getBlock();

                    if (isStorageBlock(block)) {
                        double distance = Math.sqrt(workstationPos.distSqr(checkPos));
                        if (distance < MAX_STORAGE_DISTANCE) {
                            storageBlocks.add(checkPos);
                        }
                    }
                }
            }
        }

        return storageBlocks;
    }

    /**
     * Rule 8: Find duplicate workstations of the same type within range
     */
    private static List<BlockPos> findDuplicateWorkstations(
            Level level,
            BlockPos workstationPos,
            Block workstationType
    ) {
        List<BlockPos> duplicates = new ArrayList<>();

        // Search in a cube around the workstation
        for (int x = -WORKSTATION_CONFLICT_DISTANCE; x <= WORKSTATION_CONFLICT_DISTANCE; x++) {
            for (int y = -WORKSTATION_CONFLICT_DISTANCE; y <= WORKSTATION_CONFLICT_DISTANCE; y++) {
                for (int z = -WORKSTATION_CONFLICT_DISTANCE; z <= WORKSTATION_CONFLICT_DISTANCE; z++) {
                    // Skip the workstation itself
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    BlockPos checkPos = workstationPos.offset(x, y, z);
                    Block block = level.getBlockState(checkPos).getBlock();

                    // Check if it's the same type of workstation
                    if (block == workstationType) {
                        double distance = Math.sqrt(workstationPos.distSqr(checkPos));
                        if (distance < WORKSTATION_CONFLICT_DISTANCE) {
                            duplicates.add(checkPos);
                        }
                    }
                }
            }
        }

        return duplicates;
    }

    /**
     * Check if a block is a support block (crafting table, furnace, campfire)
     */
    private static boolean isSupportBlock(Block block) {
        return block instanceof CraftingTableBlock ||
                block instanceof FurnaceBlock ||
                block instanceof CampfireBlock;
    }

    /**
     * Check if a block is a storage block (chest, barrel, double chest)
     */
    private static boolean isStorageBlock(Block block) {
        return block instanceof ChestBlock ||
                block instanceof BarrelBlock;
    }
}
