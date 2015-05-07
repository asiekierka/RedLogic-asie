package mods.immibis.core.api.multipart.util;


import java.util.ArrayList;
import java.util.List;

import mods.immibis.core.api.APILocator;
import mods.immibis.core.api.multipart.ICoverSystem;
import mods.immibis.core.api.multipart.IMultipartSystem;
import mods.immibis.core.api.multipart.IMultipartTile;
import mods.immibis.core.api.multipart.IPartContainer;
import mods.immibis.core.api.multipart.IPartContainer2;
import mods.immibis.core.api.multipart.PartCoordinates;
import mods.immibis.core.multipart.SubhitValues;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/*
 * How block breaking works, since this is slightly complicated:
 * 
 * When a client player starts breaking a part, or changes the part they are breaking, they send a packet to the server specifying which.
 * When a server player finishes breaking a part, this value is used.
 * 
 * When a player finishes breaking a part, removeBlockByPlayer is called which removes the part but saves it as the last-removed part.
 * If they were using the right tool, harvestBlock is called which drops and clears the last-removed part.
 * 
 * The available part numbers are split into tile-owned parts and system-owned parts.
 * System-owned parts have subhit = (-1 - partNumber) and are handled by the ICoverSystem if available.
 * Tile-owned parts have subhit = partNumber and are handled by the IMultipartTile. 
 * 
 * If all tile-owned parts are broken the tile should call ICoverSystem.convertToSystemBlock to convert
 * the block to a block that contains only the system-owned parts - for example, with MicroblockCoverSystem
 * the block becomes a microblock container block.
 * 
 * Note: A "subhit" refers to the subHit value obtained in a ray trace.
 */
public abstract class BlockMultipartBase extends BlockContainer {
	
	private final IMultipartSystem system = APILocator.getMultipartSystem();
	
