package com.github.syren_dev_tech.vwam.data;

import net.minecraft.resources.ResourceLocation;
import com.github.syren_dev_tech.vwam.village.VillageStockpile;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

/**
 * Represents a production recipe for a villager profession.
 * Can produce items either by crafting (requires ingredients) or generating (no ingredients, time-based).
 */
public class ProductionRecipe {
    private final ResourceLocation professionId;
    private final String recipeId;
    private final int minLevel;

    private final String outputItem;
    private final int outputQuantity;

    // Production method flags
    private final boolean isCraftingRecipe;  // true = requires ingredients, false = generation
    private final float baseProductionTime;  // In in-game hours

    private final Ingredient[] ingredients;  // Empty array if no ingredients needed
    private final int priority;              // Lower = higher priority in round-robin

    // Stock management
    private final int maxStock;              // Maximum amount in stockpile before stopping production
    private final boolean requiresIngredients;

    public ProductionRecipe(
            ResourceLocation professionId,
            String recipeId,
            int minLevel,
            String outputItem,
            int outputQuantity,
            float baseProductionTime,
            Ingredient[] ingredients,
            int priority,
            int maxStock
    ) {
        this.professionId = professionId;
        this.recipeId = recipeId;
        this.minLevel = minLevel;
        this.outputItem = outputItem;
        this.outputQuantity = outputQuantity;
        this.baseProductionTime = baseProductionTime;
        this.ingredients = ingredients;
        this.priority = priority;
        this.maxStock = maxStock;

        // Determine recipe type
        this.requiresIngredients = ingredients != null && ingredients.length > 0;
        this.isCraftingRecipe = requiresIngredients;
    }

    public static ProductionRecipe fromJson(ResourceLocation professionId, JsonObject json) {
        String recipeId = json.get("recipe_id").getAsString();
        int minLevel = json.has("min_level") ? json.get("min_level").getAsInt() : 1;
        String outputItem = json.get("output_item").getAsString();
        int outputQuantity = json.has("output_quantity") ? json.get("output_quantity").getAsInt() : 1;
        int maxStock = json.has("max_stock") ? json.get("max_stock").getAsInt() : 64;
        int priority = json.has("priority") ? json.get("priority").getAsInt() : 0;

        // Production time
        float baseTime;
        if (json.has("base_production_hours")) {
            baseTime = json.get("base_production_hours").getAsFloat();
        } else if (json.has("generation_time_hours")) {
            // Alternative name for time-based generation
            baseTime = json.get("generation_time_hours").getAsFloat();
        } else {
            baseTime = 1.0f;
        }

        // Ingredients (optional)
        Ingredient[] ingredients = new Ingredient[0];
        if (json.has("ingredients")) {
            ingredients = parseIngredients(json.getAsJsonArray("ingredients"));
        }

        return new ProductionRecipe(
                professionId,
                recipeId,
                minLevel,
                outputItem,
                outputQuantity,
                baseTime,
                ingredients,
                priority,
                maxStock
        );
    }

    private static Ingredient[] parseIngredients(JsonArray array) {
        if (array == null || array.size() == 0) {
            return new Ingredient[0];
        }

        Ingredient[] ingredients = new Ingredient[array.size()];
        for (int i = 0; i < array.size(); i++) {
            JsonObject obj = array.get(i).getAsJsonObject();
            ingredients[i] = new Ingredient(
                    obj.get("item").getAsString(),
                    obj.get("quantity").getAsInt()
            );
        }
        return ingredients;
    }

    // Getters
    public ResourceLocation getProfessionId() { return professionId; }
    public String getRecipeId() { return recipeId; }
    public int getMinLevel() { return minLevel; }
    public String getOutputItem() { return outputItem; }
    public int getOutputQuantity() { return outputQuantity; }
    public float getBaseProductionTime() { return baseProductionTime; }
    public Ingredient[] getIngredients() { return ingredients; }
    public int getPriority() { return priority; }
    public int getMaxStock() { return maxStock; }

    public boolean isCraftingRecipe() { return isCraftingRecipe; }
    public boolean isGenerationRecipe() { return !isCraftingRecipe; }
    public boolean requiresIngredients() { return requiresIngredients; }

    /**
     * Check if this recipe can produce given the current stockpile state
     * @param stockpile The village stockpile
     * @param villagerLevel The villager's level
     * @return True if recipe can execute
     */
    public boolean canProduce(VillageStockpile stockpile, int villagerLevel) {
        // Check villager level
        if (villagerLevel < minLevel) {
            return false;
        }

        // Check stock limit
        int currentStock = stockpile.getQuantity(outputItem);
        if (currentStock >= maxStock) {
            return false;  // Already at or above max stock
        }

        // Check ingredients if required
        if (requiresIngredients) {
            for (Ingredient ingredient : ingredients) {
                int available = stockpile.getQuantity(ingredient.item);
                if (available < ingredient.quantity) {
                    return false;  // Missing ingredients
                }
            }
        }

        return true;
    }

    /**
     * Execute production: consume ingredients and add output to stockpile
     * @param stockpile The village stockpile
     * @return True if successful, false if failed
     */
    public boolean executeProd(VillageStockpile stockpile) {
        // Re-check constraints before executing
        int currentStock = stockpile.getQuantity(outputItem);
        if (currentStock >= maxStock) {
            return false;
        }

        // Consume ingredients
        if (requiresIngredients) {
            for (Ingredient ingredient : ingredients) {
                if (!stockpile.removeItems(ingredient.item, ingredient.quantity)) {
                    return false;  // Failed to consume ingredients
                }
            }
        }

        // Produce output
        stockpile.addItems(outputItem, outputQuantity);
        return true;
    }

    /**
     * Represents an ingredient needed for production
     */
    public static class Ingredient {
        public final String item;
        public final int quantity;

        public Ingredient(String item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
    }
}
