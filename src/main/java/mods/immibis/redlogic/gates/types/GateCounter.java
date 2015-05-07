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
import mods.immibis.redlogic.gates.GateLogic.Flippable;
import mods.immibis.redlogic.gates.GateLogic.WithPointer;
import mods.immibis.redlogic.gates.GateLogic.WithRightClickAction;
import mods.immibis.redlogic.gates.GateRendering;
import mods.immibis.redlogic.gates.GateTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class GateCounter {

	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			final IScannedInput inDecr = process.createInput();
			final IScannedInput inIncr = process.createInput();
			final IScannedOutput outZero = process.createOutput();
			final IScannedOutput outMax = process.createOutput();
			
			nodes[FRONT].getWire(0).addInput(inDecr);
			nodes[BACK].getWire(0).addInput(inIncr);
			nodes[LEFT].getWire(0).addOutput(outZero);
			nodes[RIGHT].getWire(0).addOutput(outMax);
			
			final int max = logicTag.getInteger("max");
			final int incr = logicTag.getInteger("+");
			final int decr = logicTag.getInteger("-");
			
			return Arrays.<ICompilableBlock>asList(new ICompilableBlock() {
				@Override
				public IScannedInput[] getInputs() {
					return new IScannedInput[] {inIncr, inDecr};
				}
				@Override
				public IScannedOutput[] getOutputs() {
					return new IScannedOutput[] {outZero, outMax};
				}
				
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
					final String ctrField = ctx.createField("I");
					
					Label dontIncr = new Label(), dontDecr = new Label();
					
					// if(posedge(inIncr)) ctrField = Math.min(ctrField + incr, max);
					inputs[0].compile(ctx);
					ctx.detectRisingEdge();
					ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFEQ, dontIncr);
					ctx.loadField(ctrField, "I");
					ctx.pushInt(incr);
					ctx.getCodeVisitor().visitInsn(Opcodes.IADD);
					ctx.pushInt(max);
					ctx.getCodeVisitor().visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "min", "(II)I");
					ctx.storeField(ctrField, "I");
					ctx.getCodeVisitor().visitLabel(dontIncr);
					
					// if(posedge(inDecr)) ctrField = Math.max(ctrField - decr, 0);
					inputs[1].compile(ctx);
					ctx.detectRisingEdge();
					ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFEQ, dontDecr);
					ctx.loadField(ctrField, "I");
					ctx.pushInt(decr);
					ctx.getCodeVisitor().visitInsn(Opcodes.ISUB);
					ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_0);
					ctx.getCodeVisitor().visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Math", "max", "(II)I");
					ctx.storeField(ctrField, "I");
					ctx.getCodeVisitor().visitLabel(dontDecr);
					
					return new ICompilableExpression[] {
						new ICompilableExpression() {
							@Override
							public void compile(ICompileContext ctx) {
								Label notzero = new Label(), end = new Label();
								ctx.loadField(ctrField, "I");
								ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFNE, notzero);
								ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
								ctx.getCodeVisitor().visitJumpInsn(Opcodes.GOTO, end);
								ctx.getCodeVisitor().visitLabel(notzero);
								ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_0);
								ctx.getCodeVisitor().visitLabel(end);
							}
							
							@Override
							public boolean alwaysInline() {
								return false;
							}
						},
						new ICompilableExpression() {
							@Override
							public void compile(ICompileContext ctx) {
								Label notmax = new Label(), end = new Label();
								ctx.loadField(ctrField, "I");
								ctx.pushInt(max);
								ctx.getCodeVisitor().visitJumpInsn(Opcodes.IF_ICMPNE, notmax);
								ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
								ctx.getCodeVisitor().visitJumpInsn(Opcodes.GOTO, end);
								ctx.getCodeVisitor().visitLabel(notmax);
								ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_0);
								ctx.getCodeVisitor().visitLabel(end);
							}
							
							@Override
							public boolean alwaysInline() {
								return false;
							}
						}
					};
				}
			});
		}
	}

	public static class Logic extends GateLogic implements WithRightClickAction, WithPointer, Flippable {
		
		public int value = 0;
		public int max = 10;
		public int incr = 1;
		public int decr = 1;
		private boolean wasFront, wasBack;

		@Override
		public int getPointerPosition() {
			if(max == 0)
				return 0;
			return (value * 120) / max - 60;
		}

		@Override
		public float getPointerSpeed() {
			return 0;
		}

		@Override
		public void onRightClick(EntityPlayer ply, GateTile tile) {
			ply.openGui(RedLogicMod.instance, RedLogicMod.GUI_COUNTER, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
		}
		
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return (inputs[FRONT] != 0 ? 1 : 0)
					| (inputs[BACK] != 0 ? 2 : 0)
					| (outputs[LEFT] != 0 ? 4 : 0)
					| (outputs[RIGHT] != 0 ? 8 : 0)
					// render state is truncated to 16 bits, but this
					// causes the rendering to update when the pointer moves
					| (getPointerPosition() << 20);
		}
		
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == FRONT || side == BACK;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == LEFT || side == RIGHT;
		}

		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			
			if(inputs[FRONT] != 0 && !wasFront)
				value = Math.max(0, value - decr);
			
			if(inputs[BACK] != 0 && !wasBack)
				value = Math.min(max, value + incr);
			
			outputs[LEFT] = value == 0 ? (short)255 : 0;
			outputs[RIGHT] = value == max ? (short)255 : 0;
			
			wasFront = inputs[FRONT] != 0;
			wasBack = inputs[BACK] != 0;
		}
		
		@Override
		public void read(NBTTagCompound tag) {
			super.read(tag);
			
			value = tag.getInteger("cur");
			max = tag.getInteger("max");
			incr = tag.getInteger("+");
			decr = tag.getInteger("-");
			wasFront = tag.getBoolean("front");
			wasBack = tag.getBoolean("back");
		}
		
		@Override
		public void write(NBTTagCompound tag) {
			super.write(tag);
			
			tag.setInteger("cur", value);
			tag.setInteger("max", max);
			tag.setInteger("+", incr);
			tag.setInteger("-", decr);
			tag.setBoolean("front", wasFront);
			tag.setBoolean("back", wasBack);
		}
		
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"counter-base", "counter-front", "counter-back"};
			segmentCol = new int[] {0xFFFFFF, 0, 0};
			torchX = new float[] {3f, 13f};
			torchY = new float[] {8f, 8f};
			torchState = new boolean[] {false, false};
			pointerX = new float[] {8f};
			pointerY = new float[] {11f};
		}
		
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 1) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 2) != 0 ? ON : OFF;
			torchState[0] = (renderState & 4) != 0;
			torchState[1] = (renderState & 8) != 0;
		}
		
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
			segmentCol[2] = OFF;
			torchState[0] = true;
			torchState[1] = false;
		}
	}
}
