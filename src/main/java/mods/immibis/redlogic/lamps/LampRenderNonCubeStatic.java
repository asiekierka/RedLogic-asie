package mods.immibis.redlogic.lamps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import mods.immibis.core.RenderUtilsIC;
import mods.immibis.core.api.porting.PortableBlockRenderer;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.RotatedTessellator;

@SideOnly(Side.CLIENT)
public class LampRenderNonCubeStatic implements PortableBlockRenderer {

	private RotatedTessellator rt = new RotatedTessellator();
	
	private double t;
	private int col, lampcol;
	private TileLampNonCube te;
	
	@Override
	public boolean renderWorldBlock(RenderBlocks render, IBlockAccess world, int x, int y, int z, Block block, int model) {
		TileEntity te_ = world.getTileEntity(x, y, z);
		if(!(te_ instanceof TileLampNonCube))
			return false;
		
		if(render.hasOverrideBlockTexture())
			return true;
		
		te = (TileLampNonCube)te_;
		lampcol = BlockLampCube.COLOURS[te.getColour()];
		if(!te.getIsActive())
			lampcol = BlockLampCube.getOffColour(lampcol);
		
		rt.base = Tessellator.instance;
		rt.side = te.getSide();
		rt.front = (rt.side < 2 ? Dir.NZ : Dir.NY); // arbitrary
		rt.x = x;
		rt.y = y;
		rt.z = z;
		rt.flipped = false;
		t = 0;
		
		RenderUtilsIC.setBrightness(world, x, y, z);
		
		renderModel(te.getModel());
		
		LampRenderHalo.addLamp(render.blockAccess, x, y, z);
		
		return true;
	}
	
	@Override
	public void renderInvBlock(RenderBlocks render, Block block, int meta, int model) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		
		lampcol = BlockLampCube.COLOURS[meta & 15];
		t = 0;
		te = null;
		
		rt.base = Tessellator.instance;
		rt.side = Dir.NY;
		rt.front = Dir.NZ; // arbitrary
		rt.x = -0.5;
		rt.y = -0.5;
		rt.z = -0.5;
		rt.flipped = false;
		
