package mods.immibis.microblocks.api.util;


import java.util.List;

import mods.immibis.core.multipart.SubhitValues;
import mods.immibis.microblocks.api.IMicroblockCoverSystem;
import mods.immibis.microblocks.api.IMicroblockSupporterTile2;
import mods.immibis.microblocks.api.IMicroblockSystem;
import mods.immibis.microblocks.api.MicroblockAPIUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

/**
 * Convenience class for microblock supporting tiles.
 * Handles creating an ICoverSystem, saving and loading it,
 * and implements getCollidingBoundingBoxes and collisionRayTrace.
 */
public abstract class TileCoverableBase extends TileEntity implements IMicroblockSupporterTile2 {
	
	protected IMicroblockCoverSystem cover;
	
	public TileCoverableBase() {
		IMicroblockSystem ims = MicroblockAPIUtils.getMicroblockSystem();
		if(ims != null)
			cover = ims.createMicroblockCoverSystem(this);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if(cover != null)
			cover.writeToNBT(tag);
	}
	
	@Override
	public Packet getDescriptionPacket() {
		if(cover == null)
			return null;
		
		NBTTagCompound tag = new NBTTagCompound();
		tag.setByteArray("C", cover.writeDescriptionBytes());
		S35PacketUpdateTileEntity p = new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
		return p;
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		if(cover != null)
			cover.readDescriptionBytes(pkt.func_148857_g().getByteArray("C"), 0);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		if(cover != null)
			cover.readFromNBT(tag);
	}
	
	@Override
	public IMicroblockCoverSystem getCoverSystem() {
		return cover;
	}
	
	protected abstract int getNumTileOwnedParts();
	
	@Override
	public MovingObjectPosition collisionRayTrace(Vec3 src, Vec3 dst) {
		src = src.addVector(-xCoord, -yCoord, -zCoord);
		dst = dst.addVector(-xCoord, -yCoord, -zCoord);
		
		int numTOP = getNumTileOwnedParts();
		
		MovingObjectPosition best = null;
		double bestDist = 0;
		for(int k = 0; k < numTOP; k++) {
			AxisAlignedBB partBB = getPartAABBFromPool(k);
			if(partBB == null)
				continue;
			
			MovingObjectPosition _this = partBB.calculateIntercept(src, dst);
			if(_this != null) {
				double dist = _this.hitVec.squareDistanceTo(src);
				if(best == null || dist < bestDist) {
					bestDist = dist;
					best = _this;
					best.subHit = SubhitValues.getFromTilePartIndex(k);
				}
			}
		}
		
		if(best == null)
			return null;
		MovingObjectPosition result = new MovingObjectPosition(xCoord, yCoord, zCoord, best.sideHit, best.hitVec.addVector(xCoord, yCoord, zCoord));
		result.subHit = best.subHit;
		return result;
	}
	
	@Override
	public void getCollidingBoundingBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list) {
		for(int k = 0; k < getNumTileOwnedParts(); k++) {
			AxisAlignedBB partBB = getPartAABBFromPool(k);
			if(partBB == null)
				continue;
			partBB = partBB.offset(xCoord, yCoord, zCoord);
			if(partBB.intersectsWith(mask))
				list.add(partBB);
		}
	}
	
	@Override
	public void onMicroblocksChanged() {
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
}
