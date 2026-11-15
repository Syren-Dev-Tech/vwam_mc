package com.github.syren_dev_tech.vwam.ui;

import com.github.syren_dev_tech.vwam.data.VillageConfiguration;
import com.github.syren_dev_tech.vwam.data.ProfessionTrade;
import com.github.syren_dev_tech.vwam.network.CloseVillagerTradeScreenPacket;
import com.github.syren_dev_tech.slml.CustomScreen;
import com.github.syren_dev_tech.slml.builder.UIBuilder;
import com.github.syren_dev_tech.vwam.village.VillageManager;
import com.github.syren_dev_tech.vwam.village.VillageReputation;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom villager trade screen using XML layout definitions. Loads layout from XML resources and dynamically populates trades.
 *
 * IMPORTANT: This screen loads the layout from villager_trade_screen.xml, NOT from hardcoded values. The XML file is the source of truth for UI structure.
 */
public class VillagerTradeScreen extends CustomScreen {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsUI");

    private final Villager villager;
    private final int villagerEntityId;
    private final ProfessionTrade professionTrade;
    private final VillageManager villageManager;
    private final VillageConfiguration villageConfig;
    private final ResourceLocation villageId;

    public VillagerTradeScreen(Player player, Villager villager, ProfessionTrade professionTrade, VillageManager villageManager, VillageConfiguration villageConfig, ResourceLocation villageId) {
        super(player, "vwam", "villager_trade_screen");
        this.villager = villager;
        this.villagerEntityId = villager.getId();
        this.professionTrade = professionTrade;
        this.villageManager = villageManager;
        this.villageConfig = villageConfig;
        this.villageId = villageId;

        this.setOnCloseCallback((Minecraft mc) -> {
            CloseVillagerTradeScreenPacket packet = new CloseVillagerTradeScreenPacket(villagerEntityId);
            this.minecraft.getConnection().send(packet);
            LOGGER.info("Sent close packet for villager {}", villagerEntityId);
            return true;
        });
        this.setOnInitCallback((UIBuilder uiBuilder) -> {
            populateTrades();
            return true;
        });

        String villageName = villageConfig.getDisplayName();
        String residentText = "Resident of " + villageName;

        this.definePlaceholder("village_name", villageName);
        this.definePlaceholder("resident_text", residentText);

        String profName = villager.getVillagerData().getProfession().toString().replace("minecraft:", "").replace("_", " ");
        if (profName.length() > 0) {
            profName = profName.substring(0, 1).toUpperCase() + profName.substring(1);
        }

        this.definePlaceholder("profession", profName);

        String reputationLevel = "Unknown";
        if (player != null && player.getUUID() != null) {
            VillageManager.Village village = villageManager.getVillage(villageId).orElse(null);
            if (village != null) {
                VillageReputation reputation = village.getReputation();
                if (reputation != null) {
                    float rep = reputation.getReputation(player.getUUID());
                    reputationLevel = getReputationLevel(rep);
                }
            }
        }

        this.definePlaceholder("reputation", reputationLevel);

        int villageEmeralds = 0;
        VillageManager.Village village = villageManager.getVillage(villageId).orElse(null);
        if (village != null && village.getStockpile() != null) {
            villageEmeralds = village.getStockpile().getQuantity("minecraft:emerald");
        }

        this.definePlaceholder("village_emeralds", String.valueOf(villageEmeralds));

        int playerEmeralds = 0;
        if (player != null && player.getInventory() != null) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).getItem().toString().contains("emerald")) {
                    playerEmeralds += player.getInventory().getItem(i).getCount();
                }
            }
        }

        this.definePlaceholder("player_emeralds", String.valueOf(playerEmeralds));

        this.definePlaceholder("status_message", "Ready to trade");
    }

    /**
     * Populate the trade sections with dynamically created trade elements.
     *
     * NOTE: With the new XML layout using <repeated> and <layout> tags, trades are displayed via the trade_item.xml component from JSON data. This method now simply logs trade information and lets the XML layout handle rendering.
     */
    private void populateTrades() {
        try {
            LOGGER.info("Populating trades...");

            if (professionTrade == null) {
                LOGGER.warn("No profession trade data available");
                return;
            }

            // Log villager profession information
            String professionName = villager.getVillagerData().getProfession().toString().replace("minecraft:", "").replace("_", " ");
            if (!professionName.isEmpty()) {
                professionName = professionName.substring(0, 1).toUpperCase() + professionName.substring(1);
            }

            int professionLevel = professionTrade.getLevel();
            String levelName = getProfessionLevelName(professionLevel);

            LOGGER.info("=== VILLAGER PROFESSION INFO ===");
            LOGGER.info("Profession: {}", professionName);
            LOGGER.info("Level: {} ({})", professionLevel, levelName);
            LOGGER.info("=== AVAILABLE TRADES ===");

            int tradeCount = 0;

            // Log sell trades
            ProfessionTrade.Trade[] sellTrades = professionTrade.getSellTrades();
            if (sellTrades != null && sellTrades.length > 0) {
                LOGGER.info("Villager SELLS to player:");
                for (ProfessionTrade.Trade trade : sellTrades) {
                    LOGGER.info("  {} x{} -> {} emeralds (min reputation: {})", trade.item, trade.reputationBuy, trade.reputationSell);
                    tradeCount++;
                }
            } else {
                LOGGER.info("Villager SELLS to player: (none)");
            }

            // Log buy trades
            ProfessionTrade.Trade[] buyTrades = professionTrade.getBuyTrades();
            if (buyTrades != null && buyTrades.length > 0) {
                LOGGER.info("Villager BUYS from player:");
                for (ProfessionTrade.Trade trade : buyTrades) {
                    LOGGER.info("  {} x{} -> {} emeralds (min reputation: {})", trade.item, trade.reputationBuy, trade.reputationSell);
                    tradeCount++;
                }
            } else {
                LOGGER.info("Villager BUYS from player: (none)");
            }

            LOGGER.info("=== TOTAL TRADES: {} ===", tradeCount);
            LOGGER.info("NOTE: Trades are rendered from trades.json via <repeated> tag in XML layout");

        } catch (Exception e) {
            LOGGER.error("Error populating trades", e);
        }
    }

    /**
     * Get profession level name from level number (1-5)
     */
    private String getProfessionLevelName(int level) {
        return switch (level) {
            case 1 -> "Novice";
            case 2 -> "Apprentice";
            case 3 -> "Journeyman";
            case 4 -> "Expert";
            case 5 -> "Master";
            default -> "Unknown (Level " + level + ")";
        };
    }

    /**
     * Get reputation level name
     */
    private String getReputationLevel(float reputation) {
        if (reputation >= 0.75f)
            return "Trusted";
        if (reputation >= 0.25f)
            return "Friendly";
        if (reputation >= -0.25f)
            return "Neutral";
        if (reputation >= -0.75f)
            return "Suspicious";
        return "Hostile";
    }
}

