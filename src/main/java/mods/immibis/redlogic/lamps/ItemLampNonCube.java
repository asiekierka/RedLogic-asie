package mods.immibis.redlogic.lamps;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.api.misc.ILampBlock.LampType;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemLampNonCube extends ItemBlock {

	public ItemLampNonCube(Block block) {
		super(block);
		
		setHasSubtypes(true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for(int m : BlockLampNonCube.AVAILABLE_MODELS)
			for(int t = 0; t < 3; t++)
				for(int k = 0; k < 16; k++)
					par3List.add(new ItemStack(this, 1, k | (t << 4) | (m << 6)));
	}
	
	private static final String hexDigits = "0123456789abcdef";
	private static final String typeChars = "ndi";
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		
		try {
			int dmg = stack.getItemDamage();
			return "tile.redlogic.lamp." + (dmg >> 6) + "." + typeChars.charAt((dmg >> 4) & 3) + "." + hexDigits.charAt(dmg & 15);
		} catch(IndexOutOfBoundsException e) {
			return "item.redlogic.invalid";
		}
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side^1];
		
		int onx = x + fd.offsetX;
		int ony = y + fd.offsetY;
		int onz = z + fd.offsetZ;
		Block block = world.getBlock(onx, ony, onz);
		if(!block.isSideSolid(world, onx, ony, onz, ForgeDirection.VALID_DIRECTIONS[side ^ 1]))
			return false;
		
		if(!super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
			return false;
		
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileLampNonCube) {
			((TileLampNonCube)te).setSide(side^1);
			((TileLampNonCube)te).initFromItemDamage(stack.getItemDamage());
		}
		
		return true;
	}

	public static ItemStack getItemStack(int model, LampType type, int col) {
		return new ItemStack(RedLogicMod.lampNonCube, 1, (model << 6) | (type.ordinal() << 4) | col);
	}

}
