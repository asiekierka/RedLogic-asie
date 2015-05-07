package mods.immibis.redlogic.lamps;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemLampCube extends ItemBlock {
	public ItemLampCube(Block block) {
		super(block);
		
		setHasSubtypes(true);
	}
	
	private static final String hexDigits = "0123456789abcdef";
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		try {
			return super.getUnlocalizedName() + "." + hexDigits.charAt(stack.getItemDamage());
		} catch(IndexOutOfBoundsException e) {
			return "item.immibis.redlogic.invalid";
		}
	}
	
	@Override
	public int getMetadata(int par1) {
		return par1;
	}
}
