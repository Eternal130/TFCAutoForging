package com.eternal130.tfcaf.eventLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.config.Configuration;

import com.eternal130.tfcaf.KeyBind;
import com.eternal130.tfcaf.TFCAutoForging;
import com.eternal130.tfcaf.config.ConfigFile;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class fmlEvent {

    public fmlEvent() {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void timer(TickEvent.ClientTickEvent event) {
        // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":tick事件");
        // 设置计时器的值,大于0时每tick-1
        if (TFCAutoForging.timer > 0) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":重置计时器");
            TFCAutoForging.timer--;
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        // 快捷键检测
        if (KeyBind.switchAutoForging.isPressed()) {
            ConfigFile.config.load();
            // 修改配置文件中的值
            ConfigFile.config.get(Configuration.CATEGORY_GENERAL, "enableAutoForging", false)
                .set(!ConfigFile.enableAutoForging);
            ConfigFile.enableAutoForging = ConfigFile.config.getBoolean(
                "enableAutoForging",
                Configuration.CATEGORY_GENERAL,
                ConfigFile.enableAutoForging,
                "Is it fully automatic forging?");
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            // 在游戏中提示当前值
            player.addChatMessage(
                new ChatComponentTranslation("key.eternal130.switchAutoForging.info", ConfigFile.enableAutoForging));
            // 保存配置文件
            ConfigFile.config.save();
        }
        if (KeyBind.switchForgingTip.isPressed()) {
            ConfigFile.config.load();

            // 修改配置文件中的值
            ConfigFile.config.get(Configuration.CATEGORY_GENERAL, "enableForgingTip", true)
                .set(!ConfigFile.enableForgingTip);
            ConfigFile.enableForgingTip = ConfigFile.config.getBoolean(
                "enableForgingTip",
                Configuration.CATEGORY_GENERAL,
                ConfigFile.enableForgingTip,
                "Is the next recommended step highlighted?");
            EntityPlayer player = Minecraft.getMinecraft().thePlayer;
            player.addChatMessage(
                new ChatComponentTranslation("key.eternal130.switchForgingTip.info", ConfigFile.enableForgingTip));
            // 保存配置文件
            ConfigFile.config.save();

        }
    }

}
