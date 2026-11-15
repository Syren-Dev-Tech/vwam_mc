package com.github.syren_dev_tech.vwam.production;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for village production system
 */
public class VillageProductionTest {
    
    private VillageStockpile stockpile;
    
    @Before
    public void setUp() {
        stockpile = new VillageStockpile("test_village");
    }
    
    @Test
    public void testStockpileEmpty() {
        assertEquals(0, stockpile.getStock("minecraft:wheat"));
        assertFalse(stockpile.hasStock("minecraft:wheat", 1));
    }
    
    @Test
    public void testAddStock() {
        stockpile.addStock("minecraft:wheat", 10);
        assertEquals(10, stockpile.getStock("minecraft:wheat"));
    }
    
    @Test
    public void testAddMultipleItems() {
        stockpile.addStock("minecraft:wheat", 10);
        stockpile.addStock("minecraft:carrot", 5);
        
        assertEquals(10, stockpile.getStock("minecraft:wheat"));
        assertEquals(5, stockpile.getStock("minecraft:carrot"));
        assertEquals(2, stockpile.getUniqueItemCount());
    }
    
    @Test
    public void testRemoveStock() {
        stockpile.addStock("minecraft:wheat", 10);
        int removed = stockpile.removeStock("minecraft:wheat", 3);
        
        assertEquals(3, removed);
        assertEquals(7, stockpile.getStock("minecraft:wheat"));
    }
    
    @Test
    public void testRemoveMoreThanAvailable() {
        stockpile.addStock("minecraft:wheat", 5);
        int removed = stockpile.removeStock("minecraft:wheat", 10);
        
        assertEquals(5, removed); // Only removed what was available
        assertEquals(0, stockpile.getStock("minecraft:wheat"));
    }
    
    @Test
    public void testRemoveFromEmpty() {
        int removed = stockpile.removeStock("minecraft:wheat", 5);
        assertEquals(0, removed);
    }
    
    @Test
    public void testHasStock() {
        stockpile.addStock("minecraft:wheat", 10);
        
        assertTrue(stockpile.hasStock("minecraft:wheat", 10));
        assertTrue(stockpile.hasStock("minecraft:wheat", 5));
        assertFalse(stockpile.hasStock("minecraft:wheat", 11));
    }
    
    @Test
    public void testSetStock() {
        stockpile.addStock("minecraft:wheat", 10);
        stockpile.setStock("minecraft:wheat", 20);
        
        assertEquals(20, stockpile.getStock("minecraft:wheat"));
    }
    
    @Test
    public void testSetStockToZero() {
        stockpile.addStock("minecraft:wheat", 10);
        stockpile.setStock("minecraft:wheat", 0);
        
        assertEquals(0, stockpile.getStock("minecraft:wheat"));
    }
    
    @Test
    public void testHasIngredientsFor_AllAvailable() {
        stockpile.addStock("minecraft:wheat", 3);
        stockpile.addStock("minecraft:egg", 2);
        
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("minecraft:wheat", 3);
        ingredients.put("minecraft:egg", 2);
        
        assertTrue(stockpile.hasIngredientsFor(ingredients));
    }
    
    @Test
    public void testHasIngredientsFor_PartiallyAvailable() {
        stockpile.addStock("minecraft:wheat", 3);
        
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("minecraft:wheat", 3);
        ingredients.put("minecraft:egg", 2);
        
        assertFalse(stockpile.hasIngredientsFor(ingredients));
    }
    
    @Test
    public void testHasIngredientsFor_Empty() {
        Map<String, Integer> ingredients = new HashMap<>();
        
        assertTrue(stockpile.hasIngredientsFor(ingredients));
    }
    
    @Test
    public void testConsumeIngredients_Success() {
        stockpile.addStock("minecraft:wheat", 3);
        stockpile.addStock("minecraft:egg", 2);
        
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("minecraft:wheat", 2);
        ingredients.put("minecraft:egg", 1);
        
        boolean consumed = stockpile.consumeIngredients(ingredients);
        
        assertTrue(consumed);
        assertEquals(1, stockpile.getStock("minecraft:wheat"));
        assertEquals(1, stockpile.getStock("minecraft:egg"));
    }
    
