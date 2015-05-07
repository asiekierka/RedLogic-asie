package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.compiler.util.MergeExpr;
import mods.immibis.redlogic.api.chips.compiler.util.NotExpr;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateLogic.Stateless;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

public class GateNOR {
	public static class Compiler extends GateCompiler {

		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound gateTag, int gateSettings) {
			// gateSettings 1: left input ignored
			// gateSettings 2: back input ignored
			// gateSettings 4: right input ignored
			
			List<IScannedNode> inNodes = new ArrayList<IScannedNode>(3);
			if((gateSettings & 1) == 0) inNodes.add(nodes[LEFT]);
			if((gateSettings & 2) == 0) inNodes.add(nodes[BACK]);
			if((gateSettings & 4) == 0) inNodes.add(nodes[RIGHT]);
			
			List<IScannedInput> inputs = new ArrayList<IScannedInput>(inNodes.size());
			
			for(IScannedNode n : inNodes) {
				IScannedInput i = process.createInput();
				n.getWire(0).addInput(i);
				inputs.add(i);
			}
			
			IScannedOutput output = process.createOutput();
			nodes[FRONT].getWire(0).addOutput(output);
			
			IScannedInput[] inputsArray = inputs.toArray(new IScannedInput[inputs.size()]);
			IScannedOutput[] outputsArray = new IScannedOutput[] {output};
			
			return Collections.<ICompilableBlock>singleton(new NorBlock(inputsArray, outputsArray));
		}
		
		private static class NorBlock implements ICompilableBlock {
			private IScannedOutput[] outputsArray;
			private IScannedInput[] inputsArray;
			
			NorBlock(IScannedInput[] inputs, IScannedOutput[] outputs) {
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
				return new ICompilableExpression[] {NotExpr.createNOT(MergeExpr.createOR(inputs))};
			}
		}
	}
	
	public static class Logic extends GateLogic implements Stateless {
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			boolean left = inputs[LEFT] != 0 && (gateSettings & 1) == 0;
			boolean back = inputs[BACK] != 0 && (gateSettings & 2) == 0;
			boolean right = inputs[RIGHT] != 0 && (gateSettings & 4) == 0;
			outputs[FRONT] = !left && !back && !right ? (short)255 : 0;
		}
		@Override
		public boolean connectsToDirection(int side, int gateSettings) {
			switch(side) {
			case LEFT: return (gateSettings & 1) == 0;
			case BACK: return (gateSettings & 2) == 0;
			case RIGHT: return (gateSettings & 4) == 0;
			default: return true;
			}
		}
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return
				(inputs[FRONT] != 0 || outputs[FRONT] != 0 ? 1 : 0)
				| (inputs[BACK] != 0 ? 2 : 0)
				| (inputs[LEFT] != 0 ? 4 : 0)
				| (inputs[RIGHT] != 0 ? 8 : 0)
				| (outputs[FRONT] != 0 ? 16 : 0)
				| (gateSettings << 5);
		}
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return (side == LEFT && (gateSettings & 1) == 0)
				|| (side == BACK && (gateSettings & 2) == 0)
				|| (side == RIGHT && (gateSettings & 4) == 0);
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT;
		}
		@Override
		public int configure(int gateSettings) {
			return (gateSettings + 1) & 7;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"nor-base", "nor-out", "nor-back", "nor-right", "nor-left"};
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0, 0};
			torchX = new float[] {7.5f};
			torchY = new float[] {7.5f};
			torchState = new boolean[] {false};
		}
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 1) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 64) != 0 ? DISABLED : (renderState & 2) != 0 ? ON : OFF;
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