	@Override
	public boolean getEnableStats() {return false;}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity) {
		try {
			IMultipartTile te = ((IMultipartTile)world.getTileEntity(x, y, z));
			if(te == null) {
				System.out.println("[Immibis Core] multipart block at "+x+","+y+","+z+" has no tile entity! block name is "+this.getUnlocalizedName()+", block class is "+getClass().getName());
				return;
			}
			ICoverSystem ci = te.getCoverSystem();
			if(ci != null)
				ci.getCollidingBoundingBoxes(mask, list);
			te.getCollidingBoundingBoxes(mask, list);
		} catch(ClassCastException e) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return system.getTransparentIcon();
	}
	
	public void harvestBlockMultipart(World world, EntityPlayer ply, int x, int y, int z, int blockMetadata) {
		super.harvestBlock(world, ply, x, y, z, blockMetadata);
	}
	
	@Override
	public final boolean canHarvestBlock(EntityPlayer ply, int meta) {
		return true;
	}
	
	public float getPartHardness(World w, int x, int y, int z, int part) {
		return super.getBlockHardness(w, x, y, z);
	}
	
	@Override
	public final float getBlockHardness(World w, int x, int y, int z) {
		return -1;
	}
	
	@Override
	public boolean removedByPlayer(World w, EntityPlayer ply, int x, int y, int z) {
		return system.onRemoveBlockByPlayer(w, ply, x, y, z);
	}
	
	@Override
	public void onBlockClicked(World w, int i, int j, int k, EntityPlayer ply) {
		system.onBlockClicked(w, i, j, k, ply);
	}
	
	
	
	@Override
	public final float getPlayerRelativeBlockHardness(EntityPlayer ply, World world, int x, int y, int z) {
		return system.getPlayerRelativeBlockHardness(ply, world, x, y, z);
	}
	
	
	
	@Override
	public final ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return system.getDrops();
	}
	
	protected BlockMultipartBase(Material mat) {
		super(mat);
	}
	
	
	
	
	
	
	
	/**
	 * Override this for custom collision ray tracing.
	 * Return the ray trace result, or null if nothing intersects the ray.
	 * If the return value is null, the tile entity's collisionRayTrace method is consulted instead.
	 */
	public MovingObjectPosition wrappedCollisionRayTrace(World world, int i, int j, int k, Vec3 vec3d, Vec3 vec3d1) {
		return null;
	}
	
	@Override
	public final MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 src, Vec3 dst) {
		try {
			IMultipartTile tile = (IMultipartTile)world.getTileEntity(x, y, z);
			
			if(tile == null) {
				System.out.println("[Immibis Core] multipart block at "+x+","+y+","+z+" has no tile entity! block name is "+this.getUnlocalizedName()+", block class is "+getClass().getName());
				return null;
			}
			
			ICoverSystem ci = tile.getCoverSystem();
			MovingObjectPosition ciPos = ci == null ? null : ci.collisionRayTrace(src, dst);
			
			MovingObjectPosition tilePos = wrappedCollisionRayTrace(world, x, y, z, src, dst);
			
			if(tilePos != null && SubhitValues.isCoverSystem(tilePos.subHit))
				throw new AssertionError("wrappedCollisionRayTrace subhit value in wrong range");
			
			if(tilePos == null) {
				tilePos = tile.collisionRayTrace(src, dst);
			
				if(tilePos != null && SubhitValues.isCoverSystem(tilePos.subHit))
					throw new AssertionError("ICoverableTile.collisionRayTrace must return a non-negative subHit");
			}
			
			if(tilePos == null) return ciPos;
			if(ciPos == null) return tilePos;
			
			double ciDist = ciPos.hitVec.squareDistanceTo(src);
			double tileDist = tilePos.hitVec.squareDistanceTo(src);
			
			return ciDist < tileDist ? ciPos : tilePos;
			
		} catch(ClassCastException e) {
			world.setBlock(x, y, z, Blocks.air, 0, 2);
			return super.collisionRayTrace(world, x, y, z, src, dst);
		}
	}
	
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		return system.onBlockActivated(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);
	}
	
	@Override
	public final boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public final boolean renderAsNormalBlock() {
		return false;
	}
	
	
	
	
	/**
	 * Override this for custom rendering of the wrapped block.
	 */
	public int wrappedGetRenderType() {
		return 0;
	}

	@Override
	public final int getRenderType() {
		return system.overrideRenderType(wrappedGetRenderType(), true);
	}
	
	
	

	

	@Override
	public final int getDamageValue(World par1World, int par2, int par3, int par4) {
		// we don't know which part the player was looking at
		return 0;
	}
	
	@Override
	public final ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		IMultipartTile te = (IMultipartTile)world.getTileEntity(x, y, z);
		if(te == null) {
			System.out.println("[Immibis Core] multipart block at "+x+","+y+","+z+" has no tile entity! block name is "+this.getUnlocalizedName()+", block class is "+getClass().getName());
			return null;
		}
		
		if(!SubhitValues.isCoverSystem(target.subHit))
			return te.pickPart(target, SubhitValues.getTilePartIndex(target.subHit));
		ICoverSystem ci = te.getCoverSystem();
		return ci == null ? null : ci.pickPart(target, SubhitValues.getCSPartIndex(target.subHit));
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		return true;
	}
	
	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(!(te instanceof IMultipartTile)) {
			System.out.println("[Immibis Core] multipart block at "+x+","+y+","+z+" has "+(te == null ? "no tile entity" : "the wrong tile entity ("+te.getClass().getName()+")")+"! block name is "+this.getUnlocalizedName()+", block class is "+getClass().getName());
			return true;
		}
		IMultipartTile imt = ((IMultipartTile)te);
		if(imt.isSolidOnSide(side))
			return true;
		ICoverSystem ci = imt.getCoverSystem();
		return ci != null && ci.isSolidOnSide(side);
	}

	@SideOnly(Side.CLIENT)
	public void renderInvBlock(RenderBlocks render, int meta) {
		system.renderInvBlockUsingOverriddenRenderType(render, this, meta);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		return true; // disable default block destroy effects
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
		PartCoordinates p = system.getBreakingPart(Minecraft.getMinecraft().thePlayer);
		if(p == null || p.x != target.blockX || p.y != target.blockY || p.z != target.blockZ)
			return true;
		
		TileEntity t = worldObj.getTileEntity(p.x, p.y, p.z);
		if(!(t instanceof IMultipartTile))
			return true;
		
		IPartContainer ipc = p.isCoverSystemPart ? ((IMultipartTile)t).getCoverSystem() : (IMultipartTile)t;
		if(!(ipc instanceof IPartContainer2))
			return true;
		
		((IPartContainer2)ipc).addPartHitEffects(p.part, target.sideHit, effectRenderer);
		
        return true;
    }
}
