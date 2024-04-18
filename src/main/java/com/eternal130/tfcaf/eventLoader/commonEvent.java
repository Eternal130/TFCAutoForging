package com.eternal130.tfcaf.eventLoader;

import com.eternal130.tfcaf.TFCAutoForging;
import com.eternal130.tfcaf.config.ConfigFile;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class commonEvent {

    public commonEvent() {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        // 检测配置文件是否被改变,当配置文件改变时,同步配置文件中的值到内存
        if (eventArgs.modID.equals(TFCAutoForging.MODID)) ConfigFile.load();
    }
}
