package mods.immibis.redlogic.chips.compiler;

import java.util.HashSet;
import java.util.Set;

import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.api.chips.scanner.IScannedWire;

public class DigraphOutput implements IScannedOutput {
	DigraphBlock function;
	Set<DigraphInput> linkedTo = new HashSet<DigraphInput>();
	ICompilableExpression expression;
	Set<IScannedWire> wires = new HashSet<IScannedWire>();
	
	public DigraphOutput() {}
	
	public DigraphOutput(DigraphBlock db) {
		this.function = db;
	}

	@Override
	public String toString() {
		return "O:" + function;
	}
}