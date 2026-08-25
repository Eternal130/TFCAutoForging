package com.eternal130.tfcaf.eventLoader;

import static com.eternal130.tfcaf.config.ConfigFile.enableAutoForging;
import static com.eternal130.tfcaf.config.ConfigFile.enableForgingTip;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.eternal130.tfcaf.KeyBind;
import net.dries007.tfc.api.capability.forge.CapabilityForgeable;
import net.dries007.tfc.api.capability.forge.IForgeable;
import net.dries007.tfc.api.recipes.anvil.AnvilRecipe;
import net.dries007.tfc.client.gui.GuiAnvilPlan;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.ItemStackHandler;
import org.lwjgl.opengl.GL11;

import com.eternal130.tfcaf.TFCAutoForging;
import com.eternal130.tfcaf.Util;
import com.eternal130.tfcaf.config.ConfigFile;

public class mcEvent {

    static ResourceLocation res = new ResourceLocation("tfcaf", "textures/gui/highlight_step.png");// 锻造提示的纹理
    static boolean wasInAnvilGui = false; // 跟踪上一次是否在铁砧GUI中
    // 本件锻造任务开始时输入槽物品的标识(item的数字id),用于检测输入槽物品被替换
    static int jobInputItemId = -1;
    // 已发出本件最后一步敲击,等待服务器确认完工(输入槽物品被替换)后停机
    static boolean finalStrikeSent = false;
    // 输入槽上一帧为空,用于检测"取出再放回"(物品id不变,只能靠空->有物品的变化识别玩家意图)
    static boolean slotWasEmpty = false;
    // 离开过砧GUI(关闭/切到方案界面等),重开时视为玩家明确的继续信号,用于解除完工停机
    static boolean wasGuiChangedSinceLastDraw = false;

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
            boolean isInAnvilGui = event.getGui() instanceof GuiAnvilTFC;
            // 检测GUI关闭：上一次在铁砧GUI中，当前不在(方案界面除外,那属于同一工作流),重置状态
            if (wasInAnvilGui && !isInAnvilGui && !(event.getGui() instanceof GuiAnvilPlan)) {
                TFCAutoForging.isWaitingForServer = false;
                TFCAutoForging.waitTimeout = 0;
                TFCAutoForging.lastWorkValue = -1;
                TFCAutoForging.isJobDone = false;
                jobInputItemId = -1;
                finalStrikeSent = false;
            }
            if (wasInAnvilGui != isInAnvilGui) {
                wasGuiChangedSinceLastDraw = true;
            }
            wasInAnvilGui = isInAnvilGui;
            if (isInAnvilGui) {
                // 检测当前gui是否是砧gui
                TEAnvilTFC anvilTE = getTEAnvilTFC((GuiAnvilTFC) event.getGui());
                ItemStackHandler inventory = getInventory(anvilTE);
                ItemStack inputStack = inventory.getStackInSlot(0);

                // 取出再放回(物品id不变,但经历了空->有物品)或重开GUI/选完方案回到砧GUI,
                // 都视为玩家明确的继续信号,解除完工停机
                if (slotWasEmpty && !inputStack.isEmpty()
                    || (wasGuiChangedSinceLastDraw && TFCAutoForging.isJobDone)) {
                    TFCAutoForging.isJobDone = false;
                }
                slotWasEmpty = inputStack.isEmpty();
                wasGuiChangedSinceLastDraw = false;

                // 完工确认:发出最后一步后输入槽物品被替换,说明服务器已产出成品放回输入槽,本件完工停机
                // 停机的同时把基线更新为产物的id,这样产物继续留在槽里不会被误判为玩家换料
                if (finalStrikeSent && !inputStack.isEmpty()
                    && Item.getIdFromItem(inputStack.getItem()) != jobInputItemId) {
                    TFCAutoForging.isJobDone = true;
                    finalStrikeSent = false;
                    jobInputItemId = Item.getIdFromItem(inputStack.getItem());
                } else if (inputStack.isEmpty()) {
                    // 输入槽被清空(玩家取走产物或原料)或容器同步抖动(槽位短暂为空),
                    // 保留基线:同步抖动后同id物品回来时不会触发换料逻辑,避免误清状态
                } else if (jobInputItemId == -1) {
                    jobInputItemId = Item.getIdFromItem(inputStack.getItem());
                } else if (Item.getIdFromItem(inputStack.getItem()) != jobInputItemId) {
                    // 槽内物品相对基线发生变化且不是完工顶替,是玩家主动换料,视为开始新的一件,解除停机
                    TFCAutoForging.isJobDone = false;
                    jobInputItemId = Item.getIdFromItem(inputStack.getItem());
                }

                if (enableAutoForging || enableForgingTip) {
                    // 当锻造提示功能和自动锻造功能有一个开启时就计算下一步锻造步骤
                    // 当前锻造数值,此值在选择任意锻造操作时改变
                    int currentPoint = anvilTE.getWorkingProgress();
                    // 目标锻造数值,此值由世界种子和锻造配方唯一指定,当当前锻造数值等于目标锻造数值,并且最后三步满足锻造要求时,锻造完成
                    int targetPoint = anvilTE.getWorkingTarget();

                    // 服务器响应检测
                    if (TFCAutoForging.isWaitingForServer) {
                        // 如果当前数值与上次记录的点击前数值不同，说明服务器已更新进度
                        if (currentPoint != TFCAutoForging.lastWorkValue) {
                            TFCAutoForging.isWaitingForServer = false;
                        } else if (TFCAutoForging.waitTimeout == 0) {
                            // 超时仍未收到服务器响应,锻造数值同步异常,
                            // 强制解除等待状态,让下一次循环重试,防止自动锻造卡死
                            TFCAutoForging.isWaitingForServer = false;
                        }
                    }

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
                    int[] lastOperations = getRules(AnvilRecipe.getRules());
                    for (int i = 0; i < 3; i++) {
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
                        drawbox(x, y);
                    }
                    // 当开启自动锻造功能并且计时器为0时,且服务器已响应（不在等待状态）时才执行点击
                    // 完工后停机,直到玩家重开GUI或换料,防止服务器自动重选配方后对产物继续开工
                    if (enableAutoForging && TFCAutoForging.timer == 0
                        && !TFCAutoForging.isWaitingForServer
                        && !TFCAutoForging.isJobDone) {
                        ItemStack stack = inventory.getStackInSlot(0);
                        IForgeable cap = stack.getCapability(CapabilityForgeable.FORGEABLE_CAPABILITY, null);
                        // 温度不够时不进行锻造
                        if (cap == null || !cap.isWorkable())
                            return;
                        // 记录当前数值，并标记为"正在等待服务器响应"
                        TFCAutoForging.lastWorkValue = currentPoint;
                        TFCAutoForging.isWaitingForServer = true;
                        TFCAutoForging.waitTimeout = TFCAutoForging.WAIT_TIMEOUT_TICKS;
                        // 本次敲击为本件最后一步时置标志,等待服务器确认完工后停机
                        finalStrikeSent = Util.isFinalStep(
                            targetPoint - currentPoint - ruleOffset,
                            lastOperations,
                            steps);
                        // TFCAutoForging.logger.info(TFCAutoForging.MODID + ":敲击!");
                        // 重置计时器的值,可以在配置文件中修改,配置文件可以在游戏中动态修改
                        TFCAutoForging.timer = (short) ConfigFile.autoForgingCooldown;
                        // 获取按钮列表,触发按按钮事件需要这个参数
                        List<GuiButton> buttonlist = getButtonList((GuiContainerTFC) event.getGui());
                        // TFCAutoForging.LOG.info("待点按钮id {},待点按钮索引 {},总按钮数 {}",
                        // buttonlist.get(Util.buttonMapping.get(offsetNextOperation)),Util.buttonMapping.get(offsetNextOperation)
                        // , buttonlist);
                        // 因为这个方法是protected权限,因此使用反射来调用,该方法用于处理按钮点击,下面的name为该方法的混淆名,该名被mcp翻译了,反编译代码中看不到正常名
                        // 使用getDeclaredMethods()方法可以获取所有方法的混淆名,顺序和反编译代码中的相同,因此很好找
                        Method actionPerformed = event.getGui().getClass()
                                .getDeclaredMethod("func_146284_a", GuiButton.class);
                        // 设置权限为public
                        actionPerformed.setAccessible(true);
                        // 调用方法,因为按钮索引和用于计算的operations索引都不同,所以经过映射后填入
                        actionPerformed
                                .invoke(event.getGui(), buttonlist.get(Util.buttonMapping.get(offsetNextOperation)));
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
        // ClientTickEvent每tick触发两次(START和END阶段),只在START阶段计时,否则timer和waitTimeout会以2倍速递减
        if (event.phase != TickEvent.Phase.START) {
            return;
        }
        // TFCAutoForging.logger.info(TFCAutoForging.MODID + ":tick事件");
        // 设置计时器的值,大于0时每tick-1
        if (TFCAutoForging.timer > 0) {
            // TFCAutoForging.logger.info(TFCAutoForging.MODID + ":重置计时器");
            TFCAutoForging.timer--;
        }
        if (TFCAutoForging.isWaitingForServer && TFCAutoForging.waitTimeout > 0) {
            TFCAutoForging.waitTimeout--;
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
            // 切换时重置状态，防止卡死
            TFCAutoForging.isWaitingForServer = false;
            TFCAutoForging.waitTimeout = 0;
            EntityPlayer player = Minecraft.getMinecraft().player;
            // 在游戏中提示当前值
            player.sendMessage(new TextComponentTranslation("key.eternal130.switchAutoForging.info", ConfigFile.enableAutoForging));
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
        Field fields = gui.getClass().getSuperclass().getDeclaredField("tile");
        fields.setAccessible(true);
        return (TEAnvilTFC) fields.get(gui);
    }
    private ItemStackHandler getInventory(TEAnvilTFC te) throws NoSuchFieldException, IllegalAccessException {
        Field fields = te.getClass().getSuperclass().getDeclaredField("inventory");
        fields.setAccessible(true);
        return (ItemStackHandler) fields.get(te);
    }
    private int[] getRules(ForgeRule[] rules) {
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

    private void drawbox(int x, int y) {
        // Load the texture from the resource file
        Minecraft.getMinecraft()
                .getTextureManager()
                .bindTexture(res);

        // Enable texture rendering
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();

        // Set the color to white to avoid tinting the texture
        GlStateManager.color(1, 1, 1, 1);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // Calculate the texture coordinates based on the current frame
        int frame = (int) ((Minecraft.getSystemTime() / ConfigFile.highlightStepCooldown) % ConfigFile.totalFrames); // Example
        // frame
        // calculation
        float uMin = ((int) (frame % ConfigFile.framesPerRow)) / (float) ConfigFile.framesPerRow;
        float vMin = ((int) (frame / ConfigFile.framesPerRow)) / (float) ConfigFile.framesPerColumn;
        float uMax = uMin + 1.0f / ConfigFile.framesPerRow;
        float vMax = vMin + 1.0f / ConfigFile.framesPerColumn;

        // Define the vertices with texture coordinates
        bufferBuilder.pos(x - (double) (ConfigFile.textureWidth - 16) / 2, y + 16 + (double) (ConfigFile.textureHeight - 16) / 2, 100).tex(uMin, vMax).endVertex();
        bufferBuilder.pos(x + 16 + (double) (ConfigFile.textureWidth - 16) / 2, y + 16 + (double) (ConfigFile.textureHeight - 16) / 2, 100).tex(uMax, vMax).endVertex();
        bufferBuilder.pos(x + 16 + (double) (ConfigFile.textureWidth - 16) / 2, y - (double) (ConfigFile.textureHeight - 16) / 2, 100).tex(uMax, vMin).endVertex();
        bufferBuilder.pos(x - (double) (ConfigFile.textureWidth - 16) / 2, y - (double) (ConfigFile.textureHeight - 16) / 2, 100).tex(uMin, vMin).endVertex();

        tessellator.draw();

// Disable texture rendering
        GlStateManager.enableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
    }
}
