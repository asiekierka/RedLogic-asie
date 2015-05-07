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
import mods.immibis.redlogic.gates.GateLogic.Stateless;
import mods.immibis.redlogic.gates.GateLogic.WithBundledConnections;
import net.minecraft.nbt.NBTTagCompound;

public class GateBundledNOT {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			final IScannedInput[] inputs = new IScannedInput[16];
			final IScannedOutput[] outputs = new IScannedOutput[16];
			
			for(int k = 0; k < 16; k++) {
				inputs[k] = process.createInput();
				outputs[k] = process.createOutput();
				
				nodes[BACK].getWire(k).addInput(inputs[k]);
				
				if((gateSettings & 1) == 0) nodes[LEFT ].getWire(k).addOutput(outputs[k]);
				if((gateSettings & 2) == 0) nodes[FRONT].getWire(k).addOutput(outputs[k]);
				if((gateSettings & 4) == 0) nodes[RIGHT].getWire(k).addOutput(outputs[k]);
			}
			
			return Collections.<ICompilableBlock>singleton(new ICompilableBlock() {
				@Override
				public IScannedOutput[] getOutputs() {
					return outputs;
				}
				
				@Override
				public IScannedInput[] getInputs() {
					return inputs;
				}
				
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
					ICompilableExpression[] rv = new ICompilableExpression[16];
					for(int k = 0; k < 16; k++)
						rv[k] = NotExpr.createNOT(inputs[k]);
					return rv;
				}
			});
		}
	}
	
	public static class Logic extends GateLogic implements Stateless, WithBundledConnections {
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			outputs[FRONT] = outputs[LEFT] = outputs[RIGHT] = (short)(~inputs[BACK]);
			if((gateSettings & 1) != 0)
				outputs[LEFT] = 0;
			if((gateSettings & 2) != 0)
				outputs[FRONT] = 0;
			if((gateSettings & 4) != 0)
				outputs[RIGHT] = 0;
		}
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			boolean connFront = (gateSettings & 2) == 0;
			boolean connBack = true;
			boolean connLeft = (gateSettings & 1) == 0;
			boolean connRight = (gateSettings & 4) == 0;
			
			return (connFront ? 1 : 0) | (connBack ? 2 : 0) | (connLeft ? 4 : 0) | (connRight ? 8 : 0);
		}
		
		@Override
		public int configure(int gateSettings) {
			return (gateSettings + 1) & 7;
		}
		
		@Override
		public boolean isBundledConnection(int side) {
			return true;
		}
		@Override
		public boolean connectsToDirection(int side, int gateSettings) {
			if(side == LEFT)
				return (gateSettings & 1) == 0;
			else if(side == FRONT)
				return (gateSettings & 2) == 0;
			else if(side == RIGHT)
				return (gateSettings & 4) == 0;
			else
				return true;
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
	}
	
	public static class Rendering extends BaseRenderBundledSimpleLogic {
		public Rendering() {super("bnot");}
	}
}
