package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.Arrays;
import java.util.Collection;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.compiler.util.ZeroExpr;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class GateSynchronizer {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			final IScannedInput inLeft = process.createInput();
			final IScannedInput inRight = process.createInput();
			final IScannedInput inReset = process.createInput();
			final IScannedOutput out = process.createOutput();
			
			nodes[LEFT].getWire(0).addInput(inLeft);
			nodes[RIGHT].getWire(0).addInput(inRight);
			nodes[BACK].getWire(0).addInput(inReset);
			nodes[FRONT].getWire(0).addOutput(out);
			
			return Arrays.<ICompilableBlock>asList(new ICompilableBlock() {
				@Override
				public IScannedInput[] getInputs() {
					return new IScannedInput[] {inLeft, inRight, inReset};
				}
				@Override
				public IScannedOutput[] getOutputs() {
					return new IScannedOutput[] {out};
				}
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
					String leftLatchField = ctx.createField("Z");
					String rightLatchField = ctx.createField("Z");
					
					final int pulseLocal = ctx.createLocal("Z");
					
					Label leftAlreadyLatched = new Label(), rightAlreadyLatched = new Label();
					Label end = new Label(), notReset = new Label();
					
					ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_0);
					ctx.getCodeVisitor().visitVarInsn(Opcodes.ISTORE, pulseLocal);
					
					if(!(inputs[2] instanceof ZeroExpr)) {
						// reset input
						
						inputs[2].compile(ctx);
						ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFEQ, notReset);
						ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_0);
						ctx.storeField(leftLatchField, "Z");
						ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_0);
						ctx.storeField(rightLatchField, "Z");
						ctx.getCodeVisitor().visitJumpInsn(Opcodes.GOTO, end);
						ctx.getCodeVisitor().visitLabel(notReset);
					}
					
					ctx.loadField(leftLatchField, "Z");
					ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFNE, leftAlreadyLatched);
					inputs[0].compile(ctx);
					ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
					ctx.getCodeVisitor().visitInsn(Opcodes.IXOR);
					ctx.detectRisingEdge();
					ctx.storeField(leftLatchField, "Z");
					ctx.getCodeVisitor().visitLabel(leftAlreadyLatched);
					
					ctx.loadField(rightLatchField, "Z");
					ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFNE, rightAlreadyLatched);
					inputs[1].compile(ctx);
					ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
					ctx.getCodeVisitor().visitInsn(Opcodes.IXOR);
					ctx.detectRisingEdge();
					ctx.storeField(rightLatchField, "Z");
					ctx.getCodeVisitor().visitLabel(rightAlreadyLatched);
					
					ctx.loadField(rightLatchField, "Z");
					ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFEQ, end);
					ctx.loadField(leftLatchField, "Z");
					ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFEQ, end);
					ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_0);
					ctx.storeField(leftLatchField, "Z");
					ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_0);
					ctx.storeField(rightLatchField, "Z");
					ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
					ctx.getCodeVisitor().visitVarInsn(Opcodes.ISTORE, pulseLocal);
					ctx.getCodeVisitor().visitLabel(end);
					
					return new ICompilableExpression[] {
						new ICompilableExpression() {
							
							@Override
							public void compile(ICompileContext ctx) {
								ctx.getCodeVisitor().visitVarInsn(Opcodes.ILOAD, pulseLocal);
							}
							
							@Override
							public boolean alwaysInline() {
								return true;
							}
						}
					};
				}
			});
		}
	}
	public static class Logic extends GateLogic {
		private boolean leftLatch, rightLatch, wasLeft, wasRight;
		private int pulseTicks;
		
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[LEFT] == 0 && wasLeft)
				leftLatch = true;
			if(inputs[RIGHT] == 0 && wasRight)
				rightLatch = true;
			if(inputs[BACK] != 0)
				leftLatch = rightLatch = false;
			if(leftLatch && rightLatch) {
				pulseTicks = 2;
				leftLatch = rightLatch = false;
			}
			
			wasLeft = inputs[LEFT] != 0;
			wasRight = inputs[RIGHT] != 0;
			
			if(pulseTicks > 0) {
				outputs[FRONT] = (short)255;
				pulseTicks--;
			} else
				outputs[FRONT] = 0;
		}
		
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == LEFT || side == RIGHT || side == BACK;
		}
		
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT;
		}
		
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return (inputs[LEFT] != 0 ? 1 : 0)
				| (inputs[RIGHT] != 0 ? 2 : 0)
				| (inputs[BACK] != 0 ? 4 : 0)
				| (outputs[FRONT] != 0 ? 8 : 0)
				| (leftLatch ? 16 : 0)
				| (rightLatch ? 32 : 0);
		}
		
		@Override
		public void write(NBTTagCompound tag) {
			super.write(tag);
			
			tag.setByte("f", (byte)(pulseTicks
				| (leftLatch ? 4 : 0)
				| (rightLatch ? 8 : 0)
				| (wasLeft ? 16 : 0)
				| (wasRight ? 32 : 0)
				));
		}
		
		@Override
		public void read(NBTTagCompound tag) {
			super.read(tag);
			
			byte f = tag.getByte("f");
			pulseTicks = f & 3;
			leftLatch = (f & 4) != 0;
			rightLatch = (f & 8) != 0;
			wasLeft = (f & 16) != 0;
			wasRight = (f & 32) != 0;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0, 0, 0, 0};
			segmentTex = new String[] {"sync-base", "sync-left", "sync-right", "sync-back", "sync-middle", "sync-left-middle", "sync-right-middle"};
			torchX = new float[] {8f};
			torchY = new float[] {3f};
			torchState = new boolean[] {false};
		}
		
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 1) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 2) != 0 ? ON : OFF;
			segmentCol[3] = (renderState & 4) != 0 ? ON : OFF;
			torchState[0] = (renderState & 8) != 0;
			segmentCol[4] = (renderState & 48) != 0 ? ON : OFF;
			segmentCol[5] = (renderState & 16) != 0 ? OFF : ON;
			segmentCol[6] = (renderState & 32) != 0 ? OFF : ON;
		}
		
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
			segmentCol[2] = OFF;
			segmentCol[3] = OFF;
			segmentCol[4] = OFF;
			segmentCol[5] = ON;
			segmentCol[6] = ON;
			torchState[0] = false;
		}
	}
}
