package mods.immibis.redlogic.interaction;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.multipart.util.BlockMultipartBase;
import mods.immibis.core.api.util.Dir;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockLumarButton extends BlockMultipartBase {
	public float hardness;
	
	public BlockLumarButton() {
		super(Material.rock);

		this.hardness = 0.5F;
		setCreativeTab(CreativeTabs.tabRedstone);
		setHardness(hardness);
		setStepSound(soundTypeStone);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		for(LumarButtonModel lbm : LumarButtonModel.VALUES)
			lbm.glowIcon = par1IconRegister.registerIcon(lbm.glowIconName);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return Blocks.stone.getIcon(0, 0);
	}
	
	public static boolean canBeAttached(World w, int x, int y, int z, int side, LumarButtonModel model, boolean _default) {
		if(side < 2 && model != LumarButtonModel.Plate) return false;
		
		ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[side^1];
		return w.isSideSolid(x+dir.offsetX, y+dir.offsetY, z+dir.offsetZ, ForgeDirection.VALID_DIRECTIONS[side^1], _default);
    }
	
	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4,
		Block par5, int par6) {
		// TODO Auto-generated method stub
		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileLumarButton();
	}
	
	@Override
	public boolean canProvidePower() {
		return true;
	}
	
	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5) {
		TileLumarButton te = (TileLumarButton)par1World.getTileEntity(par2, par3, par4);
		if(!te.isProperlyAttached()) {
			// button falls off when the block it's attached to is destroyed
			dropBlockAsItem(par1World, par2, par3, par4, te.getItemDropped());
			te.convertToCoverContainerBlock();
		} else {
			te.onNeighbourChange();
		}
    }
	
	@Override
	public int isProvidingStrongPower(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		TileLumarButton te = (TileLumarButton)par1iBlockAccess.getTileEntity(par2, par3, par4);
		return te.isPowering() && par5 == (te.getSide() ^ 1) ? 15 : 0;
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		TileLumarButton te = (TileLumarButton)par1iBlockAccess.getTileEntity(par2, par3, par4);
		return te.isPowering() ? 15 : 0;
	}
	
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		TileLumarButton te = (TileLumarButton)par1World.getTileEntity(par2, par3, par4);
		te.press();
		return true;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		return null;
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
		// XXX setBlockBoundsBasedOnState is not threadsafe - but vanilla...
		AxisAlignedBB bb = ((TileLumarButton)par1IBlockAccess.getTileEntity(par2, par3, par4)).getBoundingBox();
		setBlockBounds((float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ);
	}
	
	@Override
	public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity) {
		// pressure plates
		if(!par1World.isRemote) {
			TileLumarButton t = (TileLumarButton)par1World.getTileEntity(par2, par3, par4);
			if(t.getModel() == LumarButtonModel.Plate && t.getSide() == Dir.NY) {
				AxisAlignedBB bb = t.getMobSensitiveBoundingBox();
				if(par1World.getEntitiesWithinAABB(EntityLivingBase.class, bb.offset(par2, par3, par4)).size() > 0)
					t.pressAndHold();
			}
		}
	}
	
	@Override
	protected void dropBlockAsItem(World p_149642_1_, int p_149642_2_, int p_149642_3_, int p_149642_4_, ItemStack p_149642_5_) {
		super.dropBlockAsItem(p_149642_1_, p_149642_2_, p_149642_3_, p_149642_4_, p_149642_5_);
	}
	
	public static int renderType;
	@Override
	public int getRenderType() {
		return renderType;
	}
}
