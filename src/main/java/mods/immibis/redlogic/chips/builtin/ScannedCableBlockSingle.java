package mods.immibis.redlogic.chips.builtin;

import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.NodeType;
import mods.immibis.redlogic.api.wiring.IInsulatedRedstoneWire;
import mods.immibis.redlogic.api.wiring.IWire;

public class ScannedCableBlockSingle extends ScannedCableBlock {
	private static final long serialVersionUID = 1L;
	
	private int colour;
	
	public ScannedCableBlockSingle(IScanProcess process, IWire wiretile) {
		super(process, wiretile, NodeType.SINGLE_WIRE);
		if(wiretile instanceof IInsulatedRedstoneWire)
			colour = ((IInsulatedRedstoneWire)wiretile).getInsulatedWireColour();
	}
	
	public int getColour() {
		return colour;
	}
}
