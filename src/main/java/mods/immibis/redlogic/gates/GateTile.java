package mods.immibis.redlogic.gates;


import java.util.Arrays;
import java.util.List;

import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.EnumPosition;
import mods.immibis.microblocks.api.IMicroblockCoverSystem;
import mods.immibis.microblocks.api.Part;
import mods.immibis.microblocks.api.PartType;
import mods.immibis.microblocks.api.util.TileCoverableMultipartBase;
import mods.immibis.redlogic.CommandDebug;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.Utils;
import mods.immibis.redlogic.api.wiring.*;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLong;
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

public class GateTile extends TileCoverableMultipartBase implements IRedstoneUpdatable, IConnectable, IBundledUpdatable, IBundledEmitter, IRedstoneEmitter {
	private EnumGates type; // should never be null
	private GateLogic logic; // null on the client
	private byte side; // side of the block the gate is on
	private byte front; // direction the "front" of the gate is facing
	private boolean isNotStateless;
	private int gateSettings;
	private GateLogic.WithPointer pointer;
	private boolean flipped;
	private boolean hasBundledConnections;
	
	/* // null if not hasBundledConnections, indexed by [relSide][colour]
	private byte[][] bundledOutputs;
	private byte[][] bundledInputs;
	private byte[][] prevBundledOutputs;
	private byte[][] prevBundledInputs;*/
	
	float pointerPos; // rendering only
	float pointerSpeed; // rendering only
	
	@Override
	public Packet getDescriptionPacket() {
		if(type == null)
			return null; // should not happen
		
		NBTTagCompound data = new NBTTagCompound();		
		if(getCoverSystem() != null)
			data.setByteArray("c", getCoverSystem().writeDescriptionBytes());
		data.setByte("t", (byte)(type.ordinal() | (flipped ? 0x80 : 0)));
		data.setByte("s", side);
		data.setByte("f", front);
		data.setShort("r", (short)prevRenderState);
		if(pointer != null) {
			data.setShort("p", (short)pointer.getPointerPosition());
			data.setFloat("P", pointer.getPointerSpeed());
		}
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, data);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		try {
			NBTTagCompound data = pkt.func_148857_g();
			if(getCoverSystem() != null)
				getCoverSystem().readDescriptionBytes(data.getByteArray("c"), 0);
			
			type = EnumGates.VALUES[data.getByte("t") & 0x7F];
			side = data.getByte("s");
			front = data.getByte("f");
			flipped = (data.getByte("t") & 0x80) != 0;
			
			prevRenderState = data.getShort("r") & 0xFFFFF;
			
			if(data.hasKey("p")) {
				pointerPos = data.getShort("p");
				pointerSpeed = data.getFloat("P");
			}
		} catch(Exception e) {
			new Exception("Corrupted RedLogic gate data received at "+xCoord+","+yCoord+","+zCoord, e).printStackTrace();
		}
		
		if(!CommandDebug.DONT_RERENDER)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public int getSide() {
		return side;
	}
	
	public GateTile(EnumGates type, int side, int front) {
		if(type == null)
			throw new IllegalArgumentException("type cannot be null");
		this.type = type;
		this.side = (byte)side;
		this.front = (byte)front;
		createLogic();
	}
	
	public GateTile() {
		this(EnumGates.INVALID, 0, 0);
	}
	
	private long toBitfield16(short[] a) {
		if(a.length > 4)
			throw new IllegalArgumentException("array too long");
		long rv = 0;
		for(int k = 0; k < a.length; k++)
			rv = (rv << 16) | a[k];
		return rv;
	}
	
	private void fromBitfield8(long bf, short[] a) {
		if(a.length > 8)
			throw new IllegalArgumentException("array too long");
		for(int k = a.length - 1; k >= 0; k--) {
			a[k] = (short)(bf & 255);
			bf >>= 8;
		}
	}
	
