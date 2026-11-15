package com.github.syren_dev_tech.vwam.client;

/**
 * Singleton accessor for the SkinProvider.
 */
public class SkinProviderManager {
    private static final SkinProvider INSTANCE = new SkinProvider();

    private SkinProviderManager() {
    }

    public static SkinProvider getInstance() {
        return INSTANCE;
    }
}
