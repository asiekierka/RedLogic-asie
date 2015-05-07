package mods.immibis.redlogic.chips.builtin;

import net.minecraft.world.World;
import mods.immibis.redlogic.api.chips.scanner.CircuitLayoutException;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannableHook;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.array.ArrayCellTile;

public class ScannableArrayCells implements IScannableHook {
	@Override
	public IScannedBlock getScannedBlock(IScanProcess process, World w, int x, int y, int z) throws CircuitLayoutException {
		ArrayCellTile te = (ArrayCellTile)w.getTileEntity(x, y, z);
		switch(te.getType()) {
		case NULL: return new ScannedArrayCellBlockNull(process, te.getSide(), te.getFront());
		case INVERT: return new ScannedArrayCellBlockInvert(process, te.getSide(), te.getFront());
		case NON_INVERT: return new ScannedArrayCellBlockNonInvert(process, te.getSide(), te.getFront());
		}
		return null;
	}
}
