package com.eternal130.tfcaf;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyBind {

    // 按键类
    public static KeyMapping switchAutoForging;
    public static KeyMapping switchForgingTip;

    public KeyBind() {
        switchAutoForging = new KeyMapping("key.eternal130.switchAutoForging", GLFW.GLFW_KEY_F, "key.categories.tfcaf");
        switchForgingTip = new KeyMapping("key.eternal130.switchForgingTip", GLFW.GLFW_KEY_G, "key.categories.tfcaf");
        ClientRegistry.registerKeyBinding(switchAutoForging);
        ClientRegistry.registerKeyBinding(switchForgingTip);
    }
}
