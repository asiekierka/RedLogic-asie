package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateLogic.Flippable;
import mods.immibis.redlogic.gates.GateLogic.Stateless;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class GateDLatch {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			
			final IScannedInput in = process.createInput();
			final IScannedInput inControl = process.createInput();
			final IScannedOutput out = process.createOutput();
			
			nodes[FRONT].getWire(0).addOutput(out);
			nodes[LEFT].getWire(0).addOutput(out);
			nodes[RIGHT].getWire(0).addInput(inControl);
			nodes[BACK].getWire(0).addInput(in);
			
			return Collections.<ICompilableBlock>singleton(new ICompilableBlock() {
				@Override
				public IScannedOutput[] getOutputs() {
					return new IScannedOutput[] {out};
				}
				
				@Override
				public IScannedInput[] getInputs() {
					return new IScannedInput[] {in, inControl};
				}
				
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
					
					final String fieldName = ctx.createField("Z");
					
					Label noSetLabel = new Label();
					
					inputs[1].compile(ctx);
					ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFEQ, noSetLabel);
					inputs[0].compile(ctx);
					ctx.getCodeVisitor().visitVarInsn(Opcodes.ALOAD, 0);
					ctx.getCodeVisitor().visitInsn(Opcodes.SWAP);
					ctx.getCodeVisitor().visitFieldInsn(Opcodes.PUTFIELD, ctx.getClassNameInternal(), fieldName, "Z");
					ctx.getCodeVisitor().visitLabel(noSetLabel);
					
					
					return new ICompilableExpression[] {new ICompilableExpression() {
						@Override
						public void compile(ICompileContext ctx) {
							ctx.getCodeVisitor().visitVarInsn(Opcodes.ALOAD, 0);
							ctx.getCodeVisitor().visitFieldInsn(Opcodes.GETFIELD, ctx.getClassNameInternal(), fieldName, "Z");
						}
						
						@Override
						public boolean alwaysInline() {
							return true;
						}
					}};
				}
			});
		}
	}
	
	public static class Logic extends GateLogic implements Stateless, Flippable {
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[RIGHT] != 0)
				outputs[FRONT] = outputs[LEFT] = (short)(inputs[BACK] != 0 ? 255 : 0);
		}
		
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return
				(inputs[BACK] != 0 ? 1 : 0)
				| (inputs[RIGHT] != 0 ? 2 : 0)
				| (outputs[FRONT] != 0 ? 4 : 0)
				;
		}
		
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == BACK || side == RIGHT;
		}
		
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT || side == LEFT;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0};
			segmentTex = new String[] {"dlatch-base", "dlatch-in", "dlatch-enable", "dlatch-out"};
		}
		
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 1) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 2) != 0 ? ON : OFF;
			segmentCol[3] = (renderState & 4) != 0 ? ON : OFF;
		}
		
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
			segmentCol[2] = OFF;
			segmentCol[3] = OFF;
		}
	}
}
