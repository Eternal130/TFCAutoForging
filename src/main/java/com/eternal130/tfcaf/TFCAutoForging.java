package com.eternal130.tfcaf;

import com.eternal130.tfcaf.proxy.CommonProxy;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = TFCAutoForging.MODID, name = TFCAutoForging.NAME, version = TFCAutoForging.VERSION, dependencies = "required-after:tfc", guiFactory = "com.eternal130.tfcaf.config.GUIFactory")
public class TFCAutoForging
{
    public static final String MODID = "tfcaf";
    public static final String NAME = "TFC Auto Forging";
    public static final String VERSION = "1.4";
    public static short timer = 0;
    public static int lastWorkValue = -1; // 记录点击瞬间的数值
    public static boolean isWaitingForServer = false; // 是否正在等待服务器响应
    // 等待服务器响应的超时计数器(tick),锻造数值需等服务器同步回客户端,
    // 超时后强制解除等待状态并重试,防止自动锻造因同步异常卡死
    public static short waitTimeout = 0;
    public static final short WAIT_TIMEOUT_TICKS = 20;
    // 单件完工状态:服务器完成锻造后会把产物放回输入槽,且当产物只匹配一个配方时会自动重选该配方,
    // 不加停机的话自动锻造会对产物继续开工,造成连锻
    public static boolean isJobDone = false;

    public static Logger logger;
    @SidedProxy(clientSide = "com.eternal130.tfcaf.proxy.ClientProxy", serverSide = "com.eternal130.tfcaf.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static TFCAutoForging instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
