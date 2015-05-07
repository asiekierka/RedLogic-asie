package mods.immibis.redlogic.gates;

import java.util.Collection;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import net.minecraft.nbt.NBTTagCompound;

public abstract class GateCompiler {
	public abstract Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings);
}
