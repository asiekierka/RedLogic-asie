package mods.immibis.redlogic.chips.ingame;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import mods.immibis.redlogic.RedLogicMod;

public class ItemCustomCircuit extends ItemBlock {
	public ItemCustomCircuit(Block block) {
		super(block);
		
		setMaxStackSize(64);
		setHasSubtypes(true);
		setUnlocalizedName("redlogic.custom-circuit");
	}
	
	public static String getClassName(ItemStack stack) {
		if(!(stack.getItem() instanceof ItemCustomCircuit))
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
		String className = getClassName(stack);

		if (className == null) {
			lines.add(EnumChatFormatting.RED + "Corrupt!");
		} else if (showIDs) {
			lines.add("Class name:");
			int chunksize = 30;
			for(int st = 0; st < className.length(); st += chunksize)
				lines.add(className.substring(st, Math.min(st + chunksize, className.length())));
		}
	}

	public static ItemStack createItemStack(int color, String className) {
		ItemStack st = new ItemStack(RedLogicMod.customCircuitBlock, 1, color);
		st.stackTagCompound = new NBTTagCompound();
		st.stackTagCompound.setString("classname", className);
		return st;
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer ply, World w, int x, int y, int z, int side, float subX, float subY, float subZ) {
		Block old = w.getBlock(x, y, z);
		if(old == null || old.isReplaceable(w, x, y, z)) {
			// replace this block
		} else {
			ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side];
			x += fd.offsetX; y += fd.offsetY; z += fd.offsetZ;
			
			if(!w.isAirBlock(x, y, z))
				return false; // can't place here
		}
		
		w.setBlock(x, y, z, RedLogicMod.customCircuitBlock, 0, 0);
		if(w.getBlock(x, y, z) == RedLogicMod.customCircuitBlock) {
			((TileCustomCircuit)w.getTileEntity(x, y, z)).init(getClassName(stack), getColor(stack), ply);
			stack.stackSize--;
		}
		
		return true;
	}

	public static int getColor(ItemStack stack) {
		return stack.getItemDamage();
	}
}
