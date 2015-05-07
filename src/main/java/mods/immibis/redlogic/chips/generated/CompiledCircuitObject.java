package mods.immibis.redlogic.chips.generated;

import java.io.Serializable;

// Everything used by generated code is prefixed with _ (except the constructor)
public abstract class CompiledCircuitObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public boolean _inputs[][] = new boolean[6][];
	public boolean _outputs[][] = new boolean[6][];
	
	public int rotation;
	
	protected abstract void _update();
	protected abstract void _unpackInputs();
	protected abstract void _packOutputs();
	
	public void update() {
		_unpackInputs();
		_update();
		_packOutputs();
	}
	
	protected static int[] _makearray(int a, int b, int c, int d, int e, int f) {
		return new int[] {a, b, c, d, e, f};
	}
	
	protected CompiledCircuitObject(int[] numInWires, int[] numOutWires, int rotation) {
		this.rotation = rotation;
		for(int k = 0; k < 6; k++) {
			_inputs[k] = new boolean[numInWires[k]];
			_outputs[k] = new boolean[numOutWires[k]];
		}
	}
	
	protected CompiledCircuitObject(int[] numInWires, int[] numOutWires) {
		this(numInWires, numOutWires, 0);
	}
}
