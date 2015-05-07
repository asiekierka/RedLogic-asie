package mods.immibis.redlogic.chips.compiler;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;

public class DigraphBlock {
	IScannedInput[] inputs;
	IScannedOutput[] outputs;
	boolean addedToOrderYet;
	ICompilableBlock code;
	
	// used by DFS search that breaks loops
	DigraphBlock dfsParent;
	boolean dfsVisiting;
	boolean dfsVisited;
	
	boolean canAdd() {
		if(addedToOrderYet)
			return false;
		
		for(IScannedInput i : inputs) {
			assert ((DigraphInput)i).linkedTo != null : "An input of "+code+" is part of no wires.";
			if(!((DigraphInput)i).linkedTo.function.addedToOrderYet)
				return false;
		}
		return true;
	}
	
	// for debugging
	private static int nextID = 0;
	private int funcID = nextID++;
	@Override public String toString() {return (addedToOrderYet?"F":"U")+funcID;}
}