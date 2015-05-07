package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;

import java.util.Random;

import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateRendering;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;

public class GateRandomizer {
	public static class Logic extends GateLogic {
		private int ticksLeft;
		private Random random = new Random();
		
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[BACK] != 0 && ticksLeft == 0) {
				ticksLeft = 20;
				outputs[FRONT] = random.nextBoolean() ? (short)255 : 0;
				outputs[LEFT] = random.nextBoolean() ? (short)255 : 0;
				outputs[RIGHT] = random.nextBoolean() ? (short)255 : 0;
			}
			
			if(inputs[BACK] == 0)
				ticksLeft = 0;
			
			if(ticksLeft > 0)
				ticksLeft--;
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
			return (inputs[BACK] != 0 ? 1 : 0) | (outputs[LEFT] != 0 ? 2 : 0) | (outputs[RIGHT] != 0 ? 4 : 0) | (outputs[FRONT] != 0 ? 8 : 0);
		}
		
		@Override
		public void write(NBTTagCompound tag) {
			super.write(tag);
			tag.setByte("ticksLeft", (byte)ticksLeft);
		}
		
		@Override
		public void read(NBTTagCompound tag) {
			super.read(tag);
			ticksLeft = tag.getByte("ticksLeft");
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			torchX = new float[] {8f, 3f, 13f};
			torchY = new float[] {3f, 8f, 8f};
			torchState = new boolean[] {false, false, false};
			segmentTex = new String[] {"randomizer-base", "randomizer-in"};
			segmentCol = new int[] {0xFFFFFF, 0};
		}
		
		@Override
		public void loadTextures(IIconRegister register) {
			super.loadTextures(register);
			
			torchTexOn = register.registerIcon(ICON_PREFIX+"randomizer-torch-on");
			torchTexOff = register.registerIcon(ICON_PREFIX+"randomizer-torch-off");
		}
		
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 1) != 0 ? ON : OFF;
			torchState[0] = (renderState & 8) != 0;
			torchState[1] = (renderState & 2) != 0;
			torchState[2] = (renderState & 4) != 0;
		}
		
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
			torchState[0] = false;
			torchState[1] = false;
			torchState[2] = false;
		}
	}
}
