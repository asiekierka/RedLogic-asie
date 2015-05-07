package mods.immibis.redlogic;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import mods.immibis.core.ItemCombined;
import mods.immibis.redlogic.chips.ingame.TileChipScanner;

public class RLMachineBlockItem extends ItemCombined {
	public RLMachineBlockItem(Block block) {
		super(block, "redlogic", new String[] {
			"chipscanner",
			"chipiomarker",
			"chipcompiler",
			"chipfabricator",
		});
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		if(!super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
			return false;
		
		if(metadata == RLMachineBlock.META_CHIP_SCANNER) {
			TileEntity te = world.getTileEntity(x, y, z);
			if(te instanceof TileChipScanner)
				((TileChipScanner)te).setInitialRotation(player);
		}
		
		return true;
	}
}
