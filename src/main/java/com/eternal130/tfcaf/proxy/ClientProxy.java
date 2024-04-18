package com.eternal130.tfcaf.proxy;

import com.eternal130.tfcaf.KeyBind;
import com.eternal130.tfcaf.Util;
import com.eternal130.tfcaf.eventLoader.commonEvent;
import com.eternal130.tfcaf.eventLoader.fmlEvent;
import com.eternal130.tfcaf.eventLoader.mcEvent;

import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        // 注册各个事件处理程序,预计算锻造步骤,注册快捷键
        new mcEvent();
        new fmlEvent();
        new commonEvent();
        Util.preCalculator();
        new KeyBind();
    }

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

}
