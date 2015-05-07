package mods.immibis.redlogic.chips.scanner;

import java.io.Serializable;

import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedWire;

public class ScannedNodeSingle implements IScannedNode, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/*
	 * Uses a disjoint forest data structure.
	 * Top-level nodes have parent==null.
	 */
	
	private ScannedNodeSingle parent;
	private ScannedCircuit circuit;
	private int rank = 0;

	public ScannedNodeSingle(ScannedCircuit circuit) {
		this.circuit = circuit;
	}
	
	ScannedNodeSingle getRoot() {
		if(parent == null)
			return this;
		else
			return parent = parent.getRoot();
	}
	
	@Override
	public void mergeWith(IScannedNode otherNode) {
		if(wire != null) throw new IllegalStateException("already finished scanning");
		
		ScannedNodeSingle a = getRoot();
		ScannedNodeSingle b = ((ScannedNodeSingle)otherNode).getRoot();
		
		if(a == b)
			return;
		
		assert a.parent == null;
		assert b.parent == null;
		
		if(a.rank > b.rank) {
			b.parent = a;
			
		} else if(a.rank < b.rank) {
			a.parent = b;
			
		} else {
			b.parent = a;
			a.rank++;
		}
	}
	
	
	
	private IScannedWire wire;
	@Override
	public int getNumWires() {
		return 1;
	}
	@Override
	public IScannedWire getWire(int index) {
		if(wire == null) throw new IllegalStateException("still scanning");
		return wire;
	}
	
	@Override
	public String toString() {
		if(wire == null) return "<ScannedNode>";
		else return String.valueOf(wire);
	}
	
	
	
	public ScannedNodeSingle finalizeConnections() {
		if(wire != null)
			return this;
		
		if(parent == null) {
			wire = circuit.createWire();
			
		} else {
			parent.finalizeConnections();
			wire = parent.wire;
			parent = null;
		}
		
		return this;
	}

	@Override
	public IScannedNode getSubNode(int wire) {
		if(wire == 0)
			return this;
		throw new IndexOutOfBoundsException("wire "+wire);
	}
}
