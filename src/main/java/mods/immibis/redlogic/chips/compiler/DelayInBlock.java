package mods.immibis.redlogic.chips.compiler;

import org.objectweb.asm.Opcodes;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

public class DelayInBlock implements ICompilableBlock {
	private String fieldName;
	
	public DelayInBlock(IScanProcess process, String fn) {fieldName = fn; inputs[0] = process.createInput();}
	
	public IScannedInput[] inputs = new IScannedInput[1];
	public IScannedOutput[] outputs = new IScannedOutput[0];

	@Override
	public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
		ctx.getCodeVisitor().visitVarInsn(Opcodes.ALOAD, 0);
		inputs[0].compile(ctx);
		ctx.getCodeVisitor().visitFieldInsn(Opcodes.PUTFIELD, ctx.getClassNameInternal(), fieldName, "Z");
		return null;
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
