package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.RotatedTessellator;
import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.compiler.ICompileContext;
import mods.immibis.redlogic.api.chips.compiler.util.ZeroExpr;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.gates.GateBlock;
import mods.immibis.redlogic.gates.GateCompiler;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateLogic.Flippable;
import mods.immibis.redlogic.gates.GateLogic.WithRightClickAction;
import mods.immibis.redlogic.gates.GateRendering;
import mods.immibis.redlogic.gates.GateTile;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class GateToggleLatch {
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			
			final IScannedInput in1 = process.createInput();
			final IScannedInput in2 = process.createInput();
			final IScannedOutput out0 = process.createOutput();
			final IScannedOutput out1 = process.createOutput();
			
			nodes[LEFT].getWire(0).addInput(in1);
			nodes[RIGHT].getWire(0).addInput(in2);
			nodes[FRONT].getWire(0).addOutput(out0);
			nodes[BACK].getWire(0).addOutput(out1);
			
			return Collections.<ICompilableBlock>singleton(new ICompilableBlock() {
				@Override
				public IScannedOutput[] getOutputs() {
					return new IScannedOutput[] {out0, out1};
				}
				
				@Override
				public IScannedInput[] getInputs() {
					return new IScannedInput[] {in1, in2};
				}
				
				@Override
				public ICompilableExpression[] compile(ICompileContext ctx, ICompilableExpression[] inputs) {
					final String stateField = ctx.createField("Z");
					
					MethodVisitor mv = ctx.getCodeVisitor();
					
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitFieldInsn(Opcodes.GETFIELD, ctx.getClassNameInternal(), stateField, "Z");
					
					for(ICompilableExpression input : inputs) {
						if(input instanceof ZeroExpr)
							continue;
					
						String oldValueField = ctx.createField("Z");
						
						// GET I1		- STATE I1
						// DUP			- STATE I1 I1
						// GET OLD I1	- STATE I1 I1 OLDI1
						// SWAP			- STATE I1 OLDI1 I1
						// PUT OLD I1	- STATE I1 OLDI1
						// NOT			- STATE I1 NOTOLDI1
						// AND			- STATE POSEDGE
						// XOR			- NEWSTATE
						
						input.compile(ctx);
						mv.visitInsn(Opcodes.DUP);
						mv.visitVarInsn(Opcodes.ALOAD, 0);
						mv.visitFieldInsn(Opcodes.GETFIELD, ctx.getClassNameInternal(), oldValueField, "Z");
						mv.visitInsn(Opcodes.SWAP);
						mv.visitVarInsn(Opcodes.ALOAD, 0);
						mv.visitInsn(Opcodes.SWAP);
						mv.visitFieldInsn(Opcodes.PUTFIELD, ctx.getClassNameInternal(), oldValueField, "Z");
						mv.visitInsn(Opcodes.ICONST_1);
						mv.visitInsn(Opcodes.IXOR);
						mv.visitInsn(Opcodes.IAND);
						mv.visitInsn(Opcodes.IXOR);
					}
					
					// set new state
					mv.visitVarInsn(Opcodes.ALOAD, 0);
					mv.visitInsn(Opcodes.SWAP);
					mv.visitFieldInsn(Opcodes.PUTFIELD, ctx.getClassNameInternal(), stateField, "Z");
					
					return new ICompilableExpression[] {
						new ICompilableExpression() {
							@Override
							public void compile(ICompileContext ctx) {
								// return !state
								ctx.getCodeVisitor().visitVarInsn(Opcodes.ALOAD, 0);
								ctx.getCodeVisitor().visitFieldInsn(Opcodes.GETFIELD, ctx.getClassNameInternal(), stateField, "Z");
								ctx.getCodeVisitor().visitInsn(Opcodes.ICONST_1);
								ctx.getCodeVisitor().visitInsn(Opcodes.IXOR);
							}
							
							@Override
							public boolean alwaysInline() {
								return false;
							}
						},
						
						new ICompilableExpression() {
							@Override
							public void compile(ICompileContext ctx) {
								// return state
								ctx.getCodeVisitor().visitVarInsn(Opcodes.ALOAD, 0);
								ctx.getCodeVisitor().visitFieldInsn(Opcodes.GETFIELD, ctx.getClassNameInternal(), stateField, "Z");
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
	
	public static class Logic extends GateLogic implements Flippable, WithRightClickAction {
		private boolean wasLeft, wasRight;
		private boolean state;
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[LEFT] != 0 && !wasLeft) {
				state = !state;
				tickSoundPending = true;
			}
			if(inputs[RIGHT] != 0 && !wasRight) {
				state = !state;
				tickSoundPending = true;
			}
			wasLeft = inputs[LEFT] != 0;
			wasRight = inputs[RIGHT] != 0;
			outputs[FRONT] = !state ? (short)255 : 0;
			outputs[BACK] = state ? (short)255 : 0;
		}
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == LEFT || side == RIGHT;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT || side == BACK;
		}
		@Override
		public void write(NBTTagCompound tag) {
			tag.setBoolean("wasLeft", wasLeft);
			tag.setBoolean("wasRight", wasRight);
			tag.setBoolean("state", state);
		}
		@Override
		public void read(NBTTagCompound tag) {
			wasLeft = tag.getBoolean("wasLeft");
			wasRight = tag.getBoolean("wasRight");
			state = tag.getBoolean("state");
		}
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return (inputs[LEFT] != 0 ? 1 : 0)
					| (inputs[RIGHT] != 0 ? 2 : 0)
					| (outputs[FRONT] != 0 ? 4 : 0)
					| (outputs[BACK] != 0 ? 8 : 0);
		}
		@Override
		public void onRightClick(EntityPlayer ply, GateTile tile) {
			state = !state;
			tickSoundPending = true;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"toggle-base", "toggle-left", "toggle-right"};
			segmentCol = new int[] {0xFFFFFF, 0, 0};
			torchX = new float[] {4.5f, 4.5f};
			torchY = new float[] {3.5f, 12.5f};
			torchState = new boolean[] {true, false};
		}
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
			segmentCol[2] = OFF;
			torchState[0] = true;
			torchState[1] = false;
		}
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 1) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 2) != 0 ? ON : OFF;
			torchState[0] = (renderState & 4) != 0;
			torchState[1] = (renderState & 8) != 0;
		}
		
		@Override
		public void customRender(RotatedTessellator rt, RenderBlocks render) {
	        boolean leverDirection = !torchState[1];
	        
	        // size: 6x8 pixels
	        double minX = 7/16., minZ = 4/16., maxX = 13/16., maxZ = 12/16., minY = GateBlock.THICKNESS, maxY = 4/16.;
	        
	        rt.base.setColorOpaque(255, 255, 255);
        	
	        {
	        	// draw lever base
	        	
	        	/*double minX = 8/16.;
		        double maxX = 11/16.;
		        double minZ = 5/16.;
		        double maxZ = 11/16.;
		        double minY = GateBlock.THICKNESS;
		        double maxY = minY + 2/16.;*/
	        	
	        		
		        
		        
		        IIcon tex = render.hasOverrideBlockTexture() ? render.overrideBlockTexture : Blocks.cobblestone.getIcon(0, 0);
		        
		        rt.addVertexWithUV(minX, minY, minZ, tex.getInterpolatedU(minX*16), tex.getInterpolatedV(minY*16));
		        rt.addVertexWithUV(minX, maxY, minZ, tex.getInterpolatedU(minX*16), tex.getInterpolatedV(maxY*16));
		        rt.addVertexWithUV(maxX, maxY, minZ, tex.getInterpolatedU(maxX*16), tex.getInterpolatedV(maxY*16));
		        rt.addVertexWithUV(maxX, minY, minZ, tex.getInterpolatedU(maxX*16), tex.getInterpolatedV(minY*16));

		        rt.addVertexWithUV(maxX, minY, maxZ, tex.getInterpolatedU(maxX*16), tex.getInterpolatedV(minY*16));
		        rt.addVertexWithUV(maxX, maxY, maxZ, tex.getInterpolatedU(maxX*16), tex.getInterpolatedV(maxY*16));
		        rt.addVertexWithUV(minX, maxY, maxZ, tex.getInterpolatedU(minX*16), tex.getInterpolatedV(maxY*16));
		        rt.addVertexWithUV(minX, minY, maxZ, tex.getInterpolatedU(minX*16), tex.getInterpolatedV(minY*16));
		        
		        rt.addVertexWithUV(maxX, minY, minZ, tex.getInterpolatedU(minZ*16), tex.getInterpolatedV(minY*16));
		        rt.addVertexWithUV(maxX, maxY, minZ, tex.getInterpolatedU(minZ*16), tex.getInterpolatedV(maxY*16));
		        rt.addVertexWithUV(maxX, maxY, maxZ, tex.getInterpolatedU(maxZ*16), tex.getInterpolatedV(maxY*16));
		        rt.addVertexWithUV(maxX, minY, maxZ, tex.getInterpolatedU(maxZ*16), tex.getInterpolatedV(minY*16));

		        rt.addVertexWithUV(minX, minY, maxZ, tex.getInterpolatedU(maxZ*16), tex.getInterpolatedV(minY*16));
		        rt.addVertexWithUV(minX, maxY, maxZ, tex.getInterpolatedU(maxZ*16), tex.getInterpolatedV(maxY*16));
		        rt.addVertexWithUV(minX, maxY, minZ, tex.getInterpolatedU(minZ*16), tex.getInterpolatedV(maxY*16));
		        rt.addVertexWithUV(minX, minY, minZ, tex.getInterpolatedU(minZ*16), tex.getInterpolatedV(minY*16));
		        
		        rt.addVertexWithUV(minX, maxY, minZ, tex.getInterpolatedU(minX*16), tex.getInterpolatedV(minZ*16));
		        rt.addVertexWithUV(minX, maxY, maxZ, tex.getInterpolatedU(minX*16), tex.getInterpolatedV(maxZ*16));
		        rt.addVertexWithUV(maxX, maxY, maxZ, tex.getInterpolatedU(maxX*16), tex.getInterpolatedV(maxZ*16));
		        rt.addVertexWithUV(maxX, maxY, minZ, tex.getInterpolatedU(maxX*16), tex.getInterpolatedV(minZ*16));
	        }
	        
	        // render lever handle
	        {
	        	IIcon tex = Blocks.lever.getIcon(0, 0);
	        	
	        	double d0 = (double)tex.getMinU();
		        double d1 = (double)tex.getMinV();
		        double d2 = (double)tex.getMaxU();
		        double d3 = (double)tex.getMaxV();
		        Vec3[] avec3 = new Vec3[8];
		        float f4 = 0.0625F;
		        float f5 = 0.0625F;
		        float f6 = 0.625F;
		        avec3[0] = Vec3.createVectorHelper((double)(-f4), 0.0D, (double)(-f5));
		        avec3[1] = Vec3.createVectorHelper((double)f4, 0.0D, (double)(-f5));
		        avec3[2] = Vec3.createVectorHelper((double)f4, 0.0D, (double)f5);
		        avec3[3] = Vec3.createVectorHelper((double)(-f4), 0.0D, (double)f5);
		        avec3[4] = Vec3.createVectorHelper((double)(-f4), (double)f6, (double)(-f5));
		        avec3[5] = Vec3.createVectorHelper((double)f4, (double)f6, (double)(-f5));
		        avec3[6] = Vec3.createVectorHelper((double)f4, (double)f6, (double)f5);
		        avec3[7] = Vec3.createVectorHelper((double)(-f4), (double)f6, (double)f5);
		        
		        int i1 = 5; // or 6

		        for (int j1 = 0; j1 < 8; ++j1)
		        {
		            if (leverDirection)
		            {
		                //avec3[j1].zCoord -= 0.0625D;
		                avec3[j1].rotateAroundX(((float)Math.PI * 2F / 9F));
		            }
		            else
		            {
		                //avec3[j1].zCoord += 0.0625D;
		                avec3[j1].rotateAroundX(-((float)Math.PI * 2F / 9F));
		            }

		            if (i1 == 6)
		            {
		                avec3[j1].rotateAroundY(((float)Math.PI / 2F));
		            }
		            
		            else if (i1 != 0 && i1 != 7)
		            {
		                /*avec3[j1].xCoord += (double)par2 + 0.5D;
		                avec3[j1].yCoord += (double)((float)par3 + 0.125F);
		                avec3[j1].zCoord += (double)par4 + 0.5D;*/
		            	avec3[j1].xCoord += (minX + maxX) / 2;
		            	avec3[j1].yCoord += 0.125f;
		            	avec3[j1].zCoord += (minZ + maxZ) / 2;
		            }
		        }

		        Vec3 vec3 = null;
		        Vec3 vec31 = null;
		        Vec3 vec32 = null;
		        Vec3 vec33 = null;

		        for (int k1 = 0; k1 < 6; ++k1)
		        {
		            if (k1 == 0)
		            {
		                d0 = (double)tex.getInterpolatedU(7.0D);
		                d1 = (double)tex.getInterpolatedV(6.0D);
		                d2 = (double)tex.getInterpolatedU(9.0D);
		                d3 = (double)tex.getInterpolatedV(8.0D);
		            }
		            else if (k1 == 2)
		            {
		                d0 = (double)tex.getInterpolatedU(7.0D);
		                d1 = (double)tex.getInterpolatedV(6.0D);
		                d2 = (double)tex.getInterpolatedU(9.0D);
		                d3 = (double)tex.getMaxV();
		            }

		            if (k1 == 0)
		            {
		                vec3 = avec3[0];
		                vec31 = avec3[1];
		                vec32 = avec3[2];
		                vec33 = avec3[3];
		            }
		            else if (k1 == 1)
		            {
		                vec3 = avec3[7];
		                vec31 = avec3[6];
		                vec32 = avec3[5];
		                vec33 = avec3[4];
		            }
		            else if (k1 == 2)
		            {
		                vec3 = avec3[1];
		                vec31 = avec3[0];
		                vec32 = avec3[4];
		                vec33 = avec3[5];
		            }
		            else if (k1 == 3)
		            {
		                vec3 = avec3[2];
		                vec31 = avec3[1];
		                vec32 = avec3[5];
		                vec33 = avec3[6];
		            }
		            else if (k1 == 4)
		            {
		                vec3 = avec3[3];
		                vec31 = avec3[2];
		                vec32 = avec3[6];
		                vec33 = avec3[7];
		            }
		            else if (k1 == 5)
		            {
		                vec3 = avec3[0];
		                vec31 = avec3[3];
		                vec32 = avec3[7];
		                vec33 = avec3[4];
		            }

		            rt.addVertexWithUV(vec3.xCoord, vec3.yCoord, vec3.zCoord, d0, d3);
		            rt.addVertexWithUV(vec31.xCoord, vec31.yCoord, vec31.zCoord, d2, d3);
		            rt.addVertexWithUV(vec32.xCoord, vec32.yCoord, vec32.zCoord, d2, d1);
		            rt.addVertexWithUV(vec33.xCoord, vec33.yCoord, vec33.zCoord, d0, d1);
		        }
	        }
		}
	}
}
