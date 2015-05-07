package mods.immibis.redlogic.chips.compiler;

import java.util.HashSet;
import java.util.Set;

import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedWire;

public class DigraphInput implements IScannedInput {
	DigraphBlock function;
	DigraphOutput linkedTo;
	Set<IScannedWire> wires = new HashSet<IScannedWire>();
	
	// true if this input was chosen to be delayed to break a loop
	boolean useDelay;
	
	public DigraphInput() {}
	
	public DigraphInput(DigraphBlock db) {
		this.function = db;
	}

	public DigraphInput(DigraphBlock db, DigraphOutput linkedTo) {
		this(db);
		this.linkedTo = linkedTo;
		linkedTo.linkedTo.add(this);
	}

	@Override
	public String toString() {
		return "I:" + function + "<" + linkedTo;
	}
}