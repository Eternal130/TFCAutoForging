package com.eternal130.tfcaf;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "tfcaf", value = Dist.CLIENT)
public class modEvent {
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        TFCAutoForging.switchAutoForging = new KeyMapping("key.eternal130.switchAutoForging", GLFW.GLFW_KEY_F, "key.categories.tfcaf");
        TFCAutoForging.switchForgingTip = new KeyMapping("key.eternal130.switchForgingTip", GLFW.GLFW_KEY_G, "key.categories.tfcaf");
        TFCAutoForging.switchPacketForging = new KeyMapping("key.eternal130.switchPacketForging", GLFW.GLFW_KEY_H, "key.categories.tfcaf");
        event.register(TFCAutoForging.switchAutoForging);
        event.register(TFCAutoForging.switchForgingTip);
        event.register(TFCAutoForging.switchPacketForging);
    }
}
