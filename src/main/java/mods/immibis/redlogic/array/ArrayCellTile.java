package mods.immibis.redlogic.array;

import java.util.List;

import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.EnumAxisPosition;
import mods.immibis.microblocks.api.EnumPosition;
import mods.immibis.microblocks.api.IMicroblockCoverSystem;
import mods.immibis.microblocks.api.Part;
import mods.immibis.microblocks.api.PartType;
import mods.immibis.microblocks.api.util.TileCoverableMultipartBase;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.Utils;
import mods.immibis.redlogic.api.wiring.IConnectable;
import mods.immibis.redlogic.api.wiring.IRedAlloyPropagator;
import mods.immibis.redlogic.api.wiring.IRedstoneEmitter;
import mods.immibis.redlogic.api.wiring.IRedstoneUpdatable;
import mods.immibis.redlogic.api.wiring.IRedstoneWire;
import mods.immibis.redlogic.api.wiring.IWire;
import mods.immibis.redlogic.api.wiring.IWireUpdateOperation;
import mods.immibis.redlogic.rendering.ArrayCellRenderer;

public class ArrayCellTile extends TileCoverableMultipartBase implements IConnectable, IRedstoneEmitter, IRedstoneUpdatable, IRedAlloyPropagator {

	private static final int LOGIC_DELAY = 2; // ticks
	
	public static double getThickness(ArrayCellType type) {
		return type == ArrayCellType.NULL ? 0.375 : 0.75;
	}
	
	@Override
	public boolean canUpdate() {
		return false;
	}
	
	private ArrayCellType type = ArrayCellType.NULL;
	
	private byte side; // side of the block the gate is on
	private byte front; // direction the "front" of the gate is facing
	//private boolean isUpdatingFB, recursiveUpdatePendingFB;
	//private boolean isUpdatingLR, recursiveUpdatePendingLR;
	private boolean isUpdatingStrength, recursiveUpdatePending;
	boolean tickPending;
	
	private short frontBackStrength;
	private short leftRightStrength;
	private short emittedStrength;
	
	@Override
	public EnumPosition getPartPosition(int subHit) {
		return EnumPosition.getFacePosition(side);
	}

	@Override
	public boolean isPlacementBlocked(PartType<?> type, EnumPosition pos) {
		if(type.getSize() > 1-getThickness(this.type))
			return true;
		
		switch(side) {
		case Dir.NX: return pos.x != EnumAxisPosition.Positive;
		case Dir.PX: return pos.x != EnumAxisPosition.Negative;
		case Dir.NY: return pos.y != EnumAxisPosition.Positive;
		case Dir.PY: return pos.y != EnumAxisPosition.Negative;
		case Dir.NZ: return pos.z != EnumAxisPosition.Positive;
		case Dir.PZ: return pos.z != EnumAxisPosition.Negative;
		}
		
		return true;
	}

	@Override
	public boolean isPositionOccupied(EnumPosition pos) {
		return pos == EnumPosition.getFacePosition(side);
	}

	@Override
	public float getPlayerRelativePartHardness(EntityPlayer ply, int part) {
		return ply.getCurrentPlayerStrVsBlock(RedLogicMod.gates, false) / 0.25f / 30f;
		//return ply.getCurrentPlayerStrVsBlock(RedLogicMod.gates, false, getBlockMetadata()) / 0.25f / 30f;
	}
	
	@Override
	public boolean canPlayerHarvestPart(EntityPlayer entityPlayer, int part) {
		return true;
	}

	@Override
	public ItemStack pickPart(MovingObjectPosition rayTrace, int part) {
		return new ItemStack(RedLogicMod.arrayCells, 1, type.ordinal());
	}

	@Override
	public boolean isPartContainerSideSolid(ForgeDirection side) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderPartContainer(RenderBlocks render) {
		return ArrayCellRenderer.instance.renderActualBlock(render, worldObj, xCoord, yCoord, zCoord, getBlockType(), ArrayCellBlock.renderType);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderPart(RenderBlocks render, int part) {
		return renderPartContainer(render);
	}

	@Override
	public void removePartByPlayer(EntityPlayer ply, int part, boolean harvest) {
		if(harvest)
			RedLogicMod.arrayCells.dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, pickPart(null, 0));
		if(!worldObj.isRemote) {
			IMicroblockCoverSystem cs = getCoverSystem();
			if(cs != null)
				cs.convertToContainerBlock();
			else
				worldObj.setBlockToAir(xCoord, yCoord, zCoord);
		}
	}
	
