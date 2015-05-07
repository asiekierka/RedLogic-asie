package mods.immibis.redlogic.wires;

import mods.immibis.redlogic.api.wiring.IBareRedstoneWire;

public class PlainRedAlloyTile extends RedAlloyTile implements IBareRedstoneWire {
	
	public static int getVisualWireColour(int strength) {
		return (strength/2 + 127) << 16;
	}
	
	public static int getVisualEmissiveLightLevel(int strength) {
		return strength / 16;
	}
	
	@Override
	public int getVisualWireColour() {
		return (getRedstoneSignalStrength()/2 + 127) << 16;
	}
	
	@Override
	public int getVisualEmissiveLightLevel() {
		return getVisualEmissiveLightLevel(getRedstoneSignalStrength());
	}
	
	{
		syncSignalStrength = true;
		connectToBlockBelow = true;
	}
}
