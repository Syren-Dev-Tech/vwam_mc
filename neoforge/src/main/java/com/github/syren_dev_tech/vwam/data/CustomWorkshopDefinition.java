package com.github.syren_dev_tech.vwam.data;

import net.minecraft.resources.ResourceLocation;
import com.google.gson.JsonObject;

/**
 * Represents a workshop definition that specifies requirements for villager workstations.
 * Includes validation rules, quality metrics, and production rates.
 */
public class CustomWorkshopDefinition {
    private final ResourceLocation id;
    private final ResourceLocation primaryBlock;
    private final String displayName;

    // Validation requirements
    private final int supportWorkstationCount; // Required # of support workstations
    private final int storageRequirement;      // Required # of storage blocks
    private final int maxDistanceToSupport;    // Max distance to support workstations
    private final int maxDistanceToStorage;    // Max distance to storage
    private final int minLightLevel;           // Minimum light level required
    private final int clearRadiusRequired;     // Radius of clear space needed
    private final int maxNearbyWorkstations;   // Max identical workstations nearby

    // Quality/production configuration
    private final boolean trackQuality;        // Whether to track workshop quality
    private final ProductionConfig[] qualityTiers; // Quality tier definitions

    // Beauty/trade discount configuration
    private final boolean trackBeauty;         // Whether to track beauty score
    private final DecorationConfig[] decorations; // Decoration types and their values

    public CustomWorkshopDefinition(
            ResourceLocation id,
            ResourceLocation primaryBlock,
            String displayName,
            int supportWorkstationCount,
            int storageRequirement,
            int maxDistanceToSupport,
            int maxDistanceToStorage,
            int minLightLevel,
            int clearRadiusRequired,
            int maxNearbyWorkstations,
            boolean trackQuality,
            ProductionConfig[] qualityTiers,
            boolean trackBeauty,
            DecorationConfig[] decorations
    ) {
        this.id = id;
        this.primaryBlock = primaryBlock;
        this.displayName = displayName;
        this.supportWorkstationCount = supportWorkstationCount;
        this.storageRequirement = storageRequirement;
        this.maxDistanceToSupport = maxDistanceToSupport;
        this.maxDistanceToStorage = maxDistanceToStorage;
        this.minLightLevel = minLightLevel;
        this.clearRadiusRequired = clearRadiusRequired;
        this.maxNearbyWorkstations = maxNearbyWorkstations;
        this.trackQuality = trackQuality;
        this.qualityTiers = qualityTiers;
        this.trackBeauty = trackBeauty;
        this.decorations = decorations;
    }

    public static CustomWorkshopDefinition fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation primaryBlock = ResourceLocation.parse(json.get("primary_block").getAsString());
        String displayName = json.has("display_name") ? json.get("display_name").getAsString() : id.getPath();

        int supportCount = json.has("support_workstation_count") ? json.get("support_workstation_count").getAsInt() : 3;
        int storageReq = json.has("storage_requirement") ? json.get("storage_requirement").getAsInt() : 1;
        int maxSupport = json.has("max_distance_to_support") ? json.get("max_distance_to_support").getAsInt() : 9;
        int maxStorage = json.has("max_distance_to_storage") ? json.get("max_distance_to_storage").getAsInt() : 9;
        int minLight = json.has("min_light_level") ? json.get("min_light_level").getAsInt() : 9;
        int clearRadius = json.has("clear_radius_required") ? json.get("clear_radius_required").getAsInt() : 1;
        int maxNearby = json.has("max_nearby_workstations") ? json.get("max_nearby_workstations").getAsInt() : 1;

        boolean trackQuality = json.has("track_quality") ? json.get("track_quality").getAsBoolean() : false;
        ProductionConfig[] qualityTiers = parseQualityTiers(json);

        boolean trackBeauty = json.has("track_beauty") ? json.get("track_beauty").getAsBoolean() : false;
        DecorationConfig[] decorations = parseDecorations(json);

        return new CustomWorkshopDefinition(
                id, primaryBlock, displayName,
                supportCount, storageReq, maxSupport, maxStorage, minLight, clearRadius, maxNearby,
                trackQuality, qualityTiers,
                trackBeauty, decorations
        );
    }

    private static ProductionConfig[] parseQualityTiers(JsonObject json) {
        if (!json.has("quality_tiers")) return new ProductionConfig[0];

        var array = json.getAsJsonArray("quality_tiers");
        ProductionConfig[] tiers = new ProductionConfig[array.size()];

        for (int i = 0; i < array.size(); i++) {
            JsonObject tier = array.get(i).getAsJsonObject();
            tiers[i] = new ProductionConfig(
                    tier.get("quality_level").getAsString(),
                    tier.get("max_avg_distance").getAsInt(),
                    tier.get("production_multiplier").getAsFloat()
            );
        }

        return tiers;
    }

    private static DecorationConfig[] parseDecorations(JsonObject json) {
        if (!json.has("decorations")) return new DecorationConfig[0];

        var array = json.getAsJsonArray("decorations");
        DecorationConfig[] decorations = new DecorationConfig[array.size()];

        for (int i = 0; i < array.size(); i++) {
            JsonObject deco = array.get(i).getAsJsonObject();
            decorations[i] = new DecorationConfig(
                    deco.get("block").getAsString(),
                    deco.get("beauty_value").getAsDouble()
            );
        }

        return decorations;
    }

    // Getters
    public ResourceLocation getId() { return id; }
    public ResourceLocation getPrimaryBlock() { return primaryBlock; }
    public String getDisplayName() { return displayName; }
    public int getSupportWorkstationCount() { return supportWorkstationCount; }
    public int getStorageRequirement() { return storageRequirement; }
    public int getMaxDistanceToSupport() { return maxDistanceToSupport; }
    public int getMaxDistanceToStorage() { return maxDistanceToStorage; }
    public int getMinLightLevel() { return minLightLevel; }
    public int getClearRadiusRequired() { return clearRadiusRequired; }
    public int getMaxNearbyWorkstations() { return maxNearbyWorkstations; }
    public boolean isTrackQuality() { return trackQuality; }
    public ProductionConfig[] getQualityTiers() { return qualityTiers; }
    public boolean isTrackBeauty() { return trackBeauty; }
    public DecorationConfig[] getDecorations() { return decorations; }

    /**
     * Production tier configuration (Cramped, Tight, Normal, Roomy, Spacious)
     */
    public static class ProductionConfig {
        public final String qualityLevel;
        public final int maxAverageDistance;
        public final float productionMultiplier;

        public ProductionConfig(String qualityLevel, int maxAverageDistance, float productionMultiplier) {
            this.qualityLevel = qualityLevel;
            this.maxAverageDistance = maxAverageDistance;
            this.productionMultiplier = productionMultiplier;
        }
    }

    /**
     * Decoration configuration for beauty scoring
     */
    public static class DecorationConfig {
        public final String block;
        public final double beautyValue;

        public DecorationConfig(String block, double beautyValue) {
            this.block = block;
            this.beautyValue = beautyValue;
        }
    }
}
