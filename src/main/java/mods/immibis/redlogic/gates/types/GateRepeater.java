package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.nbt.NBTTagCompound;

public class GateRepeater {
	public static class Logic extends GateLogic {
		private static int[] DELAYS = {
			1, 2, 4, 8, 16, 32, 64, 128 // in redstone ticks
		};
		
		/*
		 * When the input turns on, the output turns on after N ticks. (state=false, timer=N)
		 * When the input turns off, the output turns off after N ticks. (state=true, timer=N)
		 * If the repeater is turning on and the input turns off, nothing happens.
		 * If the repeater is turning off and the input turns on, the timer resets.
		 */
		
		private boolean state;
		private int timer;
		
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[BACK] != 0 && state) {
				timer = 0;
				return;
			}
			
			if((inputs[BACK] != 0) != state && timer == 0) {
				timer = DELAYS[gateSettings] * 2 - 2; // 2 game ticks per redstone tick
				
				if(timer == 0) {
					state = !state;
					outputs[FRONT] = state ? (short)255 : 0;
				}
			}
			
			if(timer > 0) {
				timer--;
				if(timer == 0) {
					state = !state;
					outputs[FRONT] = state ? (short)255 : 0;
				}
			}
		}
		
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == BACK;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT;
		}
		
		@Override
		public void read(NBTTagCompound tag) {
			super.read(tag);
			timer = tag.getInteger("timer");
			state = tag.getBoolean("state");
		}
		
		@Override
		public void write(NBTTagCompound tag) {
			super.write(tag);
			tag.setInteger("timer", timer);
			tag.setBoolean("state", state);
		}
		
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return gateSettings |
				(outputs[FRONT] != 0 ? 32768 : 0);
		}
		
		@Override
		public int configure(int gateSettings) {
			return (gateSettings + 1) % DELAYS.length;
		}
		
		@Override
		public boolean connectsToDirection(int side, int gateSettings) {
			return side == FRONT || side == BACK;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"repeater-base", "repeater-strip"};
			segmentCol = new int[] {0xFFFFFF, 0};
			torchX = new float[] {8f, 8f};
			torchY = new float[] {3f, 6f};
			torchState = new boolean[] {false, false};
		}
		
		@Override
		public void set(int renderState) {
			boolean out = (renderState & 32768) != 0;
			
			torchY[1] = (renderState & 7) + 6;
			
			torchState[0] = torchState[1] = out;
			
			segmentCol[1] = out ? ON : OFF;
		}
		
		@Override
		public void setItemRender() {
			torchY[1] = 6f;
			
			torchState[0] = torchState[1] = false;
			
			segmentCol[1] = OFF;
		}
	}
}
