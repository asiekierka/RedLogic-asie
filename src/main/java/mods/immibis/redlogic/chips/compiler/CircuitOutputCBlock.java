package mods.immibis.redlogic.chips.compiler;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

public class CircuitOutputCBlock implements ICompilableBlock {
	private int dir, wires;
	
	private IScannedOutput[] outputs = new IScannedOutput[0];
	private IScannedInput[] inputs;
	
	public CircuitOutputCBlock(IScanProcess process, int dir, int wires) {
		this.dir = dir;
		this.wires = wires;
		
		inputs = new IScannedInput[wires];
		for(int k = 0; k < inputs.length; k++)
			inputs[k] = process.createInput();
		
	}
	
	@Override
	public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
		for(int k = 0; k < wires; k++) {
			inputs[k].compile(ctx);
			ctx.storeOutput(dir, k);
		}
		
		return new ICompilableExpression[0];
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
