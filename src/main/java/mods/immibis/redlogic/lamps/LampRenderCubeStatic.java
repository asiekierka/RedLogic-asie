package mods.immibis.redlogic.lamps;

import mods.immibis.core.api.porting.PortableBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public class LampRenderCubeStatic implements PortableBlockRenderer {

	@Override
	public boolean renderWorldBlock(RenderBlocks render, IBlockAccess world, int x, int y, int z, Block block, int model) {
		BlockLampCube.renderingColouredPart = true;
		render.renderStandardBlock(block, x, y, z);
		BlockLampCube.renderingColouredPart = false;
		render.renderStandardBlock(block, x, y, z);
		
		if(((BlockLampCube)block).powered && ((BlockLampCube)block).type != BlockLampCube.LampType.Indicator)
			LampRenderHalo.addLamp(world, x, y, z);
		
		return true;
	}

	@Override
	public void renderInvBlock(RenderBlocks render, Block block, int meta, int model) {
		BlockLampCube.renderType = 0;
		
		BlockLampCube.renderingColouredPart = true;
		render.renderBlockAsItem(block, meta, 1);
		BlockLampCube.renderingColouredPart = false;
		render.renderBlockAsItem(block, meta, 1);
		
		BlockLampCube.renderType = model;
	}
}
