package mods.immibis.redlogic.chips.ingame;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;

import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.UtilsDye;

public class RecipeDyeCustomCircuit implements IRecipe {

	public RecipeDyeCustomCircuit() {
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting ic) {
		int colour = -1;
		ItemStack targetStack = null;
		for(int k = 0; k < ic.getSizeInventory(); k++) {
			ItemStack s = ic.getStackInSlot(k);
			if(s != null) {
				if(s.getItem() instanceof ItemCustomCircuit) {
					targetStack = s.copy();
				} else if(colour == -1) {
					colour = UtilsDye.getDyeColor(s);
				} else {
					return null;
				}
			}
		}

		if(colour == -1 || targetStack == null || !targetStack.hasTagCompound())
			return null;

		targetStack.setItemDamage(colour);
		return targetStack;
	}
	
	@Override
	public boolean matches(InventoryCrafting inventorycrafting, World world) {
		return getCraftingResult(inventorycrafting) != null;
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(Item.getItemFromBlock(RedLogicMod.customCircuitBlock));
	}
	
	@Override
	public int getRecipeSize() {
		return 2;
	}
	
	static {
		RecipeSorter.register(RecipeDyeCustomCircuit.class.getName(), RecipeDyeCustomCircuit.class, RecipeSorter.Category.SHAPELESS, "");
	}
}
