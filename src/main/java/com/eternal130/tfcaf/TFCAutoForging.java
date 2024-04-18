package com.eternal130.tfcaf;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eternal130.tfcaf.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = TFCAutoForging.MODID,
    version = Tags.VERSION,
    name = TFCAutoForging.MODNAME,
    acceptedMinecraftVersions = "[1.7.10]",
    guiFactory = "com.eternal130.tfcaf.config.GUIFactory",
    dependencies = "required-after:terrafirmacraft")
public class TFCAutoForging {

    public static final String MODID = "tfcaf";
    public static final String MODNAME = "TFC Auto Forging";
    public static final Logger LOG = LogManager.getLogger(MODID);
    public static short timer = 0;
    // 下面的proxy会在调用时自行判断在服务器还是客户端运行
    @SidedProxy(
        clientSide = "com.eternal130.tfcaf.proxy.ClientProxy",
        serverSide = "com.eternal130.tfcaf.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

}
