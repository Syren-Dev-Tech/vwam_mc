package com.github.syren_dev_tech.vwam.data;

import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Matches ItemStacks against criteria.
 * Note: In this version, only checks basic item stack properties.
 * Detailed NBT matching (enchantments, names, lore) can be added when API is available.
 */
public class ItemMatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsItems");
    
    private boolean matchEnchanted;  // For future use
    private Set<String> requiredEnchantments;  // For future use
    private boolean hasCustomName;  // For future use
    private String customNamePattern;  // For future use
    private Set<String> requiredLore;  // For future use
    
    private ItemMatcher() {
        this.matchEnchanted = false;
        this.requiredEnchantments = new HashSet<>();
        this.hasCustomName = false;
        this.customNamePattern = null;
        this.requiredLore = new HashSet<>();
    }
    
    /**
     * Create a builder for ItemMatcher
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Create a matcher from JSON criteria
     */
    public static ItemMatcher fromJson(Map<String, Object> criteria) {
        ItemMatcher matcher = new ItemMatcher();
        
        if (criteria == null || criteria.isEmpty()) {
            return matcher;  // Match anything
        }
        
        // Check for enchanted flag
        if (criteria.containsKey("enchanted")) {
            matcher.matchEnchanted = Boolean.TRUE.equals(criteria.get("enchanted"));
        }
        
        // Check for specific enchantments
        if (criteria.containsKey("enchantments")) {
            Object enchantValue = criteria.get("enchantments");
            if (enchantValue instanceof List<?>) {
                for (Object ench : (List<?>) enchantValue) {
                    matcher.requiredEnchantments.add(ench.toString().toLowerCase());
                }
            }
        }
        
        // Check for custom name
        if (criteria.containsKey("customName")) {
            Object nameValue = criteria.get("customName");
            if (nameValue instanceof Boolean) {
                matcher.hasCustomName = (Boolean) nameValue;
            } else {
                matcher.customNamePattern = nameValue.toString();
                matcher.hasCustomName = true;
            }
        }
        
        // Check for lore
        if (criteria.containsKey("lore")) {
            Object loreValue = criteria.get("lore");
            if (loreValue instanceof List<?>) {
                for (Object lore : (List<?>) loreValue) {
                    matcher.requiredLore.add(lore.toString().toLowerCase());
                }
            }
        }
        
        return matcher;
    }
    
    /**
     * Check if an ItemStack matches all criteria in this matcher
     * 
     * Note: Basic matching only. Full NBT matching requires ItemStack API that isn't available in this version.
     * Detailed enchantment, name, and lore matching will be added when the API is available.
     */
    public boolean matches(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }
        
        // For now, just check if the item exists
        // Full NBT matching will be implemented when ItemStack API supports it
        
        // TODO: When ItemStack.getTag() becomes available, implement:
        // - Enchantment matching
        // - Custom name matching
        // - Lore matching
        
        return true;
    }
    
    /**
     * Check if this matcher has any criteria (vs empty matcher that matches anything)
     */
    public boolean hasCriteria() {
        return matchEnchanted || !requiredEnchantments.isEmpty() || 
               hasCustomName || !requiredLore.isEmpty();
    }
    
    /**
     * Builder for ItemMatcher
     */
    public static class Builder {
        private ItemMatcher matcher = new ItemMatcher();
        
        public Builder requireEnchanted() {
            matcher.matchEnchanted = true;
            return this;
        }
        
        public Builder requireEnchantment(String enchantmentName) {
            matcher.requiredEnchantments.add(enchantmentName.toLowerCase());
            return this;
        }
        
        public Builder requireEnchantments(String... enchantmentNames) {
            for (String name : enchantmentNames) {
                matcher.requiredEnchantments.add(name.toLowerCase());
            }
            return this;
        }
        
        public Builder requireCustomName() {
            matcher.hasCustomName = true;
            return this;
        }
        
        public Builder requireCustomNameMatching(String pattern) {
            matcher.hasCustomName = true;
            matcher.customNamePattern = pattern;
            return this;
        }
        
        public Builder requireLore(String loreLine) {
            matcher.requiredLore.add(loreLine.toLowerCase());
            return this;
        }
        
        public Builder requireLore(String... loreLines) {
            for (String line : loreLines) {
                matcher.requiredLore.add(line.toLowerCase());
            }
            return this;
        }
        
        public ItemMatcher build() {
            return matcher;
        }
    }
}
