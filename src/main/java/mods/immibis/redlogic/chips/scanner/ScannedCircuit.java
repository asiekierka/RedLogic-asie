package mods.immibis.redlogic.chips.scanner;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mods.immibis.core.api.util.XYZ;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedWire;
import mods.immibis.redlogic.api.chips.scanner.NodeType;
import mods.immibis.redlogic.chips.builtin.ScannedCableBlock;
import mods.immibis.redlogic.chips.compiler.DigraphInput;
import mods.immibis.redlogic.chips.compiler.DigraphOutput;


public class ScannedCircuit implements IScanProcess, Serializable {
	private static final long serialVersionUID = 1L;
	
	private final XYZ size;
	public final int rotation;
	private IScannedBlock[][][] scannedBlocks;
	private Set<ScannedWire> wires = new HashSet<ScannedWire>();
	private Set<ScannedNodeSingle> nodes = new HashSet<ScannedNodeSingle>();
	private Set<IScannedBlock> nonWireBlocks = new HashSet<IScannedBlock>();
	
	private IScannedNode[] inputNodes = new IScannedNode[6];
	private IScannedNode[] outputNodes = new IScannedNode[6];

	public ScannedCircuit(XYZ size, int rotation) {
		this.size = size;
		this.rotation = rotation;
		scannedBlocks = new IScannedBlock[size.x][size.y][size.z];
	}
	
	public void addScannedBlock(XYZ xyz, IScannedBlock scb) {
		if(scannedBlocks[xyz.x][xyz.y][xyz.z] != null)
			throw new IllegalArgumentException("Already added block at "+xyz);
		if(!(scb instanceof ScannedCableBlock))
			nonWireBlocks.add(scb);
		scannedBlocks[xyz.x][xyz.y][xyz.z] = scb;
	}
	
	public XYZ getSize() {
		return size;
	}
	
	public IScannedBlock getScannedBlock(XYZ xyz) {
		return scannedBlocks[xyz.x][xyz.y][xyz.z];
	}
	
	public IScannedBlock getScannedBlock(int x, int y, int z) {
		if(x < 0 || y < 0 || z < 0 || x >= size.x || y >= size.y || z >= size.z)
			return null;
		return scannedBlocks[x][y][z];
	}
	
	@Override
	public IScannedNode createNode(NodeType type) {
		if(type == NodeType.SINGLE_WIRE) {
			ScannedNodeSingle node = new ScannedNodeSingle(this);
			nodes.add(node);
			return node;
			
		} else if(type == NodeType.BUNDLED) {
			ScannedNodeBundled node = new ScannedNodeBundled();
			for(int k = 0; k < 16; k++) {
				ScannedNodeSingle subnode = new ScannedNodeSingle(this);
				node.subnodes[k] = subnode;
				nodes.add(subnode);
			}
			return node;
			
		} else {
			throw new IllegalArgumentException("node type "+type);
		}
	}

	public void finalizeNodeConnections() {
		for(ScannedNodeSingle node : nodes) {
			node.finalizeConnections();
		}
	}

	public Set<ScannedWire> getWires() {
		return wires;
	}

	public IScannedWire createWire() {
		ScannedWire wire = new ScannedWire();
		wires.add(wire);
		return wire;
	}
	
	@Override
	public IScannedNode getInputNode(int dir, NodeType type) {
		if(inputNodes[dir] == null)
			inputNodes[dir] = createNode(type);
		return inputNodes[dir];
	}
	
	@Override
	public IScannedNode getOutputNode(int dir, NodeType type) {
		if(outputNodes[dir] == null)
			outputNodes[dir] = createNode(type);
		return outputNodes[dir];
	}
	
	public IScannedNode getInputNode(int dir) {return inputNodes[dir];}
	public IScannedNode getOutputNode(int dir) {return outputNodes[dir];}

	public Collection<IScannedBlock> getNonWireBlocks() {
		return nonWireBlocks;
	}
	
	@Override
	public DigraphInput createInput() {
		return new DigraphInput();
	}
	
	@Override
	public DigraphOutput createOutput() {
		return new DigraphOutput();
	}
}
