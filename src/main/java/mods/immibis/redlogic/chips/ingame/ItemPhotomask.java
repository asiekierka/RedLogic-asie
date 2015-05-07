package mods.immibis.redlogic.chips.ingame;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.redlogic.RedLogicMod;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemPhotomask extends Item {

	public ItemPhotomask() {
		super();
		
		setMaxStackSize(1);
		setUnlocalizedName("redlogic.photomask");
		setTextureName("redlogic:photomask");
	}
	
	public static String getClassName(ItemStack stack) {
		if(stack.getItem() != RedLogicMod.photomaskItem)
			return null;
		if(!stack.hasTagCompound())
			return null;
		if(!stack.stackTagCompound.hasKey("classname"))
			return null;
		return stack.stackTagCompound.getString("classname");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer ply, List lines, boolean showIDs) {
		super.addInformation(stack, ply, lines, showIDs);
		
		if(showIDs) {
			lines.add("Class name:");
			String n = getClassName(stack);
			int chunksize = 30;
			for(int st = 0; st < n.length(); st += chunksize)
				lines.add(n.substring(st, Math.min(st+chunksize, n.length())));
		}
	}

	public static ItemStack createItemStack(String className) {
		ItemStack st = new ItemStack(RedLogicMod.photomaskItem);
		st.stackTagCompound = new NBTTagCompound();
		st.stackTagCompound.setString("classname", className);
		return st;
	}
	
	@Override
	public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
	}

}
