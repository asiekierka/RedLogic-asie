package mods.immibis.redlogic.wires;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import mods.immibis.core.api.util.XYZ;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.api.wiring.IBundledUpdatable;
import mods.immibis.redlogic.api.wiring.IWireUpdateOperation;

/**
 * Obvious, sub-optimal, reference implementation of IWireUpdateOperation.
 */
public class SimpleWireUpdateOperation extends IWireUpdateOperation {

	
	private final World world;
	private SimpleWireUpdateOperation(World world) {
		this.world = world;
	}
	
	public static SimpleWireUpdateOperation create(World world) {
		return new SimpleWireUpdateOperation(world);
	}
	
	public static void init() {
		try {
			IWireUpdateOperation.ZZ_FACTORY_METHOD = SimpleWireUpdateOperation.class.getDeclaredMethod("create", World.class);
			IWireUpdateOperation.ZZ_FACTORY_METHOD.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private Set<XYZ> queuedBlockUpdates = new HashSet<>();
	private Set<XYZ> queuedBundledUpdates = new HashSet<>();
	
	@Override
	public void queueBlockUpdate(int x, int y, int z) {
		queuedBlockUpdates.add(new XYZ(x, y, z));
	}

	@Override
	public void queueNeighbourBlockUpdates(int x, int y, int z) {
		queueBlockUpdate(x+1, y, z);
		queueBlockUpdate(x-1, y, z);
		queueBlockUpdate(x, y+1, z);
		queueBlockUpdate(x, y-1, z);
		queueBlockUpdate(x, y, z+1);
		queueBlockUpdate(x, y, z-1);
	}

	@Override
	public void queueBundledUpdate(int x, int y, int z) {
		queuedBundledUpdates.add(new XYZ(x, y, z));
	}

	@Override
	public void queueBundledUpdate(TileEntity tile) {
		queueBundledUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
	}

	@Override
	public void release() {
	}

	@Override
	public void finish() {
		for(XYZ xyz : queuedBlockUpdates)
			if(world.blockExists(xyz.x, xyz.y, xyz.z))
				world.notifyBlockOfNeighborChange(xyz.x, xyz.y, xyz.z, RedLogicMod.wire);
		for(XYZ xyz : queuedBundledUpdates)
			if(world.blockExists(xyz.x, xyz.y, xyz.z)) {
				TileEntity te = world.getTileEntity(xyz.x, xyz.y, xyz.z);
				if(te instanceof IBundledUpdatable)
					((IBundledUpdatable)te).onBundledInputChanged();
			}
	}

}
