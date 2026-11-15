package com.github.syren_dev_tech.vwam.villager;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;

/**
 * Determines the current scheduled activity of a villager based on in-game time.
 * Villagers follow a daily routine that varies based on their profession and the time of day.
 *
 * Minecraft day cycle:
 * - Tick 0 (6:00 AM): Dawn - Villagers wake up
 * - Tick 2000 (8:00 AM): Morning - Villagers start working
 * - Tick 8000 (2:00 PM): Afternoon - Villagers rest/socialize
 * - Tick 11000 (5:00 PM): Evening - Villagers gather POI and relax
 * - Tick 14000 (8:00 PM): Night - Villagers sleep
 * - Tick 24000: Back to tick 0 (next day)
 */
public class VillagerActivitySchedule {
    
    // Time boundaries (in ticks, where 0 = 6:00 AM)
    private static final long DAWN_START = 0;           // 6:00 AM
    private static final long MORNING_START = 2000;     // 8:00 AM
    private static final long AFTERNOON_START = 8000;   // 2:00 PM
    private static final long EVENING_START = 11000;    // 5:00 PM
    private static final long NIGHT_START = 14000;      // 8:00 PM
    private static final long DAY_LENGTH = 24000;
    
    /**
     * Get the current scheduled activity for a villager
     * 
     * @param villager The villager to check
     * @param dayTime The current in-game time of day (0-23999)
     * @return A string describing the villager's current activity
     */
    public static String getCurrentActivity(Villager villager, long dayTime) {
        if (dayTime < MORNING_START) {
            return "Waking Up";
        } else if (dayTime < AFTERNOON_START) {
            return getWorkingActivity(villager);
        } else if (dayTime < EVENING_START) {
            return "Resting/Socializing";
        } else if (dayTime < NIGHT_START) {
            return "Gathering POI";
        } else {
            return "Sleeping";
        }
    }
    
    /**
     * Get the specific working activity for a villager based on their profession
     */
    private static String getWorkingActivity(Villager villager) {
        VillagerData data = villager.getVillagerData();
        VillagerProfession profession = data.getProfession();
        
        // Handle different professions
        if (profession == null || profession == VillagerProfession.NONE) {
            return "Idle";
        }
        
        // Return a generic "Working" activity for all professions
        // In a real scenario, you might want profession-specific activities
        return "Working";
    }
    
    /**
     * Get the profession name as a readable string
     */
    public static String getProfessionName(Villager villager) {
        VillagerProfession profession = villager.getVillagerData().getProfession();
        
        if (profession == null || profession == VillagerProfession.NONE) {
            return "Unemployed";
        }
        
        // Get the profession name from the registry
        String professionId = profession.toString();
        // Extract the profession name from minecraft:profession_name format
        if (professionId.contains(":")) {
            String name = professionId.substring(professionId.indexOf(":") + 1);
            // Convert snake_case to Title Case
            return formatProfessionName(name);
        }
        
        return formatProfessionName(professionId);
    }
    
    /**
     * Format a profession name from snake_case to Title Case
     */
    private static String formatProfessionName(String name) {
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder();
        
        for (String part : parts) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(part.substring(0, 1).toUpperCase())
                  .append(part.substring(1));
        }
        
        return result.toString();
    }
    
    /**
     * Get the villager's profession level as a roman numeral
     * Levels: 1-5 (Novice, Apprentice, Journeyman, Expert, Master)
     */
    public static String getProfessionLevel(Villager villager) {
        int level = villager.getVillagerData().getLevel();
        return convertToRomanNumeral(level);
    }
    
    /**
     * Convert a number to roman numeral format
     */
    private static String convertToRomanNumeral(int number) {
        if (number <= 0 || number > 5) {
            return String.valueOf(number);
        }
        
        String[] romanNumerals = {"", "I", "II", "III", "IV", "V"};
        return romanNumerals[number];
    }
}
