package com.eternal130.tfcaf.eventLoader;

import static com.eternal130.tfcaf.config.ConfigFile.enableAutoForging;
import static com.eternal130.tfcaf.config.ConfigFile.enableForgingTip;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import com.dunk.tfc.GUI.GuiAnvil;
import com.dunk.tfc.GUI.GuiContainerTFC;
import com.dunk.tfc.TileEntities.TEAnvil;
import com.dunk.tfc.api.Crafting.AnvilManager;
import com.dunk.tfc.api.Crafting.PlanRecipe;
import com.dunk.tfc.api.Enums.RuleEnum;
import com.eternal130.tfcaf.TFCAutoForging;
import com.eternal130.tfcaf.Util;
import com.eternal130.tfcaf.config.ConfigFile;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tfcquickpockets.ClientStuff;

public class mcEvent {

    static boolean hasTFCQuickPockets = false;// 标志tfcquickpockets这个mod是否存在
    static ResourceLocation res = new ResourceLocation("tfcaf", "textures/gui/highlight_step.png");// 锻造提示的纹理
    static boolean wasInAnvilGui = false; // 跟踪上一次是否在铁砧GUI中
    // 本件锻造任务开始时输入槽物品的标识(item的数字id),用于检测输入槽物品被替换
    static int jobInputItemId = -1;
    // 本件锻造任务开始时选定的方案,玩家重选方案视为主动开始新任务,解除完工停机
    static String jobPlan = "";
    // 已发出本件最后一步敲击,等待服务器确认完工(输入槽物品被替换)后停机
    static boolean finalStrikeSent = false;

