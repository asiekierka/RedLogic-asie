package mods.immibis.redlogic.chips.builtin;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannableHook;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.api.wiring.IBundledWire;
import mods.immibis.redlogic.api.wiring.IRedstoneWire;

public class ScannableWires implements IScannableHook {
	@Override
	public IScannedBlock getScannedBlock(IScanProcess process, World w, int x, int y, int z) {
		TileEntity te = w.getTileEntity(x, y, z);
		
		if(te instanceof IRedstoneWire)
			return new ScannedCableBlockSingle(process, (IRedstoneWire)te);
		
		if(te instanceof IBundledWire)
			return new ScannedCableBlockBundled(process, (IBundledWire)te);
		
		return null;
	}
	
	static void init() {
	}
	
	
}
