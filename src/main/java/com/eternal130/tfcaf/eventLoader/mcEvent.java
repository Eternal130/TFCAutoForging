package com.eternal130.tfcaf.eventLoader;

import static com.eternal130.tfcaf.config.ConfigFile.enableAutoForging;
import static com.eternal130.tfcaf.config.ConfigFile.enableForgingTip;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import com.bioxx.tfc.GUI.GuiAnvil;
import com.bioxx.tfc.GUI.GuiContainerTFC;
import com.bioxx.tfc.TileEntities.TEAnvil;
import com.bioxx.tfc.api.Crafting.AnvilManager;
import com.bioxx.tfc.api.Crafting.PlanRecipe;
import com.bioxx.tfc.api.Enums.RuleEnum;
import com.eternal130.tfcaf.TFCAutoForging;
import com.eternal130.tfcaf.Util;
import com.eternal130.tfcaf.config.ConfigFile;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class mcEvent {

    public mcEvent() {
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
            if (event.gui instanceof GuiAnvil) {
                // 检测当前gui是否是铁砧gui或者是tfcquickpocket替换后的铁砧gui
                TEAnvil anvilTE = ((GuiAnvil) event.gui).anvilTE;
                if (enableAutoForging || enableForgingTip) {
                    // 当锻造提示功能和自动锻造功能有一个开启时就计算下一步锻造步骤
                    // 当前锻造数值,此值在选择任意锻造操作时改变
                    int currentPoint = anvilTE.getItemCraftingValue();
                    // 目标锻造数值,此值由世界种子和锻造配方唯一指定,当当前锻造数值等于目标锻造数值,并且最后三步满足锻造要求时,锻造完成
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
                        GL11.glColor4f(0, 0, 1, 1);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glDisable(GL11.GL_LIGHTING);
                        Tessellator tessellator = Tessellator.instance;
                        tessellator.startDrawing(GL11.GL_LINE_LOOP);
                        tessellator.addVertex(x, y, 100);
                        tessellator.addVertex(x, y + 16, 100);
                        tessellator.addVertex(x + 16, y + 16, 100);
                        tessellator.addVertex(x + 16, y, 100);
                        tessellator.draw();
                        GL11.glEnable(GL11.GL_LIGHTING);
                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                        GL11.glColor4f(1, 1, 1, 1);
                    }
                    // 当开启自动锻造功能并且计时器为0时
                    if (enableAutoForging && TFCAutoForging.timer == 0) {
                        // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":敲击!");
                        // 重置计时器的值,可以在配置文件中修改,配置文件可以在游戏中动态修改
                        TFCAutoForging.timer = (short) ConfigFile.autoForgingCooldown;
                        // 获取按钮列表,触发按按钮事件需要这个参数
                        List<GuiButton> buttonlist = getButtonList((GuiContainerTFC) event.gui);
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
                        Method actionPerformed = event.gui.getClass()
                            .getDeclaredMethod("func_146284_a", GuiButton.class);
                        // 设置权限为public
                        actionPerformed.setAccessible(true);
                        // 调用方法,因为按钮索引和锻造步骤索引以及用于计算的operations索引都不同,所以前两个都经过映射后填入
                        actionPerformed
                            .invoke(event.gui, buttonlist.get(7 - Util.buttonMapping.get(offsetNextOperation)));

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
            if (rule.Action != 0 && rule.ordinal() % 6 == 0) {
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
        o: for (RuleEnum rule : rules) {
            // 这个步骤的序号对6取余后是1
            if (rule.ordinal() % 6 == 1) {
                // 遍历lastOperations,如果有空位就填充,并且因为锻造需求里每步出现一次,所以只填充一次,跳出大循环
                for (int i = 0; i < 3; i++) {
                    if (!flag[i]) {
                        lastOperations[i] = Util.operationsTfc.get(rule.Action);
                        flag[i] = true;
                        break o;
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
        // 原代码，只能匹配正常顺序的锻造要求，其他非顺序但可以完成的配方不能匹配，因此上面换了另一种填充方法
        // for (int i = 0; i < 3; i++) {
        // switch (rules[i].Action) {
        // case -1:
        // case 3:
        // lastOperations[i] = 4;
        // break;
        // case 0:
        // lastOperations[i] = 3;
        // break;
        // case 1:
        // lastOperations[i] = 0;
        // break;
        // case 4:
        // lastOperations[i] = 5;
        // break;
        // case 5:
        // lastOperations[i] = 6;
        // break;
        // case 6:
        // lastOperations[i] = 7;
        // break;
        // }
        // }
        return lastOperations;
    }
}
