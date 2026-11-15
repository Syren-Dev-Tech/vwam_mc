package com.github.syren_dev_tech.vwam.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles network packet registration
 */
public class NetworkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("VillagerWorkshopsNetwork");

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        LOGGER.info("Registering network packets");
        
        // Create a StreamCodec for CompoundTag
        StreamCodec<RegistryFriendlyByteBuf, CompoundTag> nbtCodec = new StreamCodec<RegistryFriendlyByteBuf, CompoundTag>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, CompoundTag value) {
                buf.writeNbt(value);
            }

            @Override
            public CompoundTag decode(RegistryFriendlyByteBuf buf) {
                return buf.readNbt();
            }
        };
        
        // Create a StreamCodec for String
        StreamCodec<RegistryFriendlyByteBuf, String> stringCodec = new StreamCodec<RegistryFriendlyByteBuf, String>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, String value) {
                buf.writeUtf(value);
            }

            @Override
            public String decode(RegistryFriendlyByteBuf buf) {
                return buf.readUtf(32767);
            }
        };
        
        // Register OpenVillagerTradeScreenPacket
        StreamCodec<RegistryFriendlyByteBuf, OpenVillagerTradeScreenPacket> codec = 
            StreamCodec.composite(
                ByteBufCodecs.INT,
                (packet) -> packet.villagerEntityId,
                ResourceLocation.STREAM_CODEC,
                (packet) -> packet.villageId,
                stringCodec,
                (packet) -> packet.villageName,
                ByteBufCodecs.INT,
                (packet) -> packet.villagerLevel,
                ResourceLocation.STREAM_CODEC,
                (packet) -> packet.professionId,
                nbtCodec,
                (packet) -> packet.stockpileData,
                OpenVillagerTradeScreenPacket::new
            );
        
        var type = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<OpenVillagerTradeScreenPacket>(OpenVillagerTradeScreenPacket.ID);
        
        event.registrar(OpenVillagerTradeScreenPacket.ID.toString())
            .playToClient(
                type,
                codec,
                (IPayloadHandler<OpenVillagerTradeScreenPacket>) OpenVillagerTradeScreenPacket::handle
            );
        
        // Register CloseVillagerTradeScreenPacket
        StreamCodec<RegistryFriendlyByteBuf, CloseVillagerTradeScreenPacket> closeCodec = 
            StreamCodec.composite(
                ByteBufCodecs.INT,
                (packet) -> packet.villagerEntityId,
                CloseVillagerTradeScreenPacket::new
            );
        
        var closeType = new net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<CloseVillagerTradeScreenPacket>(CloseVillagerTradeScreenPacket.ID);
        
        event.registrar(CloseVillagerTradeScreenPacket.ID.toString())
            .playToServer(
                closeType,
                closeCodec,
                (IPayloadHandler<CloseVillagerTradeScreenPacket>) CloseVillagerTradeScreenPacket::handle
            );
        
        LOGGER.info("Registered all network packets");
    }
}
