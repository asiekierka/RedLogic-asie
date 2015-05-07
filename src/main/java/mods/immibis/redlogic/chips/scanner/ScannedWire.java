package mods.immibis.redlogic.chips.scanner;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.api.chips.scanner.IScannedWire;

public class ScannedWire implements IScannedWire, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public Set<IScannedInput> inputs = new HashSet<IScannedInput>();
	public Set<IScannedOutput> outputs = new HashSet<IScannedOutput>();
	
	@Override
	public void addInput(IScannedInput input) {
		inputs.add(input);
	}
	
	@Override
	public void addOutput(IScannedOutput output) {
		outputs.add(output);
	}
	
	// for debugging
	private static int nextID = 0;
	private int wireID = nextID++;
	@Override public String toString() {return "Wire"+wireID;}
}
