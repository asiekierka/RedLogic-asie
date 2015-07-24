package mods.immibis.redlogic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;

public final class UtilsDye {
	private UtilsDye() {

	}

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

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new UtilsDye());
		for(String dye : dyeNames)
			for(ItemStack s : OreDictionary.getOres(dye))
				dyeItems.add(s.getItem());
		for(int k = 0; k < 16; k++)
			dyeOreIDs[k] = OreDictionary.getOreID(dyeNames[k]);
	}

	public static int getDyeColor(ItemStack s) {
		int colour = -1;
		if(dyeItems.contains(s.getItem())) {
			for(int c = 0; c < 16 && colour == -1; c++)
				for(ItemStack dyeStack : OreDictionary.getOres(dyeOreIDs[15-c]))
					if(OreDictionary.itemMatches(s, dyeStack, false)) {
						colour = c;
						break;
					}
		}
		return colour;
	}
}
