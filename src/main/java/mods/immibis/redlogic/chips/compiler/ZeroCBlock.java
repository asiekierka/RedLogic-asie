package mods.immibis.redlogic.chips.compiler;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.compiler.util.ZeroExpr;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

public class ZeroCBlock implements ICompilableBlock {
	
	private IScannedInput[] inputs = new IScannedInput[0];
	private IScannedOutput[] outputs = new IScannedOutput[1];
	
	public ZeroCBlock(IScanProcess process) {
		outputs[0] = process.createOutput();
	}
	
	@Override
	public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
		return new ICompilableExpression[] {new ZeroExpr()};
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
