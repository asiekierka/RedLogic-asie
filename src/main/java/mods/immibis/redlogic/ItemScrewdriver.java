package mods.immibis.redlogic;

import mods.immibis.redlogic.array.ArrayCellTile;
import mods.immibis.redlogic.chips.ingame.TileChipScanner;
import mods.immibis.redlogic.chips.ingame.TileCustomCircuit;
import mods.immibis.redlogic.chips.ingame.TileIOMarker;
import mods.immibis.redlogic.gates.GateTile;
import mods.immibis.redlogic.interaction.TileLumarButton;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class ItemScrewdriver extends Item {
	public ItemScrewdriver() {
		super();
		setUnlocalizedName("redlogic.screwdriver");
		setTextureName("redlogic:screwdriver");
		setCreativeTab(CreativeTabs.tabTools);
		setMaxStackSize(1);
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer ply, World w, int x, int y, int z, int side, float par8, float par9, float par10) {
		Block block = w.getBlock(x, y, z);
		TileEntity te = w.getTileEntity(x, y, z);
		
		if(block == RedLogicMod.gates) {
		
			if(w.isRemote)
				return true;
			
			GateTile te_ = (GateTile)te;
			if(ply.isSneaking())
				te_.configure();
			else
				te_.rotate();
			
			return true;
		}
		
		if(block == RedLogicMod.arrayCells) {
			
			if(w.isRemote)
				return true;
			
			((ArrayCellTile)te).rotate();
			
			return true;
		}
		
		if(block == RedLogicMod.machineBlock) {
			if(te instanceof TileIOMarker) {
				if(!w.isRemote)
					((TileIOMarker)te).toggleMode();
				return true;
			}
			
			if(te instanceof TileChipScanner) {
				if(!w.isRemote)
					((TileChipScanner)te).rotate();
				return true;
			}
			
			return false;
		}
		
		if(block == RedLogicMod.lumarButton) {
			if(te instanceof TileLumarButton) {
				if(!w.isRemote)
					((TileLumarButton)te).configure(ply);
				return true;
			}
			return false;
		}
		
		if(block == RedLogicMod.customCircuitBlock) {
			if(!w.isRemote)
				((TileCustomCircuit)te).rotate();
			return true;
		}
		
		return false;
	}
}
