package com.eternal130.tfcaf;

import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TFCAutoForging.MODID)
public class TFCAutoForging
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "tfcaf";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static KeyMapping switchAutoForging;
    public static KeyMapping switchForgingTip;
    public static KeyMapping switchPacketForging;
    public static int timer = 0;

    public static int lastWorkValue = -1;                // 记录点击瞬间的数值
    public static boolean isWaitingForServer = false;    // 是否正在等待服务器响应
    // 等待服务器响应的超时计数器(tick),锻造数值需等服务器同步回客户端,
    // 超时后强制解除等待状态并重试,防止自动锻造因同步异常卡死
    public static int waitTimeout = 0;
    public static final int WAIT_TIMEOUT_TICKS = 20;
    // 单件完工状态:服务器完成锻造后会把产物放回输入槽,且当产物只匹配一个配方时会自动重选该配方,
    // 不加停机的话自动锻造会对产物继续开工,造成连锻
    public static boolean isJobDone = false;

    public TFCAutoForging()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::setup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigFile.CONFIG);
//        new ForgeEvent();
    }

    private void setup(final FMLClientSetupEvent event)
    {
        Util.preCalculator();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
