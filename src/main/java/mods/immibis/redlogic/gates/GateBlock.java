package mods.immibis.redlogic.gates;


import java.util.List;
import java.util.Random;

import mods.immibis.core.api.multipart.util.BlockMultipartBase;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.RedLogicMod;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class GateBlock extends BlockMultipartBase {
	
	private int renderType = SidedProxy.instance.getUniqueBlockModelID("mods.immibis.redlogic.gates.GateStaticRenderer", true);
	
	public static final float THICKNESS = 1/8.0f;

	public GateBlock() {
		super(RedLogicMod.circuitMaterial);

		setCreativeTab(CreativeTabs.tabRedstone);
	}
	
	// BEGIN RENDERING
	static IIcon textureOverride = null;
	static int colourOverride = -1;
	static int renderSide;
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if(textureOverride != null)
			return textureOverride;
		else
			return blockIcon;
	}
	
	@Override
	public int getRenderColor(int par1) {
		if(colourOverride < 0)
			return super.getRenderColor(par1);
		else
			return colourOverride;	
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		if(renderSide < 0)
			return par5 != -(renderSide + 1);
		else
			return par5 == renderSide;
	}
	
	@Override
	public int colorMultiplier(IBlockAccess par1iBlockAccess, int par2, int par3, int par4) {
		if(colourOverride >= 0)
			return colourOverride;
		else
			return super.colorMultiplier(par1iBlockAccess, par2, par3, par4);
	}
	
	static int renderTypeOverride = -1;
	@Override
	public int getRenderType() {
		return renderTypeOverride >= 0 ? renderTypeOverride : renderType;
	}

	// END RENDERING
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for(EnumGates type : EnumGates.VALUES)
			if(type != EnumGates.INVALID)
				list.add(new ItemStack(this, 1, type.ordinal()));
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new GateTile();
	}
	
	@Override
	public boolean canProvidePower() {
		return true;
	}
	
	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		return true;
	}
	
	@Override
	public void onNeighborBlockChange(World w, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(w, x, y, z, block);
		
		GateTile te = (GateTile)w.getTileEntity(x, y, z);
		if(!te.checkCanStay()) {
			te.removePartByPlayer(null, 0, true);
			return;
		}
		
		if((block.isAir(w, x, y, z) || block.canProvidePower()) && !w.isRemote) {
			te.updateLogic(false, false);
		}
	}
	
	@Override
	public int isProvidingStrongPower(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		return ((GateTile)par1iBlockAccess.getTileEntity(par2, par3, par4)).getVanillaOutputStrength(par5 ^ 1);
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		return ((GateTile)par1iBlockAccess.getTileEntity(par2, par3, par4)).getVanillaOutputStrength(par5 ^ 1);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		for(EnumGates type : EnumGates.VALUES)
			type.getRendering().loadTextures(reg);
		blockIcon = reg.registerIcon("redlogic:gate/base");
	}
	
	private int getSide(IBlockAccess w, int x, int y, int z) {
		TileEntity te = w.getTileEntity(x,y,z);
		if(!(te instanceof GateTile))
			return Dir.NY;
		return ((GateTile)te).getSide();
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess w, int x, int y, int z) {
		switch(getSide(w,x,y,z)) {
		case Dir.NX: setBlockBounds(0, 0, 0, GateBlock.THICKNESS, 1, 1); break;
		case Dir.NY: setBlockBounds(0, 0, 0, 1, GateBlock.THICKNESS, 1); break;
		case Dir.NZ: setBlockBounds(0, 0, 0, 1, 1, GateBlock.THICKNESS); break;
		case Dir.PX: setBlockBounds(1 - GateBlock.THICKNESS, 0, 0, 1, 1, 1); break;
		case Dir.PY: setBlockBounds(0, 1 - GateBlock.THICKNESS, 0, 1, 1, 1); break;
		case Dir.PZ: setBlockBounds(0, 0, 1 - GateBlock.THICKNESS, 1, 1, 1); break;
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World w, int x, int y, int z) {
		switch(getSide(w,x,y,z)) {
		case Dir.NX: return AxisAlignedBB.getBoundingBox(0, 0, 0, GateBlock.THICKNESS, 1, 1).offset(x, y, z);
		case Dir.NY: return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, GateBlock.THICKNESS, 1).offset(x, y, z);
		case Dir.NZ: return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, GateBlock.THICKNESS).offset(x, y, z);
		case Dir.PX: return AxisAlignedBB.getBoundingBox(1 - GateBlock.THICKNESS, 0, 0, 1, 1, 1).offset(x, y, z);
		case Dir.PY: return AxisAlignedBB.getBoundingBox(0, 1 - GateBlock.THICKNESS, 0, 1, 1, 1).offset(x, y, z);
		case Dir.PZ: return AxisAlignedBB.getBoundingBox(0, 0, 1 - GateBlock.THICKNESS, 1, 1, 1).offset(x, y, z);
		}
		return null;
	}
	
	@Override
	public void updateTick(World w, int x, int y, int z, Random par5Random) {
		((GateTile)w.getTileEntity(x, y, z)).scheduledTick();
	}
	
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		return ((GateTile)par1World.getTileEntity(par2, par3, par4)).onBlockActivated(par5EntityPlayer);
	}
	
	@Override
	protected void dropBlockAsItem(World p_149642_1_, int p_149642_2_, int p_149642_3_, int p_149642_4_, ItemStack p_149642_5_) {
		super.dropBlockAsItem(p_149642_1_, p_149642_2_, p_149642_3_, p_149642_4_, p_149642_5_);
	}
}
