package com.github.syren_dev_tech.vwam.network;

import com.github.syren_dev_tech.vwam.events.FrozenVillagerTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Packet sent from client to server when the villager trade screen is closed.
 * Tells the server to unfreeze the villager's AI.
 */
public class CloseVillagerTradeScreenPacket implements CustomPacketPayload {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsNetwork");
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("vwam", "close_villager_trade_screen");

    public final int villagerEntityId;

    public CloseVillagerTradeScreenPacket(int villagerEntityId) {
        this.villagerEntityId = villagerEntityId;
    }

    public static void encode(CloseVillagerTradeScreenPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.villagerEntityId);
    }

    public static CloseVillagerTradeScreenPacket decode(FriendlyByteBuf buf) {
        int villagerEntityId = buf.readInt();
        return new CloseVillagerTradeScreenPacket(villagerEntityId);
    }

    @Override
    public net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<CloseVillagerTradeScreenPacket> type() {
        return new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<CloseVillagerTradeScreenPacket>(ID);
    }

    public static void handle(CloseVillagerTradeScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Object player = context.player();
            if (!(player instanceof ServerPlayer serverPlayer)) {
                LOGGER.warn("CloseVillagerTradeScreen packet received with invalid player type");
                return;
            }

            ServerLevel serverLevel = serverPlayer.serverLevel();
            if (serverLevel == null) {
                LOGGER.warn("Player {} has no server level!", serverPlayer.getName().getString());
                return;
            }

            // Get the villager entity from the world
            Entity entity = serverLevel.getEntity(packet.villagerEntityId);
            if (!(entity instanceof Villager villager)) {
                LOGGER.warn("Entity {} is not a villager or was unloaded!", packet.villagerEntityId);
                return;
            }

            // Verify this player is actually the one trading with this villager
            java.util.UUID villagerUUID = villager.getUUID();
            java.util.UUID playerUUID = serverPlayer.getUUID();
            if (!FrozenVillagerTracker.isPlayerTradingWith(villagerUUID, playerUUID)) {
                LOGGER.warn("Player {} tried to close trade for villager {} but they're not trading", 
                    serverPlayer.getName().getString(), packet.villagerEntityId);
                return;
            }

            LOGGER.info("Server received CloseVillagerTradeScreen packet for villager {} from player {}", 
                packet.villagerEntityId, serverPlayer.getName().getString());

            // Unfreeze the villager on the server
            FrozenVillagerTracker.unfreezeVillager(villager);
            LOGGER.info("Unfroze villager {} AI on server", packet.villagerEntityId);
        });
    }
}
