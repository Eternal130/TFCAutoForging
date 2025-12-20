package com.eternal130.tfcaf;

import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(TFCAutoForging.MODID)
public class TFCAutoForging
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tfcaf";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static KeyMapping switchAutoForging;
    public static KeyMapping switchForgingTip;
    public static int timer = 0;

    public TFCAutoForging(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.CLIENT, ConfigFile.CONFIG);
//        new forgeEvent();
    }

    private void setup(final FMLClientSetupEvent event)
    {
        Util.preCalculator();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
