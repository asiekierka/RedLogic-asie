package mods.immibis.redlogic.interaction;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.redlogic.RedLogicMod;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemLumarButton extends ItemBlock {
	
	public ItemLumarButton(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	private static final String hexDigits = "0123456789abcdef";
	private static final String[] types = {"n", "l", "sl", "t"};
	private static final String[] models = {"button", "plate"};
	
	@Override
	public String getUnlocalizedName(ItemStack is) {
		int dmg = is.getItemDamage();
		try {
			return "tile.redlogic."+models[(dmg>>8) & 15]+"."+types[(dmg>>4) & 15]+"."+hexDigits.charAt(dmg & 15);
		} catch(IndexOutOfBoundsException e) {
			return "item.redlogic.invalid;";
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for(LumarButtonModel model : LumarButtonModel.VALUES)
			for(LumarButtonType type : LumarButtonType.VALUES)
				for(int col = 0; col < 16; col++)
					par3List.add(new ItemStack(this, 1, TileLumarButton.getDamageValue(col, type, model)));
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		if(!BlockLumarButton.canBeAttached(world, x, y, z, side, LumarButtonModel.VALUES[((stack.getItemDamage() >> 8) & 15) % LumarButtonModel.VALUES.length], true))
			return false;
		
		if(!super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
			return false;
		
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileLumarButton)
			((TileLumarButton)te).initializeFromDamageValue(stack.getItemDamage(), side);
		
		else if(world.getBlock(x, y, z).equals(RedLogicMod.lumarButton))
			// sanity check: can't place a button without the tile entity. this should never happen
			world.setBlockToAir(x, y, z);
		
		return true;
	}
}
