package com.github.syren_dev_tech.vwam.events;

import com.github.syren_dev_tech.vwam.data.RegistryHandler;
import com.github.syren_dev_tech.vwam.data.ProfessionTrade;
import com.github.syren_dev_tech.vwam.data.VillageConfiguration;
import com.github.syren_dev_tech.vwam.network.OpenVillagerTradeScreenPacket;
import com.github.syren_dev_tech.vwam.village.VillageDetector;
import com.github.syren_dev_tech.vwam.village.VillageManager;
import com.github.syren_dev_tech.vwam.village.ReputationEffects;
import com.github.syren_dev_tech.vwam.workshop.WorkshopManager;
import com.github.syren_dev_tech.vwam.workshop.WorkshopValidationResult;
import com.github.syren_dev_tech.vwam.villager.VillagerRoutineManager;
import com.github.syren_dev_tech.vwam.villager.VillagerRoutineStatus;
import com.github.syren_dev_tech.vwam.villager.VillagerRoutineEventHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler for villager-related events, including village detection and trading.
 */
public class VillagerEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsEvents");
    private static final String CUSTOM_PROFESSION_NBT = "CustomProfession";

    @SubscribeEvent
    public void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            LOGGER.info("Villager {} joined level", villager.getId());
            
            // Only process on server side
            if (event.getLevel().isClientSide) {
                return;
            }
            
            ServerLevel serverLevel = (ServerLevel) event.getLevel();
            
            // Detect and tag villager with village ID
            VillageDetector.detectAndTagVillager(villager, serverLevel);
            
            // Log the village assignment
            String villageId = VillageDetector.getVillageId(villager);
            LOGGER.info("Villager {} assigned to village: {}", villager.getId(), villageId);
            
            // Check if this villager has a custom profession to apply
            CompoundTag tag = villager.getPersistentData();
            if (tag.contains(CUSTOM_PROFESSION_NBT, Tag.TAG_STRING)) {
                String professionIdStr = tag.getString(CUSTOM_PROFESSION_NBT);
                LOGGER.info("Villager {} has custom profession: {}", villager.getId(), professionIdStr);
                ResourceLocation professionId = ResourceLocation.parse(professionIdStr);
                applyCustomProfession(villager, professionId);
            }

            // Initialize workshop validation for this villager
            WorkshopManager workshopManager = WorkshopManager.getInstance();
            workshopManager.getWorkshopState(villager);
            LOGGER.info("Initialized workshop state for villager {}", villager.getId());
        }
    }

    /**
     * Handle player right-click interactions with villagers
     */
    @SubscribeEvent
    public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        LOGGER.debug("Entity interact event fired - Target: {}", event.getTarget().getClass().getSimpleName());
        
        // Only handle on server side
        if (event.getLevel().isClientSide) {
            LOGGER.debug("EntityInteract on client side, ignoring");
            return;
        }

        // Check if target is a villager
        if (!(event.getTarget() instanceof Villager)) {
            LOGGER.debug("EntityInteract target is not a Villager: {}", event.getTarget().getClass().getSimpleName());
            return;
        }

        Villager villager = (Villager) event.getTarget();
        LOGGER.info("Player {} clicked on villager {}", event.getEntity().getName().getString(), villager.getId());

        // Check if this villager is part of a village
        if (!VillageDetector.isPartOfVillage(villager)) {
            LOGGER.info("Villager {} is not part of any village, allowing vanilla interaction", villager.getId());
            return;
        }
        
        // Check villager routine status - can only trade if socialized
        if (!VillagerRoutineEventHandler.canVillagerTrade(villager)) {
            LOGGER.info("Villager {} cannot trade - not socialized yet", villager.getId());
            // Optionally prevent the trade interaction here
            // For now, we'll allow vanilla handling as a fallback
        }

        String villageIdStr = VillageDetector.getVillageId(villager);
        String villageName = VillageDetector.getVillageName(villager);
        LOGGER.info("Villager {} is part of village: {} ({})", villager.getId(), villageIdStr, villageName);
        
        // Trigger daily routine checks if this is first work attempt
        VillagerRoutineEventHandler.onVillagerFirstWorkAttempt(villager);
        
        // Only proceed if player is a ServerPlayer
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            LOGGER.warn("Interacting entity is not a ServerPlayer");
            return;
        }
        
        // Convert village ID string to ResourceLocation
        ResourceLocation villageId = ResourceLocation.fromNamespaceAndPath("vwam", villageIdStr);
        
        // CHECK PROFESSION - villager must have a valid profession
        VillagerData villagerData = villager.getVillagerData();
        VillagerProfession vanillaProfession = villagerData.getProfession();
        if (vanillaProfession == VillagerProfession.NONE) {
            LOGGER.info("Player {} attempted to trade with unemployed villager {}", 
                serverPlayer.getName().getString(), villager.getId());
            serverPlayer.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    "§cThis villager doesn't have a profession yet!§r"),
                true // Show above hotbar
            );
            return; // Prevent trade interaction
        }
        
        // CHECK REPUTATION - player must not be too hostile
        if (!ReputationEffects.canPlayerTrade(serverPlayer, villageId)) {
            LOGGER.info("Player {} cannot trade - reputation is too hostile with village {}", 
                serverPlayer.getName().getString(), villageId);
            serverPlayer.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                    "§cThe villagers won't trade with you - your reputation is too low!§r"),
                true // Show above hotbar
            );
            return; // Prevent trade interaction
        }
        
        // Show reputation info
        LOGGER.info("Player {} reputation with village {}: {}", 
            serverPlayer.getName().getString(), villageId,
            ReputationEffects.getReputationStatus(serverPlayer, villageId));

        try {
            // Get configuration and info
            int villagerLevel = villager.getVillagerData().getLevel();

            LOGGER.info("Attempting to open trade menu for villager {} (level: {}, village: {})", 
                    villager.getId(), villagerLevel, villageId);

            // Get village configuration
            VillageConfiguration villageConfig = RegistryHandler.getInstance()
                    .getVillageConfiguration(villageId)
                    .orElse(null);
            
            if (villageConfig == null) {
                LOGGER.info("No configuration found for village {}, using default config", villageId);
                // Create a default configuration for this village
                villageConfig = new VillageConfiguration(
                    villageId,
                    villageIdStr,
                    64,    // baseStockAmount
                    0.25f, // demandThreshold
                    0.75f, // surplusThreshold
                    1.5f,  // demandPriceMultiplier
                    0.5f,  // surplusPriceMultiplier
                    0.0f,  // initialReputation
                    1.0f,  // maxReputation
                    -1.0f, // minReputation
                    0.05f, // tradeReputationGain
                    -0.5f, // hostileThreshold
                    0.5f,  // friendlyThreshold
                    0.1f,  // tradeDiscountPerStep
                    2.0f,  // heroBuffMultiplier
                    24     // heroBuffDurationHours
                );
            }
            LOGGER.info("Using village configuration for {}", villageId);

            // Get profession ID - map vanilla profession to our custom one
            ResourceLocation professionId = ResourceLocation.fromNamespaceAndPath("vwam", "farmer"); // default fallback
            CompoundTag tag = villager.getPersistentData();
            
            // First check if villager has custom profession assigned
            if (tag.contains(CUSTOM_PROFESSION_NBT, Tag.TAG_STRING)) {
                String professionIdStr = tag.getString(CUSTOM_PROFESSION_NBT);
                professionId = ResourceLocation.parse(professionIdStr);
                LOGGER.info("Using custom profession for villager {}: {}", villager.getId(), professionId);
            } else {
                // Map vanilla profession to our custom profession (already verified not NONE above)
                String professionName = mapVanillaProfessionToCustom(vanillaProfession);
                if (professionName == null) {
                    LOGGER.info("Player {} attempted to trade with villager {} that has unmappable profession {}", 
                        serverPlayer.getName().getString(), villager.getId(), vanillaProfession);
                    serverPlayer.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(
                            "§cThis villager doesn't have a tradeable profession!§r"),
                        true
                    );
                    return;
                }
                professionId = ResourceLocation.fromNamespaceAndPath("vwam", professionName);
                LOGGER.info("Mapped vanilla profession {} to custom profession {} for villager {}", 
                        vanillaProfession, professionId, villager.getId());
            }
            LOGGER.info("Profession for villager {}: {}", villager.getId(), professionId);
            
            // Get or create village
            VillageManager villageManager = RegistryHandler.getVillageManager();
            LOGGER.info("Got village manager");
            
            VillageManager.Village village = villageManager.getVillage(villageId).orElse(null);
            if (village == null) {
                LOGGER.info("Village {} not found in manager, registering new village", villageId);
                // Create a new village using config
                village = villageManager.registerVillage(villageConfig);
                LOGGER.info("Registered new village: {}", villageId);
                
                // Initialize stockpile with all profession items at baseline amounts
                initializeVillageStockpile(village, villageConfig);
            } else {
                LOGGER.info("Village {} found, checking if stockpile needs initialization", villageId);
                // Village exists - check if stockpile is empty and initialize if needed
                if (village.getStockpile().getAllItems().isEmpty()) {
                    LOGGER.info("Village {} has empty stockpile, initializing items", villageId);
                    initializeVillageStockpile(village, villageConfig);
                }
            }
            
            // Serialize stockpile to NBT
            CompoundTag stockpileData = new CompoundTag();
            com.github.syren_dev_tech.vwam.village.VillageStockpile stockpile = village.getStockpile();
            java.util.Map<String, Integer> allItems = stockpile.getAllItems();
            for (java.util.Map.Entry<String, Integer> entry : allItems.entrySet()) {
                stockpileData.putInt(entry.getKey(), entry.getValue());
            }
            LOGGER.info("Village {} stockpile has {} items", villageId, allItems.size());

            // Send packet to open the trade screen on client
            LOGGER.info("Opening trade menu for player {}", serverPlayer.getName().getString());
            OpenVillagerTradeScreenPacket packet = new OpenVillagerTradeScreenPacket(
                    villager.getId(),
                    villageId,
                    villageName,
                    villagerLevel,
                    professionId,
                    stockpileData
            );
            serverPlayer.connection.send(packet);
            LOGGER.info("Successfully sent trade menu packet for villager {} (profession: {})", 
                    villager.getId(), professionId);
            
            // Freeze the villager in place so it doesn't wander during trading
            // Also prevents multiple players from trading with the same villager
            boolean frozeSuccessfully = FrozenVillagerTracker.freezeVillager(villager, serverPlayer);
            if (!frozeSuccessfully) {
                LOGGER.info("Cannot open trade - villager {} is already being traded with by another player", villager.getId());
                serverPlayer.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                        "\u00a7cThis villager is currently trading with another player!\u00a7r"),
                    true // Show above hotbar
                );
                return; // Prevent trade interaction
            }
            villager.setNoAi(true);
            LOGGER.info("Froze villager {} AI for player {} to prevent wandering", 
                villager.getId(), serverPlayer.getName().getString());
            
            // Prevent vanilla merchant screen from opening
            event.setCanceled(true);
            LOGGER.info("Canceled vanilla merchant screen");
            
        } catch (Exception e) {
            LOGGER.error("Failed to open trade menu", e);
            e.printStackTrace();
        }
    }

    /**
     * Apply a custom profession to a villager
     */
    private void applyCustomProfession(Villager villager, ResourceLocation professionId) {
        com.github.syren_dev_tech.vwam.data.CustomProfession customProfession = RegistryHandler.getInstance()
                .getProfessionOrThrow(professionId);

        if (customProfession == null) {
            LOGGER.warn("Custom profession {} not found", professionId);
            return;
        }

        try {
            Block workstationBlock = getBlockFromRegistry(customProfession.getWorkstationBlock());
            if (workstationBlock == null || workstationBlock == Blocks.AIR) {
                LOGGER.warn("Workstation block not found for profession {}", professionId);
                return;
            }

            VillagerData data = villager.getVillagerData();
            CompoundTag tag = villager.getPersistentData();
            tag.putString(CUSTOM_PROFESSION_NBT, professionId.toString());
            tag.putString("ProfessionDisplayName", customProfession.getDisplayName());
            tag.putString("WorkstationBlock", customProfession.getWorkstationBlock().toString());

            VillagerData updatedData = new VillagerData(data.getType(), VillagerProfession.LIBRARIAN, data.getLevel());
            villager.setVillagerData(updatedData);

            LOGGER.info("Applied custom profession {} to villager {}", professionId, villager.getId());
        } catch (Exception e) {
            LOGGER.error("Failed to apply custom profession", e);
        }
    }

    /**
     * Map vanilla VillagerProfession to our custom profession names
     */
    /**
     * Initialize a village's stockpile with all profession items at baseline amounts
     */
    private void initializeVillageStockpile(VillageManager.Village village, VillageConfiguration villageConfig) {
        LOGGER.info("Initializing stockpile for village {}", village.getVillageId());
        
        com.github.syren_dev_tech.vwam.village.VillageStockpile stockpile = village.getStockpile();
        int baseAmount = villageConfig.getBaseStockAmount();
        
        // Collect all unique items from all profession sell trades
        java.util.Set<String> itemsToInitialize = new java.util.HashSet<>();
        
        for (com.github.syren_dev_tech.vwam.data.CustomProfession profession : RegistryHandler.getInstance().getAllProfessions()) {
            // Get all trade levels for this profession (for now just use level 1)
            java.util.Map<Integer, com.github.syren_dev_tech.vwam.data.ProfessionTrade> allTrades = 
                    RegistryHandler.getInstance().getAllProfessionTrades(profession.getId());
            
            for (java.util.Map.Entry<Integer, com.github.syren_dev_tech.vwam.data.ProfessionTrade> entry : allTrades.entrySet()) {
                com.github.syren_dev_tech.vwam.data.ProfessionTrade trade = entry.getValue();
                
                // Add all sell trade items (what this profession produces)
                for (com.github.syren_dev_tech.vwam.data.ProfessionTrade.Trade sellTrade : trade.getSellTrades()) {
                    itemsToInitialize.add(sellTrade.item);
                }
                
                // Add all buy trade items (what this profession buys)
                for (com.github.syren_dev_tech.vwam.data.ProfessionTrade.Trade buyTrade : trade.getBuyTrades()) {
                    itemsToInitialize.add(buyTrade.item);
                }
            }
        }
        
        // Initialize each item in the stockpile at baseline amount
        for (String itemId : itemsToInitialize) {
            stockpile.initializeItem(itemId, baseAmount);
        }
        
        LOGGER.info("Village {} stockpile initialized with {} items at baseline amount {}", 
                village.getVillageId(), itemsToInitialize.size(), baseAmount);
    }

    private String mapVanillaProfessionToCustom(VillagerProfession vanillaProfession) {
        if (vanillaProfession == VillagerProfession.FARMER) return "farmer";
        if (vanillaProfession == VillagerProfession.LIBRARIAN) return "librarian";
        if (vanillaProfession == VillagerProfession.ARMORER) return "armorer";
        if (vanillaProfession == VillagerProfession.BUTCHER) return "butcher";
        if (vanillaProfession == VillagerProfession.CARTOGRAPHER) return "cartographer";
        if (vanillaProfession == VillagerProfession.CLERIC) return "cleric";
        if (vanillaProfession == VillagerProfession.FISHERMAN) return "fisherman";
        if (vanillaProfession == VillagerProfession.FLETCHER) return "fletcher";
        if (vanillaProfession == VillagerProfession.SHEPHERD) return "shepherd";
        if (vanillaProfession == VillagerProfession.TOOLSMITH) return "toolsmith";
        if (vanillaProfession == VillagerProfession.WEAPONSMITH) return "weaponsmith";
        if (vanillaProfession == VillagerProfession.LEATHERWORKER) return "leatherworker";
        
        // For unknown professions, return null - do NOT default to farmer
        LOGGER.warn("Unknown profession: {} - cannot determine valid trade profile", vanillaProfession);
        return null;
    }

    private Block getBlockFromRegistry(ResourceLocation blockId) {
        return Blocks.AIR;
    }

    public static ResourceLocation getCustomProfessionId(Villager villager) {
        CompoundTag tag = villager.getPersistentData();
        if (tag.contains(CUSTOM_PROFESSION_NBT, Tag.TAG_STRING)) {
            return ResourceLocation.parse(tag.getString(CUSTOM_PROFESSION_NBT));
        }
        return null;
    }

    public static String getCustomProfessionDisplayName(Villager villager) {
        CompoundTag tag = villager.getPersistentData();
        if (tag.contains("ProfessionDisplayName", Tag.TAG_STRING)) {
            return tag.getString("ProfessionDisplayName");
        }
        return "Unknown";
    }

    /**
     * Called periodically to check for villagers sleeping in beds
     */
    public void checkVillagerSleep(Villager villager) {
        if (villager.level() != null && !villager.level().isClientSide) {
            BlockPos pos = villager.blockPosition();
            if (villager.level().getBlockState(pos).getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                VillagerRoutineEventHandler.onVillagerSleep(villager);
            }
        }
    }

    /**
     * Get routine status for a villager (for debugging or UI)
     */
    public static VillagerRoutineStatus getRoutineStatus(Villager villager) {
        VillagerRoutineManager manager = VillagerRoutineManager.getInstance();
        return manager.getRoutineStatus(villager);
    }
}