	@Override
	public void getPartContainerDrops(List<ItemStack> drops, int fortune) {
		drops.add(pickPart(null, 0));
	}

	@Override
	public AxisAlignedBB getPartAABBFromPool(int part) {
		return Part.getBoundingBoxFromPool(EnumPosition.getFacePosition(side), getThickness(type));
	}

	@Override
	protected int getNumTileOwnedParts() {
		return 1;
	}

	public int getSide() {
		return side;
	}

	public int getFront() {
		return front;
	}

	public ArrayCellType getType() {
		return type;
	}

	@Override
	public short getEmittedSignalStrength(int blockFace, int toDirection) {
		if(blockFace != side || worldObj.isRemote)
			return 0;
		if((toDirection & 6) == (front & 6))
			return frontBackStrength;
		else
			return leftRightStrength;
	}

	// gets a direction that is either left or right
	private int getSideDirection() {
		int k = (front + 2) % 6;
		if((k & 6) == (side & 6))
			k = (k + 2) % 6;
		assert (k & 6) != (side & 6);
		assert (k & 6) != (front & 6);
		return k;
	}
	
	private int getUpdatedFrontBackStrength() {
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[front];
		int s = Math.max(Utils.getPowerStrength(worldObj, xCoord + fd.offsetX, yCoord + fd.offsetY, zCoord + fd.offsetZ, front^1, side),
			Utils.getPowerStrength(worldObj, xCoord - fd.offsetX, yCoord - fd.offsetY, zCoord - fd.offsetZ, front, side));
		return Math.max(0, s-1);
	}
	
	private int getUpdatedLeftRightStrength() {
		int a_side = getSideDirection();
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[a_side];
		int s = Math.max(Utils.getPowerStrength(worldObj, xCoord + fd.offsetX, yCoord + fd.offsetY, zCoord + fd.offsetZ, a_side^1, side),
			Utils.getPowerStrength(worldObj, xCoord - fd.offsetX, yCoord - fd.offsetY, zCoord - fd.offsetZ, a_side, side));
		return Math.max(0, Math.max(s-1, emittedStrength));
	}
	
	private int getEmittedStrengthLR() {
		if(type == ArrayCellType.INVERT)
			return frontBackStrength == 0 ? 255 : 0;
		if(type == ArrayCellType.NON_INVERT)
			return frontBackStrength == 0 ? 0 : 255;
		return 0;
	}
	
	private void propagateRedstoneFB(IWireUpdateOperation updateOperation) {
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[front];
		propagateRedstone(xCoord + fd.offsetX, yCoord + fd.offsetY, zCoord + fd.offsetZ, updateOperation);
		propagateRedstone(xCoord - fd.offsetX, yCoord - fd.offsetY, zCoord - fd.offsetZ, updateOperation);
	}
	
	private void propagateRedstoneLR(IWireUpdateOperation updateOperation) {
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[getSideDirection()];
		propagateRedstone(xCoord + fd.offsetX, yCoord + fd.offsetY, zCoord + fd.offsetZ, updateOperation);
		propagateRedstone(xCoord - fd.offsetX, yCoord - fd.offsetY, zCoord - fd.offsetZ, updateOperation);
	}
	
	private void propagateRedstone(int x, int y, int z, IWireUpdateOperation updateOperation) {
		if(!worldObj.blockExists(x, y, z))
			return;
		TileEntity te = worldObj.getTileEntity(x, y, z);
		if(te instanceof IRedAlloyPropagator)
			((IRedAlloyPropagator)te).propagateRedAlloySignal(updateOperation);
		else
			updateOperation.queueBlockUpdate(x, y, z);
	}

