package com.github.syren_dev_tech.vwam.trading;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event handler for trading restrictions.
 * Prevents players from trading with villagers outside of work hours.
 * 
 * Work hours: 8:00 AM - 2:00 PM (in-game time)
 * 
 * This handler should be called whenever a trade is about to occur and will either:
 * - Allow the trade if the villager is working
 * - Block the trade and notify the player if the villager is not working
 */
public class TradeRestrictionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("TradeRestrictionHandler");
    
    /**
     * Check if a trade should be allowed between a player and villager
     * 
     * @param player The player attempting to trade
     * @param villager The villager being traded with
     * @return true if trade is allowed, false otherwise
     */
    public static boolean canPlayerTrade(Player player, Villager villager) {
        boolean isWorking = TradingTimeRestriction.isVillagerWorking(villager, villager.level());
        
        if (!isWorking) {
            // Notify player why they can't trade
            String message = TradingTimeRestriction.getWorkStatusMessage(villager, villager.level());
            player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(message),
                true // Show as action bar message
            );
            
            TradingTimeRestriction.logTradingAttempt(villager, villager.level(), false);
        } else {
            TradingTimeRestriction.logTradingAttempt(villager, villager.level(), true);
        }
        
        return isWorking;
    }
    
    /**
     * Get a detailed message about when this villager works
     */
    public static String getWorkScheduleMessage(Villager villager) {
        return String.format(
            "%s works from 8:00 AM to 2:00 PM. Visit during those hours to trade!",
            TradingTimeRestriction.getProfessionName(villager)
        );
    }
    
    /**
     * Check if trade would be allowed at a specific time (for testing/planning)
     */
    public static boolean wouldTradeBeAllowed(long dayTime) {
        return TradingTimeRestriction.isWithinWorkingHours(dayTime);
    }
}
