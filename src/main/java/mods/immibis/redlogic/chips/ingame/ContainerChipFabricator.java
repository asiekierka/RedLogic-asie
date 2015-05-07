package mods.immibis.redlogic.chips.ingame;

import mods.immibis.core.api.util.BaseContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerChipFabricator extends BaseContainer<TileChipFabricator> {
	public ContainerChipFabricator(EntityPlayer ply, TileChipFabricator inv) {
		super(ply, inv);
		
		addSlotToContainer(new Slot(inv, TileChipFabricator.SLOT_REDSTONE, 34, 22));
		addSlotToContainer(new Slot(inv, TileChipFabricator.SLOT_STONE, 53, 22));
		addSlotToContainer(new Slot(inv, TileChipFabricator.SLOT_PHOTOMASK, 72, 22));
		addSlotToContainer(new Slot(inv, TileChipFabricator.SLOT_OUT, 140, 46));
		
		for(int k = 0; k < 9; k++)
			addSlotToContainer(new Slot(ply.inventory, k, 13 + 18*k, 129));
		for(int y = 0; y < 3; y++)
			for(int x = 0; x < 9; x++)
				addSlotToContainer(new Slot(ply.inventory, 9 + y*9 + x, 13 + 18*x, 71 + 18*y));
	}
	
	int progress; // out of 101
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		setProgressBar(0, (int)(inv.getProgress() * 102));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int par1, int par2) {
		if(par1 == 0)
			progress = par2;
	}
}
