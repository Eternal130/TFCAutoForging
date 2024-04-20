package com.eternal130.tfcaf;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = "tfcaf", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class modEvent {
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        TFCAutoForging.switchAutoForging = new KeyMapping("key.eternal130.switchAutoForging", GLFW.GLFW_KEY_F, "key.categories.tfcaf");
        TFCAutoForging.switchForgingTip = new KeyMapping("key.eternal130.switchForgingTip", GLFW.GLFW_KEY_G, "key.categories.tfcaf");
        event.register(TFCAutoForging.switchAutoForging);
        event.register(TFCAutoForging.switchForgingTip);
    }
}
