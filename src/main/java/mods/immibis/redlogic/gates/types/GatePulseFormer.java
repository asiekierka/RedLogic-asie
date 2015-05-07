package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.Arrays;
import java.util.Collection;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

public class GatePulseFormer {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			final IScannedInput in = process.createInput();
			final IScannedOutput out = process.createOutput();
			
			nodes[FRONT].getWire(0).addOutput(out);
			nodes[BACK].getWire(0).addInput(in);
			
			return Arrays.<ICompilableBlock>asList(new ICompilableBlock() {
				@Override
				public IScannedInput[] getInputs() {
					return new IScannedInput[] {in};
				}
				@Override
				public IScannedOutput[] getOutputs() {
					return new IScannedOutput[] {out};
				}
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, final ICompilableExpression[] inputs) {
					return new ICompilableExpression[] {
						new ICompilableExpression() {
							@Override
							public boolean alwaysInline() {
								return false;
							}
							@Override
							public void compile(ICompileContext ctx) {
								inputs[0].compile(ctx);
								ctx.detectRisingEdge();
							}
						}
					};
				}
			});
		}
	}

	public static class Logic extends GateLogic {
		private boolean prevInput;
		private int ticksLeft;
		
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[BACK] != 0 && !prevInput) {
				outputs[FRONT] = (short)255;
				ticksLeft = 3;
			}
			if(ticksLeft > 0) {
				ticksLeft--;
				if(ticksLeft == 0)
					outputs[FRONT] = 0;
			}
			prevInput = inputs[BACK] != 0;
		}
		
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == BACK;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT;
		}
		
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return (inputs[BACK] != 0 ? 1 : 0) | (outputs[FRONT] != 0 ? 2 : 0) | (ticksLeft > 0 ? 4 : 0);
		}
		
		@Override
		public void write(NBTTagCompound tag) {
			super.write(tag);
			tag.setByte("ticksLeft", (byte)ticksLeft);
		}
		
		@Override
		public void read(NBTTagCompound tag) {
			super.read(tag);
			ticksLeft = tag.getByte("ticksLeft");
		}
		
		@Override
		public boolean connectsToDirection(int side, int gateSettings) {
			return side == FRONT || side == BACK;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"former-base", "former-in", "former-3", "former-4", "former-5"};
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0, 0};
			torchX = new float[] {8f, 4.5f, 11.5f};
			torchY = new float[] {2f, 8f, 8f};
			torchState = new boolean[] {false, false, false};
		}
		
		@Override
		public void set(int renderState) {
			boolean in = (renderState & 1) != 0;
			boolean out = (renderState & 2) != 0;
			boolean changing = (renderState & 4) != 0;
			torchState[0] = out;
			torchState[1] = !in;
			torchState[2] = in && (!out || changing);
			segmentCol[1] = in ? ON : OFF;
			segmentCol[2] = in ? OFF : ON;
			segmentCol[3] = in ? OFF : ON;
			segmentCol[4] = /*in && !out*/ torchState[2] ? ON : OFF;
		}
		
		@Override
		public void setItemRender() {
			torchState[0] = false;
			torchState[1] = true;
			torchState[2] = false;
			segmentCol[1] = OFF;
			segmentCol[2] = ON;
			segmentCol[3] = ON;
			segmentCol[4] = OFF;
		}
	}
}
