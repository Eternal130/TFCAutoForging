package com.eternal130.tfcaf.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import com.eternal130.tfcaf.TFCAutoForging;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class ConfigGUI extends GuiConfig {

    // 该类用于在游戏中修改配置文件
    public ConfigGUI(GuiScreen parentScreen) {
        super(parentScreen, getConfigElements(), TFCAutoForging.MODID, false, false, TFCAutoForging.MODNAME);
    }

    private static List<IConfigElement> getConfigElements() {

        List<IConfigElement> configElementsList = new ArrayList<>();
        for (String catName : ConfigFile.config.getCategoryNames()) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":{}", catName);
            if (catName.contains(".")) continue;
            configElementsList.add(new ConfigElement(ConfigFile.config.getCategory(catName)));
        }
        return configElementsList;
    }
}
