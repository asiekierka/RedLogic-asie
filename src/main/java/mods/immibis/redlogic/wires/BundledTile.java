package mods.immibis.redlogic.wires;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;
import mods.immibis.redlogic.CommandDebug;
import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import mods.immibis.redlogic.api.wiring.IBundledPropagator;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import mods.immibis.redlogic.api.wiring.IBundledWire;
import mods.immibis.redlogic.api.wiring.IWireUpdateOperation;

public class BundledTile extends WireTile implements IBundledEmitter, IBundledUpdatable, IBundledWire, IBundledPropagator {
	
	private byte[] strength = new byte[16];
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		tag.setByteArray("strength", strength);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		strength = tag.getByteArray("strength");
	}
	
	@Override
	protected boolean canConnectToWire(WireTile wire) {
		return super.canConnectToWire(wire) || wire instanceof InsulatedRedAlloyTile;
	}

	@Override
	public boolean canUpdate() {
		return false;
	}
	
	private byte[] oldStrength = new byte[16];
	
	private short isUpdating = 0;
	private short recursiveUpdatePending = -1;
	
	private void updateStrengthFromBlock(int x, int y, int z, int side, int dir, short mask) {
		TileEntity te = worldObj.getTileEntity(x, y, z);
		
		if(te instanceof InsulatedRedAlloyTile) {
			InsulatedRedAlloyTile o = (InsulatedRedAlloyTile)te;
			int colour = o.getInsulatedWireColour();
			if((mask & (1 << colour)) == 0)
				return;
			int o_strength = o.getRedstoneSignalStrength() - 1;
			if((strength[colour] & 0xFF) < o_strength)
				strength[colour] = (byte)o_strength;
			
		} else if(te instanceof IBundledEmitter) {
			byte[] o_strength = ((IBundledEmitter)te).getBundledCableStrength(side, dir);
			if(o_strength != null) {
				// null = all 0
				
				for(int k = 0; k < 16; k++) {
					if((mask & (1 << k)) == 0)
						continue;
					int o_c_strength = (o_strength[k] & 0xFF) - 1;
					if((strength[k] & 0xFF) < o_c_strength)
						strength[k] = (byte)o_c_strength;
				}
			}
		}
	}
	
	private void updateStrengthFromSurroundingBlocks(short mask) {
		for(int k = 0; k < 16; k++)
		{
			if((mask & (1 << k)) == 0)
				continue;
			oldStrength[k] = strength[k];
			strength[k] = 0;
		}
		
		for(int side = 0; side < 6; side++) {
			for(int dir = 0; dir < 6; dir++) {
				if(connectsInDirection(side, dir)) {
					ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[dir];
					int x = xCoord + fd.offsetX, y = yCoord + fd.offsetY, z = zCoord + fd.offsetZ;
					
					int oside = side, odir = dir^1;
					
					if(connectsInDirectionAroundCorner(side, dir)) {
						fd = ForgeDirection.VALID_DIRECTIONS[side];
						x += fd.offsetX;
						y += fd.offsetY;
						z += fd.offsetZ;
						
						oside = dir ^ 1; odir = side ^ 1;
					}
					
					updateStrengthFromBlock(x, y, z, oside, odir, mask);
				}
			}
			
			if(connectsInDirectionByJacketedWire(side)) {
				ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side];
				int x = xCoord + fd.offsetX, y = yCoord + fd.offsetY, z = zCoord + fd.offsetZ;
				
				updateStrengthFromBlock(x, y, z, -1, side ^ 1, mask);
			}
		}
	}
	
	private void updateStrength(short updatingColours, IWireUpdateOperation updateOperation) {
		if(worldObj.isRemote)
			return;
		
		short outerUpdatingColours = isUpdating;
		
		// XXX THIS IS TERRIBLY NOT-UNDERSTANDABLE CODE
		// HINT: This is basically RedAlloyTile.updateStrength() but for up to 16 wires in parallel
		// The bitmask updatingColours selects which wires this is being called for.
		// The fields recursiveUpdatePending and isUpdating are replaced with bitmasks.
		
		if(CommandDebug.WIRE_UPDATE_CONSOLE_SPAM)
			System.out.println("BundledTile at "+xCoord+","+yCoord+","+zCoord+" entering updateStrength for colours "+updatingColours+"; "+(updatingColours & outerUpdatingColours)+" recursive and marked pending; "+(updatingColours & ~outerUpdatingColours)+" new");
		
		recursiveUpdatePending |= (updatingColours & outerUpdatingColours);
		updatingColours &= ~outerUpdatingColours;
		
		if(updatingColours == 0)
			return;
		
		if(CommandDebug.WIRE_DEBUG_PARTICLES)
			debugEffect_bonemeal();
		
		isUpdating |= updatingColours;
		
		//try {
			do {
				updatingColours |= (recursiveUpdatePending & ~outerUpdatingColours);
				recursiveUpdatePending &= outerUpdatingColours;
				
				//System.out.println(xCoord+" "+yCoord+" "+zCoord+" inner "+updatingColours);
				
				updateStrengthFromSurroundingBlocks(updatingColours);
				
				short decreased_mask = 0;
				short different_mask = 0;
				for(int k = 0; k < 16; k++) {
					if((updatingColours & (1 << k)) == 0)
						continue;
					if(strength[k] != oldStrength[k])
						different_mask |= 1 << k;
					if((strength[k] & 0xFF) < (oldStrength[k] & 0xFF)) {
						decreased_mask |= 1 << k;
						strength[k] = 0;
					}
				}
				
				if(CommandDebug.WIRE_UPDATE_CONSOLE_SPAM)
					System.out.println("BundledTile at "+xCoord+","+yCoord+","+zCoord+" update step; strength "+Arrays.toString(strength)+"; old strength "+Arrays.toString(oldStrength)+"; changed mask "+different_mask+"; decreased mask "+decreased_mask);
				
				decreased_mask &= updatingColours;
				different_mask &= updatingColours;
				
				if(decreased_mask != 0) {
					
					updateConnectedThings(decreased_mask, updateOperation);
					updateStrengthFromSurroundingBlocks(updatingColours);
					
					different_mask = decreased_mask;
					for(int k = 0; k < 16; k++)
						if(strength[k] != oldStrength[k])
							different_mask |= 1 << k;
				}
				
				if(different_mask != 0)
					updateConnectedThings(different_mask, updateOperation);
				
				if(CommandDebug.WIRE_UPDATE_CONSOLE_SPAM && (recursiveUpdatePending & ~outerUpdatingColours) != 0)
					System.out.println("BundledTile at "+xCoord+","+yCoord+","+zCoord+" processing pending colours "+(recursiveUpdatePending & ~outerUpdatingColours)+"; strength "+Arrays.toString(strength));
				
			} while((recursiveUpdatePending & ~outerUpdatingColours) != 0);
		//} catch(StackOverflowError e) {
		//	e.printStackTrace();
		//	System.err.println("Got stack overflow, aborting!");
		//}
		
		if(CommandDebug.WIRE_UPDATE_CONSOLE_SPAM)
			System.out.println("BundledTile at "+xCoord+","+yCoord+","+zCoord+" exiting updateStrength for colours "+updatingColours);

		isUpdating &= ~updatingColours;
	}
	
	private void updateConnectedThings(short colourMask, IWireUpdateOperation updateOperation) {
		int notifiedSides = 0;
		
		if(CommandDebug.WIRE_DEBUG_PARTICLES)
			debugEffect_bonemeal();
		
		for(int side = 0; side < 6; side++) {
			for(int dir = 0; dir < 6; dir++) {
				if(connectsInDirection(side, dir)) {
					ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[dir];
					int x = xCoord + fd.offsetX, y = yCoord + fd.offsetY, z = zCoord + fd.offsetZ;
					
					if(connectsInDirectionAroundCorner(side, dir)) {
						fd = ForgeDirection.VALID_DIRECTIONS[side];
						x += fd.offsetX;
						y += fd.offsetY;
						z += fd.offsetZ;
					
					} else {
						if((notifiedSides & (1 << dir)) != 0)
							continue;
						notifiedSides |= 1 << dir;
					}
					
					TileEntity t = worldObj.getTileEntity(x, y, z);
					if(t instanceof IBundledPropagator)
						((IBundledPropagator)t).propagateBundledSignal(colourMask, updateOperation);
					else if(t instanceof IBundledUpdatable)
						updateOperation.queueBundledUpdate(t);
				}
			}
			
			if(connectsInDirectionByJacketedWire(side)) {
				if((notifiedSides & (1 << side)) == 0) {
					notifiedSides |= 1 << side;
					
					ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side];
					int x = xCoord + fd.offsetX, y = yCoord + fd.offsetY, z = zCoord + fd.offsetZ;
					
					TileEntity t = worldObj.getTileEntity(x, y, z);
					if(t instanceof IBundledPropagator)
						((IBundledPropagator)t).propagateBundledSignal(colourMask, updateOperation);
					else if(t instanceof IBundledUpdatable)
						updateOperation.queueBundledUpdate(t);
				}
			}
		}
	}
	
	@Override
	public void onBundledInputChanged() {
		try (IWireUpdateOperation operation = IWireUpdateOperation.create(worldObj)) {
			updateStrength((short)-1, operation);
			operation.finish();
		}
	}
	
	@Override
	void onNeighbourBlockChange() {
		super.onNeighbourBlockChange();
		onBundledInputChanged();
	}
	
	@Override
	public byte[] getBundledCableStrength(int blockFace, int direction) {
		return connectsInDirection(blockFace, direction) ? strength : null;
	}
	
	@Override
	protected boolean debug(EntityPlayer ply) {
		if(!worldObj.isRemote)
		{
			int[] i = new int[16];
			for(int k = 0; k < 16; k++)
				i[k] = strength[k] & 0xFF;
			ply.addChatMessage(new ChatComponentText("Bundled cable strength: " + Arrays.toString(i)));
		}
		return true;
	}

	@Override
	public void propagateBundledSignal(short colourMask, IWireUpdateOperation operation) {
		updateStrength(colourMask, operation);
	}
}
