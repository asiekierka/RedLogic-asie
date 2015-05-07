package mods.immibis.redlogic.chips.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import mods.immibis.redlogic.api.chips.compiler.ICompilableBlock;
import mods.immibis.redlogic.api.chips.compiler.ICompilableExpression;
import mods.immibis.redlogic.api.chips.scanner.IScannedBlock;
import mods.immibis.redlogic.api.chips.scanner.IScannedInput;
import mods.immibis.redlogic.api.chips.scanner.IScannedNode;
import mods.immibis.redlogic.api.chips.scanner.IScannedOutput;
import mods.immibis.redlogic.chips.scanner.ScannedCircuit;
import mods.immibis.redlogic.chips.scanner.ScannedWire;

// TODO: all the casts to DigraphInput and DigraphOutput are UGLY
public class CircuitCompiler {
	
	/**
	 * @return Class name
	 */
	public static String compile(ScannedCircuit circuit) {
		
		// indexed by [direction]
		int[] numOutputWires = new int[6];
		int[] numInputWires = new int[6];
		
		boolean listDataFlowBlocks = Boolean.getBoolean("mods.immibis.redlogic.chips.listdfblocks");
		boolean traceSort = Boolean.getBoolean("mods.immibis.redlogic.chips.tracesort");
		boolean traceCompile = Boolean.getBoolean("mods.immibis.redlogic.chips.tracecompile");
		
		Collection<ICompilableBlock> scannedFunctions = new ArrayList<ICompilableBlock>();
		
		// default output function
		ICompilableBlock defaultOutputBlock = new ZeroCBlock(circuit);
		scannedFunctions.add(defaultOutputBlock);
		IScannedOutput defaultOutput = defaultOutputBlock.getOutputs()[0];
		
		for(int k = 0; k < 6; k++) {
			IScannedNode inNode = circuit.getInputNode(k);
			IScannedNode outNode = circuit.getOutputNode(k);
			
			// "in"/"out" perspective is reversed as we're looking from inside the circuit
			// (in nodes have outputs, and out nodes have inputs)
			
			if(inNode != null) {
				numInputWires[k] = inNode.getNumWires();
				ICompilableBlock block = new CircuitInputCBlock(circuit, k, numInputWires[k]);
				
				for(int i = 0; i < inNode.getNumWires(); i++)
					inNode.getWire(i).addOutput(block.getOutputs()[i]);
				
				scannedFunctions.add(block);
			}
			
			if(outNode != null) {
				numOutputWires[k] = outNode.getNumWires();
				ICompilableBlock block = new CircuitOutputCBlock(circuit, k, numOutputWires[k]);
				
				for(int i = 0; i < outNode.getNumWires(); i++)
					outNode.getWire(i).addInput(block.getInputs()[i]);
				
				scannedFunctions.add(block);
			}
		}
		
		for(IScannedBlock block : circuit.getNonWireBlocks())
			scannedFunctions.addAll(block.toCompilableBlocks());
		
		// build data flow graph
		Collection<DigraphBlock> rootNodes = new ArrayList<DigraphBlock>();
		Collection<DigraphBlock> functions = new ArrayList<DigraphBlock>();
		for(ICompilableBlock sf : scannedFunctions) {
			DigraphBlock f = new DigraphBlock();
			
			f.inputs = sf.getInputs();
			f.outputs = sf.getOutputs();
			
			if(CircuitCompiler.class.desiredAssertionStatus()) {
				assert f.inputs != null : sf+" returned null inputs array";
				assert f.outputs != null : sf+" returned null outputs array";
			
				for(IScannedInput i : f.inputs) assert i != null : sf+" returned null input";
				for(IScannedOutput o : f.outputs) assert o != null : sf+" returned null output";
			}
			
			f.code = sf;
			functions.add(f);
			for(IScannedInput i : f.inputs) ((DigraphInput)i).function = f;
			for(IScannedOutput o : f.outputs) ((DigraphOutput)o).function = f;
			
			if(listDataFlowBlocks) System.out.println(f+": "+f.code+": inputs from "+Arrays.toString(f.inputs)+", outputs to "+Arrays.toString(f.outputs));
			
			if(f.inputs.length == 0)
				rootNodes.add(f);
		}
		
		for(ScannedWire wire : circuit.getWires()) {
			if(wire.inputs.size() == 0)
				continue;
			
			if(listDataFlowBlocks) System.out.println("wire: "+wire.outputs+" -> "+wire.inputs);
			
			IScannedOutput valueFrom;
			
			if(wire.outputs.size() == 0) {
				valueFrom = defaultOutput;
		
			} else if(wire.outputs.size() > 1) {
				DigraphBlock db = new DigraphBlock();
				db.code = new MultipleOutputsCBlock(circuit, wire.outputs.size());
				
				// link all the outputs on this wire to the inputs of the new block
				db.inputs = new DigraphInput[wire.outputs.size()];
				Iterator<IScannedOutput> output_it = wire.outputs.iterator();
				int k = 0;
				while(output_it.hasNext()) {
					DigraphOutput o = (DigraphOutput)output_it.next();
					DigraphInput i = new DigraphInput();
					
					i.function = db;
					i.linkedTo = o;
					o.linkedTo.add(i);
					
					db.inputs[k++] = i;
				}
				
				assert k == wire.outputs.size() : "???";
				
				db.outputs = new DigraphOutput[1];
				db.outputs[0] = new DigraphOutput(db);
				valueFrom = db.outputs[0];
				
				functions.add(db);
				
				// TODO test this (generated or gate)
			
			} else {
				valueFrom = wire.outputs.iterator().next();
			}
			
			assert valueFrom != null;
			
			for(IScannedInput i : wire.inputs) {
				assert ((DigraphInput)i).linkedTo == null : "An input of "+((DigraphInput)i).function.code+" is part of multiple wires (linked to "+((DigraphInput)i).linkedTo.function.code+" and "+((DigraphOutput)valueFrom).function.code+")"; 
				((DigraphInput)i).linkedTo = (DigraphOutput)valueFrom;
				((DigraphOutput)valueFrom).linkedTo.add((DigraphInput)i);
			}
		}
		
		// if any inputs weren't part of any wires, link them to the default output
		for(DigraphBlock f : functions)
			for(IScannedInput isi : f.inputs)
				if(((DigraphInput)isi).linkedTo == null)
					((DigraphInput)isi).linkedTo = (DigraphOutput)defaultOutput;
		
		CompiledCircuit ctx = new CompiledCircuit(numInputWires, numOutputWires, circuit.rotation);
		
		// break loops - DFS from all 0-input nodes, was O(e), now it might not be (TODO re-check)
		{
			int nextNameID = 0;
			
			Collection<DigraphInput> delayedInputs = new ArrayList<DigraphInput>();
			dfsBreakLoops(rootNodes, functions, delayedInputs);
			
			for(DigraphInput i : delayedInputs) {
				String fn = "__d" + (nextNameID++);
				
				ctx.createField(fn, "Z");
				
				DigraphBlock in = new DigraphBlock();
				in.code = new DelayInBlock(circuit, fn);
				in.outputs = new DigraphOutput[0];
				in.inputs = new DigraphInput[1];
				in.inputs[0] = new DigraphInput(in, i.linkedTo);
				
				DigraphBlock out = new DigraphBlock();
				out.code = new DelayOutBlock(circuit, fn);
				out.inputs = new DigraphInput[0];
				out.outputs = new DigraphOutput[1];
				out.outputs[0] = new DigraphOutput(out);
				
				i.linkedTo.linkedTo.remove(i);
				i.linkedTo = (DigraphOutput)out.outputs[0];
				i.linkedTo.linkedTo.add(i);
				
				functions.add(in);
				functions.add(out);
			}
		} 
		
		// topological sort - currently O(n^2) worst case, TODO improve efficiency
		List<DigraphBlock> evalOrder = new ArrayList<DigraphBlock>(functions.size());
		while(true) {
			
			if(evalOrder.size() == functions.size())
				break; // done
			
			boolean changedAnything = false;
			
			for(DigraphBlock f : functions) {
				if(f.canAdd()) {
					changedAnything = true;
					evalOrder.add(f);
					f.addedToOrderYet = true;
					if(traceSort) System.out.println("adding "+f+" (inputs="+Arrays.toString(f.inputs)+", outputs="+Arrays.toString(f.outputs)+")");
				}
			}
			
			if(!changedAnything) {
				for(DigraphBlock f : functions)
					if(!f.addedToOrderYet)
						System.out.println("Not added: "+f);
				throw new RuntimeException("circuit has loops. loops were supposed to be broken at an earlier stage of processing");
			}
		}
		
		ctx.startEmittingCode();
		
		for(DigraphBlock f : evalOrder) {
			ICompilableExpression[] inputExprs = new ICompilableExpression[f.inputs.length];
			for(int k = 0; k < f.inputs.length; k++)
				inputExprs[k] = ((DigraphInput)f.inputs[k]).linkedTo.expression;
			
			if(traceCompile) System.out.println("  compile "+f+" <- "+Arrays.toString(f.inputs));
			ICompilableExpression[] outputExprs = f.code.compile(ctx, inputExprs);
			for(int k = 0; k < f.outputs.length; k++) {
				DigraphOutput o = (DigraphOutput)f.outputs[k];
				o.expression = outputExprs[k];
				if(traceCompile) System.out.println(f+" Out"+k+": "+o.linkedTo.size()+" "+o.expression.alwaysInline());
				if(o.linkedTo.size() > 1 && !o.expression.alwaysInline()) {
					ICompilableBlock cacheBlock = new CacheCBlock(circuit);
					o.expression = cacheBlock.compile(ctx, new ICompilableExpression[] {o.expression})[0];
				}
			}
		}
		
		ctx.finishEmittingCode();
		
		return ctx.getClassName();
	}
	
