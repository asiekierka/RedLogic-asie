package mods.immibis.redlogic.gates.tests;

import java.io.IOException;
import java.util.Collection;

import mods.immibis.core.api.util.XYZ;
import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.scanner.CircuitLayoutException;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.NodeType;
import mods.immibis.redlogic.chips.compiler.CircuitCompiler;
import mods.immibis.redlogic.chips.generated.CCOFactory;
import mods.immibis.redlogic.chips.generated.CompiledCircuitObject;
import mods.immibis.redlogic.chips.scanner.ScannedCircuit;
import mods.immibis.redlogic.gates.EnumGates;
import mods.immibis.redlogic.gates.GateLogic;

public class GateTestbed {
	private EnumGates type;
	private String className;
	private int settings;
	private GateLogic logic;
	private CompiledCircuitObject cco;
	private short[] logic_inputs = new short[4];
	private short[] logic_outputs = new short[4];
	private boolean[][] cco_inputs = null;
	private boolean[][] cco_outputs = null;
	
	public GateTestbed(EnumGates type, int settings, boolean useCompiler) {
		this.type = type;
		this.settings = settings;
		
		if(useCompiler) {
			if(type.getCompiler() == null)
				throw new IllegalArgumentException("Cannot compile "+type);
			className = compile();
		}
		
		resetGate();
	}
	
	private boolean isBundledConnection(int dir) {
		return (logic instanceof GateLogic.WithBundledConnections) && ((GateLogic.WithBundledConnections)logic).isBundledConnection(dir);
	}
	
	private String compile() {
		final ScannedCircuit circuit = new ScannedCircuit(new XYZ(1, 1, 1), 0);
		
		//System.out.println(logic+" "+(logic instanceof GateLogic.WithBundledConnections));
		logic = type.createLogic();
		
		final IScannedNode[] nodes = new IScannedNode[4];
		for(int k = 0; k < 4; k++) {
			NodeType nt = isBundledConnection(k) ? NodeType.BUNDLED : NodeType.SINGLE_WIRE;
			IScannedNode n = circuit.getInputNode(k, nt);
			n.mergeWith(circuit.getOutputNode(k, nt));
			nodes[k] = n;
		}
		
		circuit.addScannedBlock(new XYZ(0, 0, 0), new IScannedBlock() {
			@Override
			public Collection<ICompilableBlock> toCompilableBlocks() {
				return type.getCompiler().toCompilableBlocks(circuit, nodes, null, settings);
			}
			@Override public void onConnect(IScannedBlock with, int wireside, int dir) throws CircuitLayoutException {}
			@Override public IScannedNode getNode(int wireside, int dir) {return null;}
		});
		
		circuit.finalizeNodeConnections();
		return CircuitCompiler.compile(circuit);
	}
	
	public void resetGate() {
		logic = type.createLogic();
		if(className != null) {
			try {
				cco = CCOFactory.instance.createObject(className);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			if(cco_inputs == null)
				cco_inputs = cco._inputs;
			else
				cco._inputs = cco_inputs;
			
			if(cco_outputs == null)
				cco_outputs = cco._outputs;
			else
				cco._outputs = cco_outputs;
		}
		
		for(int k = 0; k < 4; k++)
			logic_outputs[k] = 0;
	}

	public void setInput(int dir, int value) {
		if(isBundledConnection(dir)) {
			logic_inputs[dir] = (short)value;
			if(cco != null)
				for(int k = 0; k < 16; k++)
					cco._inputs[dir][k] = ((value & (1 << k)) != 0);
			
		} else {
			logic_inputs[dir] = (short)(value != 0 ? 255 : 0);
			if(cco != null)
				cco._inputs[dir][0] = (value != 0);
		}
	}

	public void tickGate() {
		logic.update(logic_inputs, logic_outputs, settings);
		if(cco != null)
			cco.update();
	}
	
	// returns (input | output)
	public int getOutputOrInputFromLogic(int dir) {
		if(isBundledConnection(dir)) {
			return (logic_inputs[dir] | logic_outputs[dir]) & 65535;
		} else {
			return (logic_inputs[dir] | logic_outputs[dir]) != 0 ? 1 : 0;
		}
	}
	
	public int getOutputOrInputFromCompiler(int dir) {
		if(isBundledConnection(dir)) {
			int i = 0;
			for(int k = 0; k < 16; k++)
				if(cco._outputs[dir][k] || cco._inputs[dir][k])
					i |= 1 << k;
			return i;
		} else {
			return cco._outputs[dir][0] || cco._inputs[dir][0] ? 1 : 0;
		}
	}

	public int[] getTestInputs(int dir) {
		if(isBundledConnection(dir))
			return new int[] {0, 65535, 128, 32768, 1, 46311, 56213, 7357, 12345, 54321};
		else
			return new int[] {0, 1};
	}
	
	
}
