package mods.immibis.redlogic.gates;

import mods.immibis.redlogic.RotatedTessellator;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;


public abstract class GateRendering {
	// Wire colours
	public static final int OFF = 0x400000;
	public static final int ON = 0xFF0000;
	public static final int DISABLED = 0xC0C0C0;
	
	public IIcon torchTexOn;
	public IIcon torchTexOff;
	
	public String[] segmentTex = new String[] {"base"};
	public int[] segmentCol = new int[] {0xFF0000};
	
	// Torch positions are relative to the texture, in pixels
	public float[] torchX = new float[] {};
	public float[] torchY = new float[] {};
	public boolean[] torchState = new boolean[] {};
	
	// Ditto for pointer positions
	public float[] pointerX = new float[] {};
	public float[] pointerY = new float[] {};
	
	public void set(int renderState) {}
	public void setItemRender() {}
	
	public void customRender(RotatedTessellator rt, RenderBlocks render) {}
	
	public IIcon[] segmentIcons = null;
	
	public void loadTextures(IIconRegister register) {
		segmentIcons = new IIcon[segmentTex.length];
		for(int k = 0; k < segmentTex.length; k++)
			segmentIcons[k] = register.registerIcon(ICON_PREFIX + segmentTex[k]);
		
		torchTexOn = Blocks.redstone_torch.getBlockTextureFromSide(0);
		torchTexOff = Blocks.unlit_redstone_torch.getBlockTextureFromSide(0);
	}
	
	public static final String ICON_PREFIX = "redlogic:gate/";
	
	public static class Default extends GateRendering {
		
	}

	protected void renderRaisedSquare(RotatedTessellator rt, RenderBlocks render, IIcon tex, int left, int top, int right, int bottom, int thickness) {
		double minX = left/16., maxX = right/16., minZ = top/16., maxZ = bottom/16.;
		double minY = 1.0/8.0, maxY = minY + thickness/16.0;
		
		double minU = tex.getInterpolatedU(left), maxU = tex.getInterpolatedU(right);
		double minV = tex.getInterpolatedV(top), maxV = tex.getInterpolatedV(bottom);
		
		rt.addVertexWithUV(minX, maxY, maxZ, minU, maxV);
		rt.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV);
		rt.addVertexWithUV(maxX, maxY, minZ, maxU, minV);
		rt.addVertexWithUV(minX, maxY, minZ, minU, minV);
		
		// half an actual texel
		double uOffset = 0.5 / 256; // TODO sheet width
		double vOffset = 0.5 / 256; // TODO sheet height
		
		rt.addVertexWithUV(maxX, maxY, minZ, maxU, minV + vOffset);
		rt.addVertexWithUV(maxX, minY, minZ, maxU, minV + vOffset);
		rt.addVertexWithUV(minX, minY, minZ, minU, minV + vOffset);
		rt.addVertexWithUV(minX, maxY, minZ, minU, minV + vOffset);
		
		rt.addVertexWithUV(minX, maxY, maxZ, minU, maxV - vOffset);
		rt.addVertexWithUV(minX, minY, maxZ, minU, maxV - vOffset);
		rt.addVertexWithUV(maxX, minY, maxZ, maxU, maxV - vOffset);
		rt.addVertexWithUV(maxX, maxY, maxZ, maxU, maxV - vOffset);
		
		rt.addVertexWithUV(minX, maxY, minZ, minU + uOffset, minV);
		rt.addVertexWithUV(minX, minY, minZ, minU + uOffset, minV);
		rt.addVertexWithUV(minX, minY, maxZ, minU + uOffset, maxV);
		rt.addVertexWithUV(minX, maxY, maxZ, minU + uOffset, maxV);
		
		rt.addVertexWithUV(maxX, maxY, maxZ, maxU - uOffset, maxV);
		rt.addVertexWithUV(maxX, minY, maxZ, maxU - uOffset, maxV);
		rt.addVertexWithUV(maxX, minY, minZ, maxU - uOffset, minV);
		rt.addVertexWithUV(maxX, maxY, minZ, maxU - uOffset, minV);
		
	}
}
