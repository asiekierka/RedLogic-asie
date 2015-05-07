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
import mods.immibis.redlogic.gates.GateLogic.WithBundledConnections;
import net.minecraft.nbt.NBTTagCompound;

public class GateBundledXOR {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			
			final int IPB = 16;
			
			final int numInputBundles = 3 - Integer.bitCount(gateSettings & 7);
			
			final IScannedInput[] inputs = new IScannedInput[numInputBundles * IPB];
			final IScannedOutput[] output = new IScannedOutput[IPB];
			
			int inPos = 0;
			for(int i = 0; i < IPB; i++) {
				if((gateSettings & 1) == 0)
					nodes[LEFT].getWire(i).addInput(inputs[inPos++] = process.createInput());
				
				if((gateSettings & 2) == 0)
					nodes[BACK].getWire(i).addInput(inputs[inPos++] = process.createInput());
				
				if((gateSettings & 4) == 0)
					nodes[RIGHT].getWire(i).addInput(inputs[inPos++] = process.createInput());
				
				nodes[FRONT].getWire(i).addOutput(output[i] = process.createOutput());
			}
			
			return Collections.<ICompilableBlock>singleton(new ICompilableBlock() {
				@Override
				public IScannedOutput[] getOutputs() {
					return output;
				}
				
				@Override
				public IScannedInput[] getInputs() {
					return inputs;
				}
				
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, final ICompilableExpression[] inputs) {
					ICompilableExpression[] rv = new ICompilableExpression[16];
					for(int out = 0, in = 0; out < IPB; out++, in += numInputBundles) {
						if(numInputBundles == 0)
							rv[out] = MergeExpr.createXOR();
						else if(numInputBundles == 1)
							rv[out] = MergeExpr.createXOR(inputs[in]);
						else if(numInputBundles == 2)
							rv[out] = MergeExpr.createXOR(inputs[in], inputs[in+1]);
						else if(numInputBundles == 3)
							rv[out] = MergeExpr.createXOR(inputs[in], inputs[in+1], inputs[in+2]);
						else
							throw new AssertionError("numInputBundles was "+numInputBundles);
					}
					return rv;
				}
			});
		}
	}
	
	public static class Logic extends GateLogic implements Stateless, WithBundledConnections {
		// gateSettings 1: left input ignored
		// gateSettings 2: back input ignored
		// gateSettings 4: right input ignored
		
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			int left = (gateSettings & 1) != 0 ? 0 : inputs[LEFT];
			int back = (gateSettings & 2) != 0 ? 0 : inputs[BACK];
			int right = (gateSettings & 4) != 0 ? 0 : inputs[RIGHT];
			outputs[FRONT] = (short)(left ^ back ^ right);
		}
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			boolean connFront = true;
			boolean connBack = (gateSettings & 2) == 0;
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
			else if(side == BACK)
				return (gateSettings & 2) == 0;
			else if(side == RIGHT)
				return (gateSettings & 4) == 0;
			else
				return true;
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
	}
	
	public static class Rendering extends BaseRenderBundledSimpleLogic {
		public Rendering() {super("bxor");}
	}
}