	private void fromBitfield16(long bf, short[] a) {
		if(a.length > 4)
			throw new IllegalArgumentException("array too long");
		for(int k = a.length - 1; k >= 0; k--) {
			a[k] = (short)bf;
			bf >>= 16;
		}
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		tag.setByte("type", type == null ? -1 : (byte)type.ordinal());
		tag.setByte("side", side);
		tag.setByte("front", front);
		
		tag.setBoolean("version2", true);
		
		tag.setLong("outputs", toBitfield16(outputs));
		tag.setLong("inputs", toBitfield16(inputs));
		tag.setLong("prevOutputs", toBitfield16(prevOutputs));
		
		tag.setShort("renderState", (short)renderState);
		tag.setShort("prevRenderState", (short)prevRenderState);
		tag.setBoolean("updatePending", updatePending);
		tag.setShort("gateSettings", (short)gateSettings);
		tag.setBoolean("flipped", flipped);
		if(logic != null && isNotStateless) {
			NBTTagCompound tag2 = new NBTTagCompound();
			logic.write(tag2);
			tag.setTag("logic", tag2);
		}
		/*if(hasBundledConnections) {
			for(int k = 0; k < 4; k++) {
				if(bundledOutputs[k] != null)
					tag.setByteArray("bundO"+k, bundledOutputs[k]);
				if(bundledInputs[k] != null)
					tag.setByteArray("bundI"+k, bundledInputs[k]);
				if(prevBundledOutputs[k] != null)
					tag.setByteArray("bundPO"+k, prevBundledOutputs[k]);
				if(prevBundledInputs[k] != null)
					tag.setByteArray("bundPI"+k, prevBundledInputs[k]);
			}
		}*/
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		try {
			type = EnumGates.VALUES[tag.getByte("type")];
		} catch(Exception e) {
			type = EnumGates.AND; // shouldn't happen
		}
		side = tag.getByte("side");
		front = tag.getByte("front");
		flipped = tag.getBoolean("flipped");
		
		renderState = tag.getShort("renderState") & 0xFFFF;
		prevRenderState = tag.getShort("prevRenderState") & 0xFFFF;
		
		updatePending = tag.getBoolean("updatePending");
		gateSettings = tag.getShort("gateSettings") & 0xFFFF;
		
		if(tag.getTag("inputs") instanceof NBTTagLong) {
			if(tag.getBoolean("version2")) {
				fromBitfield16(tag.getLong("inputs"), inputs);
				fromBitfield16(tag.getLong("outputs"), outputs);
				fromBitfield16(tag.getLong("prevOutputs"), prevOutputs);
				
				for(int k = 0; k < 4; k++) {
					absOutputs[relToAbsDirection(k)] = outputs[k];
					prevAbsOutputs[relToAbsDirection(k)] = prevOutputs[k];
				}
				
			} else {
				fromBitfield8(tag.getLong("inputs"), inputs);
				fromBitfield8(tag.getLong("outputs"), outputs);
				fromBitfield8(tag.getLong("absOutputs"), absOutputs);
				fromBitfield8(tag.getLong("prevAbsOutputs"), prevAbsOutputs);
				fromBitfield8(tag.getLong("prevOutputs"), prevOutputs);
			}
		}
		
		createLogic();
		
		if(logic != null && tag.hasKey("logic"))
			logic.read(tag.getCompoundTag("logic"));
		
		/*if(hasBundledConnections) {
			for(int k = 0; k < 4; k++) {
				if(tag.hasKey("bundO"+k))
					bundledOutputs[k] = tag.getByteArray("bundO"+k);
				if(tag.hasKey("bundI"+k))
					bundledInputs[k] = tag.getByteArray("bundI"+k);
				if(tag.hasKey("bundPO"+k))
					prevBundledOutputs[k] = tag.getByteArray("bundPO"+k);
				if(tag.hasKey("bundPI"+k))
					prevBundledInputs[k] = tag.getByteArray("bundPI"+k);
			}
		}*/
	}
	
	private void createLogic() {
		logic = type.createLogic();
		isNotStateless = !(logic instanceof GateLogic.Stateless);
		if(logic instanceof GateLogic.WithPointer)
			pointer = (GateLogic.WithPointer)logic;
		else
			pointer = null;
		hasBundledConnections = logic instanceof GateLogic.WithBundledConnections;
		/*if(hasBundledConnections) {
			bundledInputs = new byte[4][];
			bundledOutputs = new byte[4][];
			prevBundledInputs = new byte[4][];
			prevBundledOutputs = new byte[4][];
			
			
		}*/
	}
	
