package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.ArrayList;
import java.util.Collection;

import mods.immibis.redlogic.RotatedTessellator;
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
import mods.immibis.redlogic.gates.GateLogic.Stateless;
import mods.immibis.redlogic.gates.GateLogic.WithBundledConnections;
import mods.immibis.redlogic.gates.GateRendering;
import mods.immibis.redlogic.rendering.WireRenderer;
import mods.immibis.redlogic.wires.EnumWireType;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

public class GateBundledLatch {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			
			// Notice we use a different ICompilableBlock for each colour.
			
			// If we used one ICompilableBlock with a lot of inputs, the compiler wouldn't know
			// that we use the select input 16 times. So the compiler wouldn't cache it (it
			// thinks the wire's only used once), and we'd end up evaluating it 16 times,
			// instead of reading the cached value 16 times.
			
			// This applies to BundledLatch, BundledRelay and BundledMultiplexer.
			
			Collection<ICompilableBlock> blocks = new ArrayList<ICompilableBlock>(16);
			
			for(int wire = 0; wire < 16; wire++) {
				final IScannedInput in = process.createInput();
				final IScannedInput inControl = process.createInput();
				final IScannedOutput out = process.createOutput();
				
				nodes[FRONT].getWire(wire).addOutput(out);
				nodes[RIGHT].getWire(0).addInput(inControl);
				nodes[BACK].getWire(wire).addInput(in);
				
				blocks.add(new ICompilableBlock() {
					@Override
					public IScannedOutput[] getOutputs() {
						return new IScannedOutput[] {out};
					}
					
					@Override
					public IScannedInput[] getInputs() {
						return new IScannedInput[] {in, inControl};
					}
					
					@Override
					public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
						
						final String fieldName = ctx.createField("Z");
						
						Label noSetLabel = new Label();
						
						inputs[1].compile(ctx);
						ctx.getCodeVisitor().visitJumpInsn(Opcodes.IFEQ, noSetLabel);
						inputs[0].compile(ctx);
						ctx.getCodeVisitor().visitVarInsn(Opcodes.ALOAD, 0);
						ctx.getCodeVisitor().visitInsn(Opcodes.SWAP);
						ctx.getCodeVisitor().visitFieldInsn(Opcodes.PUTFIELD, ctx.getClassNameInternal(), fieldName, "Z");
						ctx.getCodeVisitor().visitLabel(noSetLabel);
						
						
						return new ICompilableExpression[] {new ICompilableExpression() {
							@Override
							public void compile(ICompileContext ctx) {
								ctx.getCodeVisitor().visitVarInsn(Opcodes.ALOAD, 0);
								ctx.getCodeVisitor().visitFieldInsn(Opcodes.GETFIELD, ctx.getClassNameInternal(), fieldName, "Z");
							}
							
							@Override
							public boolean alwaysInline() {
								return true;
							}
						}};
					}
				});
			}
			
			return blocks;
		}
	}
	
	public static class Logic extends GateLogic implements Stateless, Flippable, WithBundledConnections {
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[RIGHT] != 0)
				outputs[FRONT] = inputs[BACK];
			//System.out.println(inputs[BACK]+" "+outputs[FRONT]);
		}
		
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return (inputs[RIGHT] != 0 ? 1 : 0);
		}
		
		@Override
		public boolean connectsToDirection(int side, int gateSettings) {
			return side == RIGHT || side == FRONT || side == BACK;
		}
		
		@Override
		public boolean isBundledConnection(int side) {
			return side == FRONT || side == BACK;
		}
		
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == RIGHT || side == BACK;
		}
		
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT;
		}
		
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentCol = new int[] {0xFFFFFF, 0};
			segmentTex = new String[] {"blatch-base", "blatch-enable"};
		}
		
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 1) != 0 ? ON : OFF;
		}
		
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
		}
		
		@Override
		public void customRender(RotatedTessellator rt, RenderBlocks render) {
			ForgeDirection up = ForgeDirection.VALID_DIRECTIONS[rt.side ^ 1];
			
			// draw a bundled cable, offset away from the base of the gate
			double offset = 0; // was 0.125
			double dx = up.offsetX * offset, dy = up.offsetY * offset, dz = up.offsetZ * offset;
			rt.x += dx; rt.y += dy; rt.z += dz;
			WireRenderer.renderWireSide(rt, render, EnumWireType.BUNDLED, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, null, null, null, null, true, false);
			rt.x -= dx; rt.y -= dy; rt.z -= dz;
			
			renderRaisedSquare(rt, render, segmentIcons[0], 4, 4, 12, 12, 3 + (int)(offset * 16 + 0.5));
		}
	}
}
