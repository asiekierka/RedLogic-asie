package mods.immibis.redlogic.api.wiring;

/**
 * Implemented by tile entities that need to be notified when a connected bundled cable changes state.
 * 
 * This includes bundled cables themselves. To emit a bundled signal, call {@link #onBundledInputChanged()} on any
 * neighbouring tile entities that implement IBundledUpdatable, and return the new signal strengths from
 * {@link IBundledEmitter#getBundledCableStrength(int, int)}. 
 */
public interface IBundledUpdatable {
	public void onBundledInputChanged();
}
