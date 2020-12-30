package mods.immibis.redlogic.chips.scanner;

import mods.immibis.core.api.util.XYZ;
import mods.immibis.redlogic.api.chips.scanner.CircuitLayoutException;
import mods.immibis.redlogic.api.chips.scanner.IScannableHook;
import mods.immibis.redlogic.api.chips.scanner.IScannableTile;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.chips.builtin.ScannedCableBlock;
import mods.immibis.redlogic.chips.builtin.ScannedCableBlockSingle;
import mods.immibis.redlogic.chips.builtin.ScannedOutputBlock;
import mods.immibis.redlogic.chips.builtin.ScannedInputBlock;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * This class contains all the code which does the hard work converting
 * in-world circuits to ScannedCircuits (network structure).
 */
public final class CircuitScanner {
	
	private final World w;
	private final int x1, y1, z1, x2, y2, z2;
	private final ScannedCircuit circuit;

	/**
	 * @param w The world.
	 * @param x1 Min X, inclusive.
	 * @param y1 Min Y, inclusive.
	 * @param z1 Min Z, inclusive.
	 * @param x2 Max X, inclusive.
	 * @param y2 Max Y, inclusive.
	 * @param z2 Max Z, inclusive.
	 * @param rotation Rotation of the chip scanner, 0 to 3.
	 */
	public CircuitScanner(World w, int x1, int y1, int z1, int x2, int y2, int z2, int rotation) {
		this.w = w;
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
		this.circuit = new ScannedCircuit(new XYZ(x2-x1+1, y2-y1+1, z2-z1+1), rotation);
	}
		
	// coordinates are world-relative and must be inside the scanning area
	public void scanBlock(int x, int y, int z) throws CircuitLayoutException {
		assert x >= x1 && y >= y1 && z >= z1 && x <= x2 && y <= y2 && z <= z2;
		
		Block block = w.getBlock(x, y, z);
		
		IScannedBlock sb = null;
		
		{
			IScannableHook blockHook = IScannableHook.perBlock.get(block);
			if(blockHook != null)
				sb = blockHook.getScannedBlock(circuit, w, x, y, z);
		}
		
		if(sb == null) {
			if(block == null || block.isAir(w, x, y, z)) {
				for(IScannableHook h : IScannableHook.airList) {
					sb = h.getScannedBlock(circuit, w, x, y, z);
					if(sb != null) break;
				}
				
			} else {
				TileEntity te = w.getTileEntity(x, y, z);
				if(te instanceof IScannableTile) {
					sb = ((IScannableTile)te).getScannedBlock(circuit);
				}
				
				if(sb == null) {
					for(IScannableHook h : IScannableHook.list) {
						sb = h.getScannedBlock(circuit, w, x, y, z);
						if(sb != null) break;
					}
				}
			}
		}
		
		if(sb != null) {
			circuit.addScannedBlock(new XYZ(x - x1, y - y1, z - z1), sb);
		}
		
	}
	
	public ScannedCircuit finishScan() throws CircuitLayoutException {
		mergeWireNetworks(circuit);
		
		return circuit;
	}
	
	private static void mergeNodes(IScannedNode node, IScannedNode node2, IScannedBlock sb, IScannedBlock sb2) throws CircuitLayoutException {
		if(node2.getNumWires() == node.getNumWires()) {
			node.mergeWith(node2);
		
		} else if(node2.getNumWires() == 1 && node.getNumWires() == 16) {
			// special case for joining insulated wire to bundled wire
			if(sb2 instanceof ScannedCableBlockSingle) {
				int col = ((ScannedCableBlockSingle)sb2).getColour();
				node.getSubNode(col).mergeWith(node2);
			}
			
		} else if(node2.getNumWires() == 16 && node.getNumWires() == 1) {
			// same but with nodes reversed
			if(sb instanceof ScannedCableBlockSingle) {
				int col = ((ScannedCableBlockSingle)sb).getColour();
				node2.getSubNode(col).mergeWith(node);
			}
			
		} else {
			throw new CircuitLayoutException(new ChatComponentTranslation("redlogic.chipscanner.error.nodemerge", node.getNumWires(), node2.getNumWires()));
		}
	}

	private static void mergeWireNetworks(ScannedCircuit circuit) throws CircuitLayoutException {
		XYZ c = circuit.getSize();
		
		for(int x = 0; x < c.x; x++)
		for(int y = 0; y < c.y; y++)
		for(int z = 0; z < c.z; z++) {
			IScannedBlock sb = circuit.getScannedBlock(x, y, z);
			if(sb == null)
				continue;
			
			// XXX HACK to avoid creating the wrong types of I/O nodes - the type of an I/O node isn't set
			// until a wire tries to connect to an I/O marker.
			if(sb instanceof ScannedInputBlock || sb instanceof ScannedOutputBlock)
				continue;
			
			for(int dir = 0; dir < 6; dir++) {
				
				IScannedNode jnode = sb.getNode(-1, dir);
				if(jnode != null) {
					ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[dir];
					int x2 = x + fd.offsetX, y2 = y + fd.offsetY, z2 = z + fd.offsetZ;
					IScannedBlock sb2 = circuit.getScannedBlock(x2, y2, z2);
					if(sb2 != null) {
						IScannedNode node2 = sb2.getNode(-1, dir^1);
						if(node2 != null)
							mergeNodes(jnode, node2, sb, sb2);
					}
				}
				
				for(int ws = 0; ws < 6; ws++) {
					if((ws & 6) == (dir & 6)) continue;
					
					IScannedNode node = sb.getNode(ws, dir);
					if(node != null) {
						ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[dir];
						int x2 = x + fd.offsetX, y2 = y + fd.offsetY, z2 = z + fd.offsetZ;
						int ws_r = ws, dir_r = dir^1;
						if(sb instanceof ScannedCableBlock && ((ScannedCableBlock)sb).connectsInDirectionAroundCorner(ws, dir)) {
							fd = ForgeDirection.VALID_DIRECTIONS[ws];
							x2 += fd.offsetX; y2 += fd.offsetY; z2 += fd.offsetZ;
							ws_r = dir^1;
							dir_r = ws^1;
						}
						IScannedBlock sb2 = circuit.getScannedBlock(x2, y2, z2);
						if(sb2 != null) {
							IScannedNode node2 = sb2.getNode(ws_r, dir_r);
							if(node2 != null)
								mergeNodes(node, node2, sb, sb2);
						}
					}
				}
			}
		}
		
		circuit.finalizeNodeConnections();
		
		//System.out.println(circuit.getWires());
	}

}
