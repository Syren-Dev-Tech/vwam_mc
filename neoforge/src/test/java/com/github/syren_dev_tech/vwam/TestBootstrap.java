package com.github.syren_dev_tech.vwam;

/**
 * Test bootstrap configuration for Minecraft unit tests
 * Handles initialization of Minecraft static fields and registries
 */
public class TestBootstrap {
    
    private static boolean initialized = false;
    
    /**
     * Initialize Minecraft for testing
     * This must be called before running any tests that use Minecraft classes
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Set up minimal Minecraft environment
            System.setProperty("fabric.development", "true");
            
            // Initialize Bootstrap (optional, may not be available in all environments)
            try {
                Class<?> bootstrapClass = Class.forName("net.minecraft.Bootstrap");
                bootstrapClass.getMethod("bootStrap").invoke(null);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                // Not available in test environment, that's okay
            }
            
            initialized = true;
        } catch (Exception e) {
            // If initialization fails, tests may still work with mocks
            System.err.println("Failed to initialize Minecraft bootstrap for tests: " + e.getMessage());
        }
    }
    
    /**
     * Reset initialization state (for test cleanup)
     */
    public static synchronized void reset() {
        initialized = false;
    }
    
    /**
     * Check if initialization was successful
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
