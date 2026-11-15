package com.github.syren_dev_tech.vwam.data;

import net.minecraft.resources.ResourceLocation;
import com.google.gson.JsonObject;

/**
 * Represents a village configuration for managing stockpiles, reputation, and economics.
 */
public class VillageConfiguration {
    private final ResourceLocation villageId;
    private final String displayName;

    // Stockpile configuration
    private final int baseStockAmount;           // Default amount per item type
    private final float demandThreshold;         // Below this %, prices increase
    private final float surplusThreshold;        // Above this %, prices decrease
    private final float demandPriceMultiplier;   // How much price increases
    private final float surplusPriceMultiplier;  // How much price decreases

    // Reputation configuration
    private final float initialReputation;       // Starting rep for new players
    private final float maxReputation;           // Maximum possible reputation
    private final float minReputation;           // Minimum possible reputation
    private final float tradeReputationGain;     // Reputation gained per trade
    private final float hostileThreshold;        // Below this, villagers flee
    private final float friendlyThreshold;       // Above this, trade discounts apply
    private final float tradeDiscountPerStep;    // Discount per 0.1 reputation above friendly

    // Production configuration
    private final float heroBuffMultiplier;      // Production boost from "hero of village"
    private final int heroBuffDurationHours;     // How long hero buff lasts

    public VillageConfiguration(
            ResourceLocation villageId,
            String displayName,
            int baseStockAmount,
            float demandThreshold,
            float surplusThreshold,
            float demandPriceMultiplier,
            float surplusPriceMultiplier,
            float initialReputation,
            float maxReputation,
            float minReputation,
            float tradeReputationGain,
            float hostileThreshold,
            float friendlyThreshold,
            float tradeDiscountPerStep,
            float heroBuffMultiplier,
            int heroBuffDurationHours
    ) {
        this.villageId = villageId;
        this.displayName = displayName;
        this.baseStockAmount = baseStockAmount;
        this.demandThreshold = demandThreshold;
        this.surplusThreshold = surplusThreshold;
        this.demandPriceMultiplier = demandPriceMultiplier;
        this.surplusPriceMultiplier = surplusPriceMultiplier;
        this.initialReputation = initialReputation;
        this.maxReputation = maxReputation;
        this.minReputation = minReputation;
        this.tradeReputationGain = tradeReputationGain;
        this.hostileThreshold = hostileThreshold;
        this.friendlyThreshold = friendlyThreshold;
        this.tradeDiscountPerStep = tradeDiscountPerStep;
        this.heroBuffMultiplier = heroBuffMultiplier;
        this.heroBuffDurationHours = heroBuffDurationHours;
    }

    public static VillageConfiguration fromJson(ResourceLocation villageId, JsonObject json) {
        String displayName = json.has("display_name") ? json.get("display_name").getAsString() : villageId.getPath();

        // Stockpile config
        int baseStock = json.has("base_stock_amount") ? json.get("base_stock_amount").getAsInt() : 64;
        float demandThresh = json.has("demand_threshold") ? json.get("demand_threshold").getAsFloat() : 0.25f;
        float surplusThresh = json.has("surplus_threshold") ? json.get("surplus_threshold").getAsFloat() : 0.75f;
        float demandMult = json.has("demand_price_multiplier") ? json.get("demand_price_multiplier").getAsFloat() : 1.5f;
        float surplusMult = json.has("surplus_price_multiplier") ? json.get("surplus_price_multiplier").getAsFloat() : 0.5f;

        // Reputation config
        float initRep = json.has("initial_reputation") ? json.get("initial_reputation").getAsFloat() : 0.0f;
        float maxRep = json.has("max_reputation") ? json.get("max_reputation").getAsFloat() : 1.0f;
        float minRep = json.has("min_reputation") ? json.get("min_reputation").getAsFloat() : -1.0f;
        float tradeRepGain = json.has("trade_reputation_gain") ? json.get("trade_reputation_gain").getAsFloat() : 0.05f;
        float hostileThresh = json.has("hostile_threshold") ? json.get("hostile_threshold").getAsFloat() : -0.5f;
        float friendlyThresh = json.has("friendly_threshold") ? json.get("friendly_threshold").getAsFloat() : 0.5f;
        float discountPerStep = json.has("trade_discount_per_step") ? json.get("trade_discount_per_step").getAsFloat() : 0.1f;

        // Production config
        float heroMult = json.has("hero_buff_multiplier") ? json.get("hero_buff_multiplier").getAsFloat() : 2.0f;
        int heroDuration = json.has("hero_buff_duration_hours") ? json.get("hero_buff_duration_hours").getAsInt() : 24;

        return new VillageConfiguration(
                villageId,
                displayName,
                baseStock,
                demandThresh,
                surplusThresh,
                demandMult,
                surplusMult,
                initRep,
                maxRep,
                minRep,
                tradeRepGain,
                hostileThresh,
                friendlyThresh,
                discountPerStep,
                heroMult,
                heroDuration
        );
    }

    // Getters
    public ResourceLocation getVillageId() { return villageId; }
    public String getDisplayName() { return displayName; }
    public int getBaseStockAmount() { return baseStockAmount; }
    public float getDemandThreshold() { return demandThreshold; }
    public float getSurplusThreshold() { return surplusThreshold; }
    public float getDemandPriceMultiplier() { return demandPriceMultiplier; }
    public float getSurplusPriceMultiplier() { return surplusPriceMultiplier; }
    public float getInitialReputation() { return initialReputation; }
    public float getMaxReputation() { return maxReputation; }
    public float getMinReputation() { return minReputation; }
    public float getTradeReputationGain() { return tradeReputationGain; }
    public float getHostileThreshold() { return hostileThreshold; }
    public float getFriendlyThreshold() { return friendlyThreshold; }
    public float getTradeDiscountPerStep() { return tradeDiscountPerStep; }
    public float getHeroBuffMultiplier() { return heroBuffMultiplier; }
    public int getHeroBuffDurationHours() { return heroBuffDurationHours; }
}
