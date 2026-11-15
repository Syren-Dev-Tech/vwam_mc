package com.github.syren_dev_tech.vwam.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a custom workstation defined via data pack.
 */
public class CustomWorkstation {
    private final ResourceLocation id;
    private final ResourceLocation blockId;
    private final String displayName;
    private final boolean isPointOfInterest;

    public CustomWorkstation(ResourceLocation id, ResourceLocation blockId, String displayName, boolean isPointOfInterest) {
        this.id = id;
        this.blockId = blockId;
        this.displayName = displayName;
        this.isPointOfInterest = isPointOfInterest;
    }

    /**
     * Deserialize from JSON object
     */
    public static CustomWorkstation fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation blockId = ResourceLocation.parse(json.get("block").getAsString());
        String displayName = json.has("display_name") ? json.get("display_name").getAsString() : id.getPath();
        boolean isPointOfInterest = json.has("is_poi") ? json.get("is_poi").getAsBoolean() : true;

        return new CustomWorkstation(id, blockId, displayName, isPointOfInterest);
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getBlockId() {
        return blockId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isPointOfInterest() {
        return isPointOfInterest;
    }
}
