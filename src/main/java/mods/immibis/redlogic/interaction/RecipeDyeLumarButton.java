package mods.immibis.redlogic.interaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;

public class RecipeDyeLumarButton implements IRecipe {
	
	public static Set<Item> dyeItems = new HashSet<Item>();
	
	public static final String[] dyeNames = new String[] {
        "dyeBlack",
        "dyeRed",
        "dyeGreen",
        "dyeBrown",
        "dyeBlue",
        "dyePurple",
        "dyeCyan",
        "dyeLightGray",
        "dyeGray",
        "dyePink",
        "dyeLime",
        "dyeYellow",
        "dyeLightBlue",
        "dyeMagenta",
        "dyeOrange",
        "dyeWhite"
    };
	public static final Set<String> dyeNamesSet = new HashSet<String>(Arrays.asList(dyeNames));
	
	public static final int[] dyeOreIDs = new int[16];
	
	@SubscribeEvent
	public void onOreDictAdd(OreDictionary.OreRegisterEvent evt) {
		if(dyeNamesSet.contains(evt.Name))
			dyeItems.add(evt.Ore.getItem());
	}
	
	public RecipeDyeLumarButton()
	{
		MinecraftForge.EVENT_BUS.register(this);
		for(String dye : dyeNames)
			for(ItemStack s : OreDictionary.getOres(dye))
				dyeItems.add(s.getItem());
		for(int k = 0; k < 16; k++)
			dyeOreIDs[k] = OreDictionary.getOreID(dyeNames[k]);
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting ic) {
		int colour = -1;
		int buttonDmg = -1;
		for(int k = 0; k < ic.getSizeInventory(); k++) {
			ItemStack s = ic.getStackInSlot(k);
			if(s != null) {
				if(s.getItem() instanceof ItemLumarButton) {
					if(buttonDmg != -1)
						return null;
					buttonDmg = s.getItemDamage();
				
				} else if(dyeItems.contains(s.getItem())) {
					if(colour != -1)
						return null;
					
					for(int c = 0; c < 16 && colour == -1; c++)
						for(ItemStack dyeStack : OreDictionary.getOres(dyeOreIDs[15-c]))
							if(OreDictionary.itemMatches(s, dyeStack, false)) {
								colour = c;
								break;
							}
					
					if(colour == -1)
						return null;
					
				} else
					return null;
			}
		}
		
		if(colour == -1 || buttonDmg == -1)
			return null;
		
		return TileLumarButton.getItemStack(colour, TileLumarButton.getTypeFromDamage(buttonDmg), TileLumarButton.getModelFromDamage(buttonDmg));
	}
	
	@Override
	public boolean matches(InventoryCrafting inventorycrafting, World world) {
		return getCraftingResult(inventorycrafting) != null;
	}
	
	@Override
	public ItemStack getRecipeOutput() {
		return TileLumarButton.getItemStack(0, LumarButtonType.Normal, LumarButtonModel.Button);
	}
	
	@Override
	public int getRecipeSize() {
		return 2;
	}
	
	static {
		RecipeSorter.register(RecipeDyeLumarButton.class.getName(), RecipeDyeLumarButton.class, RecipeSorter.Category.SHAPELESS, "");
	}
}
