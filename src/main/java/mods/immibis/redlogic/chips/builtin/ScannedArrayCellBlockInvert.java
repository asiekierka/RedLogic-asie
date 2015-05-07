package mods.immibis.redlogic.chips.builtin;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

public class ScannedArrayCellBlockInvert extends ScannedArrayCellBlock {
	private static final long serialVersionUID = 1L;
	
	public ScannedArrayCellBlockInvert(IScanProcess process, int side, int front) {
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
				return new ICompilableExpression[] {
					new ICompilableExpression() {
						@Override
						public void compile(ICompileContext ctx) {
							inputs[0].compile(ctx);
							ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
							ctx.getCodeVisitor().visitInsn(Opcodes.IXOR);
						}
						
						@Override
						public boolean alwaysInline() {
							return false;
						}
					}
				};
			}
		});
	}
}
