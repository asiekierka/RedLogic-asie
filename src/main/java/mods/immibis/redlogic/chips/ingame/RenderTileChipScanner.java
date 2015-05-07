package mods.immibis.redlogic.chips.ingame;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class RenderTileChipScanner extends TileEntitySpecialRenderer {
	@Override
	public void renderTileEntityAt(TileEntity te_, double x, double y, double z, float partialTick) {
		TileChipScanner te = (TileChipScanner)te_;
		
		if(te.scanTicks < 0)
			return;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glPushMatrix();
		GL11.glDepthMask(false);
		GL11.glTranslated(x, y, z);
		GL11.glTranslated(-te.xCoord, -te.yCoord, -te.zCoord);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(1, 0, 0, 1);
		
		double cur = (te.scanDir == 0 ? te.crMinX : te.scanDir == 1 ? te.crMinY : te.crMinZ) + (te.scanTicks + partialTick) / TileChipScanner.NORMAL_TICKS_PER_SCAN_BLOCK;
		
		switch(te.scanDir) {
		case 0: // X
			GL11.glVertex3d(cur, te.crMinY, te.crMinZ);
			GL11.glVertex3d(cur, te.crMaxY+1, te.crMinZ);
			GL11.glVertex3d(cur, te.crMaxY+1, te.crMaxZ+1);
			GL11.glVertex3d(cur, te.crMinY, te.crMaxZ+1);
			break;
		case 1: // Y
			GL11.glVertex3d(te.crMinX, cur, te.crMinZ);
			GL11.glVertex3d(te.crMinX, cur, te.crMaxZ+1);
			GL11.glVertex3d(te.crMaxX+1, cur, te.crMaxZ+1);
			GL11.glVertex3d(te.crMaxX+1, cur, te.crMinZ);
			break;
		case 2: // Z
			GL11.glVertex3d(te.crMinX, te.crMinY, cur);
			GL11.glVertex3d(te.crMinX, te.crMaxY+1, cur);
			GL11.glVertex3d(te.crMaxX+1, te.crMaxY+1, cur);
			GL11.glVertex3d(te.crMaxX+1, te.crMinY, cur);
			break;
		}
		
		GL11.glEnd();
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(true);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		// GL_BLEND?
	}
}
