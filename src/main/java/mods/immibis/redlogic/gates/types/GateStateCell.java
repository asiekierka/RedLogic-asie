package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateLogic.Flippable;
import mods.immibis.redlogic.gates.GateLogic.WithPointer;
import mods.immibis.redlogic.gates.GateLogic.WithRightClickAction;
import mods.immibis.redlogic.gates.GateRendering;
import mods.immibis.redlogic.gates.GateTile;
import mods.immibis.redlogic.gates.TimedGateLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class GateStateCell {
	public static class Logic extends GateLogic implements WithRightClickAction, WithPointer, TimedGateLogic, Flippable {
		
		private int intervalTicks = 20, ticksLeft, pulseTicks;
		private boolean timing, paused;

		@Override
		public int getPointerPosition() {
			if(!timing)
				return 0;
			return 44 - (int)(ticksLeft * 45f / intervalTicks);
		}

		@Override
		public float getPointerSpeed() {
			if(!timing || paused)
				return 0;
			return 45f / intervalTicks;
		}

		@Override
		public void onRightClick(EntityPlayer ply, GateTile tile) {
			ply.openGui(RedLogicMod.instance, RedLogicMod.GUI_TIMER, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
		}

		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			if(inputs[LEFT] != 0 && !timing) {
				timing = true;
				ticksLeft = intervalTicks;
			}
			paused = inputs[LEFT] != 0 || inputs[BACK] != 0;
			if(timing && !paused) {
				ticksLeft--;
				if(ticksLeft <= 0) {
					pulseTicks = 2;
					timing = false;
					tickSoundPending = true;
				}
			}
			outputs[FRONT] = timing ? (short)255 : 0;
			outputs[RIGHT] = pulseTicks > 0 ? (short)255 : 0;
			if(pulseTicks > 0)
				pulseTicks--;
		}
		
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side == BACK || side == LEFT;
		}
		
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT || side == RIGHT;
		}
		
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return (outputs[FRONT] != 0 || inputs[FRONT] != 0 ? 1 : 0)
					| (inputs[BACK] != 0 ? 2 : 0)
					| (inputs[LEFT] != 0 ? 4 : 0)
					| (pulseTicks > 0 ? 8 : 0)
					| (timing ? 16 : 0)
					| (paused ? 32 : 0);
		}

		@Override
		public int getInterval() {
			return intervalTicks;
		}

		@Override
		public void setInterval(int i) {
			intervalTicks = i;
			if(ticksLeft > i)
				ticksLeft = i;
		}
		
		@Override
		public void read(NBTTagCompound tag) {
			super.read(tag);
			
			intervalTicks = tag.getInteger("intv");
			ticksLeft = tag.getInteger("left");
			pulseTicks = tag.getByte("pulse");
			timing = tag.getBoolean("timing");
			paused = tag.getBoolean("paused");
		}
		
		@Override
		public void write(NBTTagCompound tag) {
			super.write(tag);
			
			tag.setInteger("intv", intervalTicks);
			tag.setInteger("left", ticksLeft);
			tag.setByte("pulse", (byte)pulseTicks);
			tag.setBoolean("timing", timing);
			tag.setBoolean("paused", paused);
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"statecell-base", "statecell-2", "statecell-3", "statecell-4", "statecell-5", "statecell-6", "statecell-7"};
			segmentCol = new int[] {0xFFFFFF, 0, 0, 0, 0, 0, 0};
			torchX = new float[] {13f};
			torchY = new float[] {7f};
			torchState = new boolean[] {false};
			pointerX = new float[] {8f};
			pointerY = new float[] {12f};
		}
		
		@Override
		public void set(int renderState) {
			segmentCol[1] = (renderState & 1) != 0 ? ON : OFF;
			segmentCol[2] = (renderState & 2) != 0 ? ON : OFF;
			segmentCol[3] = (renderState & 4) != 0 ? ON : OFF;
			segmentCol[4] = (renderState & 16) == 0 ? ON : OFF;
			segmentCol[5] = (renderState & 16) != 0 ? ON : OFF;
			segmentCol[6] = (renderState & 8) != 0 ? ON : OFF;
			torchState[0] = (renderState & 8) != 0;
		}
		
		@Override
		public void setItemRender() {
			segmentCol[1] = OFF;
			segmentCol[2] = OFF;
			segmentCol[3] = OFF;
			segmentCol[4] = ON;
			segmentCol[5] = OFF;
			segmentCol[6] = OFF;
			torchState[0] = false;
		}
	}
}
