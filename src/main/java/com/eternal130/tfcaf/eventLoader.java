package com.eternal130.tfcaf;

import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import com.dunk.tfc.GUI.GuiAnvil;
import com.dunk.tfc.GUI.GuiContainerTFC;
import com.dunk.tfc.TileEntities.TEAnvil;
import com.dunk.tfc.api.Crafting.AnvilManager;
import com.dunk.tfc.api.Crafting.PlanRecipe;
import com.dunk.tfc.api.Enums.RuleEnum;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import tfcquickpockets.ClientStuff;

public class eventLoader {

    static boolean hasTFCQuickPockets = false;

    public eventLoader() {
        checkPockets();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void operationHighlight(GuiScreenEvent.DrawScreenEvent.Post event) {
        // TFCAutoForging.LOG.info(event.gui.toString());
        try {
            if (event.gui instanceof GuiAnvil
                || (hasTFCQuickPockets && event.gui instanceof ClientStuff.AnvilGUIWithFastBagAccess)) {
                TEAnvil anvilTE = getAnvilTE((GuiContainerTFC) event.gui);
                int currentPoint = anvilTE.getItemCraftingValue();
                int targetPoint = anvilTE.getCraftingValue();
                if (targetPoint == 0) {
                    return;
                }
                PlanRecipe p = AnvilManager.getInstance()
                    .getPlan(anvilTE.craftingPlan);
                if (p == null) {
                    return;
                }
                int ruleOffset = 0;
                RuleEnum[] rules = p.rules;
                int[] itemCraftingRules = anvilTE.itemCraftingRules;
                int[] itemRules = anvilTE.getItemRules();
                int[] lastOperations = new int[3];
                for (int i = 0; i < 3; i++) {
                    switch (rules[i].Action) {
                        case -1:
                        case 3:
                            lastOperations[i] = 4;
                            break;
                        case 0:
                            lastOperations[i] = 3;
                            break;
                        case 1:
                            lastOperations[i] = 0;
                            break;
                        case 4:
                            lastOperations[i] = 5;
                            break;
                        case 5:
                            lastOperations[i] = 6;
                            break;
                        case 6:
                            lastOperations[i] = 7;
                            break;
                    }
                    ruleOffset += Util.operations[lastOperations[i]];
                }
                // GuiAnvil中drawItemRulesImages方法绘制最后三步步骤,drawRulesImages方法绘制锻造要求
                // drawItemRulesImages中rules保存锻造要求,索引从左到右分别为012,itemRules保存最后三步,索引从左到右分别为012
                // itemRules中,0对应-3,-6,-9;1对应-15;3对应2;4对应7;5对应13;6对应16
                int x = (event.gui.width - 208) / 2;
                int y = (event.gui.height - 204) / 2;
                int offsetNextOperation = Util
                    .nextOperationOffset(targetPoint - currentPoint - ruleOffset, lastOperations, itemRules);
                // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":偏移值{}", ruleOffset);
                // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":锻造要求{},{},{}", lastOperations[0], lastOperations[1], lastOperations[2]);
                // TFCAutoForging.LOG
                // .info(TFCAutoForging.MODID + ":{}:{}", targetPoint - currentPoint, offsetNextOperation);
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
                }
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
                // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":绘制成功");
            }
        } catch (Throwable ignored) {}
    }

    public void checkPockets() {
        hasTFCQuickPockets = Loader.isModLoaded("tfcquickpockets");
    }

    private TEAnvil getAnvilTE(GuiContainerTFC gui) {
        if (hasTFCQuickPockets) {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":存在quickpockets");
            return ((ClientStuff.AnvilGUIWithFastBagAccess) gui).anvilTE;
        } else {
            // TFCAutoForging.LOG.info(TFCAutoForging.MODID + ":不存在quickpockets");
            return ((GuiAnvil) gui).anvilTE;
        }
    }
}
