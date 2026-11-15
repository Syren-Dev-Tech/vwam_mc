package com.github.syren_dev_tech.vwam.data;

import net.minecraft.resources.ResourceLocation;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.*;

/**
 * Represents a trade configuration for a specific profession.
 * Defines what items a villager can buy/sell and at what rates.
 * 
 * Uses optional reputation fields to determine trade direction:
 * - reputation_sell defined → player can SELL to villager
 * - reputation_buy defined → player can BUY from villager
 */
public class ProfessionTrade {
    private final ResourceLocation professionId;
    private final int level;

    private final Trade[] trades;  // All trades (buy and sell combined)
    private final ServiceOption[] services;

    public ProfessionTrade(
            ResourceLocation professionId,
            int level,
            Trade[] trades,
            ServiceOption[] services
    ) {
        this.professionId = professionId;
        this.level = level;
        this.trades = trades;
        this.services = services;
    }

    public static ProfessionTrade fromJson(ResourceLocation professionId, int level, JsonObject json) {
        Trade[] trades;
        
        // Support both new format (optional fields) and old format (buy_trades/sell_trades)
        if (json.has("trades")) {
            trades = parseTrades(json.getAsJsonArray("trades"));
        } else {
            // Convert old format to new format
            trades = parseTradesFromOldFormat(json);
        }
        
        ServiceOption[] services = parseServiceOptions(json.getAsJsonArray("services"));

        return new ProfessionTrade(professionId, level, trades, services);
    }

    private static Trade[] parseTrades(JsonArray array) {
        if (array == null) return new Trade[0];

        Trade[] tradeArray = new Trade[array.size()];
        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.get(i).getAsJsonObject();
            String item = obj.get("item").getAsString();
            Double reputationBuy = obj.has("reputation_buy") ? obj.get("reputation_buy").getAsDouble() : null;
            Double reputationSell = obj.has("reputation_sell") ? obj.get("reputation_sell").getAsDouble() : null;
            int tradeLevel = obj.has("level") ? obj.get("level").getAsInt() : 1;
            
            tradeArray[i] = new Trade(item, reputationBuy, reputationSell, tradeLevel);
        }
        return tradeArray;
    }

    /**
     * Convert old format (buy_trades/sell_trades) to new format (trades array with optional fields)
     */
    private static Trade[] parseTradesFromOldFormat(JsonObject json) {
        List<Trade> tradeList = new ArrayList<>();
        
        // Parse buy_trades (villager buys from player - player sells)
        if (json.has("buy_trades")) {
            JsonArray buyArray = json.getAsJsonArray("buy_trades");
            for (int i = 0; i < buyArray.size(); i++) {
                JsonObject obj = buyArray.get(i).getAsJsonObject();
                String item = obj.get("item").getAsString();
                double minReputation = obj.has("min_reputation") ? obj.get("min_reputation").getAsDouble() : 0.0;
                
                // reputation_sell = player needs this to sell to villager
                Trade trade = new Trade(item, null, minReputation, 1);
                tradeList.add(trade);
            }
        }
        
        // Parse sell_trades (villager sells to player - player buys)
        if (json.has("sell_trades")) {
            JsonArray sellArray = json.getAsJsonArray("sell_trades");
            for (int i = 0; i < sellArray.size(); i++) {
                JsonObject obj = sellArray.get(i).getAsJsonObject();
                String item = obj.get("item").getAsString();
                double minReputation = obj.has("min_reputation") ? obj.get("min_reputation").getAsDouble() : 0.0;
                
                // reputation_buy = player needs this to buy from villager
                Trade trade = new Trade(item, minReputation, null, 1);
                tradeList.add(trade);
            }
        }
        
        return tradeList.toArray(new Trade[0]);
    }

    private static ServiceOption[] parseServiceOptions(JsonArray array) {
        if (array == null) return new ServiceOption[0];

        ServiceOption[] options = new ServiceOption[array.size()];
        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.get(i).getAsJsonObject();
            options[i] = new ServiceOption(
                    obj.get("service_type").getAsString(),
                    obj.get("cooldown_hours").getAsInt(),
                    obj.has("min_reputation") ? obj.get("min_reputation").getAsDouble() : 0.0
            );
        }
        return options;
    }

    // Getters
    public ResourceLocation getProfessionId() { return professionId; }
    public int getLevel() { return level; }
    public Trade[] getTrades() { return trades; }
    
    /**
     * Get all trades where player can BUY from villager (reputation_buy is defined)
     */
    public Trade[] getBuyTrades() {
        return Arrays.stream(trades)
                .filter(Trade::canBuyFromVillager)
                .toArray(Trade[]::new);
    }
    
    /**
     * Get all trades where player can SELL to villager (reputation_sell is defined)
     */
    public Trade[] getSellTrades() {
        return Arrays.stream(trades)
                .filter(Trade::canSellToVillager)
                .toArray(Trade[]::new);
    }
    
    public ServiceOption[] getServices() { return services; }

    /**
     * Represents a single trade. The presence of reputation_buy or reputation_sell
     * determines whether this trade is available for buying/selling.
     * 
     * reputation_sell defined (non-null) = player can SELL this item to villager
     * reputation_buy defined (non-null) = player can BUY this item from villager
     */
    public static class Trade {
        public final String item;                   // minecraft item ID
        public final Double reputationBuy;          // reputation needed to BUY from villager (null if unavailable)
        public final Double reputationSell;         // reputation needed to SELL to villager (null if unavailable)
        public final int level;                     // villager profession level

        public Trade(String item, Double reputationBuy, Double reputationSell, int level) {
            this.item = item;
            this.reputationBuy = reputationBuy;
            this.reputationSell = reputationSell;
            this.level = level;
        }
        
        /**
         * Check if player can BUY this item from villager
         */
        public boolean canBuyFromVillager() {
            return reputationBuy != null;
        }
        
        /**
         * Check if player can SELL this item to villager
         */
        public boolean canSellToVillager() {
            return reputationSell != null;
        }
        
        /**
         * Get reputation requirement for buying (returns 0 if cannot buy)
         */
        public double getBuyReputation() {
            return reputationBuy != null ? reputationBuy : 0.0;
        }
        
        /**
         * Get reputation requirement for selling (returns 0 if cannot sell)
         */
        public double getSellReputation() {
            return reputationSell != null ? reputationSell : 0.0;
        }
    }

    /**
     * Represents a special service (repair, healing, etc.)
     */
    public static class ServiceOption {
        public final String serviceType;
        public final int cooldownHours;
        public final double minReputation;

        public ServiceOption(String serviceType, int cooldownHours, double minReputation) {
            this.serviceType = serviceType;
            this.cooldownHours = cooldownHours;
            this.minReputation = minReputation;
        }
    }
}
