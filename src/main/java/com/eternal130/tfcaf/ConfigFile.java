package com.eternal130.tfcaf;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigFile {
    public static final ForgeConfigSpec.BooleanValue enableAutoForging;
    public static final ForgeConfigSpec.BooleanValue enableForgingTip;
    public static final ForgeConfigSpec.IntValue autoForgingCooldown;
    public static ForgeConfigSpec CONFIG;

    public ConfigFile() {}

    static {
        ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        BUILDER.comment("General settings").push("general");
        enableAutoForging = BUILDER.comment("Is it fully automatic forging?").define("enableAutoForging", true);
        enableForgingTip = BUILDER.comment("Is the next recommended step highlighted?").define("enableDebug", true);
        autoForgingCooldown = BUILDER.comment("cooldown of each automatic forging step(tick)").defineInRange("autoForgingCooldown", 20, 1, 200);
        BUILDER.pop();
        CONFIG = BUILDER.build();
    }

}
