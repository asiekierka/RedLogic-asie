package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.compiler.util.SelectExpr;
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

public class GateMultiplexer {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			final IScannedInput inSelect = process.createInput();
			final IScannedInput inZero = process.createInput();
			final IScannedInput inOne = process.createInput();
			final IScannedOutput out = process.createOutput();
			
			nodes[FRONT].getWire(0).addOutput(out);
			nodes[BACK].getWire(0).addInput(inSelect);
			nodes[LEFT].getWire(0).addInput(inOne);
			nodes[RIGHT].getWire(0).addInput(inZero);
			
			return Collections.<ICompilableBlock>singleton(
				new ICompilableBlock() {
					@Override
					public IScannedOutput[] getOutputs() {
						return new IScannedOutput[] {out};
					}
					
					@Override
					public IScannedInput[] getInputs() {
						return new IScannedInput[] {inSelect, inZero, inOne};
					}
					
					@Override
					public ICompilableExpression[] compile(ICompileContext ctx, final ICompilableExpression[] inputs) {
						
						return new ICompilableExpression[] {SelectExpr.createSelect(inputs[0], inputs[1], inputs[2])};
						
						/*
						// out = (inOne & inSelect) | (inZero & !inSelect)
						//     = inZero ^ ((inZero ^ inOne) & inSelect)
						return new ICompilableExpression[] {
							new ICompilableExpression() {
								
								@Override
								public void compile(ICompileContext ctx) {
									inputs[1].compile(ctx);
									ctx.getCodeVisitor().visitInsn(Opcodes.DUP);
									inputs[2].compile(ctx);
									ctx.getCodeVisitor().visitInsn(Opcodes.IXOR);
									inputs[0].compile(ctx);
									ctx.getCodeVisitor().visitInsn(Opcodes.IAND);
									ctx.getCodeVisitor().visitInsn(Opcodes.IXOR);
								}
								
								@Override
								public boolean alwaysInline() {
									return false;
								}
							}
						};*/
					}
				}
			);
		}
	}
	
	public static class Logic extends GateLogic implements Stateless, Flippable {
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			outputs[FRONT] = inputs[BACK] != 0 ? inputs[LEFT] : inputs[RIGHT];
		}
		
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == BACK || side == LEFT || side == RIGHT;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT;
		}
		
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return (inputs[BACK] != 0 ? 1 : 0)
					| (inputs[LEFT] != 0 ? 2 : 0)
					| (inputs[RIGHT] != 0 ? 4 : 0)
					| (outputs[FRONT] != 0 ? 8 : 0);
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"multiplexer-base", "multiplexer-2", "multiplexer-3", "multiplexer-right", "multiplexer-5", "multiplexer-left-out", "multiplexer-right-out"};
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0, 0, 0, 0};
			torchX = new float[] {8f, 4.5f, 11.5f, 4.5f};
			torchY = new float[] {2f, 7.5f, 7.5f, 12.5f};
			torchState = new boolean[] {false, false, false, false};
		}
		
		@Override
		public void set(int renderState) {
			boolean back = (renderState & 1) != 0;
			boolean left = (renderState & 2) != 0;
			boolean right = (renderState & 4) != 0;
			boolean out = (renderState & 8) != 0;
			segmentCol[1] = back ? ON : OFF;
			segmentCol[2] = left ? ON : OFF;
			segmentCol[3] = right ? ON : OFF;
			segmentCol[4] = !back ? ON : OFF;
			segmentCol[5] = !left && back ? ON : OFF;
			segmentCol[6] = !right && !back ? ON : OFF;
			torchState[0] = out;
			torchState[1] = !left && back;
			torchState[2] = !right && !back;
			torchState[3] = !back;
		}
		
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
			segmentCol[2] = OFF;
			segmentCol[3] = OFF;
			segmentCol[4] = ON;
			segmentCol[5] = OFF;
			segmentCol[6] = ON;
			torchState[0] = false;
			torchState[1] = false;
			torchState[2] = true;
			torchState[3] = true;
		}
	}
}
