package mods.immibis.redlogic.chips.compiler;

import org.objectweb.asm.Opcodes;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

public class DelayOutBlock implements ICompilableBlock {
	
	private String fieldName;
	
	public IScannedInput[] inputs = new IScannedInput[0];
	public IScannedOutput[] outputs = new IScannedOutput[1];

	public DelayOutBlock(IScanProcess process, String fn) {fieldName = fn; outputs[0] = process.createOutput();}

	@Override
	public ICompilableExpression[] compile(final ICompileContext ctx, ICompilableExpression[] inputs) {
		return new ICompilableExpression[] {
			new ICompilableExpression() {
				@Override
				public void compile(ICompileContext ctx) {
					ctx.getCodeVisitor().visitVarInsn(Opcodes.ALOAD, 0);
					ctx.getCodeVisitor().visitFieldInsn(Opcodes.GETFIELD, ctx.getClassNameInternal(), fieldName, "Z");
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
