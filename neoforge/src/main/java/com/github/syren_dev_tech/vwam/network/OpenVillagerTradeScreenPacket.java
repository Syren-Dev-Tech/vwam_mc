package com.github.syren_dev_tech.vwam.network;

import com.github.syren_dev_tech.vwam.data.ProfessionTrade;
import com.github.syren_dev_tech.vwam.data.RegistryHandler;
import com.github.syren_dev_tech.vwam.data.VillageConfiguration;
import com.github.syren_dev_tech.vwam.ui.VillagerTradeScreen;
import com.github.syren_dev_tech.vwam.village.VillageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Packet sent from server to client to open the villager trade screen
 * Includes profession, trades, village name, and village stockpile data
 */
public class OpenVillagerTradeScreenPacket implements CustomPacketPayload {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsNetwork");
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("vwam", "open_villager_trade_screen");

    public final int villagerEntityId;
    public final ResourceLocation villageId;
    public final String villageName;  // Human-friendly village name
    public final int villagerLevel;
    public final ResourceLocation professionId;
    public final CompoundTag stockpileData; // Item -> quantity mapping

    public OpenVillagerTradeScreenPacket(int villagerEntityId, ResourceLocation villageId, String villageName, 
                                         int villagerLevel, ResourceLocation professionId, CompoundTag stockpileData) {
        this.villagerEntityId = villagerEntityId;
        this.villageId = villageId;
        this.villageName = villageName;
        this.villagerLevel = villagerLevel;
        this.professionId = professionId;
        this.stockpileData = stockpileData;
    }

    public static void encode(OpenVillagerTradeScreenPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.villagerEntityId);
        buf.writeResourceLocation(packet.villageId);
        buf.writeUtf(packet.villageName);
        buf.writeInt(packet.villagerLevel);
        buf.writeResourceLocation(packet.professionId);
        buf.writeNbt(packet.stockpileData);
    }

    public static OpenVillagerTradeScreenPacket decode(FriendlyByteBuf buf) {
        int villagerEntityId = buf.readInt();
        ResourceLocation villageId = buf.readResourceLocation();
        String villageName = buf.readUtf(32767);
        int villagerLevel = buf.readInt();
        ResourceLocation professionId = buf.readResourceLocation();
        CompoundTag stockpileData = buf.readNbt();

        return new OpenVillagerTradeScreenPacket(villagerEntityId, villageId, villageName, villagerLevel, professionId, stockpileData);
    }

    @Override
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<OpenVillagerTradeScreenPacket> type() {
        return new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<OpenVillagerTradeScreenPacket>(ID);
    }

    public static void handle(OpenVillagerTradeScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            LOGGER.info("Client received OpenVillagerTradeScreen packet for villager {} (profession: {}, village: {})", 
                packet.villagerEntityId, packet.professionId, packet.villageName);

            // Get the villager entity on client side
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                LOGGER.warn("No level on client!");
                return;
            }

            Entity entity = minecraft.level.getEntity(packet.villagerEntityId);
            if (!(entity instanceof Villager villager)) {
                LOGGER.warn("Entity {} is not a villager!", packet.villagerEntityId);
                return;
            }

            // Get the profession trades from registry
            ProfessionTrade professionTrade = RegistryHandler.getInstance().getProfessionTrade(packet.professionId, packet.villagerLevel)
                .orElse(new ProfessionTrade(packet.professionId, packet.villagerLevel, new ProfessionTrade.Trade[0], new ProfessionTrade.ServiceOption[0]));

            VillageConfiguration villageConfig = RegistryHandler.getInstance().getVillageConfiguration(packet.villageId).orElse(null);

            // If no config found, create a default one
            if (villageConfig == null) {
                LOGGER.info("No village configuration found for {}, creating default", packet.villageId);
                villageConfig = new com.github.syren_dev_tech.vwam.data.VillageConfiguration(
                    packet.villageId, 
                    packet.villageName,  // Use the village name from packet
                    64,    // baseStockAmount
                    0.25f, // demandThreshold
                    0.75f, // surplusThreshold
                    1.5f,  // demandPriceMultiplier
                    0.5f,  // surplusPriceMultiplier
                    0.0f,  // initialReputation
                    1.0f,  // maxReputation
                    -1.0f, // minReputation
                    0.05f, // tradeReputationGain
                    -0.5f, // hostileThreshold
                    0.5f,  // friendlyThreshold
                    0.1f,  // tradeDiscountPerStep
                    2.0f,  // heroBuffMultiplier
                    24     // heroBuffDurationHours
                );
            }

            VillageManager villageManager = RegistryHandler.getVillageManager();

            // Open the screen
            if (minecraft.player != null) {
                minecraft.setScreen(new VillagerTradeScreen(minecraft.player, villager, professionTrade, villageManager, villageConfig, packet.villageId));
            }
        });
    }
}
