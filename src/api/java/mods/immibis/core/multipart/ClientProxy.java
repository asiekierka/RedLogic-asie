package mods.immibis.core.multipart;

import java.util.Map;

import mods.immibis.core.ImmibisCore;
import mods.immibis.core.api.multipart.ICoverSystem;
import mods.immibis.core.api.multipart.IMultipartTile;
import mods.immibis.core.api.multipart.IPartContainer;
import mods.immibis.core.api.multipart.IPartContainer3;
import mods.immibis.core.api.multipart.PartCoordinates;
import mods.immibis.core.api.multipart.util.BlockMultipartBase;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy {
	
	@SuppressWarnings("deprecation")
	private static boolean callRender(IPartContainer partContainer, RenderBlocks rb) {
		if(partContainer instanceof IPartContainer3)
			return ((IPartContainer3)partContainer).render2(rb);
		else {
			partContainer.render(rb);
			return true;
		}
	}
	
	@SuppressWarnings("deprecation")
	private static boolean callRenderPart(IPartContainer partContainer, RenderBlocks rb, int part) {
		if(partContainer instanceof IPartContainer3)
			return ((IPartContainer3)partContainer).renderPart2(rb, part);
		else {
			partContainer.renderPart(rb, part);
			return true;
		}
	}
	
	// Returns true if anything was rendered
	public static boolean RenderWorldBlock(RenderBlocks render, IBlockAccess world, int x, int y, int z, Block block) {
		TileEntity te = world.getTileEntity(x, y, z);
		
		if(!(te instanceof IMultipartTile)) {
			return ImmibisCore.multipartSystem.renderBlockInWorldUsingOverriddenRenderType(render, block, x, y, z);
		}
		
		IMultipartTile imt = (IMultipartTile)te;
		
		ICoverSystem ci = imt.getCoverSystem();
		
		boolean damageLayer = render.overrideBlockTexture != null;
		
		boolean returnVal = false;
		
		if(!damageLayer) {
			returnVal |= callRender(imt, render);
		
			if(ci != null)
				returnVal |= callRender(ci, render);
		}
		else {
			for(Map.Entry<EntityPlayer, PartCoordinates> breaking : ImmibisCore.multipartSystem.getBreakingParts()) {
				if(!breaking.getKey().worldObj.isRemote)
					continue;
				
				PartCoordinates pc = breaking.getValue();
				if(pc.x == x && pc.y == y && pc.z == z) {
					if(!pc.isCoverSystemPart)
						returnVal |= callRenderPart(imt, render, pc.part);
					else if(ci != null)
						returnVal |= callRenderPart(ci, render, pc.part);
				}
			}
		}
		
		return returnVal;
	}
	
	public static void RenderInvBlock(RenderBlocks render, Block block, int meta) {
		if(block instanceof BlockMultipartBase)
			((BlockMultipartBase)block).renderInvBlock(render, meta);
		else
			ImmibisCore.multipartSystem.renderInvBlockUsingOverriddenRenderType(render, block, meta);
	}
	
	
	
	public ClientProxy() {
		MinecraftForge.EVENT_BUS.register(new MultipartHighlightHandler());
		
		for(final boolean inv3d : new boolean[] {false, true}) {
			RenderingRegistry.registerBlockHandler(new ISimpleBlockRenderingHandler() {
				@Override
				public boolean shouldRender3DInInventory(int renderType) {
					return inv3d;
				}
				
				@Override
				public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
					return ClientProxy.RenderWorldBlock(renderer, world, x, y, z, block);
				}
				
				@Override
				public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
					ClientProxy.RenderInvBlock(renderer, block, metadata);
				}
				
				@Override
				public int getRenderId() {
					return MultipartSystem.multipartRenderType[inv3d?1:0];
				}
			});
		}
	}
}
