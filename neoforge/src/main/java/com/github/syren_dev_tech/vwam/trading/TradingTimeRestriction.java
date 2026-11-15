package com.github.syren_dev_tech.vwam.trading;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import com.github.syren_dev_tech.vwam.villager.VillagerActivitySchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enforces trading time restrictions based on villager work schedule.
 * Villagers can only be traded with during their working hours (8:00 AM - 2:00 PM).
 * 
 * Work Schedule:
 * - 6:00 AM - 8:00 AM (Ticks 0-2000): Waking up, cannot trade
 * - 8:00 AM - 2:00 PM (Ticks 2000-8000): Working, CAN TRADE
 * - 2:00 PM - 5:00 PM (Ticks 8000-11000): Resting/socializing, cannot trade
 * - 5:00 PM - 8:00 PM (Ticks 11000-14000): Gathering POI, cannot trade
 * - 8:00 PM - 6:00 AM (Ticks 14000-24000): Sleeping, cannot trade
 */
public class TradingTimeRestriction {
    private static final Logger LOGGER = LoggerFactory.getLogger("TradingTimeRestriction");
    
    // Work schedule boundaries (in ticks)
    private static final long WORK_START = 2000;     // 8:00 AM
    private static final long WORK_END = 8000;       // 2:00 PM
    private static final long DAY_LENGTH = 24000;
    
    /**
     * Check if a villager is currently working and available for trading
     * 
     * @param villager The villager to check
     * @param level The world/level the villager is in
     * @return true if the villager is working, false otherwise
     */
    public static boolean isVillagerWorking(Villager villager, Level level) {
        long dayTime = level.getDayTime() % DAY_LENGTH;
        return isWithinWorkingHours(dayTime);
    }
    
    /**
     * Check if a specific time is within working hours
     * 
     * @param dayTime The day time in ticks (0-23999)
     * @return true if within working hours, false otherwise
     */
    public static boolean isWithinWorkingHours(long dayTime) {
        return dayTime >= WORK_START && dayTime < WORK_END;
    }
    
    /**
     * Get the minutes until the villager starts working
     * Returns 0 if currently working
     * 
     * @param dayTime The day time in ticks (0-23999)
     * @return Minutes until work starts, or 0 if currently working
     */
    public static int getMinutesUntilWorkStart(long dayTime) {
        if (isWithinWorkingHours(dayTime)) {
            return 0; // Currently working
        }
        
        // Calculate ticks until work starts
        long ticksUntil;
        if (dayTime < WORK_START) {
            // Before work starts today
            ticksUntil = WORK_START - dayTime;
        } else {
            // After work ends today - count to next day
            ticksUntil = DAY_LENGTH - dayTime + WORK_START;
        }
        
        // Convert ticks to minutes (20 ticks = 1 minute)
        return (int) (ticksUntil / 20);
    }
    
    /**
     * Get the minutes remaining in the work day
     * Returns 0 if not currently working
     * 
     * @param dayTime The day time in ticks (0-23999)
     * @return Minutes remaining in work period, or 0 if not working
     */
    public static int getMinutesRemainingInWorkDay(long dayTime) {
        if (!isWithinWorkingHours(dayTime)) {
            return 0; // Not currently working
        }
        
        long ticksRemaining = WORK_END - dayTime;
        return (int) (ticksRemaining / 20);
    }
    
    /**
     * Get the current work period description for a villager
     * 
     * @param villager The villager to describe
     * @param level The level the villager is in
     * @return A descriptive message about the villager's availability
     */
    public static String getWorkStatusMessage(Villager villager, Level level) {
        long dayTime = level.getDayTime() % DAY_LENGTH;
        String professionName = getProfessionName(villager);
        
        if (isWithinWorkingHours(dayTime)) {
            int minutesRemaining = getMinutesRemainingInWorkDay(dayTime);
            return String.format("This %s is currently working! You have %d minute(s) left to trade.", 
                professionName, minutesRemaining);
        }
        
        int minutesUntil = getMinutesUntilWorkStart(dayTime);
        return String.format("This %s is not working right now. They will be available for trading in %d minute(s).", 
            professionName, minutesUntil);
    }
    
    /**
     * Get time description for debugging
     */
    public static String getTimeDescription(long dayTime) {
        dayTime = dayTime % DAY_LENGTH;
        int hours = (int) (dayTime / 1000 + 6) % 24; // 6:00 AM is tick 0
        int minutes = (int) ((dayTime % 1000) * 60 / 1000);
        
        return String.format("%02d:%02d - %s", hours, minutes, getActivityAtTime(dayTime));
    }
    
    /**
     * Get the activity happening at a specific time
     */
    private static String getActivityAtTime(long dayTime) {
        if (dayTime < 2000) {
            return "Waking up";
        } else if (dayTime < 8000) {
            return "Working";
        } else if (dayTime < 11000) {
            return "Resting/Socializing";
        } else if (dayTime < 14000) {
            return "Gathering POI";
        } else {
            return "Sleeping";
        }
    }
    
    /**
     * Get the profession name for a villager
     */
    public static String getProfessionName(Villager villager) {
        return VillagerActivitySchedule.getProfessionName(villager);
    }
    
    /**
     * Log trading attempt for debugging
     */
    public static void logTradingAttempt(Villager villager, Level level, boolean allowed) {
        long dayTime = level.getDayTime() % DAY_LENGTH;
        String status = allowed ? "ALLOWED" : "BLOCKED";
        LOGGER.debug("Trading attempt with {} [{}]: {} at {}", 
            getProfessionName(villager),
            villager.getId(),
            status,
            getTimeDescription(dayTime));
    }
}
