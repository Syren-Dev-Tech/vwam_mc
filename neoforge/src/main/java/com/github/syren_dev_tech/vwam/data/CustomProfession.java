package com.github.syren_dev_tech.vwam.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerProfession;

/**
 * Represents a custom villager profession defined via data pack.
 */
public class CustomProfession {
    private final ResourceLocation id;
    private final ResourceLocation workstationBlock;
    private final String displayName;
    private final int level;
    private final boolean enabled;

    public CustomProfession(ResourceLocation id, ResourceLocation workstationBlock, String displayName, int level, boolean enabled) {
        this.id = id;
        this.workstationBlock = workstationBlock;
        this.displayName = displayName;
        this.level = level;
        this.enabled = enabled;
    }

    /**
     * Deserialize from JSON object
     */
    public static CustomProfession fromJson(ResourceLocation id, JsonObject json) {
        ResourceLocation workstationBlock = ResourceLocation.parse(json.get("workstation_block").getAsString());
        String displayName = json.has("display_name") ? json.get("display_name").getAsString() : id.getPath();
        int level = json.has("level") ? json.get("level").getAsInt() : 1;
        boolean enabled = json.has("enabled") ? json.get("enabled").getAsBoolean() : true;

        return new CustomProfession(id, workstationBlock, displayName, level, enabled);
    }

    public ResourceLocation getId() {
        return id;
    }

    public ResourceLocation getWorkstationBlock() {
        return workstationBlock;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
