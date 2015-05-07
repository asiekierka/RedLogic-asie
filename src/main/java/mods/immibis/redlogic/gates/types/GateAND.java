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
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateLogic.Stateless;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

public class GateAND {
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
			
			return Collections.<ICompilableBlock>singleton(new AndBlock(inputsArray, outputsArray));
		}
		
		private static class AndBlock implements ICompilableBlock {
			private IScannedOutput[] outputsArray;
			private IScannedInput[] inputsArray;
			
			AndBlock(IScannedInput[] inputs, IScannedOutput[] outputs) {
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
				return new ICompilableExpression[] {MergeExpr.createAND(inputs)};
			}
		}
		
		
	}
	
	public static class Logic extends GateLogic implements Stateless {
		// gateSettings 1: left input ignored
		// gateSettings 2: back input ignored
		// gateSettings 4: right input ignored
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			boolean left = inputs[LEFT] != 0 || (gateSettings & 1) != 0;
			boolean back = inputs[BACK] != 0 || (gateSettings & 2) != 0;
			boolean right = inputs[RIGHT] != 0 || (gateSettings & 4) != 0;
			outputs[FRONT] = (short)(left && back && right ? 255 : 0);
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
				(outputs[FRONT] != 0 ? 1 : 0)
				| (inputs[BACK] != 0 ? 2 : 0)
				| (inputs[LEFT] != 0 || outputs[LEFT] != 0 ? 4 : 0)
				| (inputs[RIGHT] != 0 || outputs[RIGHT] != 0 ? 8 : 0)
				| (gateSettings << 4);
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
			segmentTex = new String[] {"and-base", "and-ovl-out", "and-ovl-back", "and-ovl-right", "and-ovl-left"};
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0, 0};
			torchX = new float[] {8.5f, 4.5f, 8.5f, 12.5f};
			torchY = new float[] {2.5f, 6.5f, 6.5f, 6.5f};
			torchState = new boolean[] {false, true, true, true};
		}
		@Override
		public void set(int renderState) {
			boolean out_on = (renderState & 1) != 0;
			boolean back_on = (renderState & 2) != 0;
			boolean left_on = (renderState & 4) != 0;
			boolean right_on = (renderState & 8) != 0;
			boolean left_disabled = (renderState & 16) != 0;
			boolean back_disabled = (renderState & 32) != 0;
			boolean right_disabled = (renderState & 64) != 0;
			segmentCol[1] = out_on ? OFF : ON;
			segmentCol[2] = back_disabled ? DISABLED : back_on ? ON : OFF;
			segmentCol[3] = right_disabled ? DISABLED : right_on ? ON : OFF;
			segmentCol[4] = left_disabled ? DISABLED : left_on ? ON : OFF;
			torchState[0] = out_on;
			torchState[1] = !left_on && !left_disabled;
			torchState[2] = !back_on && !back_disabled;
			torchState[3] = !right_on && !right_disabled;
		}
		@Override
		public void setItemRender() {
			segmentCol[1] = ON;
			segmentCol[2] = OFF;
			segmentCol[3] = OFF;
			segmentCol[4] = OFF;
			torchState[0] = false;
			torchState[1] = true;
			torchState[2] = true;
			torchState[3] = true;
		}
	}
}
