package mods.immibis.redlogic;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import mods.immibis.core.api.porting.PortableBlockRenderer;
import mods.immibis.redlogic.chips.ingame.TileChipScanner;

public class RLMachineBlockRenderStatic implements PortableBlockRenderer {

	private static int[] rotationMapTop = {0, 1, 3, 2};
	private static int[] rotationMapBottom = {0, 2, 3, 1};
	
	@Override
	public boolean renderWorldBlock(RenderBlocks render, IBlockAccess world, int x, int y, int z, Block block, int model) {
		int meta = world.getBlockMetadata(x, y, z);
		if(meta == RLMachineBlock.META_CHIP_SCANNER) {
			
			int rotation = ((TileChipScanner)world.getTileEntity(x, y, z)).getRotation();
			
			render.uvRotateTop = rotationMapTop[rotation];
			render.uvRotateBottom = rotationMapBottom[rotation];
			render.renderStandardBlock(block, x, y, z);
			render.uvRotateTop = 0;
			render.uvRotateBottom = 0;
			
		} else
			render.renderStandardBlock(block, x, y, z);
		return true;
	}

	@Override
	public void renderInvBlock(RenderBlocks render, Block block, int meta, int model) {
		RLMachineBlock.renderType = 0;
		render.renderBlockAsItem(block, meta, 1);
		RLMachineBlock.renderType = model;
	}

}
