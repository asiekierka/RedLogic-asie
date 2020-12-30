package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.compiler.util.NotExpr;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

public class GateNOT {
	public static class Compiler extends GateCompiler {

		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound gateTag, int gateSettings) {
			// gateSettings 1: left output ignored
			// gateSettings 2: front output ignored
			// gateSettings 4: right output ignored
			
			IScannedInput input = process.createInput();
			nodes[BACK].getWire(0).addInput(input);
			
			IScannedOutput output = process.createOutput();
			if((gateSettings & 1) == 0) nodes[LEFT].getWire(0).addOutput(output);
			if((gateSettings & 2) == 0) nodes[FRONT].getWire(0).addOutput(output);
			if((gateSettings & 4) == 0) nodes[RIGHT].getWire(0).addOutput(output);
			
			return Collections.<ICompilableBlock>singleton(new NotBlock(new IScannedInput[] {input}, new IScannedOutput[] {output}));
		}
		
		private static class NotBlock implements ICompilableBlock {
			private IScannedOutput[] outputsArray;
			private IScannedInput[] inputsArray;
			
			NotBlock(IScannedInput[] inputs, IScannedOutput[] outputs) {
				this.outputsArray = outputs;
				this.inputsArray = inputs;
			}
			
			@Override
			public IScannedOutput[] getOutputs() {
				return outputsArray;
			}
			
			@Override
			public IScannedInput[] getInputs() {
				return inputsArray;
			}
			
			@Override
			public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
				return new ICompilableExpression[] {NotExpr.createNOT(inputs[0])};
			}
		}
	}
	
	public static class Logic extends GateLogic implements GateLogic.Stateless {
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			outputs[FRONT] = outputs[LEFT] = outputs[RIGHT] = (short)(inputs[BACK] != 0 ? 0 : 255);
			if((gateSettings & 1) != 0)
				outputs[LEFT] = 0;
			if((gateSettings & 2) != 0)
				outputs[FRONT] = 0;
			if((gateSettings & 4) != 0)
				outputs[RIGHT] = 0;
		}
		@Override
		public boolean connectsToDirection(int side, int gateSettings) {
			switch(side) {
			case LEFT: return (gateSettings & 1) == 0;
			case FRONT: return (gateSettings & 2) == 0;
			case RIGHT: return (gateSettings & 4) == 0;
			default: return true;
			}
		}
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return
				(inputs[FRONT] != 0 || outputs[FRONT] != 0 ? 1 : 0)
				| (inputs[BACK] != 0 ? 2 : 0)
				| (inputs[LEFT] != 0 || outputs[LEFT] != 0 ? 4 : 0)
				| (inputs[RIGHT] != 0 || outputs[RIGHT] != 0 ? 8 : 0)
				| ((outputs[FRONT] | outputs[LEFT] | outputs[RIGHT]) != 0 ? 16 : 0)
				| (gateSettings << 5);
		}
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == BACK;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return (side == LEFT && (gateSettings & 1) == 0)
				|| (side == FRONT && (gateSettings & 2) == 0)
				|| (side == RIGHT && (gateSettings & 4) == 0);
		}
		@Override
		public int configure(int gateSettings) {
			return (gateSettings + 1) % 7;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"not-base", "not-ovl-out", "not-ovl-back", "not-ovl-right", "not-ovl-left"};
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0, 0};
			torchX = new float[] {7.5f};
			torchY = new float[] {7.5f};
			torchState = new boolean[] {false};
		}
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 64) != 0 ? DISABLED : (renderState & 1) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 2) != 0 ? ON : OFF;
			segmentCol[3] = (renderState & 128) != 0 ? DISABLED : (renderState & 8) != 0 ? ON : OFF;
			segmentCol[4] = (renderState & 32) != 0 ? DISABLED : (renderState & 4) != 0 ? ON : OFF;
			torchState[0] = (renderState & 16) != 0;
		}
		@Override
		public void setItemRender() {
			segmentCol[1] = segmentCol[3] = segmentCol[4] = ON;
			segmentCol[2] = OFF;
			torchState[0] = true;
		}
	}
}
