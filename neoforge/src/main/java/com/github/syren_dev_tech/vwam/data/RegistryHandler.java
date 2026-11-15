package com.github.syren_dev_tech.vwam.data;

import com.github.syren_dev_tech.vwam.village.VillageManager;

/**
 * Singleton accessor for registries and managers.
 */
public class RegistryHandler {
    private static final CustomRegistryManager CUSTOM_REGISTRY = new CustomRegistryManager();
    private static VillageManager villageManager = null;

    private RegistryHandler() {
    }

    public static CustomRegistryManager getInstance() {
        return CUSTOM_REGISTRY;
    }

    public static VillageManager getVillageManager() {
        if (villageManager == null) {
            villageManager = new VillageManager();
        }
        return villageManager;
    }

    public static void setVillageManager(VillageManager manager) {
        villageManager = manager;
    }
}
