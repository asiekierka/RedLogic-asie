package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.compiler.util.MergeExpr;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateLogic.Stateless;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

public class GateXOR {
	public static class Compiler extends GateCompiler {

		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound gateTag, int gateSettings) {
			final IScannedInput inA = process.createInput();
			final IScannedInput inB = process.createInput();
			final IScannedOutput out = process.createOutput();
			
			nodes[LEFT].getWire(0).addInput(inA);
			nodes[RIGHT].getWire(0).addInput(inB);
			nodes[FRONT].getWire(0).addOutput(out);
			
			return Collections.<ICompilableBlock>singleton(new ICompilableBlock() {
				@Override
				public IScannedOutput[] getOutputs() {
					return new IScannedOutput[] {out};
				}
				
				@Override
				public IScannedInput[] getInputs() {
					return new IScannedInput[] {inA, inB};
				}
				
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, final ICompilableExpression[] inputs) {
					return new ICompilableExpression[] {MergeExpr.createXOR(inputs)};
				}
			});
		}
		
	}
	
	public static class Logic extends GateLogic implements Stateless {
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			outputs[FRONT] = (inputs[LEFT] != 0) ^ (inputs[RIGHT] != 0) ? (short)255 : 0;
		}
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == LEFT || side == RIGHT;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT;
		}
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return
				(inputs[LEFT] != 0 ? 1 : 0)
				| (inputs[RIGHT] != 0 ? 2 : 0)
				| (outputs[FRONT] != 0 ? 4 : 0);
		}
		@Override
		public boolean connectsToDirection(int side, int gateSettings) {
			return side != BACK;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"xor-base", "xor-left", "xor-right", "xor-middle", "xor-out"};
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0, 0};
			torchX = new float[] {4.5f, 11.5f,  8.5f};
			torchY = new float[] {9.5f,  9.5f, 13.5f};
			torchState = new boolean[] {false, false, false};
		}
		@Override
		public void set(int renderState) {
			boolean left = (renderState & 1) != 0;
			boolean right = (renderState & 2) != 0;
			boolean out = (renderState & 4) != 0;
			segmentCol[1] = left ? ON : OFF;
			segmentCol[2] = right ? ON : OFF;
			segmentCol[3] = !left && !right ? ON : OFF;
			segmentCol[4] = out ? ON : OFF;
			torchState[0] = !left && right;
			torchState[1] = left && !right;
			torchState[2] = !left && !right;
		}
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
			segmentCol[2] = OFF;
			segmentCol[3] = ON;
			segmentCol[4] = OFF;
			torchState[0] = false;
			torchState[1] = false;
			torchState[2] = true;
		}
	}
}
