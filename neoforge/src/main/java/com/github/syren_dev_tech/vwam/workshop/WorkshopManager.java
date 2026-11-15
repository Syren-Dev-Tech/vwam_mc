package com.github.syren_dev_tech.vwam.workshop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central manager for workshop validation and villager workshop bindings.
 * Handles daily validation checks and maintains workshop state for all villagers.
 */
public class WorkshopManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsManager");

    // Map of villager UUID to their workshop state
    private final Map<UUID, WorkshopState> villagerWorkshops;

    public WorkshopManager() {
        this.villagerWorkshops = new HashMap<>();
    }

    /**
     * Get or create workshop state for a villager
     */
    public WorkshopState getWorkshopState(Villager villager) {
        UUID villagerUUID = villager.getUUID();
        return villagerWorkshops.computeIfAbsent(villagerUUID, key -> new WorkshopState(villager));
    }

    /**
     * Validate a villager's workshop on the given level
     * This should be called once per day per villager
     */
    public WorkshopValidationResult validateVillagerWorkshop(Villager villager, Level level) {
        // Get villager's workstation type from profession
        // For now, we'll use a default fallback and let the workstation be discovered
        Block workstationBlock = getDefaultWorkstationForProfession(villager);
        
        if (workstationBlock == null || workstationBlock == Blocks.AIR) {
            return createInvalidResult(villager.blockPosition(), "No workstation type defined for profession");
        }

        // Find the workstation near the villager
        BlockPos workstationPos = findNearestWorkstation(level, villager.blockPosition(), workstationBlock, 32);
        if (workstationPos == null) {
            return createInvalidResult(villager.blockPosition(), "No workstation found near villager (within 32 blocks)");
        }

        // Validate the workshop
        WorkshopValidationResult result = WorkshopValidator.validateWorkshop(level, workstationPos, workstationBlock);

        // Update villager's workshop state
        WorkshopState state = getWorkshopState(villager);
        state.setValidationResult(result);

        return result;
    }

    /**
     * Validate a villager's current workshop at their position
     * This is called when the villager first tries to work each day
     */
    public WorkshopValidationResult validateCurrentWorkshop(Villager villager) {
        Level level = villager.level();
        if (level == null || level.isClientSide) {
            return null;
        }
        return validateVillagerWorkshop(villager, level);
    }

    /**
     * Quick validation check - only checks critical conditions
     * Used for runtime checks without full validation
     */
    public boolean canVillagerWork(Villager villager) {
        WorkshopState state = getWorkshopState(villager);
        return state.canWorkToday();
    }

    /**
     * Get quality multiplier for a villager's workshop
     */
    public float getWorkshopQualityMultiplier(Villager villager) {
        WorkshopState state = getWorkshopState(villager);
        return state.getQualityMultiplier();
    }

    /**
     * Get beauty rating for a villager's workshop
     */
    public WorkshopValidationResult.WorkshopBeauty getWorkshopBeautyRating(Villager villager) {
        WorkshopState state = getWorkshopState(villager);
        return state.getBeautyRating();
    }

    /**
     * Get trade multiplier from workshop beauty
     */
    public float getWorkshopBeautyTradeMultiplier(Villager villager) {
        WorkshopState state = getWorkshopState(villager);
        return state.getBeautyTradeMultiplier();
    }

    /**
     * Reset workshop state when villager changes profession
     */
    public void resetVillagerWorkshop(Villager villager) {
        UUID villagerUUID = villager.getUUID();
        WorkshopState state = villagerWorkshops.get(villagerUUID);
        if (state != null) {
            state.reset();
            LOGGER.info("Reset workshop state for villager {}", villagerUUID);
        }
    }

    /**
     * Clear all cached workshop states (for server shutdown, etc.)
     */
    public void clearAll() {
        villagerWorkshops.clear();
        LOGGER.info("Cleared all workshop states");
    }

    /**
     * Get default workstation block for a villager's profession
     * This is a simplified mapping - in production, would use profession registry
     */
    private Block getDefaultWorkstationForProfession(Villager villager) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == null) {
            return Blocks.LECTERN;  // Default fallback
        }

        // Map professions to their workstation blocks
        String professionName = profession.toString().toLowerCase();
        
        if (professionName.contains("farmer")) return Blocks.COMPOSTER;
        if (professionName.contains("librarian")) return Blocks.LECTERN;
        if (professionName.contains("cleric")) return Blocks.BREWING_STAND;
        if (professionName.contains("armorer")) return Blocks.BLAST_FURNACE;
        if (professionName.contains("toolsmith")) return Blocks.SMITHING_TABLE;
        if (professionName.contains("weaponsmith")) return Blocks.GRINDSTONE;
        if (professionName.contains("cartographer")) return Blocks.CARTOGRAPHY_TABLE;
        if (professionName.contains("fisherman")) return Blocks.BARREL;
        if (professionName.contains("shepherd")) return Blocks.LOOM;
        if (professionName.contains("fletcher")) return Blocks.FLETCHING_TABLE;
        if (professionName.contains("leatherworker")) return Blocks.CAULDRON;
        if (professionName.contains("butcher")) return Blocks.SMOKER;
        
        return Blocks.LECTERN;  // Default fallback
    }

    /**
     * Find the nearest workstation of the given type near the villager
     */
    private BlockPos findNearestWorkstation(
            Level level,
            BlockPos villagerPos,
            Block workstationType,
            int searchRadius
    ) {
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = villagerPos.offset(x, y, z);
                    Block block = level.getBlockState(checkPos).getBlock();

                    if (block == workstationType) {
                        double distance = Math.sqrt(villagerPos.distSqr(checkPos));
                        if (distance < nearestDistance) {
                            nearestDistance = distance;
                            nearest = checkPos;
                        }
                    }
                }
            }
        }

        return nearest;
    }

    /**
     * Create an invalid validation result with error message
     */
    private WorkshopValidationResult createInvalidResult(BlockPos pos, String reason) {
        java.util.List<String> reasons = new java.util.ArrayList<>();
        reasons.add(reason);
        return new WorkshopValidationResult(false, pos, reasons);
    }

    // Singleton instance
    private static WorkshopManager instance;

    public static WorkshopManager getInstance() {
        if (instance == null) {
            instance = new WorkshopManager();
        }
        return instance;
    }

    public static void setInstance(WorkshopManager manager) {
        instance = manager;
    }
}
