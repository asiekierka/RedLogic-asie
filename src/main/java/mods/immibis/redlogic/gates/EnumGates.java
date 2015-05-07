package mods.immibis.redlogic.gates;

import mods.immibis.redlogic.gates.types.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public enum EnumGates {
	AND(GateAND.Logic.class, GateAND.Rendering.class, GateAND.Compiler.class),
	OR(GateOR.Logic.class, GateOR.Rendering.class, GateOR.Compiler.class),
	NOT(GateNOT.Logic.class, GateNOT.Rendering.class, GateNOT.Compiler.class),
	RSLATCH(GateRSLatch.Logic.class, GateRSLatch.Rendering.class, GateRSLatch.Compiler.class),
	TOGGLE(GateToggleLatch.Logic.class, GateToggleLatch.Rendering.class, GateToggleLatch.Compiler.class),
	NOR(GateNOR.Logic.class, GateNOR.Rendering.class, GateNOR.Compiler.class),
	NAND(GateNAND.Logic.class, GateNAND.Rendering.class, GateNAND.Compiler.class),
	XOR(GateXOR.Logic.class, GateXOR.Rendering.class, GateXOR.Compiler.class),
	XNOR(GateXNOR.Logic.class, GateXNOR.Rendering.class, GateXNOR.Compiler.class),
	Buffer(GateBuffer.Logic.class, GateBuffer.Rendering.class, GateBuffer.Compiler.class),
	Multiplexer(GateMultiplexer.Logic.class, GateMultiplexer.Rendering.class, GateMultiplexer.Compiler.class),
	Repeater(GateRepeater.Logic.class, GateRepeater.Rendering.class, null),
	Timer(GateTimer.Logic.class, GateTimer.Rendering.class, GateTimer.Compiler.class),
	Counter(GateCounter.Logic.class, GateCounter.Rendering.class, GateCounter.Compiler.class),
	Sequencer(GateSequencer.Logic.class, GateSequencer.Rendering.class, null),
	PulseFormer(GatePulseFormer.Logic.class, GatePulseFormer.Rendering.class, GatePulseFormer.Compiler.class),
	Randomizer(GateRandomizer.Logic.class, GateRandomizer.Rendering.class, null),
	StateCell(GateStateCell.Logic.class, GateStateCell.Rendering.class, null),
	Synchronizer(GateSynchronizer.Logic.class, GateSynchronizer.Rendering.class, GateSynchronizer.Compiler.class),
	DLatch(GateDLatch.Logic.class, GateDLatch.Rendering.class, GateDLatch.Compiler.class),
	DFlop(GateDFlop.Logic.class, GateDFlop.Rendering.class, GateDFlop.Compiler.class),
	BundledLatch(GateBundledLatch.Logic.class, GateBundledLatch.Rendering.class, GateBundledLatch.Compiler.class),
	BundledRelay(GateBundledRelay.Logic.class, GateBundledRelay.Rendering.class, GateBundledRelay.Compiler.class),
	BundledMultiplexer(GateBundledMultiplexer.Logic.class, GateBundledMultiplexer.Rendering.class, GateBundledMultiplexer.Compiler.class),
	BundledAND(GateBundledAND.Logic.class, GateBundledAND.Rendering.class, GateBundledAND.Compiler.class),
	BundledOR(GateBundledOR.Logic.class, GateBundledOR.Rendering.class, GateBundledOR.Compiler.class),
	BundledNOT(GateBundledNOT.Logic.class, GateBundledNOT.Rendering.class, GateBundledNOT.Compiler.class),
	BundledXOR(GateBundledXOR.Logic.class, GateBundledXOR.Rendering.class, GateBundledXOR.Compiler.class),
	INVALID(GateINVALID.Logic.class, GateINVALID.Rendering.class, GateINVALID.Compiler.class),
	Comparator(GateComparator.Logic.class, GateComparator.Rendering.class, null),
	;
	
	private Class<? extends GateLogic> logicClass;
	private Class<? extends GateRendering> renderClass;
	//private Class<? extends GateCompiler> compilerClass;
	private GateLogic logicInst;
	private GateRendering renderInst;
	private GateCompiler compilerInst;
	
	private EnumGates(Class<? extends GateLogic> logicClass, Class<? extends GateRendering> renderClass, Class<? extends GateCompiler> compilerClass) {
		this.logicClass = logicClass;
		this.renderClass = renderClass;
		//this.compilerClass = compilerClass;
		
		if(GateLogic.Stateless.class.isAssignableFrom(logicClass))
			logicInst = createLogic();
		
		try {
			if(compilerClass == null)
				compilerInst = null;
			else
				compilerInst = compilerClass.getConstructor().newInstance(); 
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public GateLogic createLogic() {
		if(logicInst != null)
			return logicInst;
		try {
			return logicClass.getConstructor().newInstance();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public GateRendering getRendering() {
		if(renderInst != null)
			return renderInst;
		try {
			renderInst = renderClass.getConstructor().newInstance(); 
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		return renderInst;
	}

	public Class<? extends GateLogic> getLogicClass() {
		return logicClass;
	}
	
	public static final EnumGates[] VALUES = values();

	public GateCompiler getCompiler() {
		return compilerInst;
	}
}
