package mods.immibis.redlogic.chips.builtin;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

public class ScannedArrayCellBlockNonInvert extends ScannedArrayCellBlock {
	private static final long serialVersionUID = 1L;
	
	public ScannedArrayCellBlockNonInvert(IScanProcess process, int side, int front) {
		super(process, side, front);
	}
	
	@Override
	public Collection<ICompilableBlock> toCompilableBlocks() {
		final IScannedInput input = process.createInput();
		final IScannedOutput output = process.createOutput();
		
		nodeLR.getWire(0).addOutput(output);
		nodeFB.getWire(0).addInput(input);
		
		return Collections.<ICompilableBlock>singletonList(new ICompilableBlock() {
			@Override
			public IScannedOutput[] getOutputs() {
				return new IScannedOutput[] {output};
			}
			
			@Override
			public IScannedInput[] getInputs() {
				return new IScannedInput[] {input};
			}
			
			@Override
			public ICompilableExpression[] compile(ICompileContext ctx, final ICompilableExpression[] inputs) {
				return inputs;
			}
		});
	}
}
