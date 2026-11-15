package com.github.syren_dev_tech.vwam.command;

import com.github.syren_dev_tech.vwam.village.ReputationEffects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command for checking and managing village reputation
 */
public class ReputationCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger("ReputationCommand");
    
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("vwam:reputation")
                .requires(cs -> cs.hasPermission(2))
                .then(
                    Commands.argument("village", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .executes(ctx -> getReputation(
                            ctx.getSource(),
                            com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "village")
                        ))
                )
        );
    }
    
    private static int getReputation(CommandSourceStack source, String villageNamespace) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Must be executed by a player"));
            return 0;
        }
        
        try {
            ResourceLocation villageId = ResourceLocation.fromNamespaceAndPath("vwam", villageNamespace);
            String status = ReputationEffects.getReputationStatus(player, villageId);
            source.sendSuccess(() -> Component.literal(status), true);
            return 1;
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cInvalid village ID§r"));
            LOGGER.error("Error getting reputation", e);
            return 0;
        }
    }
}
