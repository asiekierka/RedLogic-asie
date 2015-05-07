package mods.immibis.redlogic.lamps;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.api.misc.ILampBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockLampCube extends Block implements ILampBlock {
	
	public static final int COLOURS[] = {
		0xFFFFFF,
		0xFFA100,
		0xFF00FF,
		0xAEAEFF,
		0xFFFF00,
		0xA1FF63,
		0xFFB9B9,
		0x9D9D9D,
		0xCBCBCB,
		0x00FFFF,
		0xAE00FF,
		0x0000FF,
		0xA55A00,
		0x00A600,
		0xFF0000,
		0x3B3B3B
	};
	
	boolean powered;
	LampType type;
	
	static IIcon iUncoloured, iColoured;
	static boolean renderingColouredPart;
	
	static int renderType = SidedProxy.instance.getUniqueBlockModelID("mods.immibis.redlogic.lamps.LampRenderCubeStatic", true);
	
	public BlockLampCube(boolean powered, LampType type) {
		super(Material.redstoneLight);
		this.powered = powered;
		this.type = type;
		
		this.setLightLevel((powered && type != LampType.Indicator) ? 1 : 0);
		
		if(type == LampType.Decorative)
			setCreativeTab(CreativeTabs.tabDecorations);
		else if(!powered)
			setCreativeTab(CreativeTabs.tabRedstone);
		
		switch(type) {
		case Normal: setBlockName("redlogic.lamp.cube.n"); break;
		case Decorative: setBlockName("redlogic.lamp.cube.d"); break;
		case Indicator: setBlockName("redlogic.lamp.cube.i"); break;
		}
		
		setStepSound(soundTypeGlass);
		setHardness(0.3f);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		// load the icons only once
		if(type != LampType.Normal || powered)
			return;
		
		iUncoloured = par1IconRegister.registerIcon("redlogic:lamp/cube-uncoloured");
		iColoured = par1IconRegister.registerIcon("redlogic:lamp/cube-coloured");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return renderingColouredPart ? iColoured : iUncoloured;
	}
	
	@Override
	public int getRenderType() {
		return renderType;
	}
	
	@Override
	public int damageDropped(int par1) {
		return par1;
	}
	
	@Override
	public void onBlockAdded(World par1World, int par2, int par3, int par4) {
		super.onBlockAdded(par1World, par2, par3, par4);
		onNeighborBlockChange(par1World, par2, par3, par4, Blocks.air);
	}
	
	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5) {
		if(par1World.isRemote || type == LampType.Decorative)
			return;
		
		boolean nowPowered = isPowered(par1World, par2, par3, par4);
		if(nowPowered != powered)
			par1World.scheduleBlockUpdate(par2, par3, par4, this, 1);
	}
	
	@Override
	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random) {
		if(par1World.isRemote || type == LampType.Decorative)
			return;
		
		boolean nowPowered = isPowered(par1World, par2, par3, par4);
		if(nowPowered != powered)
			par1World.setBlock(par2, par3, par4, getOtherBlock(), par1World.getBlockMetadata(par2, par3, par4), 3);
	}
	
	private boolean isPowered(World w, int x, int y, int z) {
		return w.isBlockIndirectlyGettingPowered(x, y, z);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getMixedBrightnessForBlock(IBlockAccess par1iBlockAccess, int par2, int par3, int par4) {
		if(renderingColouredPart && powered)
			return 0x00F000F0;
		return super.getMixedBrightnessForBlock(par1iBlockAccess, par2, par3, par4);
	}
	
	private Block getOtherBlock() {
		if(type == LampType.Normal) {
			if(powered)
				return RedLogicMod.lampCubeOff;
			else
				return RedLogicMod.lampCubeOn;
		} else if(type == LampType.Indicator) {
			if(powered)
				return RedLogicMod.lampCubeIndicatorOff;
			else
				return RedLogicMod.lampCubeIndicatorOn;
		} else {
			return this;
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Item getItem(World world, int x, int y, int z) {
		return getItemDropped(0, world.rand, 0);
	}
	
	@Override
	public Item getItemDropped(int p1, Random p2, int p3) {
		if(type != LampType.Decorative && powered)
        	return Item.getItemFromBlock(getOtherBlock());
        else
        	return super.getItemDropped(p1, p2, p3);
	}
	
	@Override
	public boolean isNormalCube() {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess par1iBlockAccess, int par2, int par3, int par4) {
		return getRenderColor(par1iBlockAccess.getBlockMetadata(par2, par3, par4));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int par1) {
		if(!renderingColouredPart)
			return 0xFFFFFF;
		
		try {
			int c = COLOURS[par1];
			if(!powered)
				c = getOffColour(c);
			return c;
		} catch(ArrayIndexOutOfBoundsException e) {
			return 0;
		}
	}
	
	public static int getOffColour(int c) {
		return (c >> 2) & 0x3F3F3F;
	}

	public static class On extends BlockLampCube {public On() {super(true, LampType.Normal);}}
	public static class Off extends BlockLampCube {public Off() {super(false, LampType.Normal);}}
	public static class Decorative extends BlockLampCube {public Decorative() {super(true, LampType.Decorative);}}
	public static class IndicatorOn extends BlockLampCube {public IndicatorOn() {super(true, LampType.Indicator);}}
	public static class IndicatorOff extends BlockLampCube {public IndicatorOff() {super(false, LampType.Indicator);}}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		if(powered && type != LampType.Decorative)
			return;
		
		for(int k = 0; k < 16; k++)
			par3List.add(new ItemStack(this, 1, k));
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
			
			w.setBlockMetadataWithNotify(x, y, z, (15 - h.getItemDamage()) & 15, 3);
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour) {
		if(world.getBlockMetadata(x, y, z) != colour) {
			world.setBlockMetadataWithNotify(x, y, z, colour, 3);
			return true;
		}
		return false;
	}

	@Override
	public LampType getType() {
		return type;
	}

	@Override
	public boolean isPowered() {
		return powered;
	}
	
	@Override
	public int getColourRGB(IBlockAccess w, int x, int y, int z) {
		return COLOURS[getColourWool(w, x, y, z)];
	}
	
	@Override
	public int getColourWool(IBlockAccess w, int x, int y, int z) {
		return w.getBlockMetadata(x, y, z);
	}
	
	@Override
	protected ItemStack createStackedBlock(int p_149644_1_) {
		if(type != LampType.Decorative && powered)
			return new ItemStack(getOtherBlock(), 1, p_149644_1_);
		else
			return new ItemStack(this, 1, p_149644_1_);
	}
}