	@Override
	public void propagateRedAlloySignal(IWireUpdateOperation updateOperation) {
		/*int newFB, newLR, oldFB, oldLR;
		
		newFB = getUpdatedFrontBackStrength();
		newLR = getUpdatedLeftRightStrength();
		oldFB = frontBackStrength;
		oldLR = leftRightStrength;*/
		
		if(isUpdatingStrength) {
			recursiveUpdatePending = true;
			return;
		}
		
		try {
			isUpdatingStrength = true;
			
			int origFB = frontBackStrength, origLR = leftRightStrength, origEmit = emittedStrength;
			
			recursiveUpdatePending = true;
			while(recursiveUpdatePending) {
				recursiveUpdatePending = false;
				
				int newFB, newLR, oldFB, oldLR;
				
				while(true) {
					
					newFB = getUpdatedFrontBackStrength();
					newLR = getUpdatedLeftRightStrength();
					oldFB = frontBackStrength;
					oldLR = leftRightStrength;
					
					if(newFB >= oldFB && newLR >= oldLR)
						break;
					
					if(newFB < oldFB) frontBackStrength = 0;
					if(newLR < oldLR) leftRightStrength = 0;
					
					if(newFB < oldFB) propagateRedstoneFB(updateOperation);
					if(newLR < oldLR) propagateRedstoneLR(updateOperation);
				}
				
				frontBackStrength = (short)newFB;
				leftRightStrength = (short)newLR;
				
				if(newFB > oldFB) propagateRedstoneFB(updateOperation);
				if(newLR > oldLR) propagateRedstoneLR(updateOperation);
			}
			
			if(emittedStrength != getEmittedStrengthLR() && !tickPending) {
				tickPending = true;
				worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, RedLogicMod.arrayCells, LOGIC_DELAY);
			}
			
			if(origFB != frontBackStrength || origLR != leftRightStrength)
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			
		} finally {
			isUpdatingStrength = false;
		}
	}
	
	@Override
	public void onRedstoneInputChanged() {
		try (IWireUpdateOperation operation = IWireUpdateOperation.create(worldObj)) {
			propagateRedAlloySignal(operation);
			operation.finish();
		}
	}


	@Override
	public boolean connects(IWire wire, int blockFace, int fromDirection) {
		return blockFace == side && fromDirection != -1 && wire instanceof IRedstoneWire;
	}

	@Override
	public boolean connectsAroundCorner(IWire wire, int blockFace, int fromDirection) {
		return false;
	}

	public void init(ArrayCellType type, int side, int front) {
		this.type = type;
		this.side = (byte)side;
		this.front = (byte)front;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound data = pkt.func_148857_g();
		int actionType = data.getShort("a");
		side = (byte)(actionType & 7);
		front = (byte)((actionType >> 3) & 7);
		type = ArrayCellType.VALUES[(actionType >> 6) & 3];
		frontBackStrength = (short)((data.getShort("S") >> 8) & 255);
		leftRightStrength = (short)(data.getShort("S") & 255);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		if(cover != null && data.hasKey("C"))
			cover.readDescriptionBytes(data.getByteArray("C"), 0);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setShort("a", (short)(side | (front << 3) | (type.ordinal() << 6)));
		tag.setShort("S", (short)((frontBackStrength << 8) | (leftRightStrength)));
		if(cover != null)
			tag.setByteArray("C", cover.writeDescriptionBytes());
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("side", side);
		tag.setByte("front", front);
		tag.setByte("type", (byte)type.ordinal());
		tag.setShort("lrs", leftRightStrength);
		tag.setShort("fbs", frontBackStrength);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		side = tag.getByte("side");
		front = tag.getByte("front");
		type = ArrayCellType.VALUES[tag.getByte("type")];
		frontBackStrength = tag.getShort("fbs");
		leftRightStrength = tag.getShort("lrs");
	}

	public int getStrengthFB() {
		return frontBackStrength;
	}
	
	public int getStrengthLR() {
		return leftRightStrength;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addPartDestroyEffects(int part, EffectRenderer er) {
		return Utils.addPartDestroyEffects(this, part, er);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addPartHitEffects(int part, int sideHit, EffectRenderer er) {
		return Utils.addPartHitEffects(this, part, sideHit, er);
	}

	public boolean checkCanStay() {
		return checkCanStay(worldObj, xCoord, yCoord, zCoord, side);
	}
	
	public static boolean checkCanStay(World world, int x, int y, int z, int side) {
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side];
		x += fd.offsetX;
		y += fd.offsetY;
		z += fd.offsetZ;
		return world.isSideSolid(x, y, z, ForgeDirection.VALID_DIRECTIONS[side^1], true);
	}

	public void rotate() {
		do
			front = (byte)((front + 2) % 6);
		while((front & 6) == (side & 6));
		
		onRedstoneInputChanged();
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	void updateEmittedStrength() {
		emittedStrength = (short)getEmittedStrengthLR();
	}
}
