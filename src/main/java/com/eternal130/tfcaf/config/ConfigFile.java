package com.eternal130.tfcaf.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class ConfigFile {

    public static boolean enableAutoForging = true;
    public static boolean enableForgingTip = true;
    public static int autoForgingCooldown = 20;
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
                enableAutoForging,
                "Is it fully automatic forging?");
        enableForgingTip = config.getBoolean(
                "enableForgingTip",
                Configuration.CATEGORY_GENERAL,
                enableForgingTip,
                "Is the next recommended step highlighted?");
        autoForgingCooldown = config.getInt(
                "autoForgingCooldown",
                Configuration.CATEGORY_GENERAL,
                autoForgingCooldown,
                1,
                200,
                "cooldown of each automatic forging step(tick)");

        if (config.hasChanged()) {
            config.save();
        }
    }
}
