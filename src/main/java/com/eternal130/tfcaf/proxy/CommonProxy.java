package com.eternal130.tfcaf.proxy;

import com.eternal130.tfcaf.TFCAutoForging;
import com.eternal130.tfcaf.config.ConfigFile;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod.EventBusSubscriber
public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        // 注册配置文件
        ConfigFile.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        TFCAutoForging.logger.info("TFCAutoForging loaded");
        // TFCAutoForging.LOG.info("I am MyMod at version " + Tags.VERSION);
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

}