    public mcEvent() {
        checkPockets();
        MinecraftForge.EVENT_BUS.register(this);// 将本类中的事件处理程序注册到forge总线
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void operationHighlight(GuiScreenEvent.DrawScreenEvent.Post event) {
        /*
         * 检测gui绘制事件,用于在铁砧gui中绘制锻造提示以及自动锻造.
         */
        // TFCAutoForging.LOG.info(event.gui.toString());
        try {
            boolean isInAnvilGui = event.gui instanceof GuiAnvil
                || (hasTFCQuickPockets && event.gui instanceof ClientStuff.AnvilGUIWithFastBagAccess);
            // 检测GUI关闭：上一次在铁砧GUI中，当前不在，重置状态
            if (wasInAnvilGui && !isInAnvilGui) {
                TFCAutoForging.isWaitingForServer = false;
                TFCAutoForging.waitTimeout = 0;
                TFCAutoForging.lastWorkValue = -1;
                TFCAutoForging.isJobDone = false;
                jobInputItemId = -1;
                jobPlan = "";
                finalStrikeSent = false;
            }
            wasInAnvilGui = isInAnvilGui;
            if (isInAnvilGui) {
                // 检测当前gui是否是铁砧gui或者是tfcquickpocket替换后的铁砧gui
                TEAnvil anvilTE = getAnvilTE((GuiContainerTFC) event.gui);

                // 完工确认:发出最后一步后输入槽物品被替换,说明服务器已产出成品顶替原料,本件完工停机
                // 停机的同时把基线更新为产物的id,这样产物继续留在槽里不会被误判为玩家换料
                if (finalStrikeSent && anvilTE.anvilItemStacks[1] != null
                    && Item.getIdFromItem(anvilTE.anvilItemStacks[1].getItem()) != jobInputItemId) {
                    TFCAutoForging.isJobDone = true;
                    finalStrikeSent = false;
                    jobInputItemId = Item.getIdFromItem(anvilTE.anvilItemStacks[1].getItem());
                } else if (anvilTE.anvilItemStacks[1] == null) {
                    // 输入槽被清空(玩家取走产物或原料)或容器同步抖动(选方案会触发服务器重开容器,槽位短暂为null),
                    // 保留基线:同步抖动后同id物品回来时不会触发换料逻辑,避免误清玩家刚选的方案
                } else if (jobInputItemId != -1
                    && Item.getIdFromItem(anvilTE.anvilItemStacks[1].getItem()) != jobInputItemId) {
                        // 槽内物品相对基线发生变化且不是完工顶替,是玩家主动换料,视为开始新的一件,解除停机继续锻造
                        // 服务器完工时只清自己侧的craftingPlan且从不同步,客户端残留的旧方案与服务器不一致;
                        // 且同一种原料可对应多个方案(如全套工具),不能自动套用旧方案,因此清空客户端方案让玩家重新选择
                        // 注意只在已知基线且物品变化时清:初次放料(基线-1)和选方案触发的GUI切换/容器重同步不能清,
                        // 否则玩家刚选的方案会被容器同步抖动清掉
                        TFCAutoForging.isJobDone = false;
                        jobInputItemId = Item.getIdFromItem(anvilTE.anvilItemStacks[1].getItem());
                        anvilTE.craftingPlan = "";
                        jobPlan = "";
                    }
                // 玩家重选方案(方案字符串变化),视为主动开始新任务
                if (!anvilTE.craftingPlan.equals(jobPlan)) {
                    TFCAutoForging.isJobDone = false;
                    finalStrikeSent = false;
                    jobPlan = anvilTE.craftingPlan;
                    jobInputItemId = anvilTE.anvilItemStacks[1] != null
                        ? Item.getIdFromItem(anvilTE.anvilItemStacks[1].getItem())
                        : -1;
                }

                if (enableAutoForging || enableForgingTip) {
                    // 当锻造提示功能和自动锻造功能有一个开启时就计算下一步锻造步骤
                    // 当前锻造数值,此值在选择任意锻造操作时改变
                    int currentPoint = anvilTE.getItemCraftingValue();
                    // 目标锻造数值,此值由世界种子和锻造配方唯一指定,当当前锻造数值等于目标锻造数值,并且最后三步满足锻造要求时,锻造完成

                    // 服务器响应检测
                    if (TFCAutoForging.isWaitingForServer) {
                        // 如果当前数值与上次记录的点击前数值不同，说明服务器已更新进度
                        if (currentPoint != TFCAutoForging.lastWorkValue) {
                            TFCAutoForging.isWaitingForServer = false;
                        } else if (TFCAutoForging.waitTimeout == 0) {
                            // 超时仍未收到服务器响应,说明该次操作被服务器的敲击冷却静默吞掉,
                            // 强制解除等待状态,让下一次循环重试,防止自动锻造卡死
                            TFCAutoForging.isWaitingForServer = false;
                        }
                    }

                    int targetPoint = anvilTE.getCraftingValue();
                    // 如果目标锻造数值为0,则退出程序,意味着当前并没有选择配方,无需继续计算
                    if (targetPoint == 0) {
                        return;
                    }
                    // 获取当前锻造配方
                    PlanRecipe p = AnvilManager.getInstance()
                        .getPlan(anvilTE.craftingPlan);
                    if (p == null) {
                        return;
                    }
                    // 此偏移值是锻造要求最后三步的和,计算锻造步骤时,优先将锻造数值调整到目标数值-偏移值,接下来只需要进行最后三步即可完成锻造
                    int ruleOffset = 0;
                    // 获取锻造要求最后三步,在gui中从左到右索引分别为0,1,2,分别为最后一步,倒数第二步,倒数第三步
                    RuleEnum[] rules = p.rules;
                    // 下面两个变量都保存当前锻造物品的最后三步,上面方法返回anvilTE中的成员变量,该变量在每次锻造后自动更新,下面那个方法直接从锻造物品的nbt中获取最后三步
                    // 两个变量的值应该相同
                    int[] itemCraftingRules = anvilTE.itemCraftingRules;
                    int[] itemRules = anvilTE.getItemRules();
                    // 用于存储锻造要求，其中的值为Util类中operations的索引
                    int[] lastOperations = getRules(rules);
                    for (Integer i : lastOperations) {
                        ruleOffset += Util.operations[i];
                    }
                    // GuiAnvil中drawItemRulesImages方法绘制最后三步步骤,drawRulesImages方法绘制锻造要求
                    // drawItemRulesImages中rules保存锻造要求,索引从左到右分别为012,itemRules保存最后三步,索引从左到右分别为012
                    // itemRules中,0对应-3,-6,-9;1对应-15;3对应2;4对应7;5对应13;6对应16
                    // x,y为当前gui的左上角,208和204分别为当前gui的宽和高,event.gui.width为当前窗口的宽
                    int x = (event.gui.width - 208) / 2;
                    int y = (event.gui.height - 204) / 2;
                    // 保存下一步锻造步骤,值为为Util类中operations的索引
                    int offsetNextOperation = Util
                        .nextOperationOffset(targetPoint - currentPoint - ruleOffset, lastOperations, itemRules);
                    // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":偏移值{}", ruleOffset);
                    // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":锻造要求{},{},{}", lastOperations[0],
                    // lastOperations[1], lastOperations[2]);
                    // TFCAutoForging.LOG
                    // .info(TFCAutoForging.MODID + ":{}:{}", targetPoint - currentPoint, offsetNextOperation);
                    // 根据锻造步骤的值选择绘制位置,锻造按钮宽和高均为16像素,彼此间隔2像素
                    switch (offsetNextOperation) {
                        case 0:
                            x += 87;
                            y += 82;
                            break;
                        case 1:
                            x += 69;
                            y += 82;
                            break;
                        case 2:
                            x += 87;
                            y += 64;
                            break;
                        case 3:
                            x += 69;
                            y += 64;
                            break;
                        case 4:
                            x += 105;
                            y += 64;
                            break;
                        case 5:
                            x += 123;
                            y += 64;
                            break;
                        case 6:
                            x += 105;
                            y += 82;
                            break;
                        case 7:
                            x += 123;
                            y += 82;
                            break;
                        default:
                            return;
                    }
                    // 如果开启锻造提示
                    if (enableForgingTip) {
                        // 下面代码可以在指定位置渲染一个16*16的方框,具体怎么渲染可以去问gpt
                        drawbox(x, y);
                    }
                    // 当开启自动锻造功能并且计时器为0时,且服务器已响应（不在等待状态）时才执行点击
                    // 完工后停机,直到玩家重开GUI或重选方案,防止客户端残留的旧方案对新放入的原料自动开工
                    if (enableAutoForging && TFCAutoForging.timer == 0
                        && !TFCAutoForging.isWaitingForServer
                        && !TFCAutoForging.isJobDone) {
                        // 不可锻造时不进行锻造
                        if (!(anvilTE.isTemperatureWorkable(1) && anvilTE.anvilItemStacks[0] != null
                            && (anvilTE.anvilItemStacks[1].getItemDamage() == 0 || anvilTE.anvilItemStacks[1].getItem()
                                .getHasSubtypes())
                            && anvilTE.getAnvilType() >= anvilTE.craftingReq)) return;
                        // 记录当前数值，并标记为"正在等待服务器响应"
                        TFCAutoForging.lastWorkValue = currentPoint;
                        TFCAutoForging.isWaitingForServer = true;
                        TFCAutoForging.waitTimeout = TFCAutoForging.WAIT_TIMEOUT_TICKS;
                        // 本次敲击为本件最后一步时置标志,等待服务器确认完工后停机
                        finalStrikeSent = Util
                            .isFinalStep(targetPoint - currentPoint - ruleOffset, lastOperations, itemRules);
                        // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":敲击!");
                        // 重置计时器的值,可以在配置文件中修改,配置文件可以在游戏中动态修改
                        TFCAutoForging.timer = (short) ConfigFile.autoForgingCooldown;
                        // 获取按钮列表,触发按按钮事件需要这个参数
                        List<GuiButton> buttonlist = getButtonList((GuiContainerTFC) event.gui);
                        // TFCAutoForging.LOG.info("待点按钮id {},待点按钮索引 {},总按钮数 {}",
                        // buttonlist.get(Util.buttonMapping.get(offsetNextOperation)),Util.buttonMapping.get(offsetNextOperation)
                        // , buttonlist);
                        // 因为这个方法是protected权限,因此使用反射来调用,该方法用于处理按钮点击,下面的name为该方法的混淆名,该名被mcp翻译了,反编译代码中看不到正常名
                        // 使用getDeclaredMethods()方法可以获取所有方法的混淆名,顺序和反编译代码中的相同,因此很好找
                        Method actionPerformed = event.gui.getClass()
                            .getDeclaredMethod("func_146284_a", GuiButton.class);
                        // 设置权限为public
                        actionPerformed.setAccessible(true);
                        // 调用方法,因为按钮索引和锻造步骤索引以及用于计算的operations索引都不同,所以前两个都经过映射后填入
                        actionPerformed
                            .invoke(event.gui, buttonlist.get(7 - Util.buttonMapping.get(offsetNextOperation)));
                    }
                    // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":绘制成功");
                }
            }
        } catch (Throwable exception) {
            throw new RuntimeException(exception);
        }
    }

    public void checkPockets() {
        /**
         * 使用forge提供的方法检测tfcquickpockets这个mod是否存在.
         */
        hasTFCQuickPockets = Loader.isModLoaded("tfcquickpockets");
    }

    private TEAnvil getAnvilTE(GuiContainerTFC gui) {
        /**
         * 因为实际运行时不能确定是否存在tfcquickpockets,因此将相关代码抽出来,否则注册到事件总线中无法正常运行,即使相关代码不会运行.
         *
         * @param gui 事件对应的gui
         * @return 返回值为TEAnvil
         */
        if (hasTFCQuickPockets) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":存在quickpockets");
            return ((ClientStuff.AnvilGUIWithFastBagAccess) gui).anvilTE;
        } else {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":不存在quickpockets");
            return ((GuiAnvil) gui).anvilTE;
        }
    }

    private List<GuiButton> getButtonList(GuiContainerTFC gui) throws NoSuchFieldException, IllegalAccessException {
        /**
         * 使用反射获取按钮列表,名字同样被混淆,使用上面那个方法就能轻松获得混淆名.
         */
        Field field = gui.getClass()
            .getSuperclass()
            .getSuperclass()
            .getSuperclass()
            .getDeclaredField("field_146292_n");
        field.setAccessible(true);
        return (List<GuiButton>) field.get(gui);
    }

    private int[] getRules(RuleEnum[] rules) {
        int[] lastOperations = new int[3];
        // 将锻造要求初始化为-1,表示没有要求
        Arrays.fill(lastOperations, -1);
        // 标志该位置要求是否已经被填充
        boolean[] flag = new boolean[3];
        // 首先遍历一次锻造目标,将确定位置的步骤填充到lastOperations中,例如HitLast,HitSecondFormLast,HitThirdFormLast
        for (RuleEnum rule : rules) {
            // 这三种序号对6取余后分别是2,3,4
            if (rule.ordinal() % 6 == 2 && !flag[0]) {
                lastOperations[0] = Util.operationsTfc.get(rule.Action);
                flag[0] = true;
            } else if (rule.ordinal() % 6 == 3 && !flag[1]) {
                lastOperations[1] = Util.operationsTfc.get(rule.Action);
                flag[1] = true;
            } else if (rule.ordinal() % 6 == 4 && !flag[2]) {
                lastOperations[2] = Util.operationsTfc.get(rule.Action);
                flag[2] = true;
            }
        }
        // 第二次遍历,填充可以位于倒数第一步和倒数第二步的步骤,例如HitLastTwo
        for (RuleEnum rule : rules) {
            // 这一步的序号对6取余后是5
            if (rule.ordinal() % 6 == 5) {
                // 如果最后一步已经填充,说明最后一步是已经定死的步骤,不可以更改,如果能进入这里的循环并且两个位置都已经填满,说明该锻造配方无法完成
                // 所以当最后一步已经填充,就将倒数第二步填充为当前步骤,否则填充最后一步
                if (flag[0]) {
                    lastOperations[1] = Util.operationsTfc.get(rule.Action);
                    flag[1] = true;
                } else {
                    lastOperations[0] = Util.operationsTfc.get(rule.Action);
                    flag[0] = true;
                }
            }
        }
        // 第三次遍历,填充可以位于倒数第二步和倒数第三步的步骤,例如HitNotLast
        for (RuleEnum rule : rules) {
            // 这一步的序号对6取余后是0,因为Any的序号是0,所以这里要判断当前步骤不是Any
            if (rule.ordinal() != 0 && rule.ordinal() % 6 == 0) {
                // 这里和上面一样,因为这两个循环填充的步骤分别适用于非倒数第三步和非最后一步,因此填充顺序分别是01和21,这样能保证每一步都成功填充
                if (flag[2]) {
                    lastOperations[1] = Util.operationsTfc.get(rule.Action);
                    flag[1] = true;
                } else {
                    lastOperations[2] = Util.operationsTfc.get(rule.Action);
                    flag[2] = true;
                }
            }
        }
        // 最后一次遍历,填充剩余的步骤,这里的步骤是可以位于任意位置的步骤,例如BendAny
        for (RuleEnum rule : rules) {
            // 这个步骤的序号对6取余后是1
            if (rule.ordinal() % 6 == 1) {
                // 遍历lastOperations,如果有空位就填充,并且因为锻造需求里每步出现一次,所以只填充一次,跳出大循环
                for (int i = 0; i < 3; i++) {
                    if (!flag[i]) {
                        lastOperations[i] = Util.operationsTfc.get(rule.Action);
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

    private void drawbox(int x, int y) {
        // Load the texture from the resource file
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(res);

        // Enable texture rendering
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);

        // Set the color to white to avoid tinting the texture
        GL11.glColor4f(1, 1, 1, 1);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        // Calculate the texture coordinates based on the current frame
        int frame = (int) ((Minecraft.getSystemTime() / ConfigFile.highlightStepCooldown) % ConfigFile.totalFrames); // Example
                                                                                                                     // frame
                                                                                                                     // calculation
        float uMin = ((int) (frame % ConfigFile.framesPerRow)) / (float) ConfigFile.framesPerRow;
        float vMin = ((int) (frame / ConfigFile.framesPerRow)) / (float) ConfigFile.framesPerColumn;
        float uMax = uMin + 1.0f / ConfigFile.framesPerRow;
        float vMax = vMin + 1.0f / ConfigFile.framesPerColumn;

        // Define the vertices with texture coordinates
        tessellator.addVertexWithUV(
            x - (double) (ConfigFile.textureWidth - 16) / 2,
            y + 16 + (double) (ConfigFile.textureHeight - 16) / 2,
            100,
            uMin,
            vMax);
        tessellator.addVertexWithUV(
            x + 16 + (double) (ConfigFile.textureWidth - 16) / 2,
            y + 16 + (double) (ConfigFile.textureHeight - 16) / 2,
            100,
            uMax,
            vMax);
        tessellator.addVertexWithUV(
            x + 16 + (double) (ConfigFile.textureWidth - 16) / 2,
            y - (double) (ConfigFile.textureHeight - 16) / 2,
            100,
            uMax,
            vMin);
        tessellator.addVertexWithUV(
            x - (double) (ConfigFile.textureWidth - 16) / 2,
            y - (double) (ConfigFile.textureHeight - 16) / 2,
            100,
            uMin,
            vMin);

        tessellator.draw();

        // Disable texture rendering
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
}
