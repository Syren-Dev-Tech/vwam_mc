package com.github.syren_dev_tech.vwam.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Manager for loading and storing custom professions, workstations, profession trades, and village configurations.
 * 
 * Loads built-in data files directly from the mod JAR using ClassLoader.
 */
public class CustomRegistryManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsRegistry");

    private final Map<ResourceLocation, CustomProfession> professions = new HashMap<>();
    private final Map<ResourceLocation, CustomWorkstation> workstations = new HashMap<>();
    private final Map<ResourceLocation, Map<Integer, ProfessionTrade>> professionTrades = new HashMap<>();
    private final Map<ResourceLocation, VillageConfiguration> villages = new HashMap<>();
    private final ItemRegistry itemRegistry = new ItemRegistry();

    public void loadDataPacks(ResourceManager resourceManager) {
        LOGGER.info("Loading custom villager professions, workstations, trades, and village configurations...");

        try {
            // Load items first (needed for profession trade lookups)
            itemRegistry.loadItems();
            
            // Load profession names from registry
            String[] professionFiles = loadProfessionNamesFromRegistry();
            String[] workstationFiles = professionFiles;  // Same as professions
            String[] villageFiles = {"default"};
            
            loadProfessions(professionFiles);
            loadWorkstations(workstationFiles);
            loadVillageConfigurations(villageFiles);

            LOGGER.info("Loaded {} items, {} professions, {} workstations, {} profession trade sets, {} village configs",
                    itemRegistry.getItemCount(), professions.size(), workstations.size(), professionTrades.size(), villages.size());
        } catch (Exception e) {
            LOGGER.error("Error loading data", e);
        }
    }

    private void loadProfessions(String[] professionFiles) {
        LOGGER.info("Loading {} professions from JAR...", professionFiles.length);
        int count = 0;
        
        for (String filename : professionFiles) {
            try {
                String path = "data/vwam/professions/" + filename + ".json";
                InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
                
                if (stream != null) {
                    loadProfessionFromStream(stream, filename);
                    stream.close();
                    count++;
                } else {
                    LOGGER.warn("File not found in JAR: {}", path);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load profession: {}", filename, e);
            }
        }
        
        LOGGER.info("Successfully loaded {} out of {} professions", count, professionFiles.length);
    }

    private void loadWorkstations(String[] workstationFiles) {
        LOGGER.info("Loading {} workstations from JAR...", workstationFiles.length);
        int count = 0;
        
        for (String filename : workstationFiles) {
            try {
                String path = "data/vwam/workstations/" + filename + ".json";
                InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
                
                if (stream != null) {
                    loadWorkstationFromStream(stream, filename);
                    stream.close();
                    count++;
                } else {
                    LOGGER.warn("File not found in JAR: {}", path);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load workstation: {}", filename, e);
            }
        }
        
        LOGGER.info("Successfully loaded {} out of {} workstations", count, workstationFiles.length);
    }

    private void loadVillageConfigurations(String[] villageFiles) {
        LOGGER.info("Loading {} village configs from JAR...", villageFiles.length);
        int count = 0;
        
        for (String filename : villageFiles) {
            try {
                String path = "data/vwam/villages/" + filename + ".json";
                InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
                
                if (stream != null) {
                    loadVillageConfigurationFromStream(stream, filename);
                    stream.close();
                    count++;
                } else {
                    LOGGER.warn("File not found in JAR: {}", path);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load village config: {}", filename, e);
            }
        }
        
        LOGGER.info("Successfully loaded {} out of {} village configs", count, villageFiles.length);
    }

    private void loadProfessionFromStream(InputStream stream, String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            JsonElement element = JsonParser.parseReader(reader);
            if (element.isJsonObject()) {
                JsonObject json = element.getAsJsonObject();
                ResourceLocation professionId = ResourceLocation.fromNamespaceAndPath("vwam", filename);

                CustomProfession profession = CustomProfession.fromJson(professionId, json);
                if (profession.isEnabled()) {
                    professions.put(professionId, profession);
                    
                    // Extract trades from profession JSON (new unified format)
                    if (json.has("trades")) {
                        Map<Integer, ProfessionTrade> tradesByLevel = new HashMap<>();
                        ProfessionTrade trade = ProfessionTrade.fromJson(professionId, 1, json);
                        tradesByLevel.put(1, trade);
                        professionTrades.put(professionId, tradesByLevel);
                        
                        int totalTrades = json.getAsJsonArray("trades").size();
                        int buyCount = (int) java.util.Arrays.stream(trade.getTrades())
                                .filter(t -> t.canBuyFromVillager()).count();
                        int sellCount = (int) java.util.Arrays.stream(trade.getTrades())
                                .filter(t -> t.canSellToVillager()).count();
                        LOGGER.info("  ✓ Loaded profession: {} ({} buy, {} sell trades)", 
                                professionId, buyCount, sellCount);
                    } else if (json.has("buy_trades") || json.has("sell_trades")) {
                        // Fallback for old format (backward compatibility)
                        LOGGER.warn("  ! Profession {} uses old format (buy_trades/sell_trades), please migrate", professionId);
                        Map<Integer, ProfessionTrade> tradesByLevel = new HashMap<>();
                        ProfessionTrade trade = ProfessionTrade.fromJson(professionId, 1, json);
                        tradesByLevel.put(1, trade);
                        professionTrades.put(professionId, tradesByLevel);
                    } else {
                        LOGGER.info("  ✓ Loaded profession: {} (no trades)", professionId);
                    }
                }
            }
        }
    }

    private void loadWorkstationFromStream(InputStream stream, String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            JsonElement element = JsonParser.parseReader(reader);
            if (element.isJsonObject()) {
                JsonObject json = element.getAsJsonObject();
                ResourceLocation workstationId = ResourceLocation.fromNamespaceAndPath("vwam", filename);

                CustomWorkstation workstation = CustomWorkstation.fromJson(workstationId, json);
                workstations.put(workstationId, workstation);
                LOGGER.info("  ✓ Loaded workstation: {} -> {}", workstationId, workstation.getBlockId());
            }
        }
    }

    private void loadVillageConfigurationFromStream(InputStream stream, String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {

            JsonElement element = JsonParser.parseReader(reader);
            if (element.isJsonObject()) {
                JsonObject json = element.getAsJsonObject();
                ResourceLocation villageId = ResourceLocation.fromNamespaceAndPath("vwam", filename);

                VillageConfiguration config = VillageConfiguration.fromJson(villageId, json);
                villages.put(villageId, config);
                LOGGER.info("  ✓ Loaded village configuration: {}", villageId);
            }
        }
    }

    // Getters for professions
    public Optional<CustomProfession> getProfession(ResourceLocation id) {
        return Optional.ofNullable(professions.get(id));
    }

    public CustomProfession getProfessionOrThrow(ResourceLocation id) {
        return professions.get(id);
    }

    public Collection<CustomProfession> getAllProfessions() {
        return Collections.unmodifiableCollection(professions.values());
    }

    // Getters for workstations
    public Optional<CustomWorkstation> getWorkstation(ResourceLocation id) {
        return Optional.ofNullable(workstations.get(id));
    }

    public CustomWorkstation getWorkstationOrThrow(ResourceLocation id) {
        return workstations.get(id);
    }

    public Collection<CustomWorkstation> getAllWorkstations() {
        return Collections.unmodifiableCollection(workstations.values());
    }

    // Getters for profession trades
    public Optional<ProfessionTrade> getProfessionTrade(ResourceLocation professionId, int level) {
        Map<Integer, ProfessionTrade> trades = professionTrades.get(professionId);
        if (trades == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(trades.get(level));
    }

    public Map<Integer, ProfessionTrade> getAllProfessionTrades(ResourceLocation professionId) {
        Map<Integer, ProfessionTrade> trades = professionTrades.get(professionId);
        if (trades == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(trades);
    }

    // Getters for village configurations
    public Optional<VillageConfiguration> getVillageConfiguration(ResourceLocation villageId) {
        return Optional.ofNullable(villages.get(villageId));
    }

    public VillageConfiguration getVillageConfigurationOrThrow(ResourceLocation villageId) {
        return villages.get(villageId);
    }

    public Collection<VillageConfiguration> getAllVillageConfigurations() {
        return Collections.unmodifiableCollection(villages.values());
    }

    /**
     * Load the list of profession names from the professions registry JSON file.
     * This allows users to define custom professions without recompiling the mod.
     */
    private String[] loadProfessionNamesFromRegistry() {
        try {
            String path = "data/vwam/professions.json";
            InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
            
            if (stream == null) {
                LOGGER.warn("Profession registry not found at {}, using empty list", path);
                return new String[0];
            }
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                JsonElement element = JsonParser.parseReader(reader);
                if (element.isJsonObject()) {
                    JsonObject json = element.getAsJsonObject();
                    if (json.has("professions")) {
                        JsonArray profArray = json.getAsJsonArray("professions");
                        String[] professions = new String[profArray.size()];
                        for (int i = 0; i < profArray.size(); i++) {
                            professions[i] = profArray.get(i).getAsString();
                        }
                        LOGGER.info("Loaded profession registry with {} professions", professions.length);
                        return professions;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load profession registry", e);
        }
        return new String[0];
    }

    /**
     * Get the item registry
     */
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }
    
    public void clear() {
        professions.clear();
        workstations.clear();
        professionTrades.clear();
        villages.clear();
        itemRegistry.clear();
    }
}
