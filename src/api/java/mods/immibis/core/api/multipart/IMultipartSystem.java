package mods.immibis.core.api.multipart;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * These methods are called by BlockMultipartBase and the transformer in Immibis's Microblocks,
 * and are deliberately not documented here.
 */
public interface IMultipartSystem {
	public boolean onRemoveBlockByPlayer(World w, EntityPlayer ply, int x, int y, int z);
	public void onBlockClicked(World w, int x, int y, int z, EntityPlayer ply);
	public float getPlayerRelativeBlockHardness(EntityPlayer ply, World world, int x, int y, int z);
	public ArrayList<ItemStack> getDrops();
	public int overrideRenderType(int base, boolean inv3d);
	public void renderInvBlockUsingOverriddenRenderType(RenderBlocks render, Block blockMultipartBase, int meta);
	public boolean renderBlockInWorldUsingOverriddenRenderType(RenderBlocks render, Block block, int x, int y, int z);
	public PartCoordinates getBreakingPart(EntityPlayer thePlayer);
	@SideOnly(Side.CLIENT) public IIcon getTransparentIcon();
	public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer ply, int side, float hx, float hy, float hz);
}
