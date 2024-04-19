package com.eternal130.tfcaf.proxy;

import com.eternal130.tfcaf.KeyBind;
import com.eternal130.tfcaf.Util;
import com.eternal130.tfcaf.eventLoader.mcEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy{
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        Util.preCalculator();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        new mcEvent();
        new KeyBind();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

}
