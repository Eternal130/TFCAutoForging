package com.eternal130.tfcaf;

import com.eternal130.tfcaf.config.ConfigFile;
import com.eternal130.tfcaf.eventLoader.mcEvent;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("tfcaf")
public class TFCAutoForging
{
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static int timer = 0;
    public static int lastWorkValue = -1; // 记录点击瞬间的数值
    public static boolean isWaitingForServer = false; // 是否正在等待服务器响应
    // 等待服务器响应的超时计数器(tick),锻造数值需等服务器同步回客户端,
    // 超时后强制解除等待状态并重试,防止自动锻造因同步异常卡死
    public static int waitTimeout = 0;
    public static final int WAIT_TIMEOUT_TICKS = 20;
    // 单件完工状态:服务器完成锻造后会把产物放回输入槽,且当产物只匹配一个配方时会自动重选该配方,
    // 不加停机的话自动锻造会对产物继续开工,造成连锻
    public static boolean isJobDone = false;

    public TFCAutoForging()
    {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigFile.CONFIG);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLClientSetupEvent event)
    {
        // some preinit code
        Util.preCalculator();
        new KeyBind();
        new mcEvent();
    }


}
