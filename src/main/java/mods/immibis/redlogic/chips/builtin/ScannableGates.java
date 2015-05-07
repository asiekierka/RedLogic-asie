package mods.immibis.redlogic.chips.builtin;

import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import mods.immibis.redlogic.api.chips.scanner.CircuitLayoutException;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannableHook;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.gates.GateTile;

class ScannableGates implements IScannableHook {
	@Override
	public IScannedBlock getScannedBlock(IScanProcess process, World w, int x, int y, int z) throws CircuitLayoutException {
		GateTile gt = ((GateTile)w.getTileEntity(x, y, z));
		if(gt.getType() == null) return null;
		
		if(gt.getType().getCompiler() == null)
			throw new CircuitLayoutException(new ChatComponentTranslation("redlogic.chipscanner.unimplemented.gate", new Object[]{gt.getType().toString()}));
		
		return new ScannedGateBlock(process, gt.getType(), gt.getFront(), gt.getSide(), gt.isFlipped(), gt.getGateSettings(), gt.getLogic());
	}
}
