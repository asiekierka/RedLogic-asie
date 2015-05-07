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
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class GateDFlop {
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
					
					final String dataFieldName = ctx.createField("Z");
					String lastInputFieldName = ctx.createField("Z");
					
					Label noSetLabel = new Label();
					
					// if(!lastInputFieldName & (lastInputFieldName = inputs[1])) {
					//   dataFieldName = inputs[0];
					// }
					//
					// GET lastInputFieldName
					// NOT
					// GET inputs[1]
					// DUP
					// PUT lastInputFieldName
					// IAND
					// IFEQ skip
					// GET inputs[0]
					// PUT dataFieldName
					// skip:
					
					ctx.loadField(lastInputFieldName, "Z");
					ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
					ctx.getCodeVisitor().visitInsn(Opcodes.IXOR);
					inputs[1].compile(ctx);
					ctx.getCodeVisitor().visitInsn(Opcodes.DUP);
					ctx.storeField(lastInputFieldName, "Z");
					ctx.getCodeVisitor().visitInsn(Opcodes.IAND);
					ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFEQ, noSetLabel);
					inputs[0].compile(ctx);
					ctx.storeField(dataFieldName, "Z");
					ctx.getCodeVisitor().visitLabel(noSetLabel);
					
					
					return new ICompilableExpression[] {new ICompilableExpression() {
						@Override
						public void compile(ICompileContext ctx) {
							ctx.loadField(dataFieldName, "Z");
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
	
	public static class Logic extends GateLogic implements Flippable {
		private boolean clockWasOn;
		
		@Override
		public void write(NBTTagCompound tag) {
			tag.setBoolean("clock", clockWasOn);
		}
		
		@Override
		public void read(NBTTagCompound tag) {
			clockWasOn = tag.getBoolean("clock");
		}
		
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[RIGHT] != 0 && !clockWasOn)
				outputs[FRONT] = outputs[LEFT] = (short)(inputs[BACK] != 0 ? 255 : 0);
			clockWasOn = inputs[RIGHT] != 0;
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
			return side == FRONT;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0};
			segmentTex = new String[] {"dflop-base", "dflop-in", "dflop-enable", "dflop-out"};
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
