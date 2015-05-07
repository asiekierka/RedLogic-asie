package mods.immibis.redlogic.chips.builtin;

import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.api.chips.scanner.IScannableHook;

public class RegisterScannables {
	public static void register() {
		IScannableHook.perBlock.put(RedLogicMod.wire, new ScannableWires());
		IScannableHook.perBlock.put(RedLogicMod.gates, new ScannableGates());
		IScannableHook.perBlock.put(RedLogicMod.arrayCells, new ScannableArrayCells());
	}
}
