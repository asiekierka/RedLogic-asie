package mods.immibis.redlogic.chips.ingame;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.RedLogicMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCustomCircuit extends BlockContainer {
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
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> rv = new ArrayList<ItemStack>();
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileCustomCircuit) {
			rv.add(ItemCustomCircuit.createItemStack(((TileCustomCircuit) te).getClassName()));
		} else if(lastBrokenClassName != null) {
			rv.add(ItemCustomCircuit.createItemStack(lastBrokenClassName));
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
		return ((TileCustomCircuit)par1iBlockAccess.getTileEntity(par2, par3, par4)).getEmittedSignalStrength(-1, par5^1);
	}
	
	@Override
	public int isProvidingWeakPower(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		return ((TileCustomCircuit)par1iBlockAccess.getTileEntity(par2, par3, par4)).getEmittedSignalStrength(-1, par5^1);
	}
	
	@Override
	public boolean isNormalCube() {
		return false;
	}
}
