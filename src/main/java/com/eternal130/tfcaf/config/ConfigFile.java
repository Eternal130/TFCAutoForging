package com.eternal130.tfcaf.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigFile {

    public static Boolean enableAutoForging = true;
    public static Boolean enableForgingTip = true;
    public static int autoForgingCooldown = 20;
    public static int highlightStepCooldown = 100;// 高亮提示单帧持续时间(ms)
    public static int totalFrames = 16;// 高亮提示动画总帧数
    public static int framesPerRow = 4;// 材质每行帧数
    public static int framesPerColumn = 4;// 材质每列帧数
    public static int textureWidth = 18;// 材质宽度
    public static int textureHeight = 18;// 材质高度
    public static Configuration config;

    public static void synchronizeConfiguration(File configFile) {
        config = new Configuration(configFile);
        config.load();
        load();
    }

    public static void load() {
        enableAutoForging = config.getBoolean(
            "enableAutoForging",
            Configuration.CATEGORY_GENERAL,
            true,
            "Is it fully automatic forging?");
        enableForgingTip = config.getBoolean(
            "enableForgingTip",
            Configuration.CATEGORY_GENERAL,
            true,
            "Is the next recommended step highlighted?");
        autoForgingCooldown = config.getInt(
            "autoForgingCooldown",
            Configuration.CATEGORY_GENERAL,
            20,
            1,
            200,
            "cooldown of each automatic forging step(tick)");
        highlightStepCooldown = config.getInt(
            "highlightStepCooldown",
            Configuration.CATEGORY_GENERAL,
            100,
            1,
            1000,
            "The duration of each frame of the highlight step(ms)");
        totalFrames = config.getInt(
            "totalFrames",
            Configuration.CATEGORY_GENERAL,
            16,
            1,
            100,
            "total frames of highlight step animation");
        framesPerRow = config.getInt(
            "framesPerRow",
            Configuration.CATEGORY_GENERAL,
            4,
            1,
            10,
            "frames per row of highlight step animation in highlight_step.png");
        framesPerColumn = config.getInt(
            "framesPerColumn",
            Configuration.CATEGORY_GENERAL,
            4,
            1,
            10,
            "frames per column of highlight step animation in highlight_step.png");
        textureWidth = config.getInt(
            "textureWidth",
            Configuration.CATEGORY_GENERAL,
            18,
            1,
            100,
            "texture width of highlight step animation(px)");
        textureHeight = config.getInt(
            "textureHeight",
            Configuration.CATEGORY_GENERAL,
            18,
            1,
            100,
            "texture height of highlight step animation(px)");

        if (config.hasChanged()) {
            config.save();
        }
    }
}