    @Test
    public void testConsumeIngredients_Insufficient() {
        stockpile.addStock("minecraft:wheat", 2);
        
        Map<String, Integer> ingredients = new HashMap<>();
        ingredients.put("minecraft:wheat", 3);
        
        boolean consumed = stockpile.consumeIngredients(ingredients);
        
        assertFalse(consumed);
        assertEquals(2, stockpile.getStock("minecraft:wheat")); // Unchanged
    }
    
    @Test
    public void testTotalItemCount() {
        stockpile.addStock("minecraft:wheat", 10);
        stockpile.addStock("minecraft:carrot", 5);
        stockpile.addStock("minecraft:egg", 3);
        
        assertEquals(18, stockpile.getTotalItemCount());
    }
    
    @Test
    public void testDirtyFlag() {
        assertFalse(stockpile.isDirty());
        
        stockpile.addStock("minecraft:wheat", 10);
        assertTrue(stockpile.isDirty());
        
        stockpile.markSaved();
        assertFalse(stockpile.isDirty());
    }
    
    @Test
    public void testClear() {
        stockpile.addStock("minecraft:wheat", 10);
        stockpile.addStock("minecraft:carrot", 5);
        
        assertEquals(2, stockpile.getUniqueItemCount());
        
        stockpile.clear();
        
        assertEquals(0, stockpile.getUniqueItemCount());
        assertEquals(0, stockpile.getStock("minecraft:wheat"));
    }
    
    @Test
    public void testProductionItemDefaults() {
        ProductionItem item = new ProductionItem(
            "minecraft:bread",
            2,
            1,
            1200,
            32,
            null
        );
        
        assertEquals("minecraft:bread", item.getItemId());
        assertEquals(2, item.getRequiredLevel());
        assertEquals(1, item.getProductQuantity());
        assertEquals(1200, item.getProductionTicksPerCycle());
        assertEquals(32, item.getStockQuota());
        assertFalse(item.hasCustomRecipe());
    }
    
    @Test
    public void testProductionItemWithCustomRecipe() {
        ProductionItem item = new ProductionItem(
            "minecraft:cake",
            3,
            1,
            2400,
            16,
            "vwam:custom_cake_recipe"
        );
        
        assertTrue(item.hasCustomRecipe());
        assertEquals("vwam:custom_cake_recipe", item.getRecipeName());
    }
    
    @Test
    public void testGetAllStock() {
        stockpile.addStock("minecraft:wheat", 10);
        stockpile.addStock("minecraft:carrot", 5);
        
        Map<String, Integer> all = stockpile.getAllStock();
        
        assertEquals(2, all.size());
        assertEquals(10, all.get("minecraft:wheat").intValue());
        assertEquals(5, all.get("minecraft:carrot").intValue());
    }
    
    @Test
    public void testRecipeManager_BreadRecipe() {
        Map<String, Integer> ingredients = RecipeManager.getDefaultRecipeIngredients("minecraft:bread");
        
        assertEquals(1, ingredients.size());
        assertEquals(3, ingredients.get("minecraft:wheat").intValue());
    }
    
    @Test
    public void testRecipeManager_CakeRecipe() {
        Map<String, Integer> ingredients = RecipeManager.getDefaultRecipeIngredients("minecraft:cake");
        
        assertEquals(4, ingredients.size());
        assertEquals(3, ingredients.get("minecraft:wheat").intValue());
        assertEquals(2, ingredients.get("minecraft:egg").intValue());
        assertEquals(1, ingredients.get("minecraft:milk_bucket").intValue());
        assertEquals(2, ingredients.get("minecraft:sugar").intValue());
    }
    
    @Test
    public void testRecipeManager_UnknownItem() {
        Map<String, Integer> ingredients = RecipeManager.getDefaultRecipeIngredients("minecraft:unknown_item");
        
        assertEquals(0, ingredients.size());
    }
    
    @Test
    public void testStockQuotaLogic() {
        stockpile.addStock("minecraft:wheat", 32);
        
        ProductionItem item = new ProductionItem(
            "minecraft:wheat",
            1,
            5,
            600,
            32,
            null
        );
        
        // Stock is at quota, so should be skipped
        int currentStock = stockpile.getStock(item.getItemId());
        boolean shouldProduce = currentStock < item.getStockQuota();
        
        assertFalse(shouldProduce);
        
        // Remove one item, now should produce
        stockpile.removeStock("minecraft:wheat", 1);
        currentStock = stockpile.getStock(item.getItemId());
        shouldProduce = currentStock < item.getStockQuota();
        
        assertTrue(shouldProduce);
    }
}
