package mods.immibis.redlogic.chips.builtin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import mods.immibis.redlogic.Utils;
import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.scanner.*;
import mods.immibis.redlogic.gates.EnumGates;
import mods.immibis.redlogic.gates.GateLogic;

class ScannedGateBlock implements IScannedBlock {
	
	private static final long serialVersionUID = 1L;
	
	private transient NBTTagCompound logicTag;
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		
		CompressedStreamTools.write(logicTag, out);
	}
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		logicTag = (NBTTagCompound)CompressedStreamTools.func_152456_a(in, new NBTSizeTracker(Long.MAX_VALUE));
	}
	
	private final EnumGates type;
	private final int front, side, settings;
	private final boolean flipped;
	//private final GateLogic logic;
	private final IScanProcess process;
	
	private IScannedNode nodes[] = new IScannedNode[4];

	public ScannedGateBlock(IScanProcess process, EnumGates type, int front, int side, boolean flipped, int settings, GateLogic logic) {
		this.type = type;
		this.front = front;
		this.side = side;
		this.flipped = flipped;
		this.settings = settings;
		//this.logic = logic;
		this.process = process;
		this.logicTag = new NBTTagCompound();
		logic.write(this.logicTag);
		
		for(int k = 0; k < 4; k++) {
			if(logic.connectsToDirection(k, settings))
				if(logic instanceof GateLogic.WithBundledConnections && ((GateLogic.WithBundledConnections)logic).isBundledConnection(k)) {
					nodes[k] = process.createNode(NodeType.BUNDLED);
				} else
					nodes[k] = process.createNode(NodeType.SINGLE_WIRE);
		}
	}

	private static int[] FLIPMAP_FLIPPED = new int[] {0, 1, 3, 2};
	
	/*private int relToAbsDirection(int rel) {
		if(flipped)
			rel = FLIPMAP_FLIPPED[rel];
		return Utils.dirMap[side][front][rel];
	}*/
	
	private int absToRelDirection(int abs) {
		if((abs & 6) == (side & 6))
			return -1;
		
		int rel = Utils.invDirMap[side][front][abs];
		if(flipped)
			rel = FLIPMAP_FLIPPED[rel];
		return rel;
	}
	
	@Override
	public IScannedNode getNode(int wireside, int dir) {
		if(wireside == side) {
			int rel = absToRelDirection(dir);
			if(rel == -1)
				return null;
			
			return nodes[rel];
		}
		
		return null;
	}
	
	@Override
	public void onConnect(IScannedBlock with, int wireside, int dir) throws CircuitLayoutException {
	}

	@Override
	public String toString() {
		return "GATE("+Arrays.toString(nodes)+")";
	}

	@Override
	public Collection<ICompilableBlock> toCompilableBlocks() {
		return type.getCompiler().toCompilableBlocks(process, nodes, logicTag, settings);
	}

}
