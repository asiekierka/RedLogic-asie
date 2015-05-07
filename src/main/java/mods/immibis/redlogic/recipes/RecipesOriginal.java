package mods.immibis.redlogic.recipes;

import mods.immibis.core.api.util.Colour;
import mods.immibis.redlogic.RLMachineBlock;
import mods.immibis.redlogic.RLNormalBlock;
import mods.immibis.redlogic.api.misc.ILampBlock.LampType;
import mods.immibis.redlogic.array.ArrayCellType;
import mods.immibis.redlogic.gates.EnumGates;
import mods.immibis.redlogic.interaction.LumarButtonModel;
import mods.immibis.redlogic.interaction.LumarButtonType;
import mods.immibis.redlogic.interaction.RecipeDyeLumarButton;
import mods.immibis.redlogic.interaction.TileLumarButton;
import mods.immibis.redlogic.lamps.BlockLampNonCube;
import mods.immibis.redlogic.lamps.ItemLampNonCube;
import mods.immibis.redlogic.wires.EnumWireType;
import mods.immibis.redlogic.wires.WireDamageValues;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import static mods.immibis.redlogic.RedLogicMod.*;

public class RecipesOriginal {

	public static void addRecipes() {
		
		
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(screwdriver),
			" YI",
			" IB",
			"/  ",
			'Y', "dyeYellow",
			'B', "dyeBlack",
			'/', Items.stick,
			'I', Items.iron_ingot
			));
		
		GameRegistry.addRecipe(new ItemStack(wire, 16, EnumWireType.RED_ALLOY.ordinal()),
			"R R",
			"RIR",
			"R R",
			'R', Items.redstone,
			'I', Items.iron_ingot
			);
		
		// insulated wire recipes
		for(int k = 0; k < 16; k++) {
			// craft 8 with dye
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wire, 8, EnumWireType.INSULATED_WIRE[k].ordinal()),
				"WWW",
				"WDW",
				"WWW",
				'W', new ItemStack(wire, 1, EnumWireType.RED_ALLOY.ordinal()),
				'D', Colour.fromWoolID(k).dyeOreDictName));
			
			// craft 8 with wool
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wire, 8, EnumWireType.INSULATED_WIRE[k].ordinal()),
				"WWW",
				"WDW",
				"WWW",
				'W', new ItemStack(wire, 1, EnumWireType.RED_ALLOY.ordinal()),
				'D', new ItemStack(Blocks.wool, 1, k)));
			
			// craft 1 with dye
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(wire, 1, EnumWireType.INSULATED_WIRE[k].ordinal()),
				new ItemStack(wire, 1, EnumWireType.RED_ALLOY.ordinal()),
				Colour.fromWoolID(k).dyeOreDictName));
			
			// craft 1 with wool
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(wire, 1, EnumWireType.INSULATED_WIRE[k].ordinal()),
				new ItemStack(wire, 1, EnumWireType.RED_ALLOY.ordinal()),
				new ItemStack(Blocks.wool, 1, k)));
			
			// recolour 8
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(wire, 8, EnumWireType.INSULATED_WIRE[k].ordinal()),
				"WWW",
				"WDW",
				"WWW",
				'W', "RedLogic:insulated_wire",
				'D', Colour.fromWoolID(k).dyeOreDictName));
			
			// recolour 1
			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(wire, 1, EnumWireType.INSULATED_WIRE[k].ordinal()),
				"RedLogic:insulated_wire",
				Colour.fromWoolID(k).dyeOreDictName));
			
			
		}
		
		GameRegistry.addRecipe(new ItemStack(wire, 2, EnumWireType.BUNDLED.ordinal()),
			"WWW",
			"WSW",
			"WWW",
			'W', new ItemStack(wire, 1, EnumWireType.RED_ALLOY.ordinal()),
			'S', Items.string
		);
		
		for(EnumWireType type : EnumWireType.VALUES) {
			if(!type.hasJacketedForm())
				continue;
			
			ItemStack plain = new ItemStack(wire, 1, type.ordinal());
			ItemStack free = new ItemStack(wire, 1, type.ordinal() | WireDamageValues.DMG_FLAG_JACKETED);
			
			GameRegistry.addRecipe(plain, "X", 'X', free);
			GameRegistry.addRecipe(free, "X", 'X', plain);
		}
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.AND.ordinal()),
			" T ",
			"TTT",
			" R ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.OR.ordinal()),
			" T ",
			"RTR",
			" R ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.NOT.ordinal()),
			" R ",
			"RTR",
			" R ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.RSLATCH.ordinal()),
			"STR",
			"R R",
			"RTS",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.cobblestone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.TOGGLE.ordinal()),
			" T ",
			"RLR",
			" T ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'L', Blocks.lever);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.NOR.ordinal()),
			" T ",
			"RRR",
			" R ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.NAND.ordinal()),
			" R ",
			"TTT",
			" R ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.XOR.ordinal()),
			"RRR",
			"TRT",
			"RTR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.XNOR.ordinal()),
			"RTR",
			"TRT",
			"RTR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.Buffer.ordinal()),
			"RTR",
			"RTR",
			" R ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.Multiplexer.ordinal()),
			"RTR",
			"T T",
			"RTR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.Repeater.ordinal()),
			"RRR",
			"R_R",
			"RRR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'_', Items.repeater);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.Timer.ordinal()),
			" T ",
			"RIR",
			"   ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'I', Items.iron_ingot);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.Counter.ordinal()),
			"T  ",
			"IRR",
			"T  ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'I', Items.iron_ingot);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.Sequencer.ordinal()),
			" T ",
			"TIT",
			" T ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'I', Items.iron_ingot);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.PulseFormer.ordinal()),
			"RTR",
			"TRT",
			"RR ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.Randomizer.ordinal()),
			" T ",
			"TGT",
			" R ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'G', Items.glowstone_dust);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.StateCell.ordinal()),
			" RT",
			"RXI",
			" R ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'X', new ItemStack(gates, 1, EnumGates.RSLATCH.ordinal()),
			'I', Items.iron_ingot);
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.Synchronizer.ordinal()),
			"RTR",
			"XRX",
			"R R",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'X', new ItemStack(gates, 1, EnumGates.RSLATCH.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.DLatch.ordinal()),
			"XTR",
			"TRR",
			"RRR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'X', new ItemStack(gates, 1, EnumGates.RSLATCH.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.DFlop.ordinal()),
			"XTR",
			"TRY",
			"RRR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'X', new ItemStack(gates, 1, EnumGates.RSLATCH.ordinal()),
			'Y', new ItemStack(gates, 1, EnumGates.PulseFormer.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.BundledLatch.ordinal()),
			" B ",
			" DR",
			" B ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'D', new ItemStack(gates, 1, EnumGates.DLatch.ordinal()),
			'B', new ItemStack(wire, 1, EnumWireType.BUNDLED.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.BundledRelay.ordinal()),
			" B ",
			" AR",
			" B ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'A', new ItemStack(gates, 1, EnumGates.AND.ordinal()),
			'B', new ItemStack(wire, 1, EnumWireType.BUNDLED.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.BundledMultiplexer.ordinal()),
			" B ",
			"BMB",
			" R ",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'M', new ItemStack(gates, 1, EnumGates.Multiplexer.ordinal()),
			'B', new ItemStack(wire, 1, EnumWireType.BUNDLED.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.BundledAND.ordinal()),
			"RBR",
			"BMB",
			"RBR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'M', new ItemStack(gates, 1, EnumGates.AND.ordinal()),
			'B', new ItemStack(wire, 1, EnumWireType.BUNDLED.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.BundledOR.ordinal()),
			"RBR",
			"BMB",
			"RBR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'M', new ItemStack(gates, 1, EnumGates.OR.ordinal()),
			'B', new ItemStack(wire, 1, EnumWireType.BUNDLED.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.BundledNOT.ordinal()),
			"RBR",
			"BMB",
			"RBR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'M', new ItemStack(gates, 1, EnumGates.NOT.ordinal()),
			'B', new ItemStack(wire, 1, EnumWireType.BUNDLED.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.BundledXOR.ordinal()),
			"RBR",
			"BMB",
			"RBR",
			'R', Items.redstone,
			'T', Blocks.redstone_torch,
			'S', Blocks.stone,
			'M', new ItemStack(gates, 1, EnumGates.XOR.ordinal()),
			'B', new ItemStack(wire, 1, EnumWireType.BUNDLED.ordinal()));
		
		GameRegistry.addRecipe(new ItemStack(gates, 1, EnumGates.Comparator.ordinal()),
			" R ",
			"RCR",
			'R', new ItemStack(wire, 1, EnumWireType.RED_ALLOY.ordinal()),
			'C', Items.comparator);
		
		// NOT USED
		/*GameRegistry.addRecipe(new ColouredRecipeDye(
			"GrG",
			"GrG",
			"G?G",
			ColouredRecipeDye.makeItemMap(
				'G', "glass",
				'r', Items.redstone
			)
			));*/
		
		
		
		for(int k = 0; k < 16; k++) {
			String dyeOreDictName = Colour.fromWoolID(k).dyeOreDictName;
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lampCubeIndicatorOff, 1, k),
				"GrG",
				"GrG",
				"GdG",
				'G', Blocks.glass,
				'd', dyeOreDictName,
				'r', Items.redstone));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lampCubeOff, 1, k),
				"GrG",
				"GgG",
				"GdG",
				'G', Blocks.glass,
				'd', dyeOreDictName,
				'r', Items.redstone,
				'g', Blocks.glowstone));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(lampCubeDecorative, 1, k),
				"G G",
				"GgG",
				"GdG",
				'G', Blocks.glass,
				'g', Blocks.glowstone,
				'd', dyeOreDictName,
				'r', Items.redstone));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemLampNonCube.getItemStack(BlockLampNonCube.MODEL_FLAT, LampType.Decorative, k),
				"GGG",
				" g ",
				" d ",
				'G', Blocks.glass,
				'g', Blocks.glowstone,
				'd', dyeOreDictName,
				'r', Items.redstone));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemLampNonCube.getItemStack(BlockLampNonCube.MODEL_FLAT, LampType.Indicator, k),
				"GGG",
				"rdr",
				'G', Blocks.glass,
				'g', Blocks.glowstone,
				'd', dyeOreDictName,
				'r', Items.redstone));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemLampNonCube.getItemStack(BlockLampNonCube.MODEL_FLAT, LampType.Normal, k),
				"GGG",
				"dgr",
				'G', Blocks.glass,
				'g', Blocks.glowstone,
				'd', dyeOreDictName,
				'r', Items.redstone));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemLampNonCube.getItemStack(BlockLampNonCube.MODEL_CAGE, LampType.Decorative, k),
				"###",
				"#d#",
				" g ",
				'G', Blocks.glass,
				'g', Blocks.glowstone,
				'd', dyeOreDictName,
				'r', Items.redstone,
				'#', Blocks.iron_bars));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemLampNonCube.getItemStack(BlockLampNonCube.MODEL_CAGE, LampType.Indicator, k),
				"###",
				"#d#",
				"r r",
				'G', Blocks.glass,
				'g', Blocks.glowstone,
				'd', dyeOreDictName,
				'r', Items.redstone,
				'#', Blocks.iron_bars));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(ItemLampNonCube.getItemStack(BlockLampNonCube.MODEL_CAGE, LampType.Normal, k),
				"###",
				"#d#",
				"g r",
				'G', Blocks.glass,
				'g', Blocks.glowstone,
				'd', dyeOreDictName,
				'r', Items.redstone,
				'#', Blocks.iron_bars));
		}
		
		for(int k = 0; k < 16; k++) {
			String dyeOreDictName = Colour.fromWoolID(k).dyeOreDictName;
			GameRegistry.addRecipe(new ShapedOreRecipe(TileLumarButton.getItemStack(k, LumarButtonType.Normal, LumarButtonModel.Button),
				" d ",
				"rbg",
				'b', Blocks.stone_button,
				'g', Items.glowstone_dust,
				'r', Items.redstone,
				'd', dyeOreDictName));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(TileLumarButton.getItemStack(k, LumarButtonType.Latch, LumarButtonModel.Button),
				" d ",
				"rbg",
				'b', Blocks.stone_button,
				'g', Items.glowstone_dust,
				'r', Blocks.redstone_torch,
				'd', dyeOreDictName));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(TileLumarButton.getItemStack(k, LumarButtonType.SelfLatch, LumarButtonModel.Button),
				" d ",
				"rbg",
				'b', Blocks.stone_button,
				'g', Items.glowstone_dust,
				'r', new ItemStack(gates, 1, EnumGates.RSLATCH.ordinal()),
				'd', dyeOreDictName));
		
			GameRegistry.addRecipe(new ShapedOreRecipe(TileLumarButton.getItemStack(k, LumarButtonType.Toggle, LumarButtonModel.Button),
				" d ",
				"rbg",
				'b', Blocks.stone_button,
				'g', Items.glowstone_dust,
				'r', new ItemStack(gates, 1, EnumGates.TOGGLE.ordinal()),
				'd', dyeOreDictName));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(TileLumarButton.getItemStack(k, LumarButtonType.Normal, LumarButtonModel.Plate),
				" d ",
				"rbg",
				'b', Blocks.stone_pressure_plate,
				'g', Items.glowstone_dust,
				'r', Items.redstone,
				'd', dyeOreDictName));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(TileLumarButton.getItemStack(k, LumarButtonType.Latch, LumarButtonModel.Plate),
				" d ",
				"rbg",
				'b', Blocks.stone_pressure_plate,
				'g', Items.glowstone_dust,
				'r', Blocks.redstone_torch,
				'd', dyeOreDictName));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(TileLumarButton.getItemStack(k, LumarButtonType.SelfLatch, LumarButtonModel.Plate),
				" d ",
				"rbg",
				'b', Blocks.stone_pressure_plate,
				'g', Items.glowstone_dust,
				'r', new ItemStack(gates, 1, EnumGates.RSLATCH.ordinal()),
				'd', dyeOreDictName));
			
			GameRegistry.addRecipe(new ShapedOreRecipe(TileLumarButton.getItemStack(k, LumarButtonType.Toggle, LumarButtonModel.Plate),
				" d ",
				"rbg",
				'b', Blocks.stone_pressure_plate,
				'g', Items.glowstone_dust,
				'r', new ItemStack(gates, 1, EnumGates.TOGGLE.ordinal()),
				'd', dyeOreDictName));
		}
		
		GameRegistry.addRecipe(new RecipeDyeLumarButton());
		
		GameRegistry.addRecipe(new ItemStack(plainBlock, 16, RLNormalBlock.META_CLEANWALL),
			"SBS",
			"ISI",
			"SBS",
			'S', Blocks.sand,
			'I', Items.iron_ingot,
			'B', Blocks.brick_block);
		
		GameRegistry.addRecipe(new ItemStack(plainBlock, 1, RLNormalBlock.META_CLEANFILTER),
			"BBB",
			"BWB",
			"BBB",
			'B', Blocks.iron_bars,
			'W', new ItemStack(plainBlock, 1, RLNormalBlock.META_CLEANWALL));
		
		GameRegistry.addRecipe(new ItemStack(machineBlock, 1, RLMachineBlock.META_CHIP_SCANNER),
			"RRR",
			"RWR",
			"RDR",
			'R', Items.redstone,
			'D', Items.diamond,
			'W', new ItemStack(plainBlock, 1, RLNormalBlock.META_CLEANWALL));
		
		GameRegistry.addRecipe(new ItemStack(machineBlock, 1, RLMachineBlock.META_IO_MARKER),
			"OOO",
			"OGO",
			"ORO",
			'R', Items.redstone,
			'O', Blocks.obsidian,
			'G', Blocks.gold_block);
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machineBlock, 1, RLMachineBlock.META_CHIP_COMPILER),
			"ORR",
			"ODR",
			"OYR",
			'D', Blocks.diamond_block,
			'O', Blocks.obsidian,
			'R', Items.redstone,
			'Y', "dyeYellow"
			));
		
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(machineBlock, 1, RLMachineBlock.META_CHIP_FABRICATOR),
			"ORR",
			"OIR",
			"OWR",
			'I', Blocks.iron_block,
			'O', Blocks.obsidian,
			'R', Items.redstone,
			'W', "dyeWhite"
			));
		
		GameRegistry.addRecipe(new ItemStack(arrayCells, 1, ArrayCellType.NULL.ordinal()),
			"SXS",
			"XXX",
			"SXS",
			'S', Blocks.stone,
			'X', new ItemStack(wire, 1, EnumWireType.RED_ALLOY.ordinal())
			);
		
		GameRegistry.addRecipe(new ItemStack(arrayCells, 1, ArrayCellType.INVERT.ordinal()),
			"SXS",
			"XiX",
			"SXS",
			'S', Blocks.stone,
			'X', new ItemStack(wire, 1, EnumWireType.RED_ALLOY.ordinal()),
			'i', Blocks.redstone_torch
			);
		
		GameRegistry.addRecipe(new ItemStack(arrayCells, 1, ArrayCellType.NON_INVERT.ordinal()),
			"SXS",
			"XrX",
			"SXS",
			'S', Blocks.stone,
			'X', new ItemStack(wire, 1, EnumWireType.RED_ALLOY.ordinal()),
			'r', Items.repeater
			);
	}

}
