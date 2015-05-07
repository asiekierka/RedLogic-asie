package mods.immibis.redlogic.chips.compiler;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

import org.objectweb.asm.Opcodes;

public class CacheCBlock implements ICompilableBlock {
	private IScannedInput[] inputs = new IScannedInput[1];
	private IScannedOutput[] outputs = new IScannedOutput[1];
	
	public CacheCBlock(IScanProcess process) {
		outputs[0] = process.createOutput();
		inputs[0] = process.createInput();
	}
	
	@Override
	public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
		inputs[0].compile(ctx);
		final int index = ctx.createLocal("Z");
		ctx.getCodeVisitor().visitVarInsn(Opcodes.ISTORE, index);
		
		return new ICompilableExpression[] {
			new ICompilableExpression() {
				@Override
				public void compile(ICompileContext ctx) {
					ctx.getCodeVisitor().visitVarInsn(Opcodes.ILOAD, index);
				}
				@Override
				public boolean alwaysInline() {
					return true;
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
