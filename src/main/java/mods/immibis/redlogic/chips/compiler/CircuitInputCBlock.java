package mods.immibis.redlogic.chips.compiler;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

public class CircuitInputCBlock implements ICompilableBlock {
	private int dir, wires;
	
	private IScannedInput[] inputs = new IScannedInput[0];
	private IScannedOutput[] outputs;
	
	public CircuitInputCBlock(IScanProcess process, int dir, int wires) {
		this.dir = dir;
		this.wires = wires;
		
		outputs = new IScannedOutput[wires];
		for(int k = 0; k < outputs.length; k++)
			outputs[k] = process.createOutput();
	}
	
	@Override
	public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
		
		ICompilableExpression[] exprs = new ICompilableExpression[wires];
		
		for(int k = 0; k < wires; k++) {
			final int wireNo = k;
			exprs[k] = new ICompilableExpression() {
				@Override
				public void compile(ICompileContext ctx) {
					ctx.loadInput(dir, wireNo);
				}
				@Override
				public boolean alwaysInline() {
					return true;
				}
			};
		}
		
		return exprs;
	}

	@Override
	public IScannedInput[] getInputs() {
		return inputs;
	}

	@Override
	public IScannedOutput[] getOutputs() {
		return outputs;
	}
}
