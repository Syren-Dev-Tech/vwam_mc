package com.github.syren_dev_tech.vwam.ui;

import com.github.syren_dev_tech.vwam.data.ProfessionTrade;
import com.github.syren_dev_tech.vwam.data.VillageConfiguration;
import com.github.syren_dev_tech.vwam.data.RegistryHandler;
import com.github.syren_dev_tech.vwam.village.VillageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles opening of custom villager trade screens.
 * Replaces vanilla merchant screens for custom professions.
 */
public class VillagerTradeScreenHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsUI");

    /**
     * Open the custom trade screen for a villager
     * This should be called when a player right-clicks a custom profession villager
     *
     * @param level The level the player is in
     * @param player The player opening the screen
     * @param villager The villager being traded with
     */
    public static void openCustomTradeScreen(
            Level level,
            Player player,
            Villager villager
    ) {
        if (!level.isClientSide) {
            LOGGER.warn("openCustomTradeScreen called on server side!");
            return;
        }

        // Get villager's custom profession from NBT
        String professionIdStr = villager.getPersistentData().getString("CustomProfession");
        if (professionIdStr.isEmpty()) {
            LOGGER.warn("Villager {} has no CustomProfession NBT tag", villager.getId());
            return;
        }

        ResourceLocation professionId = ResourceLocation.parse(professionIdStr);
        String villageIdStr = villager.getPersistentData().getString("VillageId");
        if (villageIdStr.isEmpty()) {
            LOGGER.warn("Villager {} is not linked to a village", villager.getId());
            return;
        }

        ResourceLocation villageId = ResourceLocation.parse(villageIdStr);
        int villagerLevel = villager.getVillagerData().getLevel();

        // Get profession trades from registry
        ProfessionTrade professionTrade = getProfessionTrades(professionId, villagerLevel);
        if (professionTrade == null) {
            LOGGER.warn("No trades found for profession {} level {}",
                    professionId, villagerLevel);
            return;
        }

        // Get village configuration
        VillageConfiguration villageConfig = getVillageConfig(villageId);
        if (villageConfig == null) {
            LOGGER.warn("No configuration found for village {}", villageId);
            return;
        }

        // Get village manager singleton
        VillageManager villageManager = RegistryHandler.getVillageManager();

        // Open custom trade screen
        LOGGER.info("Opening custom trade screen for profession {} at level {} for village {}", 
                professionId, villagerLevel, villageId);
        Minecraft.getInstance().setScreen(
                new VillagerTradeScreen(player, villager, professionTrade, villageManager, villageConfig, villageId)
        );
    }

    /**
     * Get profession trades from the registry by profession and level
     */
    private static ProfessionTrade getProfessionTrades(ResourceLocation professionId, int level) {
        try {
            return RegistryHandler.getInstance()
                    .getProfessionTrade(professionId, level)
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.error("Failed to get profession trades for {} level {}", professionId, level, e);
            return null;
        }
    }

    /**
     * Get village configuration from the registry
     */
    private static VillageConfiguration getVillageConfig(ResourceLocation villageId) {
        try {
            return RegistryHandler.getInstance()
                    .getVillageConfiguration(villageId)
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.error("Failed to get village configuration for {}", villageId, e);
            return null;
        }
    }
}
