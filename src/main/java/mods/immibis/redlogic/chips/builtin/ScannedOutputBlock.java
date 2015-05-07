package mods.immibis.redlogic.chips.builtin;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.scanner.CircuitLayoutException;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.NodeType;

public class ScannedOutputBlock implements IScannedBlock {
	
	private static final long serialVersionUID = 1L;
	
	private IScanProcess circuit;
	private NodeType nodetype;

	public ScannedOutputBlock(IScanProcess circuit, NodeType nodetype) {
		this.circuit = circuit;
		this.nodetype = nodetype;
	}
	
	@Override
	public IScannedNode getNode(int wireside, int dir) {
		return circuit.getOutputNode(dir^1, nodetype);
	}
	
	@Override
	public void onConnect(IScannedBlock with, int wireside, int dir) throws CircuitLayoutException {
		
	}
	
	@Override
	public String toString() {
		return "OUT";
	}
	
	@Override
	public Collection<ICompilableBlock> toCompilableBlocks() {
		return Collections.emptySet();
	}
}
