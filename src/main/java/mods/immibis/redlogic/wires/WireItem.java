package mods.immibis.redlogic.wires;

import java.util.Locale;

import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.EnumPosition;
import mods.immibis.microblocks.api.IMicroblockCoverSystem;
import mods.immibis.microblocks.api.IMicroblockSupporterTile;
import mods.immibis.microblocks.api.MicroblockAPIUtils;
import mods.immibis.microblocks.api.Part;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class WireItem extends ItemBlock {
	
	public WireItem(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack is) {
		int damage = is.getItemDamage();
		boolean jacketed = WireDamageValues.isJacketed(damage);
		EnumWireType type = WireDamageValues.getType(damage);
		if(type == null)
			return "item.redlogic.invalid";
		
		String name = "item.redlogic.wire." + type.name().toLowerCase(Locale.ROOT).replace('_', '-');
		
		if(jacketed)
			name += ".j";
		
		return name;
	}
	
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer ply, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		Block var11 = w.getBlock(x, y, z);
		
		EnumWireType type = WireDamageValues.getType(stack.getItemDamage());
		if(type == null)
			return false;
		
		boolean jacketed = WireDamageValues.isJacketed(stack.getItemDamage());
		
		if(!jacketed) {
			
			// placing normal wire
			
			if(!Utils.canPlaceWireOnSide(w, x, y, z, ForgeDirection.VALID_DIRECTIONS[side], false))
				return false; // the block-side that was clicked on must be solid
	
	        boolean mergeIntoWireTile = false;
			boolean mergeIntoMicroblockTile = false;
			
			Block microblockContainerBlock = MicroblockAPIUtils.getMicroblockContainerBlock();
			
			if (var11.equals(Blocks.snow_layer))
	            side = 1;
	        else if (!mergeIntoWireTile && !var11.equals(Blocks.vine) && !var11.equals(Blocks.tallgrass) && !var11.equals(Blocks.deadbush) && !var11.isReplaceable(w, x, y, z))
	        {
	        	switch(side) {
	        	case Dir.NX: x--; break;
	        	case Dir.PX: x++; break;
	        	case Dir.NY: y--; break;
	        	case Dir.PY: y++; break;
	        	case Dir.NZ: z--; break;
	        	case Dir.PZ: z++; break;
	        	}
	        	
	        	mergeIntoWireTile = w.getBlock(x, y, z).equals(RedLogicMod.wire) && ((WireTile)w.getTileEntity(x, y, z)).canPlaceWireOnSide(type, side ^ 1);
	        	mergeIntoMicroblockTile = microblockContainerBlock != null && w.getBlock(x, y, z).equals(microblockContainerBlock) && !((IMicroblockSupporterTile)w.getTileEntity(x, y, z)).getCoverSystem().isPositionOccupied(EnumPosition.getFacePosition(side ^ 1));
	        }
	
	        if (stack.stackSize == 0)
	            return false;
	        else if (!ply.canPlayerEdit(x, y, z, side, stack))
	            return false;
	        else if(mergeIntoWireTile) {
	        	if(w.isRemote)
	        		return true;
	        	
	        	WireTile wt = (WireTile)w.getTileEntity(x, y, z);
	        	
	        	if(wt.addWire(type, side ^ 1)) {
	        		Block var12 = Block.getBlockFromItem(this);
	        		w.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, var12.stepSound.getBreakSound(), (var12.stepSound.getVolume() + 1.0F) / 2.0F, var12.stepSound.getPitch() * 0.8F);
	        		--stack.stackSize;
	        	}
	        	
	        	return true;
	        	
	        } else if(mergeIntoMicroblockTile) {
	        	if(w.isRemote)
	        		return true;
	        	
	        	IMicroblockSupporterTile oldTile = (IMicroblockSupporterTile)w.getTileEntity(x, y, z);
	        	
	        	WireTile tile;
	        	
	        	try {
					tile = type.teclass.getConstructor().newInstance();
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
	        	
	        	tile.rawAddWire(type, side ^ 1);
	        	for(Part p : oldTile.getCoverSystem().getAllParts())
	        		tile.getCoverSystem().addPart(p);
	
	        	// don't cause block or client update before tile is set
	        	w.setBlock(x, y, z, RedLogicMod.wire, EnumWireType.CLASS_TO_META.get(tile.getClass()), 0);
	        	w.setTileEntity(x, y, z, tile);
	        	w.markBlockForUpdate(x, y, z);
	        	
	    		w.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, var11.stepSound.getBreakSound(), (var11.stepSound.getVolume() + 1.0F) / 2.0F, var11.stepSound.getPitch() * 0.8F);
	    		--stack.stackSize;
	    		
	    		tile.notifyExtendedNeighbours();
	        	
	        	return true;
	        	
	        } else if (w.canPlaceEntityOnSide(Block.getBlockFromItem(this), x, y, z, false, side, ply, stack)) {
	        	if(w.isRemote)
	        		return true;
	        	
	            int var13 = this.getMetadata(stack.getItemDamage());
	
	            if (placeBlockAt(stack, ply, w, x, y, z, side, hitX, hitY, hitZ, var13))
	            {
	                w.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, var11.stepSound.getBreakSound(), (var11.stepSound.getVolume() + 1.0F) / 2.0F, var11.stepSound.getPitch() * 0.8F);
	                --stack.stackSize;
	            }
	
	            return true;
	        }
	        else
	        {
	            return false;
	        }
	        
		} else {
			// placing jacketed wire
			
	        boolean mergeIntoWireTile = false;
			boolean mergeIntoMicroblockTile = false;
			
			Block microblockContainerBlock = MicroblockAPIUtils.getMicroblockContainerBlock();
			
			if(w.getBlock(x, y, z).equals(RedLogicMod.wire)) {
				WireTile wt = (WireTile)w.getTileEntity(x, y, z);
				if(wt.canAddJacketedWire(type)) {
					mergeIntoWireTile = true;
				}
			}
			
			if (!mergeIntoWireTile && !var11.equals(Blocks.vine) && !var11.equals(Blocks.tallgrass) && !var11.equals(Blocks.deadbush) && !var11.isReplaceable(w, x, y, z))
	        {
	        	switch(side) {
	        	case Dir.NX: x--; break;
	        	case Dir.PX: x++; break;
	        	case Dir.NY: y--; break;
	        	case Dir.PY: y++; break;
	        	case Dir.NZ: z--; break;
	        	case Dir.PZ: z++; break;
	        	}
	        }
			
			mergeIntoMicroblockTile = microblockContainerBlock != null && w.getBlock(x, y, z).equals(microblockContainerBlock);
			if(mergeIntoMicroblockTile) {
				
				IMicroblockCoverSystem imcs = ((IMicroblockSupporterTile)w.getTileEntity(x, y, z)).getCoverSystem();
				if(imcs.isPositionOccupied(EnumPosition.Centre) || imcs.isPositionOccupied(EnumPosition.PostX)
					|| imcs.isPositionOccupied(EnumPosition.PostY) || imcs.isPositionOccupied(EnumPosition.PostZ))
					mergeIntoMicroblockTile = false;
			}
	
	        if (stack.stackSize == 0)
	            return false;
	        else if (!ply.canPlayerEdit(x, y, z, side, stack))
	            return false;
	        else if(mergeIntoWireTile) {
	        	if(w.isRemote)
	        		return true;
	        	
	        	WireTile wt = (WireTile)w.getTileEntity(x, y, z);
	        	
	        	if(wt.addJacketedWire(type)) {
	        		w.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, var11.stepSound.getBreakSound(), (var11.stepSound.getVolume() + 1.0F) / 2.0F, var11.stepSound.getPitch() * 0.8F);
	        		--stack.stackSize;
	        	}
	        	
	        	return true;
	        	
	        } else if(mergeIntoMicroblockTile) {
	        	if(w.isRemote)
	        		return true;
	        	
	        	IMicroblockSupporterTile oldTile = (IMicroblockSupporterTile)w.getTileEntity(x, y, z);
	        	
	        	WireTile tile;
	        	
	        	try {
					tile = type.teclass.getConstructor().newInstance();
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
	        	
	        	tile.rawAddJacketedWire(type);
	        	for(Part p : oldTile.getCoverSystem().getAllParts())
	        		tile.getCoverSystem().addPart(p);
	
	        	// don't cause block or client update before tile is set
	        	w.setBlock(x, y, z, RedLogicMod.wire, EnumWireType.CLASS_TO_META.get(tile.getClass()), 0);
	        	w.setTileEntity(x, y, z, tile);
	        	w.markBlockForUpdate(x, y, z);
	        	
	    		w.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, var11.stepSound.getBreakSound(), (var11.stepSound.getVolume() + 1.0F) / 2.0F, var11.stepSound.getPitch() * 0.8F);
	    		--stack.stackSize;
	    		
	    		tile.notifyExtendedNeighbours();
	        	
	        	return true;
	        	
	        } else if (w.canPlaceEntityOnSide(Block.getBlockFromItem(this), x, y, z, false, side, ply, stack)) {
	        	if(w.isRemote)
	        		return true;
	        	
	        	
	            int var13 = this.getMetadata(stack.getItemDamage());
	
	            if (placeBlockAt(stack, ply, w, x, y, z, side, hitX, hitY, hitZ, var13))
	            {
	                w.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, var11.stepSound.getBreakSound(), (var11.stepSound.getVolume() + 1.0F) / 2.0F, var11.stepSound.getPitch() * 0.8F);
	                --stack.stackSize;
	            }
	
	            return true;
	        }
	        else
	        {
	            return false;
	        }
		}
	}
	
	@Override
	public boolean func_150936_a(World w, int x, int y, int z, int side, EntityPlayer ply, ItemStack stack) {
		Block var11 = w.getBlock(x, y, z);
		
		EnumWireType type = WireDamageValues.getType(stack.getItemDamage());
		if(type == null)
			return false;
		
		if(!WireDamageValues.isJacketed(stack.getItemDamage())) {
			if(!Utils.canPlaceWireOnSide(w, x, y, z, ForgeDirection.VALID_DIRECTIONS[side], false))
				return false;
			
			boolean mergeIntoWireTile = false;
			boolean mergeIntoMicroblockTile = false;
	
			Block microblockContainerBlock = MicroblockAPIUtils.getMicroblockContainerBlock();
			
			if (var11.equals(Blocks.snow_layer))
	            side = 1;
	        else if (!mergeIntoWireTile && !mergeIntoMicroblockTile && !var11.equals(Blocks.vine) && !var11.equals(Blocks.tallgrass) && !var11.equals(Blocks.deadbush) && !var11.isReplaceable(w, x, y, z))
	        {
	        	switch(side) {
	        	case Dir.NX: x--; break;
	        	case Dir.PX: x++; break;
	        	case Dir.NY: y--; break;
	        	case Dir.PY: y++; break;
	        	case Dir.NZ: z--; break;
	        	case Dir.PZ: z++; break;
	        	}
	        }
			mergeIntoMicroblockTile = microblockContainerBlock != null && w.getBlock(x, y, z).equals(microblockContainerBlock) && !((IMicroblockSupporterTile)w.getTileEntity(x, y, z)).getCoverSystem().isPositionOccupied(EnumPosition.getFacePosition(side ^ 1));
	        mergeIntoWireTile = w.getBlock(x, y, z).equals(RedLogicMod.wire) && ((WireTile)w.getTileEntity(x, y, z)).canPlaceWireOnSide(type, side ^ 1);
			
	        return mergeIntoMicroblockTile || mergeIntoWireTile || w.canPlaceEntityOnSide(Block.getBlockFromItem(this), x, y, z, false, side, ply, stack);
		
		} else {
			// jacketed wire
			if(var11.equals(RedLogicMod.wire) && ((WireTile)w.getTileEntity(x, y, z)).canAddJacketedWire(type))
				return true;
	
			Block microblockContainerBlock = MicroblockAPIUtils.getMicroblockContainerBlock();
			
			if(!var11.equals(Blocks.snow_layer) && !var11.equals(Blocks.vine) && !var11.equals(Blocks.tallgrass) && !var11.equals(Blocks.deadbush) && !var11.isReplaceable(w, x, y, z))
			{
	        	switch(side) {
	        	case Dir.NX: x--; break;
	        	case Dir.PX: x++; break;
	        	case Dir.NY: y--; break;
	        	case Dir.PY: y++; break;
	        	case Dir.NZ: z--; break;
	        	case Dir.PZ: z++; break;
	        	}
	        }
	        
	        boolean mergeIntoMicroblockTile = microblockContainerBlock != null && w.getBlock(x, y, z).equals(microblockContainerBlock);
			if(mergeIntoMicroblockTile) {
				
				IMicroblockCoverSystem imcs = ((IMicroblockSupporterTile)w.getTileEntity(x, y, z)).getCoverSystem();
				if(imcs.isPositionOccupied(EnumPosition.Centre) || imcs.isPositionOccupied(EnumPosition.PostX)
					|| imcs.isPositionOccupied(EnumPosition.PostY) || imcs.isPositionOccupied(EnumPosition.PostZ))
					mergeIntoMicroblockTile = false;
			}
	        
	        return mergeIntoMicroblockTile || w.canPlaceEntityOnSide(Block.getBlockFromItem(this), x, y, z, false, side, ply, stack);
		}
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		Block me = Block.getBlockFromItem(this);
		
		EnumWireType type = WireDamageValues.getType(stack.getItemDamage());
		if(type == null)
			return false;
		
		if(!world.setBlock(x, y, z, me, EnumWireType.CLASS_TO_META.get(type.teclass), 0))
			return false;
		
		if(world.getBlock(x, y, z).equals(me)) {
			me.onBlockPlacedBy(world, x, y, z, player, stack);
			
			WireTile tile;
			
			try {
				tile = type.teclass.getConstructor().newInstance();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			
			if(WireDamageValues.isJacketed(stack.getItemDamage()))
				tile.rawAddJacketedWire(type);
			else
				tile.rawAddWire(type, side ^ 1);
			
			world.setTileEntity(x, y, z, tile);
			tile.notifyExtendedNeighbours();
		}

		return true;
	}
	
	@Override
	public int getMetadata(int par1) {
		return par1;
	}
}
