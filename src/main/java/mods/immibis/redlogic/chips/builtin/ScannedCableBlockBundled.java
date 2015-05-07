package mods.immibis.redlogic.chips.builtin;

import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.NodeType;
import mods.immibis.redlogic.api.wiring.IWire;

class ScannedCableBlockBundled extends ScannedCableBlock {

	private static final long serialVersionUID = 1L;
	
	public ScannedCableBlockBundled(IScanProcess process, IWire wireTile) {
		super(process, wireTile, NodeType.BUNDLED);
	}

}
