package mods.immibis.redlogic.lamps;

import java.util.ArrayList;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.redlogic.api.misc.ILampBlock.LampType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockLampNonCube extends BlockContainer {
	
	public static final int MODEL_STANDING_FRAME = 0;
	public static final int MODEL_CAGE = 1;
	public static final int MODEL_FLAT = 2;
	
	public static final int[] AVAILABLE_MODELS = {MODEL_CAGE, MODEL_FLAT};

	public BlockLampNonCube() {
		super(Material.redstoneLight);
		
		setCreativeTab(CreativeTabs.tabRedstone);
		
		setStepSound(soundTypeGlass);
		setHardness(0.3f);
		this.setLightLevel(15);
	}
	
	private static int renderType = SidedProxy.instance.getUniqueBlockModelID("mods.immibis.redlogic.lamps.LampRenderNonCubeStatic", true);
	
	public static IIcon iWhite;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iWhite = par1IconRegister.registerIcon("redlogic:white");
		blockIcon = iWhite;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		TileEntity te = par1World.getTileEntity(par2, par3, par4);
		if(te instanceof TileLampNonCube)
			return ((TileLampNonCube)te).getCollisionBoxFromPool();
		return super.getCollisionBoundingBoxFromPool(par1World, par2, par3, par4);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int par2, int par3, int par4) {
		return getCollisionBoundingBoxFromPool(par1World, par2, par3, par4);
	}
	
	@Override
	public MovingObjectPosition collisionRayTrace(World par1World, int par2, int par3, int par4, Vec3 par5Vec3, Vec3 par6Vec3) {
		AxisAlignedBB bb = getCollisionBoundingBoxFromPool(par1World, par2, par3, par4);
		MovingObjectPosition result = bb.calculateIntercept(par5Vec3, par6Vec3);
		if(result == null)
			return null;
		
		result.typeOfHit = MovingObjectType.BLOCK;
		result.blockX = par2;
		result.blockY = par3;
		result.blockZ = par4;
		
		return result;
	}
	
	@Override
	public int getRenderType() {
		return renderType;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileLampNonCube();
	}
	
	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileLampNonCube) {
			switch(((TileLampNonCube)te).getType()) {
			case Indicator: return 0;
			case Decorative: return 15;
			case Normal: return ((TileLampNonCube)te).getIsActive() ? 15 : 0;
			}
		}
		return 0;
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileLampNonCube)
			return ((TileLampNonCube)te).getDroppedItem();
		return null;
	}
	
	@Override
	public void onBlockAdded(World par1World, int par2, int par3, int par4) {
		super.onBlockAdded(par1World, par2, par3, par4);
		par1World.scheduleBlockUpdate(par2, par3, par4, this, 1);
	}
	
	private static TileLampNonCube lastRemoved = null;
	@Override
	public void onBlockHarvested(World par1World, int par2, int par3, int par4, int par5, EntityPlayer par6EntityPlayer) {
		if(!par1World.isRemote)
			lastRemoved = (TileLampNonCube)par1World.getTileEntity(par2, par3, par4);
		super.onBlockHarvested(par1World, par2, par3, par4, par5, par6EntityPlayer);
	}
	
	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> rv = new ArrayList<ItemStack>();
		if(!world.isRemote && lastRemoved != null) {
			rv.add(lastRemoved.getDroppedItem());
			lastRemoved = null;
		}
		return rv;
	}
	
	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5) {
		if(par1World.isRemote) return;
		TileEntity te_ = par1World.getTileEntity(par2, par3, par4);
		if(!(te_ instanceof TileLampNonCube)) return;
		TileLampNonCube te = (TileLampNonCube)te_;
		if(!(te.getIsInitialised())) return;
		
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[te.getSide()];
		int onx = par2 + fd.offsetX;
		int ony = par3 + fd.offsetY;
		int onz = par4 + fd.offsetZ;
		if(!par1World.isSideSolid(onx, ony, onz, ForgeDirection.VALID_DIRECTIONS[te.getSide()^1], true)) {
			dropBlockAsItem(par1World, par2, par3, par4, te.getDroppedItem());
			te.invalidate();
			par1World.setTileEntity(par2, par3, par4, null);
			par1World.setBlock(par2, par3, par4, Blocks.air);
		}
		
		if(te.getType() == LampType.Decorative) return;
		
		boolean nowPowered = isPowered(par1World, par2, par3, par4);
		if(nowPowered != te.getIsActive())
			par1World.scheduleBlockUpdate(par2, par3, par4, this, 1);
		
		
	}
	
	@Override
	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
		if(par1World.isRemote) return;
		TileEntity te_ = par1World.getTileEntity(par2, par3, par4);
		if(!(te_ instanceof TileLampNonCube)) return;
		TileLampNonCube te = (TileLampNonCube)te_;
		if(te.getType() == LampType.Decorative) return;
		
		
		boolean nowPowered = isPowered(par1World, par2, par3, par4);
		if(nowPowered != te.getIsActive())
			te.setIsActive(nowPowered);
	}
	
	private boolean isPowered(World w, int x, int y, int z) {
		return w.isBlockIndirectlyGettingPowered(x, y, z);
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		return false;
	}
	
	@Override
	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour) {
		TileLampNonCube tile = (TileLampNonCube)world.getTileEntity(x, y, z);
		if(tile.getColour() != colour) {
			tile.setColour(colour);
			world.markBlockForUpdate(x, y, z);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer ply, int par6, float par7, float par8, float par9) {
		ItemStack h = ply.getCurrentEquippedItem();
		if(h != null && h.getItem().equals(Items.dye)) {
			if(!w.isRemote) {
				if(MinecraftServer.getServer().isBlockProtected(w, x, y, z, ply)) {
					// No permission
					if(ply instanceof EntityPlayerMP)
						((EntityPlayerMP)ply).playerNetServerHandler.netManager.channel().write(new S23PacketBlockChange(x, y, z, w));
					return true;
				}
			}
			
			recolourBlock(w, x, y, z, null, (15 - h.getItemDamage()) & 15);
			
			return true;
		}
		return false;
	}
}
