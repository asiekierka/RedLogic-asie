package mods.immibis.redlogic.wires;


import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.multipart.util.BlockMultipartBase;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.redlogic.CommandDebug;
import mods.immibis.redlogic.InvalidTile;
import mods.immibis.redlogic.RedLogicMod;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WireBlock extends BlockMultipartBase {
	
	private int renderType = SidedProxy.instance.getUniqueBlockModelID("mods.immibis.redlogic.rendering.WireRenderer", true);

	public WireBlock() {
		super(RedLogicMod.circuitMaterial);
		setCreativeTab(CreativeTabs.tabRedstone);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		for(EnumWireType wireType : EnumWireType.VALUES)
			wireType.loadTextures(reg, wireType.textureName, wireType.texNameSuffix);
	}
	
	@Override
	public int getRenderType() {
		return renderType;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for(EnumWireType type : EnumWireType.VALUES)
			par3List.add(new ItemStack(this, 1, type.ordinal()));
	
		for(EnumWireType type : EnumWireType.VALUES)
			if(type.hasJacketedForm())
				par3List.add(new ItemStack(this, 1, type.ordinal() | WireDamageValues.DMG_FLAG_JACKETED));
	}
	
	@Override
	public TileEntity createTileEntity(World world, int meta) {
		Class<? extends WireTile> clazz = EnumWireType.META_TO_CLASS.get(meta);
		
		if(clazz == null) {
			return new InvalidTile();
		}
		
		try {
			return clazz.getConstructor().newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return null;
	}
	
	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5) {
		((WireTile)par1World.getTileEntity(par2, par3, par4)).onNeighbourBlockChange();
	}
	
	@Override
	public int isProvidingStrongPower(IBlockAccess w, int x, int y, int z, int opposite_dir) {
		int meta = w.getBlockMetadata(x, y, z);
		
		if(meta == EnumWireType.PLAIN_RED_ALLOY_META) {
			RedAlloyTile tile = ((RedAlloyTile)w.getTileEntity(x, y, z));
			
			return tile.canProvideStrongPowerInDirection(opposite_dir ^ 1) ? tile.getVanillaRedstoneStrength() : 0;
		}
		
		return 0;
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess w, int x, int y, int z, int opposite_dir) {
		int meta = w.getBlockMetadata(x, y, z);
		
		if(meta == EnumWireType.PLAIN_RED_ALLOY_META || meta == EnumWireType.INSULATED_RED_ALLOY_META) {
			RedAlloyTile tile = ((RedAlloyTile)w.getTileEntity(x, y, z));
			
			return tile.canProvideWeakPowerInDirection(opposite_dir ^ 1) ? tile.getVanillaRedstoneStrength() : 0;
		}
		
		return 0;
	}
	
	@Override
	public boolean canProvidePower() {
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		if(CommandDebug.WIRE_READING)
			return ((WireTile)par1World.getTileEntity(par2, par3, par4)).debug(par5EntityPlayer);
		return super.onBlockActivated(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);
	}
	
	@Override
	protected void dropBlockAsItem(World p_149642_1_, int p_149642_2_, int p_149642_3_, int p_149642_4_, ItemStack p_149642_5_) {
		super.dropBlockAsItem(p_149642_1_, p_149642_2_, p_149642_3_, p_149642_4_, p_149642_5_);
	}
}
