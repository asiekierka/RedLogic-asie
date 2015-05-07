package mods.immibis.redlogic.api.wiring;

/**
 * Used for tiles that participate in red alloy signal propagation algorithm - specifically,
 * red alloy wire, and blocks that emulate it such as array cells.
 * 
 * Red alloy propagators should usually also be {@link IRedstoneUpdatable}, and/or listen for block updates,
 * as redstone emitters do not call propagateRedAlloySignal.
 * 
 * The current algorithm when propagateRedAlloySignal is called (which is probably not optimal) is:
 * <ol>
 * <li>If being called recursively, set a flag <tt>recursiveCallPending</tt> and return.
 * <li>Calculate the <tt>newSignalStrength</tt> from the surrounding blocks. (This is the second step) 
 * <li>If <tt>newSignalStrength &lt; currentSignalStrength</tt>:
 *   <ol>
 *   <li>Set the <tt>currentSignalStrength</tt> to zero.
 *   <li>Propagate signal to neighbours (via propagateRedAlloySignal).
 *   <li>Calculate the <tt>newSignalStrength</tt> from the surrounding blocks.
 *   </ol>
 * <li>If <tt>newSignalStrength != currentSignalStrength</tt>: (this is not exclusive with the previous block)
 *   <ol>
 *   <li>Set <tt>currentSignalStrength</tt> to <tt>newSignalStrength</tt>.
 *   <li>Propagate signal to neighbours (via propagateRedAlloySignal).
 *   </ol>
 * <li>If <tt>recursiveCallPending</tt> is true, set it to false and go to just before the second step.
 * <li>
 * </ol>
 */
public interface IRedAlloyPropagator {
	public void propagateRedAlloySignal(IWireUpdateOperation operation);
}
