package com.eternal130.tfcaf;

import static com.eternal130.tfcaf.TFCAutoForging.MODID;

import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.GuiScreenEvent;

import org.lwjgl.opengl.GL11;

import com.dunk.tfc.GUI.GuiAnvil;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class eventLoader {

    @SubscribeEvent
    public void operationHighlight(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.gui instanceof GuiAnvil) {
            GuiAnvil gui = (GuiAnvil) event.gui;
            int x = (gui.width - 208) / 2;
            int y = (gui.height - 204) / 2;
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
            TFCAutoForging.LOG.info(MODID + ":绘制成功");
        }

    }
}
