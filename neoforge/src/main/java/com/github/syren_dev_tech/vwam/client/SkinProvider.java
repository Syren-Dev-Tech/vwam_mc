package com.github.syren_dev_tech.vwam.client;

import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages custom villager skins for professions via resource packs.
 * Skin textures should be located in resource packs at:
 * assets/villager_workshops/textures/entity/villager/profession/{profession_name}.png
 */
public class SkinProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsSkins");
    private static final String SKIN_PATH_PREFIX = "textures/entity/villager/profession/";

    private final Map<ResourceLocation, ResourceLocation> professionSkins = new HashMap<>();

    /**
     * Register a custom skin texture for a profession
     * @param professionId The profession ResourceLocation
     * @param skinTexture The texture ResourceLocation
     */
    public void registerSkin(ResourceLocation professionId, ResourceLocation skinTexture) {
        professionSkins.put(professionId, skinTexture);
        LOGGER.debug("Registered skin for profession {}: {}", professionId, skinTexture);
    }

    /**
     * Get the skin texture for a profession
     * @param professionId The profession ResourceLocation
     * @return The skin texture ResourceLocation, or empty if not found
     */
    public Optional<ResourceLocation> getSkin(ResourceLocation professionId) {
        return Optional.ofNullable(professionSkins.get(professionId));
    }

    /**
     * Generate a default skin texture path for a profession
     * @param professionId The profession ResourceLocation
     * @return A ResourceLocation pointing to the default skin path
     */
    public static ResourceLocation getDefaultSkinPath(ResourceLocation professionId) {
        return ResourceLocation.fromNamespaceAndPath(
                "villager_workshops",
                SKIN_PATH_PREFIX + professionId.getNamespace() + "/" + professionId.getPath() + ".png"
        );
    }

    /**
     * Generate a skin texture path for a specific profession and variant
     * @param professionId The profession ResourceLocation
     * @param variant The variant (e.g., "biome_specific", "level_1", etc.)
     * @return A ResourceLocation pointing to the variant skin path
     */
    public static ResourceLocation getVariantSkinPath(ResourceLocation professionId, String variant) {
        return ResourceLocation.fromNamespaceAndPath(
                "villager_workshops",
                SKIN_PATH_PREFIX + professionId.getNamespace() + "/" + professionId.getPath() + "_" + variant + ".png"
        );
    }

    public void clear() {
        professionSkins.clear();
    }
}
