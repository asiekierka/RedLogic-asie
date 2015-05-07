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
import mods.immibis.redlogic.gates.GateLogic.Stateless;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

public class GateBuffer {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			final IScannedInput input = process.createInput();
			final IScannedOutput output = process.createOutput();
			
			nodes[BACK].getWire(0).addInput(input);
			nodes[FRONT].getWire(0).addOutput(output);
			nodes[LEFT].getWire(0).addOutput(output);
			nodes[RIGHT].getWire(0).addOutput(output);
			
			return Collections.<ICompilableBlock>singleton(new ICompilableBlock() {
				@Override
				public IScannedOutput[] getOutputs() {
					return new IScannedOutput[] {output};
				}
				
				@Override
				public IScannedInput[] getInputs() {
					return new IScannedInput[] {input};
				}
				
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
					return inputs;
				}
			});
		}
	}
	
	public static class Logic extends GateLogic implements Stateless {
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			outputs[FRONT] = outputs[LEFT] = outputs[RIGHT] = inputs[BACK];
		}
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == BACK;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT || side == LEFT || side == RIGHT;
		}
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return
				(outputs[FRONT] != 0 ? 1 : 0)
				| (inputs[BACK] != 0 ? 2 : 0)
				| (inputs[LEFT] != 0 || outputs[LEFT] != 0 ? 4 : 0)
				| (inputs[RIGHT] != 0 || outputs[RIGHT] != 0 ? 8 : 0);
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"buffer-base", "buffer-left", "buffer-right", "buffer-back", "buffer-out"};
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0, 0};
			torchX = new float[] {7.5f, 7.5f};
			torchY = new float[] {2.5f, 9.5f};
			torchState = new boolean[] {false, false};
		}
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 4) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 8) != 0 ? ON : OFF;
			segmentCol[3] = (renderState & 2) != 0 ? ON : OFF;
			segmentCol[4] = (renderState & 2) == 0 ? ON : OFF;
			torchState[0] = (renderState & 1) != 0;
			torchState[1] = (renderState & 2) == 0;
		}
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
			segmentCol[2] = OFF;
			segmentCol[3] = OFF;
			segmentCol[4] = ON;
			torchState[0] = false;
			torchState[1] = true;
		}
	}
}
