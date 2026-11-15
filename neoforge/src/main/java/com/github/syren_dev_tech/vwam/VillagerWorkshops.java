package com.github.syren_dev_tech.vwam;

import com.github.syren_dev_tech.vwam.client.VillagerHUDRenderer;
import com.github.syren_dev_tech.vwam.data.RegistryHandler;
import com.github.syren_dev_tech.vwam.events.VillagerEvents;
import com.github.syren_dev_tech.vwam.events.VillagerStockpileEventHandler;
import com.github.syren_dev_tech.vwam.events.VillagerTradeEventHandler;
import com.github.syren_dev_tech.vwam.network.NetworkHandler;
import com.github.syren_dev_tech.vwam.villager.VillagerRoutineEventHandler;
import com.github.syren_dev_tech.vwam.village.ReputationEventHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(VillagerWorkshops.MODID)
public class VillagerWorkshops {
    public static final String MODID = "villager_workshops";
    private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public VillagerWorkshops(IEventBus modEventBus) {
        LOGGER.info("Initializing Villager Workshops!");

        // Register network packets
        modEventBus.addListener(NetworkHandler::register);

        // Register server-side event handlers
        NeoForge.EVENT_BUS.register(new VillagerEvents());
        NeoForge.EVENT_BUS.register(new VillagerRoutineEventHandler());
        NeoForge.EVENT_BUS.register(new VillagerStockpileEventHandler());
        NeoForge.EVENT_BUS.register(new VillagerTradeEventHandler());
        NeoForge.EVENT_BUS.register(new ReputationEventHandler());
        NeoForge.EVENT_BUS.register(this);

        // Register client-side events through mod bus
        modEventBus.addListener(this::setupClient);
    }

    /**
     * Setup client-side event handlers This is called during mod load time
     */
    private void setupClient(FMLClientSetupEvent event) {
        LOGGER.info("Setting up client-side event handlers");
        NeoForge.EVENT_BUS.register(new VillagerHUDRenderer());
    }

    /**
     * Load custom professions and workstations when server starts
     */
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        if (event.getServer() != null) {
            LOGGER.info("Server started, loading custom professions and workstations...");
            RegistryHandler.getInstance().loadDataPacks(event.getServer().getResourceManager());
        }
    }
}
