package mods.immibis.redlogic.array;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import mods.immibis.core.api.multipart.util.BlockMultipartBase;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.redlogic.RedLogicMod;

public class ArrayCellBlock extends BlockMultipartBase {
	static int renderType = SidedProxy.instance.getUniqueBlockModelID("mods.immibis.redlogic.rendering.ArrayCellRenderer", true);
	
	public ArrayCellBlock() {
		super(RedLogicMod.circuitMaterial);
		setCreativeTab(CreativeTabs.tabRedstone);
	}
	
	public static int overrideRenderType = -1;
	
	@Override
	public int getRenderType() {
		return overrideRenderType >= 0 ? overrideRenderType : renderType;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new ArrayCellTile();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List par3List) {
		for(ArrayCellType type : ArrayCellType.VALUES)
			par3List.add(new ItemStack(this, 1, type.ordinal()));
	}
	
	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5) {
		ArrayCellTile te = (ArrayCellTile)par1World.getTileEntity(par2, par3, par4);
		if(!te.checkCanStay()) {
			te.removePartByPlayer(null, 0, true);
			return;
		}
		
		te.onRedstoneInputChanged();
	}
	
	@Override
	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
		ArrayCellTile te = (ArrayCellTile)par1World.getTileEntity(par2, par3, par4);
		te.tickPending = false;
		te.updateEmittedStrength();
		te.onRedstoneInputChanged();
	}
	
	public static int renderSide = 63;
	public static IIcon textureOverride;
	public static int colourOverride = -1;
	
	public static IIcon texGateBase, texNullTop, texNullTopFB, texNullTopLR, texWiring, texNonInvertFB, texNonInvertFBInv, texNonInvertTop;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		texGateBase = par1IconRegister.registerIcon("redlogic:gate/base");
		texNullTop = par1IconRegister.registerIcon("redlogic:array/nullcell_top");
		texNullTopFB = par1IconRegister.registerIcon("redlogic:array/nullcell_top_fb");
		texNullTopLR = par1IconRegister.registerIcon("redlogic:array/nullcell_top_lr");
		texNonInvertTop = par1IconRegister.registerIcon("redlogic:array/noninvert_top");
		texNonInvertFB = par1IconRegister.registerIcon("redlogic:array/noninvert_top_fb");
		texNonInvertFBInv = par1IconRegister.registerIcon("redlogic:array/noninvert_top_fb_inv");
		texWiring = par1IconRegister.registerIcon("redlogic:array/wiring");
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		return (renderSide & (1 << par5)) != 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return textureOverride == null ? texGateBase : textureOverride;
	}
	
	@Override
	public boolean canProvidePower() {
		return true;
	}
	
	@Override
	protected void dropBlockAsItem(World p_149642_1_, int p_149642_2_, int p_149642_3_, int p_149642_4_, ItemStack p_149642_5_) {
		super.dropBlockAsItem(p_149642_1_, p_149642_2_, p_149642_3_, p_149642_4_, p_149642_5_);
	}
}
