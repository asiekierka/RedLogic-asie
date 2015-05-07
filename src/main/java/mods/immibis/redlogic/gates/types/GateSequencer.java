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

public class GateSequencer {
	public static class Logic extends GateLogic implements WithRightClickAction, WithPointer, TimedGateLogic, Flippable {

		public int intervalTicks = RedLogicMod.defaultTimerTicks;
		public int ticksLeft;
		public int state;

		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			ticksLeft--;
			if(ticksLeft <= 0) {
				ticksLeft = intervalTicks;
				state = (state + 1) & 3;
				tickSoundPending = true;
			}
			
			outputs[FRONT] = state == 0 ? (short)255 : 0;
			outputs[RIGHT] = state == 1 ? (short)255 : 0;
			outputs[BACK] = state == 2 ? (short)255 : 0;
			outputs[LEFT] = state == 3 ? (short)255 : 0;
		}
		
		@Override
		public void onRightClick(EntityPlayer ply, GateTile tile) {
			ply.openGui(RedLogicMod.instance, RedLogicMod.GUI_TIMER, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
		}

		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return state;
		}

		@Override
		public int getPointerPosition() {
			return 89 - (int)(ticksLeft * 90f / intervalTicks) + state * 90 - 45;
		}

		@Override
		public float getPointerSpeed() {
			return 90f / intervalTicks;
		}
		
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return false;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return true;
		}
		
		@Override
		public void read(NBTTagCompound tag) {
			super.read(tag);
			
			intervalTicks = tag.getInteger("intv");
			ticksLeft = tag.getInteger("left");
			state = tag.getByte("state");
			
			if(intervalTicks < RedLogicMod.minTimerTicks)
				intervalTicks = RedLogicMod.minTimerTicks;
		}
		
		@Override
		public void write(NBTTagCompound tag) {
			super.write(tag);
			
			tag.setInteger("intv", intervalTicks);
			tag.setInteger("left", ticksLeft);
			tag.setByte("state", (byte)state);
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
			segmentTex = new String[] {"sequencer-base"};
			segmentCol = new int[] {0xFFFFFF};
			torchX = new float[] {8f, 2f, 14f, 8f};
			torchY = new float[] {2f, 8f, 8f, 14f};
			torchState = new boolean[] {false, false, false, false};
			pointerX = new float[] {8f};
			pointerY = new float[] {8f};
		}
		
		@Override
		public void set(int renderState) {
			torchState[0] = renderState == 0;
			torchState[1] = renderState == 3;
			torchState[2] = renderState == 1;
			torchState[3] = renderState == 2;
		}
		
		@Override
		public void setItemRender() {
			torchState[0] = true;
			torchState[1] = false;
			torchState[2] = false;
			torchState[3] = false;
		}
	}
}
