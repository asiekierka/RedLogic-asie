package mods.immibis.redlogic.chips.builtin;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;

public class ScannedArrayCellBlockNull extends ScannedArrayCellBlock {
	private static final long serialVersionUID = 1L;
	
	public ScannedArrayCellBlockNull(IScanProcess process, int side, int front) {
		super(process, side, front);
	}
	
	@Override
	public Collection<ICompilableBlock> toCompilableBlocks() {
		return Collections.emptyList();
	}
}
