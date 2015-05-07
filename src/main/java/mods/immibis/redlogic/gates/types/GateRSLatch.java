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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class GateRSLatch {
	// This gate behaves differently when compiled! It will oscillate rapidly until initialized.
	// When interpreted, it will instead stick in an invalid state until initialized. 
	
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			final IScannedOutput outLeft = process.createOutput();
			final IScannedOutput outRight = process.createOutput();
			final IScannedOutput outFront = process.createOutput();
			final IScannedOutput outBack = process.createOutput();
			final IScannedInput inLeft = process.createInput();
			final IScannedInput inRight = process.createInput();
			
			nodes[FRONT].getWire(0).addOutput(outLeft);
			nodes[BACK].getWire(0).addOutput(outBack);
			nodes[LEFT].getWire(0).addOutput(outLeft);
			nodes[RIGHT].getWire(0).addOutput(outRight);
			nodes[LEFT].getWire(0).addInput(inLeft);
			nodes[RIGHT].getWire(0).addInput(inRight);
			
			return Collections.<ICompilableBlock>singleton(new ICompilableBlock() {
				@Override
				public IScannedOutput[] getOutputs() {
					// matches FRONT/BACK/LEFT/RIGHT constants
					return new IScannedOutput[] {outFront, outBack, outLeft, outRight};
				}
				
				@Override
				public IScannedInput[] getInputs() {
					return new IScannedInput[] {inLeft, inRight};
				}
				
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, final ICompilableExpression[] inputs) {
					final String outLeftField = ctx.createField("Z");
					final String outRightField = ctx.createField("Z");
					
					/*if(inputs[LEFT] != 0) {
						outputs[RIGHT] = 0;
						outputs[LEFT] = inputs[RIGHT] ^ 1;
					} else {
						leftOff:
						if(inputs[RIGHT] != 0) {
							outputs[RIGHT] = (short)255;
							outputs[LEFT] = 0;
						} else {
							bothOff:
							if(outputs[LEFT] == 0) {
								outputs[RIGHT] = (short)255;
							}
						}
					}
					end:*/
					
					/*
					GET inputs[RIGHT] (saved on stack for later)
					GET inputs[LEFT]
					IFEQ leftOff
						ICONST_1
						IXOR (uses saved)
						PUT outLeftField
						ICONST_0
						PUT outRightField
						GOTO end
					leftOff:
						IFEQ bothOff (uses saved)
							ICONST_1
							PUT outRightField
							ICONST_0
							PUT outLeftField
							GOTO end
						bothOff:
							GET outLeftField
							IFNE end
							ICONST_1
							PUT outRightField
					end:
					*/
				
					MethodVisitor mv = ctx.getCodeVisitor();
					Label leftOff = new Label();
					Label bothOff = new Label();
					Label end = new Label();
					
					inputs[1].compile(ctx);
					inputs[0].compile(ctx);
					mv.visitJumpInsn(Opcodes.IFEQ, leftOff);
					mv.visitInsn(Opcodes.ICONST_1);
					mv.visitInsn(Opcodes.IXOR);
					ctx.storeField(outLeftField, "Z");
					mv.visitInsn(Opcodes.ICONST_0);
					ctx.storeField(outRightField, "Z");
					mv.visitJumpInsn(Opcodes.GOTO, end);
					mv.visitLabel(leftOff);
					mv.visitJumpInsn(Opcodes.IFEQ, bothOff);
					mv.visitInsn(Opcodes.ICONST_1);
					ctx.storeField(outRightField, "Z");
					mv.visitInsn(Opcodes.ICONST_0);
					ctx.storeField(outLeftField, "Z");
					mv.visitJumpInsn(Opcodes.GOTO, end);
					mv.visitLabel(bothOff);
					ctx.loadField(outLeftField, "Z");
					mv.visitJumpInsn(Opcodes.IFNE, end);
					mv.visitInsn(Opcodes.ICONST_1);
					ctx.storeField(outRightField, "Z");
					mv.visitLabel(end);
					
					return new ICompilableExpression[] {
						// Front
						new ICompilableExpression() {
							@Override
							public void compile(ICompileContext ctx) {
								ctx.loadField(outLeftField, "Z");
							}
							
							@Override
							public boolean alwaysInline() {
								return true;
							}
						},
						// Back
						new ICompilableExpression() {
							@Override
							public void compile(ICompileContext ctx) {
								ctx.loadField(outRightField, "Z");
							}
							
							@Override
							public boolean alwaysInline() {
								return true;
							}
						},
						// Left
						new ICompilableExpression() {
							@Override
							public void compile(ICompileContext ctx) {
								ctx.loadField(outLeftField, "Z");
							}
							
							@Override
							public boolean alwaysInline() {
								return true;
							}
						},
						// Right
						new ICompilableExpression() {
							@Override
							public void compile(ICompileContext ctx) {
								ctx.loadField(outRightField, "Z");
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
	public static class Logic extends GateLogic implements Stateless, Flippable {
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[LEFT] != 0 && inputs[RIGHT] != 0)
				outputs[LEFT] = outputs[RIGHT] = 0;
			else if(inputs[LEFT] != 0) {
				outputs[LEFT] = (short)255;
				outputs[RIGHT] = 0;
			} else if(inputs[RIGHT] != 0) {
				outputs[RIGHT] = (short)255;
				outputs[LEFT] = 0;
			} else if(outputs[LEFT] == 0 && outputs[RIGHT] == 0) {
				// arbitrarily pick
				outputs[RIGHT] = (short)255;
				outputs[LEFT] = 0;
			}
			
			outputs[FRONT] = !(inputs[RIGHT] != 0 || outputs[RIGHT] != 0) ? (short)255 : 0;
			outputs[BACK] = !(inputs[LEFT] != 0 || outputs[LEFT] != 0) ? (short)255 : 0;
		}
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == LEFT || side == RIGHT;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return true;
		}
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return
				(inputs[LEFT] != 0 || outputs[LEFT] != 0 ? 1 : 0)
				| (inputs[RIGHT] != 0 || outputs[RIGHT] != 0 ? 2 : 0)
				| (outputs[FRONT] != 0 ? 4 : 0)
				| (outputs[BACK] != 0 ? 8 : 0);
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"rs-base", "rs-left", "rs-right"};
			segmentCol = new int[] {0xFFFFFF, 0, 0};
			torchX = new float[] {6.5f, 9.5f};
			torchY = new float[] {3.5f, 12.5f};
			torchState = new boolean[] {false, false};
		}
		@Override
		public void setItemRender() {
			segmentCol[1] = ON;
			segmentCol[2] = OFF;
			torchState[0] = true;
			torchState[1] = false;
		}
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 1) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 2) != 0 ? ON : OFF;
			torchState[0] = (renderState & 4) != 0;
			torchState[1] = (renderState & 8) != 0;
		}
	}
}
