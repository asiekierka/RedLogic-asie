package mods.immibis.redlogic.chips.builtin;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.scanner.CircuitLayoutException;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.NodeType;
import mods.immibis.redlogic.api.wiring.IWire;

/**
 * A single block of either red alloy wire or bundled cable.
 */
public abstract class ScannedCableBlock implements IScannedBlock {
	private static final long serialVersionUID = 1L;
	
	private int connectMask = 0;
	private int connectCornerMask = 0;
	private int connectJacketMask = 0;
	private IScannedNode node;
	
	private static int getConnectMask(int wireside, int dir) {
		if((wireside & 6) == (dir & 6))
			throw new IllegalArgumentException("WS"+wireside+" / D"+dir);
		return 1 << ((wireside * 6) + dir - 2);
	}
	
	private boolean connectsInDirection(int wireside, int direction) {
		return (connectMask & getConnectMask(wireside, direction)) != 0;
	}
	
	public boolean connectsInDirectionAroundCorner(int wireside, int direction) {
		return (connectCornerMask & getConnectMask(wireside, direction)) != 0;
	}
	
	private boolean connectsInDirectionByJacket(int direction) {
		return (connectJacketMask & (1 << direction)) != 0;
	}
	
	@Override
	public IScannedNode getNode(int wireside, int dir) {
		if(wireside == -1) {
			if(!connectsInDirectionByJacket(dir))
				return null;
			
		} else {
			if(!connectsInDirection(wireside, dir) && !connectsInDirectionAroundCorner(wireside, dir))
				return null;
		}
		return node;
	}
	
	public IScannedNode getNode() {
		return node;
	}
	
	@Override
	public void onConnect(IScannedBlock with, int wireside, int dir) throws CircuitLayoutException {

	}
	
	public ScannedCableBlock(IScanProcess process, IWire wireTile, NodeType nodeType) {
		this.node = process.createNode(nodeType);
		
		for(int dir = 0; dir < 6; dir++) {
			for(int ws = 0; ws < 6; ws++) {
				if((ws & 6) == (dir & 6)) continue;
				if(wireTile.wireConnectsInDirection(ws, dir)) {
					connectMask |= getConnectMask(ws, dir);
					if(wireTile.wireConnectsInDirectionAroundCorner(ws, dir)) {
						connectCornerMask |= getConnectMask(ws, dir);
					}
				}
			}
			if(wireTile.wireConnectsInDirection(-1, dir))
				connectJacketMask |= 1 << dir;
		}
	}
	
	@Override
	public Collection<ICompilableBlock> toCompilableBlocks() {
		return Collections.emptySet();
	}
}
