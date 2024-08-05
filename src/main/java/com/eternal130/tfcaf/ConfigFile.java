package com.eternal130.tfcaf;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigFile {
    public static final ForgeConfigSpec.BooleanValue enableAutoForging;
    public static final ForgeConfigSpec.BooleanValue enableForgingTip;
    public static final ForgeConfigSpec.IntValue autoForgingCooldown;
    public static final ForgeConfigSpec.IntValue highlightStepCooldown;// 高亮提示单帧持续时间(ms)
    public static final ForgeConfigSpec.IntValue totalFrames;// 高亮提示动画总帧数
    public static final ForgeConfigSpec.IntValue framesPerRow;// 材质每行帧数
    public static final ForgeConfigSpec.IntValue framesPerColumn;// 材质每列帧数
    public static final ForgeConfigSpec.IntValue textureWidth;// 材质宽度
    public static final ForgeConfigSpec.IntValue textureHeight;// 材质高度
    public static ForgeConfigSpec CONFIG;

    public ConfigFile() {}

    static {
        ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        BUILDER.comment("General settings").push("general");
        enableAutoForging = BUILDER.comment("Is it fully automatic forging?").define("enableAutoForging", true);
        enableForgingTip = BUILDER.comment("Is the next recommended step highlighted?").define("enableDebug", true);
        autoForgingCooldown = BUILDER.comment("cooldown of each automatic forging step(tick)").defineInRange("autoForgingCooldown", 20, 1, 200);
        highlightStepCooldown = BUILDER.comment("The duration of each frame of the highlight step(tick)").defineInRange("highlightStepCooldown", 2, 1, 1000);
        totalFrames = BUILDER.comment("Total number of frames in the highlight step animation").defineInRange("totalFrames", 16, 1, 100);
        framesPerRow = BUILDER.comment("Number of frames per row in the texture").defineInRange("framesPerRow", 4, 1, 10);
        framesPerColumn = BUILDER.comment("Number of frames per column in the texture").defineInRange("framesPerColumn", 4, 1, 10);
        textureWidth = BUILDER.comment("texture width of highlight step animation(px)").defineInRange("textureWidth", 18, 1, 1024);
        textureHeight = BUILDER.comment("texture height of highlight step animation(px)").defineInRange("textureHeight", 18, 1, 1024);
        BUILDER.pop();
        CONFIG = BUILDER.build();
    }

}
