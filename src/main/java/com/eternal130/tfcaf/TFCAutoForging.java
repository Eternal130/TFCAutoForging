package com.eternal130.tfcaf;

import com.eternal130.tfcaf.proxy.CommonProxy;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = TFCAutoForging.MODID, name = TFCAutoForging.NAME, version = TFCAutoForging.VERSION, dependencies = "required-after:tfc", guiFactory = "com.eternal130.tfcaf.config.GUIFactory")
public class TFCAutoForging
{
    public static final String MODID = "tfcaf";
    public static final String NAME = "TFC Auto Forging";
    public static final String VERSION = "1.0";
    public static short timer = 0;

    public static Logger logger;
    @SidedProxy(clientSide = "com.eternal130.tfcaf.proxy.ClientProxy", serverSide = "com.eternal130.tfcaf.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static TFCAutoForging instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
