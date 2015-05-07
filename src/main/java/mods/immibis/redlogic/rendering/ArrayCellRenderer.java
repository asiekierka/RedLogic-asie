package mods.immibis.redlogic.rendering;

import mods.immibis.core.RenderUtilsIC;
import mods.immibis.core.api.porting.PortableBlockRenderer;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.RotatedTessellator;
import mods.immibis.redlogic.array.ArrayCellBlock;
import mods.immibis.redlogic.array.ArrayCellTile;
import mods.immibis.redlogic.array.ArrayCellType;
import mods.immibis.redlogic.gates.GateBlock;
import mods.immibis.redlogic.wires.PlainRedAlloyTile;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ArrayCellRenderer implements PortableBlockRenderer {
	
	public static final ArrayCellRenderer instance = new ArrayCellRenderer();
	private RotatedTessellator rt = new RotatedTessellator();
	
	@Override
	public boolean renderWorldBlock(RenderBlocks render, IBlockAccess world, int x, int y, int z, Block block, int model) {
		return false;
	}
	
	public boolean renderActualBlock(RenderBlocks render, IBlockAccess world, int x, int y, int z, Block block, int model) {
		ArrayCellTile te = (ArrayCellTile)world.getTileEntity(x, y, z);
		int side = te.getSide();
		int front = te.getFront();
		ArrayCellType type = te.getType();
		
		render(render, side, front, type, false, x, y, z, te.getStrengthLR(), te.getStrengthFB());
		
		return true;
	}
	
	private void render(RenderBlocks render, int side, int front, ArrayCellType type, boolean asItem, double x, double y, double z, int strengthLR, int strengthFB) {
		
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		
		
		if(side < 0 || side > 5 || front < 0 || front > 5) {
			// invalid orientation, make it obvious
			side = front = 0;
		}
		
		switch(side) {
		case Dir.NX: render.setRenderBounds(0, 0, 0, GateBlock.THICKNESS, 1, 1); break;
		case Dir.NY: render.setRenderBounds(0, 0, 0, 1, GateBlock.THICKNESS, 1); break;
		case Dir.NZ: render.setRenderBounds(0, 0, 0, 1, 1, GateBlock.THICKNESS); break;
		case Dir.PX: render.setRenderBounds(1 - GateBlock.THICKNESS, 0, 0, 1, 1, 1); break;
		case Dir.PY: render.setRenderBounds(0, 1 - GateBlock.THICKNESS, 0, 1, 1, 1); break;
		case Dir.PZ: render.setRenderBounds(0, 0, 1 - GateBlock.THICKNESS, 1, 1, 1); break;
		}
		
		rt.base = Tessellator.instance;
		rt.front = front;
		rt.side = side;
		rt.x = x;
		rt.y = y;
		rt.z = z;
		rt.flipped = false;
		rt.setColour(0xFFFFFF);
		rt.useFaceShading = true;
		ArrayCellBlock.renderSide = 63;
		ArrayCellBlock.textureOverride = null;
		ArrayCellBlock.colourOverride = -1;
		
		if((side & 6) == (front & 6)) {
			// invalid orientation, make it obvious
			render.setRenderBounds(0, 0, 0, 1, 1, 1);
		}
		
		
		if(!asItem && render.hasOverrideBlockTexture()) {
			render.renderStandardBlock(RedLogicMod.arrayCells, floor(x), floor(y), floor(z));
			return;
		}
		
		if(asItem)
			rt.base.startDrawingQuads();
		
		ArrayCellBlock.renderSide = 63 & ~(1 << (side ^ 1));
		if(asItem) {
			//rt.base.setBrightness(0x00F000F0);
			rt.setColour(0xFFFFFF);
			rt.setNormal(0, 0, -1);
			render.renderFaceZNeg(RedLogicMod.arrayCells, rt.x, rt.y, rt.z, RedLogicMod.gates.getIcon(0, 0));
            rt.setNormal(0, 0, 1);
            render.renderFaceZPos(RedLogicMod.arrayCells, rt.x, rt.y, rt.z, RedLogicMod.gates.getIcon(0, 0));
            rt.setNormal(-1, 0, 0);
            render.renderFaceXNeg(RedLogicMod.arrayCells, rt.x, rt.y, rt.z, RedLogicMod.gates.getIcon(0, 0));
            rt.setNormal(1, 0, 0);
            render.renderFaceXPos(RedLogicMod.arrayCells, rt.x, rt.y, rt.z, RedLogicMod.gates.getIcon(0, 0));
            rt.setNormal(0, -1, 0);
            render.renderFaceYNeg(RedLogicMod.arrayCells, rt.x, rt.y, rt.z, RedLogicMod.gates.getIcon(0, 0));
		} else
			render.renderStandardBlock(RedLogicMod.arrayCells, floor(x), floor(y), floor(z));
		
		double THICKNESS = ArrayCellTile.getThickness(type);
		
		if(!asItem)
			RenderUtilsIC.setBrightness(render.blockAccess, floor(x), floor(y), floor(z));
		rt.setColour(PlainRedAlloyTile.getVisualWireColour(strengthLR));
		if(!asItem)
			Tessellator.instance.setBrightness(render.blockAccess.getLightBrightnessForSkyBlocks(floor(x), floor(y), floor(z), PlainRedAlloyTile.getVisualEmissiveLightLevel(strengthLR)));
		renderCube(2./16, 4./16, GateBlock.THICKNESS, THICKNESS, 7./16, 9./16, 60, ArrayCellBlock.texWiring);
		renderCube(12./16, 14./16, GateBlock.THICKNESS, THICKNESS, 7./16, 9./16, 60, ArrayCellBlock.texWiring);
		renderCube(2./16, 14./16, THICKNESS-2./16, THICKNESS, 7./16, 9./16, 63, ArrayCellBlock.texWiring);
		
		switch(type) {
		case NULL:
			rt.setColour(PlainRedAlloyTile.getVisualWireColour(strengthLR));
			renderGateSurface(ArrayCellBlock.texNullTopLR);
			
			if(!asItem)
				RenderUtilsIC.setBrightness(render.blockAccess, floor(x), floor(y), floor(z));
			rt.setColour(0xFFFFFF);
			renderGateSurface(ArrayCellBlock.texNullTop);
			
			if(!asItem)
				Tessellator.instance.setBrightness(render.blockAccess.getLightBrightnessForSkyBlocks(floor(x), floor(y), floor(z), PlainRedAlloyTile.getVisualEmissiveLightLevel(strengthFB)));
			rt.setColour(PlainRedAlloyTile.getVisualWireColour(strengthFB));
			renderGateSurface(ArrayCellBlock.texNullTopFB);
			break;
			
		case INVERT:
			rt.setColour(PlainRedAlloyTile.getVisualWireColour(strengthLR));
			renderGateSurface(ArrayCellBlock.texNullTopLR);
			
			if(!asItem)
				RenderUtilsIC.setBrightness(render.blockAccess, floor(x), floor(y), floor(z));
			rt.setColour(0xFFFFFF);
			renderGateSurface(ArrayCellBlock.texNullTop);
			renderTorchAtAngle(render, (strengthFB == 0 ? Blocks.redstone_torch : Blocks.unlit_redstone_torch).getBlockTextureFromSide(0), 0.5, 0.5, 0.125f);
			
			if(!asItem)
				Tessellator.instance.setBrightness(render.blockAccess.getLightBrightnessForSkyBlocks(floor(x), floor(y), floor(z), PlainRedAlloyTile.getVisualEmissiveLightLevel(strengthFB)));
			rt.setColour(PlainRedAlloyTile.getVisualWireColour(strengthFB));
			renderGateSurface(ArrayCellBlock.texNullTopFB);
			break;
			
		case NON_INVERT:
			rt.setColour(PlainRedAlloyTile.getVisualWireColour(strengthLR));
			renderGateSurface(ArrayCellBlock.texNullTopLR);
			
			if(!asItem)
				RenderUtilsIC.setBrightness(render.blockAccess, floor(x), floor(y), floor(z));
			rt.setColour(0xFFFFFF);
			renderGateSurface(ArrayCellBlock.texNonInvertTop);
			renderTorchAtAngle(render, (strengthFB == 0 ? Blocks.redstone_torch : Blocks.unlit_redstone_torch).getBlockTextureFromSide(0), 6./16, 3./16, 0.375f);
			renderTorchAtAngle(render, (strengthFB != 0 ? Blocks.redstone_torch : Blocks.unlit_redstone_torch).getBlockTextureFromSide(0), 6./16, 8./16, 0.125f);
			
			if(!asItem)
				Tessellator.instance.setBrightness(render.blockAccess.getLightBrightnessForSkyBlocks(floor(x), floor(y), floor(z), PlainRedAlloyTile.getVisualEmissiveLightLevel(strengthFB)));
			rt.setColour(PlainRedAlloyTile.getVisualWireColour(strengthFB));
			renderGateSurface(ArrayCellBlock.texNonInvertFB);
			
			if(!asItem)
				Tessellator.instance.setBrightness(render.blockAccess.getLightBrightnessForSkyBlocks(floor(x), floor(y), floor(z), PlainRedAlloyTile.getVisualEmissiveLightLevel(strengthFB == 0 ? 255 : 0)));
			rt.setColour(PlainRedAlloyTile.getVisualWireColour(strengthFB == 0 ? 255 : 0));
			renderGateSurface(ArrayCellBlock.texNonInvertFBInv);
			break;
		}
		
		if(asItem)
			rt.base.draw();
	}
	
	private int floor(double x) {
		return MathHelper.floor_double(x);
	}

	private void renderCube(double x1, double x2, double y1, double y2, double z1, double z2, int faceMask, IIcon tex) {
		rt.setNormal(0.0F, -1.0F, 0.0F);
        rt.addVertexWithUV(x1, y1, z1, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(z1*16));
		rt.addVertexWithUV(x2, y1, z1, tex.getInterpolatedU(x2*16), tex.getInterpolatedV(z1*16));
		rt.addVertexWithUV(x2, y1, z2, tex.getInterpolatedU(x2*16), tex.getInterpolatedV(z2*16));
		rt.addVertexWithUV(x1, y1, z2, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(z2*16));
		
		rt.setNormal(0.0F, 0.0F, -1.0F);
        rt.addVertexWithUV(x2, y2, z1, tex.getInterpolatedU(x2*16), tex.getInterpolatedV(y2*16));
		rt.addVertexWithUV(x2, y1, z1, tex.getInterpolatedU(x2*16), tex.getInterpolatedV(y1*16));
		rt.addVertexWithUV(x1, y1, z1, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(y1*16));
		rt.addVertexWithUV(x1, y2, z1, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(y2*16));
		
		rt.setNormal(0.0F, 0.0F, 1.0F);
        rt.addVertexWithUV(x2, y2, z2, tex.getInterpolatedU(x2*16), tex.getInterpolatedV(y2*16));
		rt.addVertexWithUV(x1, y2, z2, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(y2*16));
		rt.addVertexWithUV(x1, y1, z2, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(y1*16));
		rt.addVertexWithUV(x2, y1, z2, tex.getInterpolatedU(x2*16), tex.getInterpolatedV(y1*16));
		
		rt.setNormal(-1.0F, 0.0F, 0.0F);
        rt.addVertexWithUV(x1, y2, z1, tex.getInterpolatedU(y2*16), tex.getInterpolatedV(z1*16));
		rt.addVertexWithUV(x1, y1, z1, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(z1*16));
		rt.addVertexWithUV(x1, y1, z2, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(z2*16));
		rt.addVertexWithUV(x1, y2, z2, tex.getInterpolatedU(y2*16), tex.getInterpolatedV(z2*16));
		
		rt.setNormal(1.0F, 0.0F, 0.0F);
        rt.addVertexWithUV(x2, y2, z1, tex.getInterpolatedU(y2*16), tex.getInterpolatedV(z1*16));
		rt.addVertexWithUV(x2, y2, z2, tex.getInterpolatedU(y2*16), tex.getInterpolatedV(z2*16));
		rt.addVertexWithUV(x2, y1, z2, tex.getInterpolatedU(y1*16), tex.getInterpolatedV(z2*16));
		rt.addVertexWithUV(x2, y1, z1, tex.getInterpolatedU(y1*16), tex.getInterpolatedV(z1*16));
		
		rt.setNormal(0.0F, 1.0F, 0.0F);
        rt.addVertexWithUV(x1, y2, z1, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(z1*16));
		rt.addVertexWithUV(x1, y2, z2, tex.getInterpolatedU(x1*16), tex.getInterpolatedV(z2*16));
		rt.addVertexWithUV(x2, y2, z2, tex.getInterpolatedU(x2*16), tex.getInterpolatedV(z2*16));
		rt.addVertexWithUV(x2, y2, z1, tex.getInterpolatedU(x2*16), tex.getInterpolatedV(z1*16));
	}
	
	private void renderGateSurface(IIcon tex) {
		final double u1 = tex.getMinU();
		final double v1 = tex.getMinV();
		final double u2 = tex.getMaxU();
		final double v2 = v1;
		final double u3 = u2;
		final double v3 = tex.getMaxV();
		final double u4 = u1;
		final double v4 = v3;
		final int x = 0;
		final int z = 0;
		final double y = GateBlock.THICKNESS;
		
		rt.setNormal(0, 1, 0);
		rt.addVertexWithUV(x, y, z, u1, v1);
		rt.addVertexWithUV(x, y, z+1, u4, v4);
		rt.addVertexWithUV(x+1, y, z+1, u3, v3);
		rt.addVertexWithUV(x+1, y, z, u2, v2);
		
	}

	@Override
	public void renderInvBlock(RenderBlocks render, Block block, int meta, int model) {
		if(meta < 0 || meta >= ArrayCellType.VALUES.length)
			return;
		
		render(render, Dir.NY, Dir.NX, ArrayCellType.VALUES[meta], true, -0.5, -0.25, -0.5, 0, 0);
	}
	
	public void renderTorchAtAngle(RenderBlocks render, IIcon texture, double x, double z, float Y_INSET)
    {
        RotatedTessellator var12 = rt;

        if (render.overrideBlockTexture != null)
        {
            texture = render.overrideBlockTexture;
        }

        float var16 = texture.getMinU();
        float var17 = texture.getMaxU();
        float var18 = texture.getMinV();
        float var19 = texture.getInterpolatedV(16 - Y_INSET * 16);
        double var20 = texture.getInterpolatedU(7);
        double var22 = texture.getInterpolatedV(6);
        double var24 = texture.getInterpolatedU(9);
        double var26 = texture.getInterpolatedV(8);
        double var44 = 0.0625D;
        double var46 = 0.625D - Y_INSET;
        
        var12.addVertexWithUV(x - var44, var46, z - var44, var20, var22);
        var12.addVertexWithUV(x - var44, var46, z + var44, var20, var26);
        var12.addVertexWithUV(x + var44, var46, z + var44, var24, var26);
        var12.addVertexWithUV(x + var44, var46, z - var44, var24, var22);
        
        /*
        double var28 = texture.getInterpolatedU(7);
        double var30 = texture.getInterpolatedV(13);
        double var32 = texture.getInterpolatedU(9);
        double var34 = texture.getInterpolatedV(15);
        var12.addVertexWithUV(x + var44, renderBottomY, z - var44, var32, var30);
        var12.addVertexWithUV(x + var44, renderBottomY, z + var44, var32, var34);
        var12.addVertexWithUV(x - var44, renderBottomY, z + var44, var28, var34);
        var12.addVertexWithUV(x - var44, renderBottomY, z - var44, var28, var30);*/
        
        var12.addVertexWithUV(x - var44, 1 - Y_INSET, z - 0.5, (double)var16, (double)var18);
        var12.addVertexWithUV(x - var44, 0, z - 0.5, (double)var16, (double)var19);
        var12.addVertexWithUV(x - var44, 0, z + 0.5, (double)var17, (double)var19);
        var12.addVertexWithUV(x - var44, 1 - Y_INSET, z + 0.5, (double)var17, (double)var18);
        
        var12.addVertexWithUV(x + var44, 1 - Y_INSET, z + 0.5, (double)var16, (double)var18);
        var12.addVertexWithUV(x + var44, 0, z + 0.5, (double)var16, (double)var19);
        var12.addVertexWithUV(x + var44, 0, z - 0.5, (double)var17, (double)var19);
        var12.addVertexWithUV(x + var44, 1 - Y_INSET, z - 0.50, (double)var17, (double)var18);
        
        
        var12.addVertexWithUV(x - 0.5, 1 - Y_INSET, z + var44, (double)var16, (double)var18);
        var12.addVertexWithUV(x - 0.5, 0, z + var44, (double)var16, (double)var19);
        var12.addVertexWithUV(x + 0.5, 0, z + var44, (double)var17, (double)var19);
        var12.addVertexWithUV(x + 0.5, 1 - Y_INSET, z + var44, (double)var17, (double)var18);
        
        var12.addVertexWithUV(x + 0.5, 1 - Y_INSET, z - var44, (double)var16, (double)var18);
        var12.addVertexWithUV(x + 0.5, 0, z - var44, (double)var16, (double)var19);
        var12.addVertexWithUV(x - 0.5, 0, z - var44, (double)var17, (double)var19);
        var12.addVertexWithUV(x - 0.5, 1 - Y_INSET, z - var44, (double)var17, (double)var18);
    }
	
	private void renderGateSurface(Block block, int side, int front, IIcon tex) {
		final double u1 = tex.getMinU();
		final double v1 = tex.getMinV();
		final double u2 = tex.getMaxU();
		final double v2 = v1;
		final double u3 = u2;
		final double v3 = tex.getMaxV();
		final double u4 = u1;
		final double v4 = v3;
		final int x = 0;
		final int z = 0;
		final double y = GateBlock.THICKNESS;
		
		rt.addVertexWithUV(x, y, z, u1, v1);
		rt.addVertexWithUV(x, y, z+1, u4, v4);
		rt.addVertexWithUV(x+1, y, z+1, u3, v3);
		rt.addVertexWithUV(x+1, y, z, u2, v2);
		
	}

	

}
