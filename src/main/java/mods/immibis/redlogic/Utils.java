package mods.immibis.redlogic;


import java.lang.reflect.Field;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.multipart.IPartContainer;
import mods.immibis.microblocks.api.IMicroblockCoverSystem;
import mods.immibis.microblocks.api.IMicroblockSupporterTile;
import mods.immibis.redlogic.api.wiring.IRedstoneEmitter;
import mods.immibis.redlogic.wires.PlainRedAlloyTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class Utils {
	//private static final int plainRedAlloyMeta = EnumWireType.CLASS_TO_META.get(PlainRedAlloyTile.class);
	
	/*
	 * Returns the vanilla power strength the block at the specified position is emitting to the specified edge.
	 * @return The power strength from 0 to 15.
	 */
	/*public static byte getPowerStrength(World world, int x, int y, int z, int side, int direction) {
		int block = world.getBlockId(x, y, z);
		if(block == Block.redstoneWire.blockID) {
			if(side != Dir.NY)
				return 0;
			return (byte)world.getBlockMetadata(x, y, z);
		}
		if(block == RedLogicMod.gates.blockID) {
			GateTile te = (GateTile)world.getBlockTileEntity(x, y, z);
			if(te.getSide() != side)
				return 0;
			return (byte)te.getOutputStrength(direction);
		}
		
		int meta = world.getBlockMetadata(x, y, z);
		if(block == RedLogicMod.wire.blockID) {
			if(meta == plainRedAlloyMeta) {
				RedAlloyTile te = (RedAlloyTile)world.getBlockTileEntity(x, y, z);
				if(!te.connectsInDirection(side, direction))
					return 0;
				return te.getVanillaRedstoneStrength();
			}
		}
		return (byte)world.getIndirectPowerLevelTo(x, y, z, direction ^ 1);
	}*/
	
	/** Magic array of numbers */
	private static int[][] rotationMap = {
		{9, 9, 4, 5, 3, 2},
    	{9, 9, 5, 4, 2, 3},
    	{5, 4, 9, 9, 0, 1},
    	{4, 5, 9, 9, 1, 0},
    	{2, 3, 1, 0, 9, 9},
    	{3, 2, 0, 1, 9, 9},
	};
	
	public static final int FRONT = 0;
	public static final int BACK = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;

	public static final int[][][] dirMap = new int[6][6][4]; // [side][front][rel direction] -> abs direction
	public static final int[][][] invDirMap = new int[6][6][6]; // [side][front][abs direction] -> rel direction
	static {
		for(int side = 0; side < 6; side++)
			for(int front = 0; front < 6; front++) {
				if((front & 6) == (side & 6))
					continue;
				
				dirMap[side][front][FRONT] = front;
				dirMap[side][front][BACK] = front^1;
				//dirMap[side][front][LEFT] = ForgeDirection.ROTATION_MATRIX[side][front];
				//dirMap[side][front][RIGHT] = ForgeDirection.ROTATION_MATRIX[side][front^1];
				dirMap[side][front][LEFT] = rotationMap[side][front];
				dirMap[side][front][RIGHT] = rotationMap[side][front^1];
				
				for(int dir = 0; dir < 4; dir++)
					invDirMap[side][front][dirMap[side][front][dir]] = dir;
			}
	}
	
	
	
	/**
	 * Returns true if a given edge of a block is an open space, so that
	 * wires placed on both sides of the edge will connect.
	 * If side and dir are swapped, the result is the same.
	 * 
	 * @param w The world containing the edge.
	 * @param x The X coordinate of the block containing the edge.
	 * @param y The Y coordinate of the block containing the edge.
	 * @param z The Z coordinate of the block containing the edge.
	 * @param side One of the faces the edge is on.
	 * @param dir The other face the edge is on.
	 * @return True if this edge is an open space.
	 */
	public static boolean canConnectThroughEdge(World w, int x, int y, int z, int side, int dir) {
		TileEntity te = w.getTileEntity(x, y, z);
		if(te instanceof IMicroblockSupporterTile) {
			IMicroblockCoverSystem ci = ((IMicroblockSupporterTile)te).getCoverSystem();
			return ci == null || ci.isEdgeOpen(side, dir);
		}
		return !w.isSideSolid(x, y, z, ForgeDirection.VALID_DIRECTIONS[side]) && !w.isSideSolid(x, y, z, ForgeDirection.VALID_DIRECTIONS[dir]);
	}



	private static Field wiresProvidePower = BlockRedstoneWire.class.getDeclaredFields()[0];
	static {
		try {
			wiresProvidePower.setAccessible(true);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the red-alloy power strength (0 to 255) the specified block is emitting towards the specified direction and side.
	 * onSide is used for emitters like wires; if a wire is on the NY side of the block containing it, then it will only emit power to
	 * side=NY, direction=NX/NZ/PX/PZ.
	 * onSide can be -1 to detect if power is coming through a block; the wire in the above example would use
	 * side=-1, direction=PY when detecting power coming from the block below it.
	 */
	public static short getPowerStrength(World w, int x, int y, int z, int toDirection, int onSide) {
		return getPowerStrength(w, x, y, z, toDirection, onSide, true);
	}
	
	/**
	 * Returns the red-alloy power strength (0 to 255) the specified block is emitting in the specified direction and side.
	 * onSide == -1 if detecting power input to an opaque cube, or for jacketed wire connections.
	 */
	public static short getPowerStrength(World w, int x, int y, int z, int toDirection, int onSide, boolean countRedAlloyWire) {
		if(!w.blockExists(x, y, z))
			return 0;
		
		Block b = w.getBlock(x, y, z);
		int meta = w.getBlockMetadata(x, y, z);
		if(b == null)
			return 0;
		if(b.equals(Blocks.redstone_wire))
			return (short)(meta * 17);
		if(b.hasTileEntity(meta)) {
			TileEntity te = w.getTileEntity(x, y, z);
			if(te instanceof IRedstoneEmitter) {
				if(!countRedAlloyWire && te instanceof PlainRedAlloyTile)
					return 0;
				return ((IRedstoneEmitter)te).getEmittedSignalStrength(onSide, toDirection);
			}
		}
		
		// respond to weak power, or strong power, or strong power applied through a block
		int pl = b.isProvidingStrongPower(w, x, y, z, toDirection ^ 1);
		if(pl > 0)
			return (short)(pl * 17);
		
		if(b.isNormalCube(w, x, y, z)) {
			try {
				wiresProvidePower.set(Blocks.redstone_wire, false);
				pl = w.getBlockPowerInput(x, y, z);
				wiresProvidePower.set(Blocks.redstone_wire, true);
				if(pl > 0)
					return (short)(pl * 17);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			pl = b.isProvidingWeakPower(w, x, y, z, toDirection ^ 1);
			if(pl > 0)
				return (short)(pl * 17);
		}
		
		return 0;
	}

	public static boolean canPlaceWireOnSide(World w, int x, int y, int z, ForgeDirection side, boolean _default) {
		if(!w.blockExists(x, y, z))
			return _default;
		
		Block b = w.getBlock(x, y, z);
		if(b == null)
			return false;
		if(b.equals(Blocks.glowstone) || b.equals(Blocks.piston) || b.equals(Blocks.sticky_piston) || b.equals(Blocks.piston_extension))
			return true;
		return b.isSideSolid(w, x, y, z, side);
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean addPartDestroyEffects(TileEntity te, int part, EffectRenderer er) {
		byte b0 = 4;

		AxisAlignedBB bb = ((IPartContainer)te).getPartAABBFromPool(0).offset(te.xCoord, te.yCoord, te.zCoord);
		
        for (int j1 = 0; j1 < b0; ++j1)
        {
            for (int k1 = 0; k1 < b0; ++k1)
            {
                for (int l1 = 0; l1 < b0; ++l1)
                {
                    double d0 = bb.minX + (bb.maxX - bb.minX) * (j1 + 0.5) / b0;
                    double d1 = bb.minY + (bb.maxY - bb.minY) * (k1 + 0.5) / b0;
                    double d2 = bb.minZ + (bb.maxZ - bb.minZ) * (l1 + 0.5) / b0;
                    er.addEffect((new EntityDiggingFX(te.getWorldObj(), d0, d1, d2, d0 - (bb.maxX + bb.minX)/2, d1 - (bb.maxY + bb.minY)/2, d2 - (bb.maxZ + bb.minZ)/2, te.getBlockType(), 0)));
                }
            }
        }
        return true;
	}
	
	public static void setBlockBounds(Block b, AxisAlignedBB bb) {
		b.setBlockBounds((float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ);
	}
	
	@SideOnly(Side.CLIENT)
	public static boolean addPartHitEffects(TileEntity te, int part, int sideHit, EffectRenderer er) {
		setBlockBounds(te.getBlockType(), ((IPartContainer)te).getPartAABBFromPool(part));
		er.addBlockHitEffects(te.xCoord, te.yCoord, te.zCoord, sideHit);
		return true;
	}
}