	private static void dfsBreakLoops(Collection<DigraphBlock> rootNodes, Collection<DigraphBlock> allBlocks, Collection<DigraphInput> delayedInputs) {
		boolean trace = Boolean.getBoolean("mods.immibis.redlogic.chips.tracedfs");
		
		rootNodes = new HashSet<DigraphBlock>(rootNodes);
		
		while(rootNodes.size() > 0) {
			for(DigraphBlock root : rootNodes) {
				if(root.dfsVisited)
					continue;
				
				DigraphBlock prev = null;
				DigraphBlock cur = root;
				
				root.dfsVisiting = true;
				root.dfsVisited = true;
				
				if(trace) System.out.println("dfs root: "+cur);
				
				while(cur != null) {
					if(trace) System.out.println("visiting "+cur);
					
					prev = cur;
					
					// get first unvisited child, or parent
					outer: for(IScannedOutput o : cur.outputs) {
						for(DigraphInput i : ((DigraphOutput)o).linkedTo) {
							if(!i.function.dfsVisited) {
								i.function.dfsParent = cur;
								cur = i.function;
								if(trace) System.out.println("descending to "+cur);
								cur.dfsVisited = true;
								cur.dfsVisiting = true;
								break outer;
							}
							if(i.function.dfsVisiting && !i.useDelay) {
								i.useDelay = true;
								delayedInputs.add(i);
								if(trace) System.out.println("DFS DETECTED LOOP: "+cur+" to "+i.function);
							}
						}
					}
					if(prev == cur) {
						cur.dfsVisiting = false;
						cur = cur.dfsParent;
						if(trace) System.out.println("ascending to "+cur);
					}
				}
			}
			
			rootNodes.clear();
			
			for(DigraphBlock db : allBlocks) {
				if(!db.dfsVisited)
					//throw new AssertionError("didn't visit "+db);
					rootNodes.add(db);
				if(db.dfsVisiting)
					throw new AssertionError("didn't clear visiting flag on "+db);
			}
		}
	}
}