	private boolean isFirstTick = true;
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(!worldObj.isRemote) {
			if(type == null) {
				System.err.println("RedLogic: invalid gate (no type) at "+xCoord+","+yCoord+","+zCoord+" removed");
				IMicroblockCoverSystem imcs = getCoverSystem();
				if(imcs != null)
					imcs.convertToContainerBlock();
				else
					worldObj.setBlockToAir(xCoord, yCoord, zCoord);
				
			} else if(isNotStateless)
				updateLogic(true, false);
			else if(isFirstTick) {
				updateLogic(false, false);
				isFirstTick = false;
			}
			
		} else {
			pointerPos += pointerSpeed;
		}
	}

	public int getFront() {
		return front;
	}

	public EnumGates getType() {
		return type;
	}
	
	private short[] inputs = new short[4];
	private short[] outputs = new short[4];
	private short[] absOutputs = new short[6];
	private short[] prevAbsOutputs = new short[6];
	private short[] prevOutputs = new short[4];
	private int renderState;
	private int prevRenderState;
	private boolean updatePending;
	
	private void updateRenderState() {
		renderState = logic.getRenderState(inputs, prevOutputs, gateSettings);
		if(prevRenderState != renderState) {
			prevRenderState = renderState;
			if(!CommandDebug.DONT_UPDATE_CLIENTS)
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}
	
	private static int[] FLIPMAP_FLIPPED = new int[] {0, 1, 3, 2};
	private static int[] FLIPMAP_UNFLIPPED = new int[] {0, 1, 2, 3};
	
	private int relToAbsDirection(int rel) {
		if(flipped)
			rel = FLIPMAP_FLIPPED[rel];
		return Utils.dirMap[side][front][rel];
	}
	
	private int absToRelDirection(int abs) {
		if((abs & 6) == (side & 6))
			return -1;
		
		int rel = Utils.invDirMap[side][front][abs];
		if(flipped)
			rel = FLIPMAP_FLIPPED[rel];
		return rel;
	}
	
	public void updateLogic(boolean fromTick, boolean forceUpdate) {
		if(type == null)
			return;
		
		int[] flipMap = flipped ? FLIPMAP_FLIPPED : FLIPMAP_UNFLIPPED;
		
		for(int k = 0; k < 4; k++)
			inputs[k] = getInputValue(k);
		
		// update render state with new inputs but not new outputs
		updateRenderState();
		
		if(forceUpdate || fromTick == isNotStateless) {
			logic.update(inputs, outputs, gateSettings);

			for(int k = 0; k < 4; k++)
				absOutputs[Utils.dirMap[side][front][k]] = outputs[flipMap[k]];
			absOutputs[side] = absOutputs[side^1] = 0;
			
			if(forceUpdate || !Arrays.equals(outputs, prevOutputs)) {
				
				//if(xCoord == -776)
				//	System.out.println(xCoord+","+yCoord+","+zCoord+" -- "+side+"/"+front+" -- "+Arrays.toString(inputs)+" -> "+Arrays.toString(outputs)+" -> "+Arrays.toString(absOutputs));
				
				if(!updatePending) {
					worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, RedLogicMod.gates, 2);
					updatePending = true;
				}
			}
			
			if(isNotStateless && RedLogicMod.enableGateSounds && logic.tickSoundPending && fromTick) {
				worldObj.playSoundEffect(xCoord+0.5, yCoord+0.5, zCoord+0.5, "random.click", 0.3f, 0.6f);
				logic.tickSoundPending = false;
			}
		}
		
		//System.out.println("in: "+Arrays.toString(inputs)+", out: "+Arrays.toString(outputs)+", out2: "+Arrays.toString(absOutputs));
		//System.out.println("dm: "+Arrays.toString(dirMap[side][front])+", idm: "+Arrays.toString(invDirMap[side][front]));
	}
	
	private short getInputValue(int rel) {
		int abs = relToAbsDirection(rel);
		if(hasBundledConnections && ((GateLogic.WithBundledConnections)logic).isBundledConnection(rel))
			return getBundledInputBitmask(abs);
		else
			return getInputStrength(abs);
	}
	
	private short getBundledInputBitmask(int abs) {
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[abs];
		int x = xCoord + fd.offsetX, y = yCoord + fd.offsetY, z = zCoord + fd.offsetZ;
		TileEntity te = worldObj.getTileEntity(x, y, z);
		
		if(te instanceof IBundledEmitter) {
			byte[] values = ((IBundledEmitter)te).getBundledCableStrength(side, abs^1);
			if(values == null)
				return 0;
			
			short rv = 0;
			for(int k = 15; k >= 0; k--) {
				rv <<= 1;
				if(values[k] != 0)
					rv |= 1;
			}
			
			return rv;
		}
		
		return 0;
	}
	
	private short getInputStrength(int abs) {
		switch(abs) {
		case Dir.NX: return Utils.getPowerStrength(worldObj, xCoord-1, yCoord, zCoord, abs^1, side);
		case Dir.PX: return Utils.getPowerStrength(worldObj, xCoord+1, yCoord, zCoord, abs^1, side);
		case Dir.NY: return Utils.getPowerStrength(worldObj, xCoord, yCoord-1, zCoord, abs^1, side);
		case Dir.PY: return Utils.getPowerStrength(worldObj, xCoord, yCoord+1, zCoord, abs^1, side);
		case Dir.NZ: return Utils.getPowerStrength(worldObj, xCoord, yCoord, zCoord-1, abs^1, side);
		case Dir.PZ: return Utils.getPowerStrength(worldObj, xCoord, yCoord, zCoord+1, abs^1, side);
		}
		throw new IllegalArgumentException("Invalid direction "+abs);
	}

	public int getVanillaOutputStrength(int dir) {
		int rel = absToRelDirection(dir);
		if(rel < 0)
			return 0;
		
		if(hasBundledConnections && ((GateLogic.WithBundledConnections)logic).isBundledConnection(rel))
			return 0;
		
		return prevAbsOutputs[dir] / 17;
	}
	
	@Override
	public short getEmittedSignalStrength(int blockFace, int toDirection) {
		if(blockFace != side)
			return 0;
		int rel = absToRelDirection(toDirection);
		if(hasBundledConnections && ((GateLogic.WithBundledConnections)logic).isBundledConnection(rel))
			return 0;
		return prevAbsOutputs[toDirection];
	}

	public int getRenderState() {
		return prevRenderState;
	}

	public void scheduledTick() {
		updatePending = false;
		//System.out.println(xCoord+","+yCoord+","+zCoord+" Scheduled tick. Outputs: "+Arrays.toString(prevOutputs)+" -> "+Arrays.toString(outputs));
		//System.out.println(xCoord+","+yCoord+","+zCoord+" Scheduled tick. Outputs 2: "+Arrays.toString(prevAbsOutputs)+" -> "+Arrays.toString(absOutputs));
		System.arraycopy(absOutputs, 0, prevAbsOutputs, 0, 6);
		System.arraycopy(outputs, 0, prevOutputs, 0, 4);
		updateRenderState();
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, RedLogicMod.gates);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord - 1, zCoord, RedLogicMod.gates);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord + 1, zCoord, RedLogicMod.gates);
        worldObj.notifyBlocksOfNeighborChange(xCoord - 1, yCoord, zCoord, RedLogicMod.gates);
        worldObj.notifyBlocksOfNeighborChange(xCoord + 1, yCoord, zCoord, RedLogicMod.gates);
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord - 1, RedLogicMod.gates);
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord + 1, RedLogicMod.gates);
        
        if(hasBundledConnections) {
	        for(int rel = 0; rel < 4; rel++) {
	        	if(!((GateLogic.WithBundledConnections)logic).isBundledConnection(rel))
	        		continue;
	        	
	        	int abs = relToAbsDirection(rel);
	        	ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[abs];
	        	int x = xCoord + fd.offsetX, y = yCoord + fd.offsetY, z = zCoord + fd.offsetZ;
	        	
	        	TileEntity te = worldObj.getTileEntity(x, y, z);
	        	if(te != null && te instanceof IBundledUpdatable)
	        		((IBundledUpdatable)te).onBundledInputChanged();
	        }
        }
	}
	
	// called when shift-clicked by a screwdriver
	public void configure() {
		if(logic instanceof GateLogic.Flippable) {
			flipped = !flipped;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		} else
			gateSettings = logic.configure(gateSettings);
		updateLogic(false, true);
	}
	
	// called when non-shift-clicked by a screwdriver
	public void rotate() {
		do
			front = (byte)((front + 1) % 6);
		while((front & 6) == (side & 6));
		
		updateLogic(false, true);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public boolean onBlockActivated(EntityPlayer ply) {
		if(ply.getHeldItem() != null && ply.getHeldItem().getItem() == RedLogicMod.screwdriver)
			return false;
		
		if(worldObj.isRemote)
			return type != null && GateLogic.WithRightClickAction.class.isAssignableFrom(type.getLogicClass());
		if(logic instanceof GateLogic.WithRightClickAction) {
			((GateLogic.WithRightClickAction)logic).onRightClick(ply, this);
			return true;
		}
		return false;
	}

	public GateLogic getLogic() {
		return logic;
	}

	public boolean isFlipped() {
		return flipped;
	}

	@Override
	public boolean isPlacementBlocked(PartType<?> type, EnumPosition pos) {
		return getPartAABBFromPool(0).intersectsWith(Part.getBoundingBoxFromPool(pos, type.getSize()));
	}

	@Override
	public boolean isPositionOccupied(EnumPosition pos) {
		return pos == EnumPosition.getFacePosition(side);
	}
	
	
	

	@Override
	public EnumPosition getPartPosition(int subHit) {
		if(subHit == 0)
			return EnumPosition.getFacePosition(side);
		return null;
	}

	@Override
	public AxisAlignedBB getPartAABBFromPool(int subHit) {
		if(subHit == 0)
			return Part.getBoundingBoxFromPool(EnumPosition.getFacePosition(side), GateBlock.THICKNESS);
		return null;
	}

	@Override
	protected int getNumTileOwnedParts() {
		return 1;
	}

	@Override
	public float getPlayerRelativePartHardness(EntityPlayer ply, int part) {
		//, getBlockMetadata()
		return ply.getCurrentPlayerStrVsBlock(RedLogicMod.gates, false) / 0.25f / 30f;
	}

	@Override
	public ItemStack pickPart(MovingObjectPosition rayTrace, int part) {
		return new ItemStack(RedLogicMod.gates, 1, getType().ordinal());
	}

	@Override
	public boolean isPartContainerSideSolid(ForgeDirection side) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderPartContainer(RenderBlocks render) {
		return GateStaticRenderer.instance.renderActualGate(render, worldObj, xCoord, yCoord, zCoord, getBlockType(), 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderPart(RenderBlocks render, int part) {
		return renderPartContainer(render);
	}

	@Override
	public void removePartByPlayer(EntityPlayer ply, int part, boolean harvest) {
		if(harvest)
			RedLogicMod.gates.dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, pickPart(null, 0));
			
		if(!worldObj.isRemote) {
			if(cover != null)
				cover.convertToContainerBlock();
			else
				worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.air, 0, 3);
		}
	}
	
	@Override
	public boolean canPlayerHarvestPart(EntityPlayer entityPlayer, int part) {
		return true;
	}
	
	@Override
	public void getPartContainerDrops(List<ItemStack> drops, int fortune) {
		drops.add(pickPart(null, 0));
	}


	@Override
	public void onRedstoneInputChanged() {
		updateLogic(false, false);
	}

	@Override
	public boolean connects(IWire wire, int blockFace, int fromDirection) {
		if(blockFace != side)
			return false;
		
		if(logic == null)
			return false;
		
		int rel = absToRelDirection(fromDirection);
		if(rel < 0)
			return false;
		
		boolean bundled = (hasBundledConnections && ((GateLogic.WithBundledConnections)logic).isBundledConnection(rel));
		
		if(!(bundled ? wire instanceof IBundledWire : wire instanceof IRedstoneWire))
			return false;
		
		return logic.connectsToDirection(rel, gateSettings);
	}

	@Override
	public boolean connectsAroundCorner(IWire wire, int blockFace, int fromDirection) {
		return false;
	}

	private byte[] returnedBundledCableStrength;
	
	@Override
	public byte[] getBundledCableStrength(int blockFace, int toDirection) {
		if(!hasBundledConnections)
			return null;
		
		if(blockFace != side)
			return null;
		
		int rel = absToRelDirection(toDirection);
		if(rel < 0)
			return null;
		
		if(!((GateLogic.WithBundledConnections)logic).isBundledConnection(rel))
			return null;
		
		if(returnedBundledCableStrength == null)
			returnedBundledCableStrength = new byte[16];
		
		short bitmask = prevOutputs[rel];
		for(int k = 0; k < 16; k++) {
			returnedBundledCableStrength[k] = ((bitmask & 1) != 0) ? (byte)255 : 0;
			bitmask >>= 1;
		}
		
		return returnedBundledCableStrength;
	}

	@Override
	public void onBundledInputChanged() {
		if(hasBundledConnections)
			onRedstoneInputChanged();
	}

	public int getGateSettings() {
		return gateSettings;
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
}
