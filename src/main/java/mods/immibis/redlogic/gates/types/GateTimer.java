package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.Arrays;
import java.util.Collection;

import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateLogic.WithPointer;
import mods.immibis.redlogic.gates.GateLogic.WithRightClickAction;
import mods.immibis.redlogic.gates.GateRendering;
import mods.immibis.redlogic.gates.GateTile;
import mods.immibis.redlogic.gates.TimedGateLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class GateTimer {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			IScannedInput input = process.createInput();
			IScannedOutput output = process.createOutput();
			
			nodes[BACK].getWire(0).addInput(input);
			nodes[FRONT].getWire(0).addOutput(output);
			nodes[LEFT].getWire(0).addOutput(output);
			nodes[RIGHT].getWire(0).addOutput(output);
			
			return Arrays.<ICompilableBlock>asList(new TimerBlock(input, output, logicTag.getInteger("intv"))); 
		}
		
		public static class TimerBlock implements ICompilableBlock {
			private int interval;
			private IScannedInput input;
			private IScannedOutput output;
			
			public TimerBlock(IScannedInput input, IScannedOutput output, int interval) {
				this.input = input;
				this.output = output;
				this.interval = interval;
			}
			
			@Override
			public IScannedInput[] getInputs() {
				return new IScannedInput[] {input};
			}
			
			@Override
			public IScannedOutput[] getOutputs() {
				return new IScannedOutput[] {output};
			}
			
			@Override
			public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
				
				final String timeField = ctx.createField("I");
				
				// timeField = inputs[0] ? 2 : (timeField+1) % interval;
				
				Label reset = new Label(), end = new Label();
				inputs[0].compile(ctx);
				ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFNE, reset);
				ctx.loadField(timeField, "I");
				ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
				ctx.getCodeVisitor().visitInsn(Opcodes.IADD);
				ctx.getCodeVisitor().visitIntInsn(interval < 128 ? Opcodes.BIPUSH : Opcodes.SIPUSH, interval);
				ctx.getCodeVisitor().visitInsn(Opcodes.IREM);
				ctx.getCodeVisitor().visitJumpInsn(Opcodes.GOTO, end);
				ctx.getCodeVisitor().visitLabel(reset);
				ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_2);
				ctx.getCodeVisitor().visitLabel(end);
				ctx.storeField(timeField, "I");
				
				return new ICompilableExpression[] {
					new ICompilableExpression() {
						@Override
						public void compile(ICompileContext ctx) {
							Label less = new Label(), end = new Label();
							ctx.loadField(timeField, "I");
							ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_2);
							ctx.getCodeVisitor().visitJumpInsn(Opcodes.IF_ICMPLT, less);
							ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_0);
							ctx.getCodeVisitor().visitJumpInsn(Opcodes.GOTO, end);
							ctx.getCodeVisitor().visitLabel(less);
							ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
							ctx.getCodeVisitor().visitLabel(end);
						}
						
						@Override
						public boolean alwaysInline() {
							return false;
						}
					}
				};
			}
		}
	}
	public static class Logic extends GateLogic implements WithRightClickAction, WithPointer, TimedGateLogic {

		public int intervalTicks = RedLogicMod.defaultTimerTicks;
		public int ticksLeft;
		public boolean state, stopped;

		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			stopped = inputs[BACK] != 0;
			if(inputs[BACK] != 0) {
				state = true;
				outputs[FRONT] = outputs[LEFT] = outputs[RIGHT] = 0;
				ticksLeft = 0;
				return;
			}
			
			ticksLeft--;
			if(ticksLeft <= 0) {
				state = !state;
				outputs[FRONT] = outputs[LEFT] = outputs[RIGHT] = state ? (short)255 : 0;
				ticksLeft = state ? 2 : intervalTicks - 2;
				
				if(state)
					tickSoundPending = true;
			}
		}
		
		@Override
		public void onRightClick(EntityPlayer ply, GateTile tile) {
			ply.openGui(RedLogicMod.instance, RedLogicMod.GUI_TIMER, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
		}

		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return (outputs[FRONT] != 0 ? 1 : 0)
					| (outputs[LEFT] != 0 || inputs[LEFT] != 0 ? 2 : 0)
					| (outputs[RIGHT] != 0 || inputs[RIGHT] != 0 ? 8 : 0)
					| (inputs[BACK] != 0 ? 4 : 0)
					| (stopped ? 16 : 0);
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
		public int getPointerPosition() {
			return state || stopped ? 0 : 359 - (int)((ticksLeft + 1) * 360f / (intervalTicks - 2));
		}

		@Override
		public float getPointerSpeed() {
			return state || stopped ? 0 : 360f / (intervalTicks - 2);
		}
		
		@Override
		public void read(NBTTagCompound tag) {
			super.read(tag);
			
			intervalTicks = tag.getInteger("intv");
			ticksLeft = tag.getInteger("left");
			state = tag.getBoolean("state");
			stopped = tag.getBoolean("stopped");
			
			if(intervalTicks < RedLogicMod.minTimerTicks)
				intervalTicks = RedLogicMod.minTimerTicks;
		}
		
		@Override
		public void write(NBTTagCompound tag) {
			super.write(tag);
			
			tag.setInteger("intv", intervalTicks);
			tag.setInteger("left", ticksLeft);
			tag.setBoolean("state", state);
			tag.setBoolean("stopped", stopped);
		}

		@Override
		public int getInterval() {
			return intervalTicks;
		}

		@Override
		public void setInterval(int i) {
			intervalTicks = i;
			if(i > 0)
				ticksLeft %= intervalTicks;
			else
				ticksLeft = 0;
		}
		
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"timer-base", "timer-left", "timer-back", "timer-right"};
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0};
			torchX = new float[] {8f};
			torchY = new float[] {2f};
			torchState = new boolean[] {false};
			pointerX = new float[] {8f};
			pointerY = new float[] {8f};
		}
		
		@Override
		public void set(int renderState) {
			torchState[0] = (renderState & 1) != 0;
			segmentCol[1] = (renderState & 2) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 4) != 0 ? ON : OFF;
			segmentCol[3] = (renderState & 8) != 0 ? ON : OFF;
		}
		
		@Override
		public void setItemRender() {
			torchState[0] = false;
			segmentCol[1] = OFF;
			segmentCol[2] = OFF;
			segmentCol[3] = OFF;
		}
	}
}
