package mods.immibis.redlogic.api.wiring;

/**
 * Implemented by tiles that behave like bundled cable.
 * 
 * The algorithm used is the same as with {@link IRedAlloyPropagator}, but with
 * up to 16 wires in parallel.
 * 
 * Tiles that implement IBundledPropagator should usually also implement IBundledUpdatable, as bundled-cable emitters
 * will not call {@link #propagateBundledSignal(short, IWireUpdateOperation)}.
 */
public interface IBundledPropagator {
	/**
	 * <tt>(colourMask & (1 << k)) != 0</tt> if colour <tt>k</tt> (wool colour IDs) is included in the mask.
	 */
	public void propagateBundledSignal(short colourMask, IWireUpdateOperation operation);
}
