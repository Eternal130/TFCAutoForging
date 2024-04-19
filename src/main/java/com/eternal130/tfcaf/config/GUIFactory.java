package com.eternal130.tfcaf.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;


public class GUIFactory implements IModGuiFactory {

    // 该类在主类的@Mod下标识
    @Override
    public void initialize(Minecraft minecraftInstance) {
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen guiScreen) {
        return new ConfigGUI(guiScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

//    public final class TFCAFModGui extends GuiConfig {
//        public TFCAFModGui(GuiScreen parentScreen) {
//            super(parentScreen, "tfcaf", "TerraFirmaCraft Auto Forging");
//        }
//    }

}
