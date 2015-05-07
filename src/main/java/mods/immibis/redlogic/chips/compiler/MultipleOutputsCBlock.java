package mods.immibis.redlogic.chips.compiler;

import org.objectweb.asm.Opcodes;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

/**
 * Block generated when there are multiple outputs on one wire.
 * ORs the outputs together.
 */ 
class MultipleOutputsCBlock implements ICompilableBlock {
	private IScannedInput[] inputs;
	private IScannedOutput[] outputs;
	
	public MultipleOutputsCBlock(IScanProcess process, int nIn) {
		inputs = new IScannedInput[nIn];
		for(int k = 0; k < nIn; k++)
			inputs[k] = process.createInput();
		outputs = new IScannedOutput[] {process.createOutput()};
	}
	
	@Override
	public ICompilableExpression[] compile(ICompileContext ctx, final ICompilableExpression[] inputs) {
		if(inputs.length == 0)
			throw new IllegalArgumentException("no inputs");
		
		return new ICompilableExpression[] {
			new ICompilableExpression() {
				@Override
				public void compile(ICompileContext ctx) {
					boolean first = true;
					
					for(ICompilableExpression expr : inputs) {
						expr.compile(ctx);
						if(first)
							first = false;
						else
							ctx.getCodeVisitor().visitInsn(Opcodes.IOR);
					}
				}
				
				@Override
				public boolean alwaysInline() {
					return false;
				}
			}
		};
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
