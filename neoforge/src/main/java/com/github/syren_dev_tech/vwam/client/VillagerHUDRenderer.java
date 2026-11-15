package com.github.syren_dev_tech.vwam.client;

import com.github.syren_dev_tech.vwam.villager.VillagerActivitySchedule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side event handler to render villager information on the HUD.
 * Displays profession, level, and current scheduled activity when looking at a villager.
 */
public class VillagerHUDRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerHUDRenderer");
    
    // Configuration
    private static final int TEXT_COLOR = 0xFFFFFF;  // White
    private static final int BACKGROUND_COLOR = 0x3F3F3F;  // Dark gray with transparency
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 11;
    private static final int MAX_RENDER_DISTANCE = 50;  // Blocks
    
    /**
     * Render villager information on screen when looking at a villager
     */
    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        
        // Only render if we have a player and camera
        if (minecraft.player == null || minecraft.gameRenderer.getMainCamera() == null) {
            return;
        }
        
        // Check what the player is looking at
        HitResult hitResult = minecraft.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return;
        }
        
        EntityHitResult entityHitResult = (EntityHitResult) hitResult;
        if (!(entityHitResult.getEntity() instanceof Villager)) {
            return;
        }
        
        Villager villager = (Villager) entityHitResult.getEntity();
        
        // Check if villager is in render distance
        double distance = minecraft.player.distanceTo(villager);
        if (distance > MAX_RENDER_DISTANCE) {
            return;
        }
        
        // Get villager information
        long dayTime = minecraft.level.getDayTime() % 24000;
        String profession = VillagerActivitySchedule.getProfessionName(villager);
        String level = VillagerActivitySchedule.getProfessionLevel(villager);
        String activity = VillagerActivitySchedule.getCurrentActivity(villager, dayTime);
        
        // Prepare display text
        String line1 = "Profession: " + profession;
        String line2 = "Level: " + level;
        String line3 = "Activity: " + activity;
        
        // Render on HUD
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        
        // Calculate position - top center
        int posX = (screenWidth / 2) - 50;
        int posY = 10;
        
        renderVillagerInfo(guiGraphics, posX, posY, line1, line2, line3);
    }
    
    /**
     * Render the villager information box
     */
    private void renderVillagerInfo(GuiGraphics guiGraphics, int posX, int posY, 
                                           String line1, String line2, String line3) {
        Minecraft minecraft = Minecraft.getInstance();
        
        // Calculate box dimensions
        int lineCount = 3;
        int boxHeight = (lineCount * LINE_HEIGHT) + (PADDING * 2);
        int boxWidth = 130;  // Fixed width for consistency
        
        // Draw semi-transparent background
        guiGraphics.fill(posX, posY, posX + boxWidth, posY + boxHeight, BACKGROUND_COLOR);
        
        // Draw border
        guiGraphics.fill(posX, posY, posX + boxWidth, posY + 1, TEXT_COLOR);  // Top
        guiGraphics.fill(posX, posY + boxHeight - 1, posX + boxWidth, posY + boxHeight, TEXT_COLOR);  // Bottom
        guiGraphics.fill(posX, posY, posX + 1, posY + boxHeight, TEXT_COLOR);  // Left
        guiGraphics.fill(posX + boxWidth - 1, posY, posX + boxWidth, posY + boxHeight, TEXT_COLOR);  // Right
        
        // Draw text
        int textX = posX + PADDING;
        int textY = posY + PADDING;
        
        guiGraphics.drawString(minecraft.font, line1, textX, textY, TEXT_COLOR, false);
        textY += LINE_HEIGHT;
        guiGraphics.drawString(minecraft.font, line2, textX, textY, TEXT_COLOR, false);
        textY += LINE_HEIGHT;
        guiGraphics.drawString(minecraft.font, line3, textX, textY, TEXT_COLOR, false);
    }
}
