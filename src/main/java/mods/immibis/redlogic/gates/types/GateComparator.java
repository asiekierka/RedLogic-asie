package mods.immibis.redlogic.gates.types;

import static mods.immibis.redlogic.Utils.*;
import mods.immibis.redlogic.gates.GateLogic;
import mods.immibis.redlogic.gates.GateRendering;

public class GateComparator {
	public static class Logic extends GateLogic {
		@Override
		public boolean getInputID(int side, int gateSettings) {
			return side != FRONT;
		}
		@Override
		public boolean getOutputID(int side, int gateSettings) {
			return side == FRONT;
		}
		
		@Override
		public void update(short[] inputs, short[] outputs, int gateSettings) {
			short a = inputs[BACK];
			short b = (short)Math.max(inputs[LEFT], inputs[RIGHT]);
			if((gateSettings & 1) != 0) {
				// subtract mode
				outputs[FRONT] = (short)(a > b ? a - b : 0);
			} else {
				// compare mode
				outputs[FRONT] = (short)(a > b ? a : 0);
			}
		}
		
		@Override
		public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {
			return (outputs[FRONT] > 0 ? 2 : 0) | (gateSettings & 1);
		}
		
		@Override
		public int configure(int gateSettings) {
			return gateSettings ^ 1;
		}
	}
	
	public static class Rendering extends GateRendering {
		{
			segmentTex = new String[] {"comp-base", "comp-on"};
			segmentCol = new int[] {0xFFFFFF, 0x000000};
			torchX = new float[] {4, 8, 12};
			torchY = new float[] {12, 4, 12};
			torchState = new boolean[] {false, false, false};
		}
		
		@Override
		public void set(int renderState) {
			torchState[0] = torchState[2] = (renderState & 2) != 0;
			torchState[1] = (renderState & 1) != 0;
			segmentCol[1] = (renderState & 2) != 0 ? ON : OFF;
		}
		
		@Override
		public void setItemRender() {
			set(0);
		}
	}
}
