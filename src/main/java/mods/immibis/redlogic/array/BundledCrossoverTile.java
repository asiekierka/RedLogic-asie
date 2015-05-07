package mods.immibis.redlogic.array;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.EnumAxisPosition;
import mods.immibis.microblocks.api.EnumPosition;
import mods.immibis.microblocks.api.Part;
import mods.immibis.microblocks.api.PartType;
import mods.immibis.microblocks.api.util.TileCoverableMultipartBase;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.Utils;
import mods.immibis.redlogic.api.wiring.IBundledEmitter;
import mods.immibis.redlogic.api.wiring.IBundledPropagator;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import mods.immibis.redlogic.api.wiring.IWireUpdateOperation;

public class BundledCrossoverTile extends TileCoverableMultipartBase implements IBundledUpdatable, IBundledPropagator, IBundledEmitter {

	private byte side, front;
	
	public static final double THICKNESS = 0.375;
	
	// initialized in setWorldObj on server worlds only
	private byte[] strengthFB, strengthLR, oldStrengthFB, oldStrengthLR;
	
	// colour bitmasks
	private short isUpdating, recursiveUpdateQueued;
	
	@Override
	public void setWorldObj(World p_145834_1_) {
		super.setWorldObj(p_145834_1_);
		if(!worldObj.isRemote) {
			strengthFB = new byte[16];
			strengthLR = new byte[16];
			oldStrengthFB = new byte[16];
			oldStrengthLR = new byte[16];
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		side = tag.getByte("side");
		front = tag.getByte("front");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setByte("side", side);
		tag.setByte("front", front);
	}
	
	public void init(int side, int front) {
		if(side < 0 || side > 5) throw new IllegalArgumentException();
		if(front < 0 || front > 5) throw new IllegalArgumentException();
		this.side = (byte)side;
		this.front = (byte)front;
	}

	public static boolean checkCanStay(World world, int x, int y, int z, int side) {
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side];
		x += fd.offsetX;
		y += fd.offsetY;
		z += fd.offsetZ;
		return world.isSideSolid(x, y, z, ForgeDirection.VALID_DIRECTIONS[side^1], true);
	}

	@Override
	public EnumPosition getPartPosition(int subHit) {
		return EnumPosition.getFacePosition(side);
	}

	@Override
	public boolean isPlacementBlocked(PartType<?> type, EnumPosition pos) {
		if(type.getSize() > 1-THICKNESS)
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
		return pos == EnumPosition.Centre;
	}

	@Override
	public float getPlayerRelativePartHardness(EntityPlayer ply, int part) {
		return ply.getCurrentPlayerStrVsBlock(RedLogicMod.gates, false) / 0.25f / 30f;
	}

	@Override
	public ItemStack pickPart(MovingObjectPosition rayTrace, int part) {
		return new ItemStack(getBlockType());
	}

	@Override
	public boolean isPartContainerSideSolid(ForgeDirection side) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean renderPartContainer(RenderBlocks render) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean renderPart(RenderBlocks render, int part) {
		return renderPartContainer(render);
	}

	@Override
	public void removePartByPlayer(EntityPlayer ply, int part, boolean harvest) {
		if(harvest)
			((BundledCrossoverBlock)getBlockType()).dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, new ItemStack(getBlockType()));
		if(cover != null)
			cover.convertToContainerBlock();
		else
			worldObj.setBlockToAir(xCoord, yCoord, zCoord);
	}
	
	@Override
	public boolean canPlayerHarvestPart(EntityPlayer entityPlayer, int part) {
		return true;
	}
	
	@Override
	public void getPartContainerDrops(List<ItemStack> drops, int fortune) {
		drops.add(new ItemStack(getBlockType()));
	}

	@Override
	public AxisAlignedBB getPartAABBFromPool(int part) {
		return Part.getBoundingBoxFromPool(EnumPosition.getFacePosition(side), THICKNESS);
	}

	@Override
	protected int getNumTileOwnedParts() {
		return 1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addPartDestroyEffects(int part, EffectRenderer er) {
		return Utils.addPartDestroyEffects(this, part, er);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addPartHitEffects(int part, int sideHit, EffectRenderer er) {
		return Utils.addPartHitEffects(this, part, sideHit, er);
	}

	public int getSide() {
		return side;
	}

	
	
	
	@Override
	public byte[] getBundledCableStrength(int blockFace, int toDirection) {
		if(blockFace != side)
			return null;
		if((toDirection & 6) == (front & 6))
			return strengthFB;
		else
			return strengthLR;
	}
	
	private int getSideDir() {
		int rv = (front + 2) % 6;
		if((rv & 6) == (side & 6))
			rv = (rv + 2) % 6;
		return rv;
	}

	@Override
	public void propagateBundledSignal(short colourMask, IWireUpdateOperation operation) {
		recursiveUpdateQueued |= (colourMask & isUpdating);
		colourMask &= ~isUpdating;
		if(colourMask == 0)
			return;
		
		int sideDir = getSideDir();
		
		isUpdating |= colourMask;
		try {
			
			//updateStrengthFromSurroundingBlocks(oldStrengthFB, strengthFB, front);
			//updateStrengthFromSurroundingBlocks(oldStrengthLR, strengthLR, sideDir);
			
		} finally {
			isUpdating &= ~colourMask;
		}
	}

	@Override
	public void onBundledInputChanged() {
		try (IWireUpdateOperation operation = IWireUpdateOperation.create(worldObj)) {
			propagateBundledSignal((short)-1, operation);
			operation.finish();
		}
	}
	
	

}
