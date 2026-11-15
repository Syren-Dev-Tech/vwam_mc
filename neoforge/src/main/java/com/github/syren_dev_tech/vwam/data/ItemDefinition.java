package com.github.syren_dev_tech.vwam.data;

import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Represents an item's trading definition with support for variants.
 * An item can be:
 * 1. A simple definition: single value for all variants
 * 2. A variant definition: multiple definitions with matchers for different states
 * 
 * Example: minecraft:diamond_sword can have different values for enchanted vs non-enchanted
 */
public class ItemDefinition {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsItems");
    
    private final String itemId;
    private final List<Variant> variants;
    private final double defaultValue;
    
    /**
     * Represents a single variant of an item with optional matching criteria
     */
    public static class Variant implements Comparable<Variant> {
        public final double value;
        public final ItemMatcher matcher;
        public final int priority;
        
        public Variant(double value, ItemMatcher matcher, int priority) {
            this.value = value;
            this.matcher = matcher != null ? matcher : ItemMatcher.fromJson(null);
            this.priority = priority;
        }
        
        @Override
        public int compareTo(Variant other) {
            // Higher priority first
            return Integer.compare(other.priority, this.priority);
        }
    }
    
    /**
     * Create a simple item definition (single variant, no matching criteria)
     */
    public ItemDefinition(String itemId, double value) {
        this.itemId = itemId;
        this.defaultValue = value;
        this.variants = new ArrayList<>();
        this.variants.add(new Variant(value, ItemMatcher.fromJson(null), 0));
    }
    
    /**
     * Create an item definition with variants
     */
    public ItemDefinition(String itemId, List<Variant> variants) {
        this.itemId = itemId;
        this.variants = new ArrayList<>(variants);
        Collections.sort(this.variants);  // Sort by priority
        
        // Default value is the first variant or highest priority variant
        this.defaultValue = !this.variants.isEmpty() ? this.variants.get(0).value : 1.0;
    }
    
    /**
     * Get the value for this item, considering its current state (NBT data)
     * Matches against registered variants if ItemStack is provided
     */
    public double getValue(ItemStack itemStack) {
        if (itemStack != null && !itemStack.isEmpty()) {
            // Try to match against variants (in priority order)
            for (Variant variant : variants) {
                if (variant.matcher.matches(itemStack)) {
                    return variant.value;
                }
            }
        }
        
        // Fallback to default value
        return defaultValue;
    }
    
    /**
     * Get the value for this item ID alone (no NBT matching)
     * Used when we don't have an ItemStack
     */
    public double getValue() {
        return defaultValue;
    }
    
    /**
     * Get all variants for this item
     */
    public List<Variant> getVariants() {
        return Collections.unmodifiableList(variants);
    }
    
    /**
     * Check if this item has multiple variants (beyond the default)
     */
    public boolean hasVariants() {
        return variants.size() > 1;
    }
    
    /**
     * Get item ID
     */
    public String getItemId() {
        return itemId;
    }
    
    /**
     * Get quantity of this item that equals 1 emerald
     */
    public int getQuantityPerEmerald() {
        if (defaultValue == 0) return 1;
        return (int) Math.round(1.0 / defaultValue);
    }
    
    /**
     * Get quantity of this item for a given ItemStack that equals 1 emerald
     */
    public int getQuantityPerEmerald(ItemStack itemStack) {
        double value = getValue(itemStack);
        if (value == 0) return 1;
        return (int) Math.round(1.0 / value);
    }
    
    /**
     * Get emeralds per item
     */
    public double getEmeraldsPerItem() {
        return defaultValue;
    }
    
    /**
     * Calculate total emerald cost for a given quantity
     */
    public double getTotalCost(int quantity) {
        return quantity * defaultValue;
    }
    
    /**
     * Calculate total emerald cost for a given quantity and ItemStack
     */
    public double getTotalCost(int quantity, ItemStack itemStack) {
        return quantity * getValue(itemStack);
    }
    
    /**
     * Calculate quantity needed for a given emerald amount
     */
    public int getQuantityForEmeralds(int emeralds) {
        if (defaultValue == 0) return 0;
        return (int) Math.ceil(emeralds / defaultValue);
    }
    
    /**
     * Calculate quantity needed for a given emerald amount and ItemStack
     */
    public int getQuantityForEmeralds(int emeralds, ItemStack itemStack) {
        double value = getValue(itemStack);
        if (value == 0) return 0;
        return (int) Math.ceil(emeralds / value);
    }
}
