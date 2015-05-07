package mods.immibis.redlogic.chips.ingame;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import mods.immibis.core.TileCombined;
import mods.immibis.redlogic.api.chips.scanner.CircuitLayoutException;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannableTile;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.api.chips.scanner.NodeType;
import mods.immibis.redlogic.api.wiring.IBundledWire;
import mods.immibis.redlogic.api.wiring.IConnectable;
import mods.immibis.redlogic.api.wiring.IRedstoneWire;
import mods.immibis.redlogic.api.wiring.IWire;
import mods.immibis.redlogic.chips.builtin.ScannedInputBlock;
import mods.immibis.redlogic.chips.builtin.ScannedOutputBlock;

public class TileIOMarker extends TileCombined implements IConnectable, IScannableTile {
	@Override public boolean canUpdate() {return false;}
	
	private boolean isOutput;
	private NodeType type = NodeType.SINGLE_WIRE;
	
	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		isOutput = par1nbtTagCompound.getBoolean("output");
		
		try {
			type = NodeType.values()[par1nbtTagCompound.getInteger("nodetype")];
		} catch(ArrayIndexOutOfBoundsException e) {
			type = NodeType.SINGLE_WIRE;
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setBoolean("output", isOutput);
		par1nbtTagCompound.setInteger("nodetype", type.ordinal());
	}
	
	public void toggleMode() {
		isOutput = !isOutput;
		if(!isOutput) {
			type = NodeType.values()[(type.ordinal() + 1) % NodeType.values().length];
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		}
		
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setShort("a", (short)((isOutput?1:0) | (type.ordinal()<<1)));
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}
	
	@Override
	public void onDataPacket(S35PacketUpdateTileEntity packet) {
		int actionType = packet.func_148857_g().getShort("a");
		isOutput = (actionType&1)!=0;
		type = NodeType.values()[actionType>>1];
		
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public boolean connects(IWire wire, int blockFace, int fromDirection) {
		return (wire instanceof IRedstoneWire && type == NodeType.SINGLE_WIRE) || (wire instanceof IBundledWire && type == NodeType.BUNDLED);
	}
	
	@Override
	public boolean connectsAroundCorner(IWire wire, int blockFace, int fromDirection) {
		return false;
	}

	public boolean isOutput() {
		return isOutput;
	}
	
	@Override
	public IScannedBlock getScannedBlock(IScanProcess process) throws CircuitLayoutException {
		if(isOutput)
			return new ScannedOutputBlock(process, type);
		else
			return new ScannedInputBlock(process, type);
	}

	public NodeType getNodeType() {
		return type;
	}
}
