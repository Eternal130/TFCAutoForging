package com.eternal130.tfcaf;

import static com.eternal130.tfcaf.ConfigFile.enableAutoForging;
import static com.eternal130.tfcaf.ConfigFile.enableForgingTip;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.dries007.tfc.client.screen.AnvilScreen;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeSteps;
import net.dries007.tfc.common.capabilities.forge.Forging;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ScreenEvent;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = "tfcaf", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class forgeEvent {
    static ResourceLocation res = new ResourceLocation("tfcaf", "textures/gui/highlight_step.png");// 锻造提示的纹理

    @SubscribeEvent
    public static void operationHighlight(ScreenEvent.Render.Post event) {
//        LOGGER.info("Operation highlight");
        /*
         * 检测gui绘制事件,用于在铁砧gui中绘制锻造提示以及自动锻造.
         */
        // TFCAutoForging.LOG.info(event.gui.toString());
        try {
            if (event.getScreen() instanceof AnvilScreen) {
//                Field[] fields = event.getGui().getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getDeclaredFields();
//                for(Field field : fields) {
//                    TFCAutoForging.logger.info("Field: {} ,Type: {}", field.getName(), field.getType());
//                }
                // 检测当前gui是否是铁砧gui或者是tfcquickpocket替换后的铁砧gui
                AnvilBlockEntity anvilTE = getTEAnvilTFC((AnvilScreen) event.getScreen());
                Forging forging = ((AnvilBlockEntity)anvilTE).getMainInputForging();
                Level level = ((AnvilBlockEntity)anvilTE).getLevel();
                if (enableAutoForging.get() || enableForgingTip.get()) {
                    // 当锻造提示功能和自动锻造功能有一个开启时就计算下一步锻造步骤
                    // 当前锻造数值,此值在选择任意锻造操作时改变
                    if (forging == null) {
                        return;
                    }
                    int currentPoint = forging.getWork();
                    // 目标锻造数值,此值由世界种子和锻造配方唯一指定,当当前锻造数值等于目标锻造数值,并且最后三步满足锻造要求时,锻造完成
                    int targetPoint = forging.getWorkTarget();
                    // 如果目标锻造数值为0,则退出程序,意味着当前并没有选择配方,无需继续计算
                    if (targetPoint == 0) {
                        return;
                    }
                    // 此偏移值是锻造要求最后三步的和,计算锻造步骤时,优先将锻造数值调整到目标数值-偏移值,接下来只需要进行最后三步即可完成锻造
                    int ruleOffset = 0;
                    // 获取锻造要求最后三步,在gui中从左到右索引分别为0,1,2,分别为最后一步,倒数第二步,倒数第三步
                    AnvilRecipe AnvilRecipe = forging.getRecipe(level);
                    // 下面那个变量内存有最后三步
                    ForgeSteps steps = forging.getSteps();
                    // 用于存储锻造要求，其中的值为Util类中operations的索引
                    int[] lastOperations = getRules(AnvilRecipe.getRules());
                    for (int i = 0; i < 3; i++) {
//                        try {
//                            lastOperations[i] = Util.operationsTfc.get(AnvilRecipe.getRules()[i].ordinal());
//                        } catch (Exception e) {
//                            //这里的异常是索引越界异常,因为锻造需求相比于1.7,没有any,长度可以小于3
//                            lastOperations[i] = 4;
//                        }

                        ruleOffset += Util.operations[lastOperations[i]];
                    }
                    // GuiAnvil中drawItemRulesImages方法绘制最后三步步骤,drawRulesImages方法绘制锻造要求
                    // drawItemRulesImages中rules保存锻造要求,索引从左到右分别为012,itemRules保存最后三步,索引从左到右分别为012
                    // itemRules中,0对应-3,-6,-9;1对应-15;3对应2;4对应7;5对应13;6对应16
                    // x,y为当前gui的左上角,176和192分别为当前gui的宽和高,event.gui.width为当前窗口的宽
                    int x = (event.getScreen().width - 176) / 2;
                    int y = (event.getScreen().height - 207) / 2;
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
                    if (enableForgingTip.get()) {
                        // 下面代码可以在指定位置渲染一个16*16的方框,具体怎么渲染可以去问gpt
//                        int color = 0xFF0000FF;
                        GuiGraphics guiGraphics = event.getGuiGraphics();
//                        guiGraphics.vLine(x,y,y+16,color);//左边框
//                        guiGraphics.vLine(x+15,y,y+15,color);//右边框
//                        guiGraphics.hLine(x,x+15,y,color);//上边框
//                        guiGraphics.hLine(x,x+15,y+15,color);//下边框
                        drawbox(x, y, guiGraphics.pose());
                    }
                    // 当开启自动锻造功能并且计时器为0时
                    if (enableAutoForging.get() && TFCAutoForging.timer == 0) {
                        // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":敲击!");
                        // 重置计时器的值,可以在配置文件中修改,配置文件可以在游戏中动态修改
                        TFCAutoForging.timer = ConfigFile.autoForgingCooldown.get();
                        //1.18居然能直接调用鼠标点击方法,下面那一大堆就不需要了
                        event.getScreen().mouseClicked(x+8,y+8,0);
                        // 获取按钮列表,触发按按钮事件需要这个参数
//                        List<GuiButton> buttonlist = getButtonList((GuiContainerTFC) event.getGui());
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
//                        Method actionPerformed = event.getGui().getClass()
//                                .getDeclaredMethod("func_146284_a", GuiButton.class);
//                        // 设置权限为public
//                        actionPerformed.setAccessible(true);
//                        // 调用方法,因为按钮索引和用于计算的operations索引都不同,所以经过映射后填入
//                        actionPerformed
//                                .invoke(event.getGui(), buttonlist.get(Util.buttonMapping.get(offsetNextOperation)));

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
    public static void timer(TickEvent.ClientTickEvent event) {
        // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":tick事件");
        // 设置计时器的值,大于0时每tick-1
        if (TFCAutoForging.timer > 0) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":重置计时器");
            TFCAutoForging.timer--;
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent event) {
        // 快捷键检测
        if (TFCAutoForging.switchAutoForging.consumeClick()) {
            enableAutoForging.set(!enableAutoForging.get());
            ConfigFile.CONFIG.save();
            Player player = Minecraft.getInstance().player;
            // 在游戏中提示当前值
            player.sendSystemMessage(Component.translatable("key.eternal130.switchAutoForging.info", enableAutoForging.get()));

        }
        if (TFCAutoForging.switchForgingTip.consumeClick()) {
            enableForgingTip.set(!enableForgingTip.get());
            ConfigFile.CONFIG.save();
            Player player = Minecraft.getInstance().player;
            player.sendSystemMessage(Component.translatable("key.eternal130.switchForgingTip.info", enableForgingTip.get()));

        }
    }
    private static AnvilBlockEntity getTEAnvilTFC(AnvilScreen gui) throws NoSuchFieldException, IllegalAccessException {
//        Field[] field = gui.getClass().getSuperclass().getDeclaredFields();
//        for(Field f : field) {
//            TFCAutoForging.LOGGER.info("Found field: {},type:{}", f.getName(), f.getType());
//        }
        Field fields = gui.getClass().getSuperclass().getDeclaredField("blockEntity");
        fields.setAccessible(true);
        return (AnvilBlockEntity) fields.get(gui);
    }
    private static int[] getRules(ForgeRule[] rules) {
        // 相对于1.7版本,没有any类型,每种步骤也只有五种位置,少了LastTwo这种类型,因此少遍历一次
        int[] lastOperations = new int[3];
        // 将锻造要求初始化为-1,表示没有要求
        Arrays.fill(lastOperations, -1);
        // 标志该位置要求是否已经被填充
        boolean[] flag = new boolean[3];
        // 首先遍历一次锻造目标,将确定位置的步骤填充到lastOperations中,例如Hit_Last,Hit_Second_Last,Hit_Third_Last
        // 因为hit的last和notlast相比于其他步骤是反序的,因此单独摘出来判断
        for (ForgeRule rule : rules) {
            // 这三种序号对5取余后分别是1,3,4,Hit_Last的序号是2,Hit_Not_Last的序号是1,单独摘出来判断
            if ((rule.ordinal() != 1 && rule.ordinal() % 5 == 1 && !flag[0]) || rule.ordinal() == 2) {
                lastOperations[0] = Util.operationsTfc.get(rule.ordinal());
                flag[0] = true;
            } else if (rule.ordinal() % 5 == 3 && !flag[1]) {
                lastOperations[1] = Util.operationsTfc.get(rule.ordinal());
                flag[1] = true;
            } else if (rule.ordinal() % 5 == 4 && !flag[2]) {
                lastOperations[2] = Util.operationsTfc.get(rule.ordinal());
                flag[2] = true;
            }
        }
        // 第二次遍历,填充可以位于倒数第二步和倒数第三步的步骤,例如Hit_Not_Last
        // 其他步骤的Not_last序号对5取余后是2，Hit_Not_Last的序号是1，因此单独摘出来判断
        for (ForgeRule rule : rules) {
            // 这一步的序号对5取余后是2
            if ((rule.ordinal() != 2 && rule.ordinal() % 5 == 2) || rule.ordinal() == 1){
                // 如果倒数第三步已经填充,说明倒数第三步是已经定死的步骤,不可以更改,如果能进入这里的循环并且两个位置都已经填满,说明该锻造配方无法完成
                // 所以当倒数第三步已经填充,就将倒数第二步填充为当前步骤,否则填充最后一步
                if (flag[2]) {
                    lastOperations[1] = Util.operationsTfc.get(rule.ordinal());
                    flag[1] = true;
                } else {
                    lastOperations[2] = Util.operationsTfc.get(rule.ordinal());
                    flag[2] = true;
                }
            }
        }
        // 最后一次遍历,填充剩余的步骤,这里的步骤是可以位于任意位置的步骤,例如BendAny
        for (ForgeRule rule : rules) {
            // 这个步骤的序号对5取余后是0
            if (rule.ordinal() % 5 == 0) {
                // 遍历lastOperations,如果有空位就填充,并且因为锻造需求里每步出现一次,所以只填充一次,跳出大循环
                for (int i = 0; i < 3; i++) {
                    if (!flag[i]) {
                        lastOperations[i] = Util.operationsTfc.get(rule.ordinal());
                        flag[i] = true;
                        break;
                    }
                }
            }
        }
        // 如果还有空位,说明锻造需求不满3个,这时将空位填入4,对应锻造数值是2,此时未遍历的需求只有Any,对于Any,填入的数值依然是4
        for (int i = 0; i < 3; i++) {
            if (!flag[i]) {
                lastOperations[i] = 4;
            }
        }
        return lastOperations;
    }

    private static void drawbox(int x, int y, PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, res);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
//        RenderSystem.enableTexture();

        int frame = (int) ((Minecraft.getInstance().level.getGameTime() / ConfigFile.highlightStepCooldown.get()) % ConfigFile.totalFrames.get());
        float uMin = (frame % ConfigFile.framesPerRow.get()) / (float) ConfigFile.framesPerRow.get();
        float vMin = (frame / ConfigFile.framesPerRow.get()) / (float) ConfigFile.framesPerColumn.get();
        float uMax = uMin + 1.0f / ConfigFile.framesPerRow.get();
        float vMax = vMin + 1.0f / ConfigFile.framesPerColumn.get();
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, (float) (x - (ConfigFile.textureWidth.get() - 16) / 2.0), (float) (y + (ConfigFile.textureHeight.get() - 16) / 2.0 + 16), 0).uv(uMin, vMax).endVertex();
        bufferBuilder.vertex(matrix, (float) (x + (ConfigFile.textureWidth.get() - 16) / 2.0 + 16), (float) (y + (ConfigFile.textureHeight.get() - 16) / 2.0 + 16), 0).uv(uMax, vMax).endVertex();
        bufferBuilder.vertex(matrix, (float) (x + (ConfigFile.textureWidth.get() - 16) / 2.0 + 16), (float) (y - (ConfigFile.textureHeight.get() - 16) / 2.0), 0).uv(uMax, vMin).endVertex();
        bufferBuilder.vertex(matrix, (float) (x - (ConfigFile.textureWidth.get() - 16) / 2.0), (float) (y - (ConfigFile.textureHeight.get() - 16) / 2.0), 0).uv(uMin, vMin).endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }
}
