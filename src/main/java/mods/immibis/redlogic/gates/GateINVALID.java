package mods.immibis.redlogic.gates;

import java.util.Collection;
import java.util.Collections;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.scanner.IScanProcess;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import net.minecraft.nbt.NBTTagCompound;

public class GateINVALID {
	public static class Logic extends GateLogic {
		@Override public boolean getInputID(int side, int gateSettings) {return false;}
		@Override public boolean getOutputID(int side, int gateSettings) {return false;}
		@Override public void update(short[] inputs, short[] outputs, int gateSettings) {}
	}
	
	public static class Rendering extends GateRendering {}
	
	public static class Compiler extends GateCompiler {
		@Override
		public Collection<ICompilableBlock> toCompilableBlocks(IScanProcess process, IScannedNode[] nodes, NBTTagCompound logicTag, int gateSettings) {
			return Collections.emptyList();
		}
	}
}
