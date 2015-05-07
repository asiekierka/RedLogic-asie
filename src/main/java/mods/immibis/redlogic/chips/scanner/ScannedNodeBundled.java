package mods.immibis.redlogic.chips.scanner;

import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedWire;

public class ScannedNodeBundled implements IScannedNode {
	
	private static final long serialVersionUID = 1L;
	
	ScannedNodeSingle[] subnodes = new ScannedNodeSingle[16];

	@Override
	public void mergeWith(IScannedNode node) {
		if(node.getNumWires() != 16)
			throw new IllegalArgumentException("cannot merge "+this+" with "+node+" - different # wires");
		
		for(int k = 0; k < 16; k++)
			subnodes[k].mergeWith(node.getSubNode(k));
	}

	@Override
	public int getNumWires() {
		return 16;
	}

	@Override
	public IScannedWire getWire(int index) {
		return subnodes[index].getWire(0);
	}

	@Override
	public IScannedNode getSubNode(int wire) {
		return subnodes[wire];
	}

}
