package com.eternal130.tfcaf;

import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;

public class KeyBind {

    // 按键类
    public static KeyBinding switchAutoForging;
    public static KeyBinding switchForgingTip;

    public KeyBind() {
        switchAutoForging = new KeyBinding("key.eternal130.switchAutoForging", Keyboard.KEY_F, "key.categories.tfcaf");
        switchForgingTip = new KeyBinding("key.eternal130.switchForgingTip", Keyboard.KEY_G, "key.categories.tfcaf");
        ClientRegistry.registerKeyBinding(switchAutoForging);
        ClientRegistry.registerKeyBinding(switchForgingTip);
    }
}
