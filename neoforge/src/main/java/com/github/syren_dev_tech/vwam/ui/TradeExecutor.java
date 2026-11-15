package com.github.syren_dev_tech.vwam.ui;

import com.github.syren_dev_tech.slml.helpers.InventoryHelper;
import com.github.syren_dev_tech.vwam.data.ProfessionTrade;
import com.github.syren_dev_tech.vwam.data.VillageConfiguration;
import com.github.syren_dev_tech.vwam.data.RegistryHandler;
import com.github.syren_dev_tech.vwam.data.ItemRegistry;
import com.github.syren_dev_tech.vwam.data.ItemDefinition;
import com.github.syren_dev_tech.vwam.village.VillageManager;
import com.github.syren_dev_tech.vwam.village.VillageReputation;
import com.github.syren_dev_tech.vwam.village.VillageStockpile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes trades between players and villagers. Handles price calculations, inventory management, and stockpile updates.
 * 
 * Now uses ItemRegistry for centralized item pricing.
 */
public class TradeExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsTrading");

    /**
     * Execute a buy trade (player buys from villager with emeralds)
     * Villager sells 1 emerald's worth of items to player
     *
     * @param player The player making the purchase
     * @param villager The villager selling
     * @param trade The trade to execute (sell trade)
     * @param village The village manager
     * @param villageConfig The village configuration
     * @param villageId The village ID
     * @return Error message if failed, null if successful
     */
    public static String executeBuyTrade(Player player, Villager villager, ProfessionTrade.Trade trade, VillageManager village, VillageConfiguration villageConfig, ResourceLocation villageId) {
        // Get village
        VillageManager.Village vil = village.getVillage(villageId).orElse(null);
        if (vil == null) {
            return "Village not found";
        }

        VillageStockpile stockpile = vil.getStockpile();
        VillageReputation reputation = vil.getReputation();

        // Check if this trade is available for buying
        if (!trade.canBuyFromVillager()) {
            return "§cThis villager doesn't sell this item";
        }

        // Check if villager is high enough level for this trade
        int villagerLevel = villager.getVillagerData().getLevel();
        if (villagerLevel < trade.level) {
            return String.format("§cThis villager needs to reach level %d (currently %d)", trade.level, villagerLevel);
        }

        // Check reputation requirement for buying from villager
        if (player.getUUID() != null) {
            float playerRep = reputation.getReputation(player.getUUID());
            if (playerRep < trade.getBuyReputation()) {
                return "§cInsufficient reputation for this trade";
            }
        }

        // Get item definition from registry
        ItemRegistry itemRegistry = RegistryHandler.getInstance().getItemRegistry();
        ItemDefinition itemDef = itemRegistry.getItem(trade.item)
                .orElse(null);

        if (itemDef == null) {
            LOGGER.warn("Item {} not found in registry for trade with villager {}", trade.item, villager.getId());
            return "§cTrade item not configured";
        }

        // Base quantity = items that equal 1 emerald
        int baseQuantity = itemDef.getQuantityPerEmerald();
        int baseEmeralds = 1;  // Standard unit: 1 emerald

        // Calculate price multipliers
        float supplyDemandMult = stockpile.getPriceMultiplier(trade.item, 
                villageConfig.getDemandThreshold(), 
                villageConfig.getSurplusThreshold(), 
                villageConfig.getDemandPriceMultiplier(), 
                villageConfig.getSurplusPriceMultiplier());

        float repMult = 1.0f;
        if (player.getUUID() != null) {
            repMult = reputation.getTradeDiscountMultiplier(player.getUUID(), villageConfig.getTradeDiscountPerStep());
        }

        // Adjusted quantity = base × supply_demand_mult / reputation_mult
        // (supply demand makes items more expensive if rare, reputation makes cheaper if liked)
        float adjustedQuantity = baseQuantity * supplyDemandMult / repMult;
        int quantityInt = Math.round(adjustedQuantity);
        int emeraldsNeeded = baseEmeralds;

        // Check if player has emeralds
        int playerEmeralds = InventoryHelper.countItemInInventory(player, "minecraft:emerald");
        if (playerEmeralds < emeraldsNeeded) {
            return String.format("§cNeed %d emeralds, only have %d", emeraldsNeeded, playerEmeralds);
        }

        // Remove emeralds from player
        if (!InventoryHelper.removeItemFromInventory(player, "minecraft:emerald", emeraldsNeeded)) {
            return "§cFailed to remove emeralds from inventory";
        }

        // Check if stockpile has items
        if (stockpile.getQuantity(trade.item) < quantityInt) {
            // Refund emeralds
            InventoryHelper.addItemToInventory(player, new ItemStack(Items.EMERALD, emeraldsNeeded));
            return String.format("§cVillager doesn't have %d %s in stock", quantityInt, trade.item);
        }

        // Add items to player inventory
        ItemStack itemsToGive = new ItemStack(getItemFromId(trade.item), quantityInt);
        if (!InventoryHelper.addItemToInventory(player, itemsToGive)) {
            // Return emeralds if failed
            InventoryHelper.addItemToInventory(player, new ItemStack(Items.EMERALD, emeraldsNeeded));
            return "§cInventory full!";
        }

        // Remove from village stockpile
        stockpile.removeItems(trade.item, quantityInt);

        // Add reputation
        if (player.getUUID() != null) {
            reputation.addReputation(player.getUUID(), villageConfig.getTradeReputationGain());
        }

        LOGGER.info("Buy trade executed: {} purchased {} x{} for {} emerald(s) from {}", 
                player.getName().getString(), trade.item, quantityInt, emeraldsNeeded, villager.getId());

        return null; // Success
    }

    /**
     * Execute a sell trade (player sells to villager)
     * Player gives items, villager gives 1 emerald per standard unit
     *
     * @param player The player making the sale
     * @param villager The villager buying
     * @param trade The trade to execute (buy trade)
     * @param village The village manager
     * @param villageConfig The village configuration
     * @param villageId The village ID
     * @return Error message if failed, null if successful
     */
    public static String executeSellTrade(Player player, Villager villager, ProfessionTrade.Trade trade, VillageManager village, VillageConfiguration villageConfig, ResourceLocation villageId) {
        // Get village
        VillageManager.Village vil = village.getVillage(villageId).orElse(null);
        if (vil == null) {
            return "Village not found";
        }

        VillageStockpile stockpile = vil.getStockpile();
        VillageReputation reputation = vil.getReputation();

        // Check if this trade is available for selling
        if (!trade.canSellToVillager()) {
            return "§cThis villager doesn't buy this item";
        }

        // Check if villager is high enough level for this trade
        int villagerLevel = villager.getVillagerData().getLevel();
        if (villagerLevel < trade.level) {
            return String.format("§cThis villager needs to reach level %d (currently %d)", trade.level, villagerLevel);
        }

        // Check reputation requirement for selling to villager
        if (player.getUUID() != null) {
            float playerRep = reputation.getReputation(player.getUUID());
            if (playerRep < trade.getSellReputation()) {
                return "§cInsufficient reputation for this trade";
            }
        }

        // Get item definition from registry
        ItemRegistry itemRegistry = RegistryHandler.getInstance().getItemRegistry();
        ItemDefinition itemDef = itemRegistry.getItem(trade.item)
                .orElse(null);

        if (itemDef == null) {
            LOGGER.warn("Item {} not found in registry for trade with villager {}", trade.item, villager.getId());
            return "§cTrade item not configured";
        }

        // Base quantity = items that equal 1 emerald
        int baseQuantity = itemDef.getQuantityPerEmerald();
        int baseEmeralds = 1;  // Standard unit: 1 emerald

        // Calculate price multipliers
        float supplyDemandMult = stockpile.getPriceMultiplier(trade.item, 
                villageConfig.getDemandThreshold(), 
                villageConfig.getSurplusThreshold(), 
                villageConfig.getDemandPriceMultiplier(), 
                villageConfig.getSurplusPriceMultiplier());

        float repMult = 1.0f;
        if (player.getUUID() != null) {
            repMult = reputation.getTradeDiscountMultiplier(player.getUUID(), villageConfig.getTradeDiscountPerStep());
        }

        // Adjusted quantity = base / supply_demand_mult * reputation_mult
        // (if rare, villager wants less; if player is liked, villager generous)
        float adjustedQuantity = baseQuantity / supplyDemandMult * repMult;
        int quantityInt = Math.round(adjustedQuantity);
        int emeraldsOffered = baseEmeralds;

        // Check if player has items
        int playerItems = InventoryHelper.countItemInInventory(player, trade.item);
        if (playerItems < quantityInt) {
            return String.format("§cNeed %d %s, only have %d", quantityInt, trade.item, playerItems);
        }

        // Remove items from player
        if (!InventoryHelper.removeItemFromInventory(player, trade.item, quantityInt)) {
            return "§cFailed to remove items from inventory";
        }

        // Create emerald stack to give player
        ItemStack emeraldsToGive = new ItemStack(Items.EMERALD, emeraldsOffered);

        // Add emeralds to player inventory
        if (!InventoryHelper.addItemToInventory(player, emeraldsToGive)) {
            // Return items if failed
            ItemStack itemStack = new ItemStack(getItemFromId(trade.item), quantityInt);
            InventoryHelper.addItemToInventory(player, itemStack);
            return "§cInventory full!";
        }

        // Add to village stockpile
        stockpile.addItems(trade.item, quantityInt);

        // Add reputation
        if (player.getUUID() != null) {
            reputation.addReputation(player.getUUID(), villageConfig.getTradeReputationGain());
        }

        LOGGER.info("Sell trade executed: {} sold {} x{} for {} emerald(s) to {}", 
                player.getName().getString(), trade.item, quantityInt, emeraldsOffered, villager.getId());

        return null; // Success
    }

    /**
     * Get the display text for a trade (showing adjusted price based on supply/demand/reputation)
     *
     * @param trade The base trade
     * @param player The player making the trade
     * @param supplyDemandMult The supply/demand multiplier
     * @param repMult The reputation multiplier
     * @return Formatted trade display string
     */
    public static String getTradeDisplayText(ProfessionTrade.Trade trade, Player player, float supplyDemandMult, float repMult) {
        // Get item definition from registry
        ItemRegistry itemRegistry = RegistryHandler.getInstance().getItemRegistry();
        ItemDefinition itemDef = itemRegistry.getItem(trade.item)
                .orElse(null);

        if (itemDef == null) {
            return "Unknown item";
        }

        int baseQuantity = itemDef.getQuantityPerEmerald();

        // Calculate adjusted quantities
        float itemsNeeded = baseQuantity * supplyDemandMult / repMult;
        int itemsNeededInt = Math.round(itemsNeeded);

        if (itemsNeeded != baseQuantity) {
            // Price is different from base
            return String.format("§e%d x %s §7← 1 emerald §8(base: %d)", itemsNeededInt, trade.item, baseQuantity);
        } else {
            return String.format("%d x %s ← 1 emerald", itemsNeededInt, trade.item);
        }
    }

    /**
     * Convert item ID string to actual item
     */
    private static net.minecraft.world.item.Item getItemFromId(String itemId) {
        // This is a simplified version - in production you'd use Minecraft's item registry
        // For now, return a placeholder
        return Items.WHEAT;  // TODO: Implement proper item lookup
    }
}
