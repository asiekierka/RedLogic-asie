package mods.immibis.redlogic.chips.ingame;

import org.lwjgl.opengl.GL11;

import mods.immibis.core.RenderUtilsIC;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.RotatedTessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RenderTileChipCompiler extends TileEntitySpecialRenderer {
	
	RotatedTessellator rt = new RotatedTessellator();
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f) {
		int visualState = ((TileChipCompiler)tileentity).visualState;
		
		if(visualState == 0)
			return;
		
		rt.base = Tessellator.instance;
		rt.flipped = false;
		rt.front = ((TileChipCompiler)tileentity).front ^ 1;
		rt.side = Dir.NY;
		rt.x = d0;
		rt.y = d1;
		rt.z = d2;
		
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1, 1, 1);
		RenderUtilsIC.setBrightness(tileentity.getWorldObj(), tileentity.xCoord, tileentity.yCoord, tileentity.zCoord);
		
		if((visualState & 1) != 0) {
			// schematic is inserted
			
			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("redlogic", "textures/items/schematic.png"));
			
			double y = 13.5 / 16;
			double minx = 2.0 / 16;
			double maxx = 14.0 / 16;
			
			double minz = 0.5;
			double maxz = minz + (maxx - minx);
			
			rt.base.startDrawingQuads();
			rt.addVertexWithUV(maxx, y, maxz, 0, 0);
			rt.addVertexWithUV(maxx, y, minz, 1, 0);
			rt.addVertexWithUV(minx, y, minz, 1, 1);
			rt.addVertexWithUV(minx, y, maxz, 0, 1);
			rt.base.draw();
		}
		
		if((visualState & 2) != 0) {
			// photomask is inserted
			
			Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("redlogic", "textures/items/photomask.png"));
			
			double y = 11.5 / 16;
			double minx = 2.0 / 16;
			double maxx = 14.0 / 16;
			
			double minz = 0.5;
			double maxz = minz + (maxx - minx);
			
			rt.base.startDrawingQuads();
			rt.addVertexWithUV(maxx, y, maxz, 0, 0);
			rt.addVertexWithUV(maxx, y, minz, 1, 0);
			rt.addVertexWithUV(minx, y, minz, 1, 1);
			rt.addVertexWithUV(minx, y, maxz, 0, 1);
			rt.base.draw();
		}
		
		GL11.glEnable(GL11.GL_CULL_FACE);
	}
}
