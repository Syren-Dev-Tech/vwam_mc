package com.github.syren_dev_tech.vwam.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Registry for central item trading definitions.
 * Each item can have a simple value or multiple variants for different states.
 * 
 * Supports both backward-compatible simple definitions and new variant-based definitions.
 */
public class ItemRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsItems");
    
    private final Map<String, ItemDefinition> items = new HashMap<>();
    
    /**
     * Load items from the central items.json file
     */
    public void loadItems() {
        LOGGER.info("Loading central item definitions...");
        
        try {
            String path = "data/vwam/items.json";
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            
            if (stream == null) {
                LOGGER.warn("Items registry not found at {}", path);
                return;
            }
            
            loadItemsFromStream(stream);
            stream.close();
            
            LOGGER.info("Successfully loaded {} items", items.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load items", e);
        }
    }
    
    private void loadItemsFromStream(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            
            JsonElement element = JsonParser.parseReader(reader);
            if (element.isJsonObject()) {
                JsonObject json = element.getAsJsonObject();
                
                if (json.has("items")) {
                    JsonObject itemsObj = json.getAsJsonObject("items");
                    
                    for (String itemId : itemsObj.keySet()) {
                        JsonElement itemElement = itemsObj.get(itemId);
                        ItemDefinition itemDef = parseItemDefinition(itemId, itemElement);
                        
                        if (itemDef != null) {
                            items.put(itemId, itemDef);
                        }
                    }
                    
                    LOGGER.info("  ✓ Loaded {} items from central registry", items.size());
                }
            }
        }
    }
    
    /**
     * Parse an item definition from JSON.
     */
    private ItemDefinition parseItemDefinition(String itemId, JsonElement element) {
        try {
            if (element.isJsonObject()) {
                // Single definition (backward compatible)
                JsonObject obj = element.getAsJsonObject();
                double value = obj.has("value") ? obj.get("value").getAsDouble() : 1.0;
                
                // Check if it has variants
                if (obj.has("variants") && obj.get("variants").isJsonArray()) {
                    return parseVariantDefinition(itemId, obj);
                }
                
                return new ItemDefinition(itemId, value);
                
            } else if (element.isJsonArray()) {
                // Array of variants
                return parseVariantArray(itemId, element.getAsJsonArray());
                
            } else {
                LOGGER.warn("Invalid item definition format for {}", itemId);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing item definition for {}", itemId, e);
            return null;
        }
    }
    
    /**
     * Parse a single item definition with a "variants" array
     */
    private ItemDefinition parseVariantDefinition(String itemId, JsonObject obj) {
        List<ItemDefinition.Variant> variants = new ArrayList<>();
        
        JsonArray variantArray = obj.getAsJsonArray("variants");
        for (int i = 0; i < variantArray.size(); i++) {
            JsonObject variantObj = variantArray.get(i).getAsJsonObject();
            variants.add(parseVariant(variantObj));
        }
        
        if (variants.isEmpty()) {
            // Fallback to simple definition
            double value = obj.has("value") ? obj.get("value").getAsDouble() : 1.0;
            return new ItemDefinition(itemId, value);
        }
        
        return new ItemDefinition(itemId, variants);
    }
    
    /**
     * Parse an array of variant definitions
     */
    private ItemDefinition parseVariantArray(String itemId, JsonArray array) {
        List<ItemDefinition.Variant> variants = new ArrayList<>();
        
        for (int i = 0; i < array.size(); i++) {
            if (array.get(i).isJsonObject()) {
                JsonObject variantObj = array.get(i).getAsJsonObject();
                variants.add(parseVariant(variantObj));
            }
        }
        
        if (variants.isEmpty()) {
            LOGGER.warn("No valid variants found for item {}", itemId);
            return new ItemDefinition(itemId, 1.0);
        }
        
        return new ItemDefinition(itemId, variants);
    }
    
    /**
     * Parse a single variant from JSON
     */
    private ItemDefinition.Variant parseVariant(JsonObject obj) {
        double value = obj.has("value") ? obj.get("value").getAsDouble() : 1.0;
        int priority = obj.has("priority") ? obj.get("priority").getAsInt() : 0;
        
        ItemMatcher matcher = null;
        if (obj.has("match")) {
            JsonObject matchObj = obj.getAsJsonObject("match");
            matcher = parseItemMatcher(matchObj);
        }
        
        return new ItemDefinition.Variant(value, matcher, priority);
    }
    
    /**
     * Parse item matching criteria from JSON
     */
    private ItemMatcher parseItemMatcher(JsonObject matchObj) {
        Map<String, Object> criteria = new HashMap<>();
        
        // Parse enchanted flag
        if (matchObj.has("enchanted")) {
            criteria.put("enchanted", matchObj.get("enchanted").getAsBoolean());
        }
        
        // Parse enchantments array
        if (matchObj.has("enchantments")) {
            JsonElement enchElement = matchObj.get("enchantments");
            List<String> enchantments = new ArrayList<>();
            
            if (enchElement.isJsonArray()) {
                for (JsonElement e : enchElement.getAsJsonArray()) {
                    enchantments.add(e.getAsString());
                }
            } else if (enchElement.isJsonPrimitive()) {
                enchantments.add(enchElement.getAsString());
            }
            
            criteria.put("enchantments", enchantments);
        }
        
        // Parse custom name
        if (matchObj.has("customName")) {
            JsonElement nameElement = matchObj.get("customName");
            if (nameElement.isJsonPrimitive()) {
                if (nameElement.getAsJsonPrimitive().isBoolean()) {
                    criteria.put("customName", nameElement.getAsBoolean());
                } else {
                    criteria.put("customName", nameElement.getAsString());
                }
            }
        }
        
        // Parse lore
        if (matchObj.has("lore")) {
            JsonElement loreElement = matchObj.get("lore");
            List<String> lore = new ArrayList<>();
            
            if (loreElement.isJsonArray()) {
                for (JsonElement e : loreElement.getAsJsonArray()) {
                    lore.add(e.getAsString());
                }
            } else if (loreElement.isJsonPrimitive()) {
                lore.add(loreElement.getAsString());
            }
            
            criteria.put("lore", lore);
        }
        
        return ItemMatcher.fromJson(criteria);
    }
    
    /**
     * Get an item definition by its ID
     */
    public Optional<ItemDefinition> getItem(String itemId) {
        return Optional.ofNullable(items.get(itemId));
    }
    
    /**
     * Get all loaded items
     */
    public Collection<ItemDefinition> getAllItems() {
        return Collections.unmodifiableCollection(items.values());
    }
    
    /**
     * Check if an item is registered
     */
    public boolean hasItem(String itemId) {
        return items.containsKey(itemId);
    }
    
    /**
     * Get the number of registered items
     */
    public int getItemCount() {
        return items.size();
    }
    
    public void clear() {
        items.clear();
    }
}