		rt.base.startDrawingQuads();
		renderModel(meta >> 6);
		rt.base.draw();
	}
	
	private void setHaloBBs(AxisAlignedBB a) {
		if(te == null) return;
		if(!te.getIsActive()/* || te.getType() == LampType.Indicator*/) {
			te.haloBBs = null;
			return;
		}
		if(te.haloBBs == null || te.haloBBs.length != 1)
			te.haloBBs = new AxisAlignedBB[1];
		te.haloBBs[0] = a;
	}
	
	private void renderModel(int model) {
		switch(model) {
		case 0: renderStandingCageLamp(); break;
		case 1: renderRP2CageLamp(); break;
		case 2: renderRP2FlatLamp(); break;
		}
	}
	
	private void renderStandingCageLamp() {
		double a1 = 0.25, a2 = 1-a1;
		double b1 = 0.2, b2 = 1-b1;
		double c1 = 0.10, c2 = 1-c1;
		
		a1 = (b1+c1)/2; a2 = (b2+c2)/2;
		
		double height = 0.8;
		t = -0.10/height;
		
		// inner
		rt.useFaceShading = false; rt.base.setColorOpaque_I(lampcol);
		renderBox(0, 0.78,
			a1, a2, a1, a2,
			a1, a2, a1, a2);
		
		/*{
			double _1 = (c1+b1)/2 - 0.01;
			double _2 = (c2+b2)/2 + 0.01;
			renderBox(0.21, 0.49,
				_1, _2, _1, _2,
				_1, _2, _1, _2);
		}*/
		
		// corners
		rt.useFaceShading = true; col = 0x404040;
		renderBox(0, 0.7,
			c1, b1, c1, b1,
			c1, b1, c1, b1);
		renderBox(0, 0.7,
			c1, b1, c2, b2,
			c1, b1, c2, b2);
		renderBox(0, 0.7,
			c2, b2, c1, b1,
			c2, b2, c1, b1);
		renderBox(0, 0.7,
			c2, b2, c2, b2,
			c2, b2, c2, b2);
		
		// top
		//renderBox(c1, c2, 0.7, 0.8, c1, c2);
		renderBox(c1, c2, 0.7, 0.8, b1, c1);
		renderBox(c1, c2, 0.7, 0.8, b2, c2);
		renderBox(b1, c1, 0.7, 0.8, c1, c2);
		renderBox(b2, c2, 0.7, 0.8, c1, c2);
		
		// bars
		final int nbars = 4;
		final double[] barh = {0.1, 0.2, 0.5, 0.6};
		double bw = 0.02;
		double bx1 = (c1 + b1)/2;
		double bx2 = (c2 + b2)/2;
		for(int k = 0; k < nbars; k++) {
			//double by = 0.7 * ((k+1) / (double)(nbars+1));
			double by = barh[k];
			renderBox(bx1, bx2, by-bw, by+bw, bx1-bw, bx1+bw);
			renderBox(bx1, bx2, by-bw, by+bw, bx2-bw, bx2+bw);
			renderBox(bx1-bw, bx1+bw, by-bw, by+bw, bx1, bx2);
			renderBox(bx2-bw, bx2+bw, by-bw, by+bw, bx1, bx2);
		}
	}
	
	private void renderRP2CageLamp() {
		final float px = 1/16f;
		
		// base
		rt.setColour(0x404040); rt.useFaceShading = true;
		renderBox(3*px, 13*px, 0, 1*px, 3*px, 13*px); 
		
		// cage parallel part
		renderBox(4*px, 12*px, 1*px, 14*px, 7*px, 9*px);
		renderBox(7*px, 9*px, 1*px, 14*px, 4*px, 12*px);
		
		// cage rings
		renderBox(4*px, 12*px, 4*px, 6*px, 4*px, 12*px);
		renderBox(4*px, 12*px, 9*px, 11*px, 4*px, 12*px);
		
		// glowy part
		rt.base.setColorOpaque_I(lampcol); rt.useFaceShading = false;
		renderBox(5*px, 11*px, 1*px, 13*px, 5*px, 11*px);
		
		double hs = 0.5/16; // halo size - not equal to cage size, else z-fighting
		setHaloBBs(rt.rotate(5*px-hs, 11*px+hs, 1*px-hs, 13*px+hs, 5*px-hs, 11*px+hs));
	}
	
	private void renderRP2FlatLamp() {
		
		final float px = 1/16f;
		
		// base
		rt.setColour(0x404040); rt.useFaceShading = true;
		renderBox(1*px, 15*px, 0, 2*px, 1*px, 15*px); 
		
		// glowy part
		rt.base.setColorOpaque_I(lampcol); rt.useFaceShading = false;
		renderBox(3*px, 13*px, 2*px, 4*px, 3*px, 13*px);
		
		double hs = 1f/16; // halo size
		setHaloBBs(rt.rotate(3*px-hs, 13*px+hs, 2*px, 4*px+hs, 3*px-hs, 13*px+hs));
	}
	
	private void renderBox(double x1, double x2, double y1, double y2, double z1, double z2) {
		renderBox(y1, y2, x1, x2, z1, z2, x1, x2, z1, z2);
	}
	
	private void renderBox(double y1, double y2, double x1top, double x2top, double z1top, double z2top, double x1bot, double x2bot, double z1bot, double z2bot) {
		double uMin = BlockLampNonCube.iWhite.getMinU();
		double uMax = BlockLampNonCube.iWhite.getMaxU();
		double vMin = BlockLampNonCube.iWhite.getMinV();
		double vMax = BlockLampNonCube.iWhite.getMaxV();
		
		double temp;
		
		//rt.r = ((col >> 16) & 255) / 255f;
		//rt.g = ((col >> 8) & 255) / 255f;
		//rt.b = (col & 255) / 255f;
		
		if(x1top > x2top) {temp = x1top; x1top = x2top; x2top = temp;}
		if(z1top > z2top) {temp = z1top; z1top = z2top; z2top = temp;}
		if(x1bot > x2bot) {temp = x1bot; x1bot = x2bot; x2bot = temp;}
		if(z1bot > z2bot) {temp = z1bot; z1bot = z2bot; z2bot = temp;}
		
		// bottom
		rt.setNormal(0, -1, 0);
		addVertexWithUV(x2bot, y1, z1bot, uMax, vMax);
		addVertexWithUV(x2bot, y1, z2bot, uMax, vMin);
		addVertexWithUV(x1bot, y1, z2bot, uMin, vMin);
		addVertexWithUV(x1bot, y1, z1bot, uMin, vMax);
		
		// sides
		rt.setNormal(0, 0, -1);
		addVertexWithUV(x1bot, y1, z1bot, uMax, vMax);
		addVertexWithUV(x1top, y2, z1top, uMax, vMin);
		addVertexWithUV(x2top, y2, z1top, uMin, vMin);
		addVertexWithUV(x2bot, y1, z1bot, uMin, vMax);
		
		rt.setNormal(0, 0, 1);
		addVertexWithUV(x2top, y2, z2top, uMax, vMin);
		addVertexWithUV(x1top, y2, z2top, uMin, vMin);
		addVertexWithUV(x1bot, y1, z2bot, uMin, vMax);
		addVertexWithUV(x2bot, y1, z2bot, uMax, vMax);
		
		rt.setNormal(-1, 0, 0);
		addVertexWithUV(x1top, y2, z1top, uMin, vMin);
		addVertexWithUV(x1bot, y1, z1bot, uMin, vMax);
		addVertexWithUV(x1bot, y1, z2bot, uMax, vMax);
		addVertexWithUV(x1top, y2, z2top, uMax, vMin);
		
		rt.setNormal(1, 0, 0);
		addVertexWithUV(x2top, y2, z1top, uMax, vMin);
		addVertexWithUV(x2top, y2, z2top, uMin, vMin);
		addVertexWithUV(x2bot, y1, z2bot, uMin, vMax);
		addVertexWithUV(x2bot, y1, z1bot, uMax, vMax);
		
		// top
		rt.setNormal(0, 1, 0);
		addVertexWithUV(x1top, y2, z1top, uMin, vMax);
		addVertexWithUV(x1top, y2, z2top, uMin, vMin);
		addVertexWithUV(x2top, y2, z2top, uMax, vMin);
		addVertexWithUV(x2top, y2, z1top, uMax, vMax);
	}

	private void addVertexWithUV(double x, double y, double z, double u, double v) {
		x += Math.signum(0.5-x)*y*t;
		z += Math.signum(0.5-z)*y*t;
		rt.addVertexWithUV(x, y, z, u, v);
	}

}
