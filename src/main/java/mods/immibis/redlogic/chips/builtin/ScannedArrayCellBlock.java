package mods.immibis.redlogic.chips.builtin;

import mods.immibis.redlogic.api.chips.scanner.CircuitLayoutException;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.NodeType;

public abstract class ScannedArrayCellBlock implements IScannedBlock {
	private static final long serialVersionUID = 1L;

	private int side;
	private int front;
	
	protected IScannedNode nodeFB, nodeLR;
	protected IScanProcess process;
	
	protected ScannedArrayCellBlock(IScanProcess process, int side, int front) {
		this.process = process;
		this.side = side;
		this.front = front;
		this.nodeFB = process.createNode(NodeType.SINGLE_WIRE);
		this.nodeLR = process.createNode(NodeType.SINGLE_WIRE);
	}

	@Override
	public IScannedNode getNode(int wireside, int dir) {
		if(wireside != side)
			return null;
		if((dir & 6) == (front & 6))
			return nodeFB;
		else
			return nodeLR;
	}

	@Override
	public void onConnect(IScannedBlock with, int wireside, int dir) throws CircuitLayoutException {
	}

}
