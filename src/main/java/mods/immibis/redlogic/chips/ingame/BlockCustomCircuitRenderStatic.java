package mods.immibis.redlogic.chips.ingame;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import mods.immibis.core.api.porting.PortableBlockRenderer;

public class BlockCustomCircuitRenderStatic implements PortableBlockRenderer {

	private static int[] rotationMapTop = {0, 1, 3, 2};
	
	@Override
	public boolean renderWorldBlock(RenderBlocks render, IBlockAccess world, int x, int y, int z, Block block, int model) {
		int rotation = ((TileCustomCircuit)world.getTileEntity(x, y, z)).getRotation();		
		render.uvRotateTop = rotationMapTop[rotation];
		render.renderStandardBlock(block, x, y, z);
		render.uvRotateTop = 0;
		return true;
	}

	@Override
	public void renderInvBlock(RenderBlocks render, Block block, int meta, int model) {
		BlockCustomCircuit.renderType = 0;
		render.renderBlockAsItem(block, meta, 1);
		BlockCustomCircuit.renderType = model;
	}

}
