package com.eternal130.tfcaf.eventLoader;

import static com.eternal130.tfcaf.config.ConfigFile.enableAutoForging;
import static com.eternal130.tfcaf.config.ConfigFile.enableForgingTip;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.eternal130.tfcaf.KeyBind;
import net.dries007.tfc.api.recipes.anvil.AnvilRecipe;
import net.dries007.tfc.client.gui.GuiAnvilTFC;
import net.dries007.tfc.client.gui.GuiContainerTFC;
import net.dries007.tfc.objects.te.TEAnvilTFC;
import net.dries007.tfc.util.forge.ForgeRule;
import net.dries007.tfc.util.forge.ForgeSteps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import com.eternal130.tfcaf.TFCAutoForging;
import com.eternal130.tfcaf.Util;
import com.eternal130.tfcaf.config.ConfigFile;

public class mcEvent {

    public mcEvent() {
        MinecraftForge.EVENT_BUS.register(this);// 将本类中的事件处理程序注册到forge总线
    }

    @SubscribeEvent
    public void operationHighlight(GuiScreenEvent.DrawScreenEvent.Post event) {
        /*
         * 检测gui绘制事件,用于在铁砧gui中绘制锻造提示以及自动锻造.
         */
        // TFCAutoForging.LOG.info(event.gui.toString());
        try {
            if (event.getGui() instanceof GuiAnvilTFC) {
//                Field[] fields = event.getGui().getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getDeclaredFields();
//                for(Field field : fields) {
//                    TFCAutoForging.logger.info("Field: {} ,Type: {}", field.getName(), field.getType());
//                }
                // 检测当前gui是否是铁砧gui或者是tfcquickpocket替换后的铁砧gui
                TEAnvilTFC anvilTE = getTEAnvilTFC((GuiAnvilTFC) event.getGui());
                if (enableAutoForging || enableForgingTip) {
                    // 当锻造提示功能和自动锻造功能有一个开启时就计算下一步锻造步骤
                    // 当前锻造数值,此值在选择任意锻造操作时改变
                    int currentPoint = anvilTE.getWorkingProgress();
                    // 目标锻造数值,此值由世界种子和锻造配方唯一指定,当当前锻造数值等于目标锻造数值,并且最后三步满足锻造要求时,锻造完成
                    int targetPoint = anvilTE.getWorkingTarget();
                    // 如果目标锻造数值为0,则退出程序,意味着当前并没有选择配方,无需继续计算
                    if (targetPoint == 0) {
                        return;
                    }
                    // 此偏移值是锻造要求最后三步的和,计算锻造步骤时,优先将锻造数值调整到目标数值-偏移值,接下来只需要进行最后三步即可完成锻造
                    int ruleOffset = 0;
                    // 获取锻造要求最后三步,在gui中从左到右索引分别为0,1,2,分别为最后一步,倒数第二步,倒数第三步
                    AnvilRecipe AnvilRecipe = anvilTE.getRecipe();
                    // 下面那个变量内存有最后三步,LinkedList类型,队列数据结构,最多3个元素,从左到右分别为倒数第三步,倒数第二步,倒数第一步
                    ForgeSteps steps = anvilTE.getSteps();
                    // 用于存储锻造要求，其中的值为Util类中operations的索引
                    int[] lastOperations = new int[3];
                    for (int i = 0; i < 3; i++) {
                        try {
                            lastOperations[i] = Util.operationsTfc.get(ForgeRule.getID(AnvilRecipe.getRules()[i]));
                        } catch (Exception e) {
                            //这里的异常是索引越界异常,因为锻造需求相比于1.7,没有any,长度可以小于3
                            lastOperations[i] = 4;
                        }
//                        switch (ForgeRule.getID(AnvilRecipe.getRules()[i])) {
//                            case 0:
//                            case 1:
//                            case 2:
//                            case 3:
//                            case 4:
//                                lastOperations[i] = 3;
//                                break;
//                            case 5:
//                            case 6:
//                            case 7:
//                            case 8:
//                            case 9:
//                                lastOperations[i] = 0;
//                                break;
//                            case 10:
//                            case 11:
//                            case 12:
//                            case 13:
//                            case 14:
//                                lastOperations[i] = 4;
//                                break;
//                            case 15:
//                            case 16:
//                            case 17:
//                            case 18:
//                            case 19:
//                                lastOperations[i] = 5;
//                                break;
//                            case 20:
//                            case 21:
//                            case 22:
//                            case 23:
//                            case 24:
//                                lastOperations[i] = 6;
//                                break;
//                            case 25:
//                            case 26:
//                            case 27:
//                            case 28:
//                            case 29:
//                                lastOperations[i] = 7;
//                                break;
//                        }
                        ruleOffset += Util.operations[lastOperations[i]];
                    }
                    // GuiAnvil中drawItemRulesImages方法绘制最后三步步骤,drawRulesImages方法绘制锻造要求
                    // drawItemRulesImages中rules保存锻造要求,索引从左到右分别为012,itemRules保存最后三步,索引从左到右分别为012
                    // itemRules中,0对应-3,-6,-9;1对应-15;3对应2;4对应7;5对应13;6对应16
                    // x,y为当前gui的左上角,176和192分别为当前gui的宽和高,event.gui.width为当前窗口的宽
                    int x = (event.getGui().width - 176) / 2;
                    int y = (event.getGui().height - 192) / 2;
                    // 保存下一步锻造步骤,值为为Util类中operations的索引
                    int offsetNextOperation = Util
                            .nextOperationOffset(targetPoint - currentPoint - ruleOffset, lastOperations, steps);
                    // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":偏移值{}", ruleOffset);
                    // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":锻造要求{},{},{}", lastOperations[0],
                    // lastOperations[1], lastOperations[2]);
                    // TFCAutoForging.LOG
                    // .info(TFCAutoForging.MODID + ":{}:{}", targetPoint - currentPoint, offsetNextOperation);
                    // 根据锻造步骤的值选择绘制位置,锻造按钮宽和高均为16像素,彼此间隔2像素
                    switch (offsetNextOperation) {
                        case 0:
                            x += 71;
                            y += 68;
                            break;
                        case 1:
                            x += 53;
                            y += 68;
                            break;
                        case 2:
                            x += 71;
                            y += 50;
                            break;
                        case 3:
                            x += 53;
                            y += 50;
                            break;
                        case 4:
                            x += 89;
                            y += 50;
                            break;
                        case 5:
                            x += 107;
                            y += 50;
                            break;
                        case 6:
                            x += 89;
                            y += 68;
                            break;
                        case 7:
                            x += 107;
                            y += 68;
                            break;
                        default:
                            return;
                    }
                    // 如果开启锻造提示
                    if (enableForgingTip) {
                        // 下面代码可以在指定位置渲染一个16*16的方框,具体怎么渲染可以去问gpt
                        GlStateManager.color(0, 0, 1, 1);
                        GlStateManager.disableTexture2D();
                        GlStateManager.disableLighting();

                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferBuilder = tessellator.getBuffer();
                        bufferBuilder.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
                        bufferBuilder.pos(x, y, 100).endVertex();
                        bufferBuilder.pos(x, y + 16, 100).endVertex();
                        bufferBuilder.pos(x + 16, y + 16, 100).endVertex();
                        bufferBuilder.pos(x + 16, y, 100).endVertex();
                        tessellator.draw();

                        GlStateManager.enableLighting();
                        GlStateManager.enableTexture2D();
                        GlStateManager.color(1, 1, 1, 1);
                    }
                    // 当开启自动锻造功能并且计时器为0时
                    if (enableAutoForging && TFCAutoForging.timer == 0) {
                        // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":敲击!");
                        // 重置计时器的值,可以在配置文件中修改,配置文件可以在游戏中动态修改
                        TFCAutoForging.timer = (short) ConfigFile.autoForgingCooldown;
                        // 获取按钮列表,触发按按钮事件需要这个参数
                        List<GuiButton> buttonlist = getButtonList((GuiContainerTFC) event.getGui());
                        // TFCAutoForging.LOG.info("待点按钮id {},待点按钮索引 {},总按钮数 {}",
                        // buttonlist.get(Util.buttonMapping.get(offsetNextOperation)),Util.buttonMapping.get(offsetNextOperation)
                        // , buttonlist);
                        // GuiScreenEvent.ActionPerformedEvent.Pre eventt = new
                        // GuiScreenEvent.ActionPerformedEvent.Pre(event.gui,
                        // buttonlist.get(Util.buttonMapping.get(offsetNextOperation)), buttonlist);
                        // buttonlist.get(Util.buttonMapping.get(offsetNextOperation)).func_146113_a(Minecraft.getMinecraft().getSoundHandler());
                        // if (event.gui.equals(Minecraft.getMinecraft().currentScreen))
                        // MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(event.gui,
                        // buttonlist.get(Util.buttonMapping.get(offsetNextOperation)), buttonlist));

                        // MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Pre(event.gui,
                        // buttonlist.get(Util.buttonMapping.get(offsetNextOperation)), buttonlist));

                        // 因为这个方法是protected权限,因此使用反射来调用,该方法用于处理按钮点击,下面的name为该方法的混淆名,该名被mcp翻译了,反编译代码中看不到正常名
                        // 使用getDeclaredMethods()方法可以获取所有方法的混淆名,顺序和反编译代码中的相同,因此很好找
                        Method actionPerformed = event.getGui().getClass()
                                .getDeclaredMethod("func_146284_a", GuiButton.class);
                        // 设置权限为public
                        actionPerformed.setAccessible(true);
                        // 调用方法,因为按钮索引和用于计算的operations索引都不同,所以经过映射后填入
                        actionPerformed
                                .invoke(event.getGui(), buttonlist.get(Util.buttonMapping.get(offsetNextOperation)));

                        // Method[] methods = event.gui.getClass().getDeclaredMethods();
                        // for(Method method : methods) {
                        // TFCAutoForging.LOG.info("{}:{}", method.getName(),
                        // Arrays.toString(method.getParameterTypes()));
                        // }
                    }
                    // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":绘制成功");
                }
            }
        } catch (Throwable exception) {
            throw new RuntimeException(exception);
        }
    }
    @SubscribeEvent
    public void timer(TickEvent.ClientTickEvent event) {
        // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":tick事件");
        // 设置计时器的值,大于0时每tick-1
        if (TFCAutoForging.timer > 0) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":重置计时器");
            TFCAutoForging.timer--;
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
        // 检测配置文件是否被改变,当配置文件改变时,同步配置文件中的值到内存
        if (eventArgs.getModID().equals(TFCAutoForging.MODID)) ConfigFile.load();
    }
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
            EntityPlayer player = Minecraft.getMinecraft().player;
            // 在游戏中提示当前值
            player.sendMessage(new TextComponentTranslation("key.eternal130.switchAutoForging.info", ConfigFile.enableForgingTip));
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
            EntityPlayer player = Minecraft.getMinecraft().player;
            player.sendMessage(new TextComponentTranslation("key.eternal130.switchForgingTip.info", ConfigFile.enableForgingTip));
            // 保存配置文件
            ConfigFile.config.save();

        }
    }
    private List<GuiButton> getButtonList(GuiContainerTFC gui) throws NoSuchFieldException, IllegalAccessException {
        /**
         * 使用反射获取按钮列表,名字同样被混淆,使用上面那个方法就能轻松获得混淆名.
         */
        // Field[] fields = gui.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredFields();
        // for(Field field : fields) {
        // TFCAutoForging.LOG.info("Field: {} ,Type: {}", field.getName(), field.getType());
        // }
        Field field = gui.getClass()
                .getSuperclass()
                .getSuperclass()
                .getSuperclass()
                .getSuperclass()
                .getDeclaredField("field_146292_n");
        field.setAccessible(true);
        return (List<GuiButton>) field.get(gui);
    }
    private TEAnvilTFC getTEAnvilTFC(GuiAnvilTFC gui) throws NoSuchFieldException, IllegalAccessException {
//        Field[] field = gui.getClass().getSuperclass().getDeclaredFields();
//        for(Field f : field) {
//            TFCAutoForging.logger.info("Found field: {},type:{}", f.getName(), f.getType());
//        }
        Field fields = gui.getClass().getSuperclass().getDeclaredField("tile");
        fields.setAccessible(true);
        return (TEAnvilTFC) fields.get(gui);
    }
}
