package mods.immibis.redlogic.chips.ingame;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.UtilsDye;
import mods.immibis.redlogic.lamps.BlockLampCube;

public class BlockCustomCircuit extends BlockContainer {
	protected static final int RENDER_COLOR = 16777215;

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileCustomCircuit();
	}
	
	public BlockCustomCircuit() {
		super(RedLogicMod.circuitMaterial);
		setHardness(0.25f);
		setBlockName("redlogic.custom-circuit");
		setBlockTextureName("redlogic:chip/chip");
	}

	private IIcon directionalIcon;

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		return BlockLampCube.COLOURS[((TileCustomCircuit) world.getTileEntity(x, y, z)).getColor() & 15];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int meta) {
		// For items.
		return BlockLampCube.COLOURS[meta];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		super.registerBlockIcons(par1IconRegister);
		directionalIcon = par1IconRegister.registerIcon(getTextureName()+"_dir");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		if(par1 == Dir.PY)
			return directionalIcon;
		return super.getIcon(par1, par2);
	}
	
	static int renderType = SidedProxy.instance.getUniqueBlockModelID("mods.immibis.redlogic.chips.ingame.BlockCustomCircuitRenderStatic", true);
	
	@Override
	public int getRenderType() {
		return renderType;
	}
	
	private String lastBrokenClassName;
	
	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, Block par5, int par6) {
		TileEntity te = par1World.getTileEntity(par2, par3, par4);
		if(te instanceof TileCustomCircuit)
			lastBrokenClassName = ((TileCustomCircuit)te).getClassName();
		super.breakBlock(par1World, par2, par3, par4, par5, par6);
	}

	private ItemStack getStackFromBlock(World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileCustomCircuit) {
			return ItemCustomCircuit.createItemStack(((TileCustomCircuit) te).getColor(), ((TileCustomCircuit) te).getClassName());
		} else {
			return null;
		}
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> rv = new ArrayList<ItemStack>();
		ItemStack is = getStackFromBlock(world, x, y, z);
		if(is != null) {
			rv.add(is);
		} else if(lastBrokenClassName != null) {
			rv.add(ItemCustomCircuit.createItemStack(0, lastBrokenClassName));
			lastBrokenClassName = null;
		}
		return rv;
	}
	
	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block block) {
		if(block != null && block.canProvidePower())
			((TileCustomCircuit)par1World.getTileEntity(par2, par3, par4)).onRedstoneInputChanged();
	}
	
	@Override
	public boolean canProvidePower() {
		return true;
	}
	
	@Override
	public int isProvidingStrongPower(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		return ((TileCustomCircuit)par1iBlockAccess.getTileEntity(par2, par3, par4)).getEmittedSignalStrength(-1, par5);
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		return ((TileCustomCircuit)par1iBlockAccess.getTileEntity(par2, par3, par4)).getEmittedSignalStrength(-1, par5);
	}
	
	@Override
	public boolean isNormalCube() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer ply, int side, float hitX, float hitY, float hitZ) {
		ItemStack stack = ply.getCurrentEquippedItem();
		if (stack != null) {
			int colour = UtilsDye.getDyeColor(stack);
			if (colour >= 0) {
				((TileCustomCircuit) world.getTileEntity(x, y, z)).setColor(colour);
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		return getStackFromBlock(world, x, y, z);
	}
}
