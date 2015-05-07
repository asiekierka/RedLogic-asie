package mods.immibis.redlogic.api.wiring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * This is not intended to be subclassed by clients.
 * 
 * Typical lifecycle example:
 * <pre>
 * try (IWireUpdateOperation operation = IWireUpdateOperation.create(...)) {
 *     propagateRedAlloySignal(operation);
 *     operation.finish();
 * }
 * </pre>
 */
public abstract class IWireUpdateOperation implements AutoCloseable {
	
	
	/**
	 * Marks a block to be updated when the operation is finished.
	 */
	public abstract void queueBlockUpdate(int x, int y, int z);
	
	/**
	 * Marks all neighbours of a block (but not the block itself) to be updated when the operation is finished.
	 */
	public abstract void queueNeighbourBlockUpdates(int x, int y, int z);
	
	/**
	 * Marks a block to get a call to {@link IBundledUpdatable#onBundledInputChanged()}, if applicable, when
	 * the operation is finished.
	 */
	public abstract void queueBundledUpdate(int x, int y, int z);
	public abstract void queueBundledUpdate(TileEntity tile);
	
	
	
	/**
	 * Finishes an IWireUpdateOperation.
	 * 
	 * Every operation that is created must be released. {@link #close()} calls this,
	 * so you can use a try-with-resources block.
	 */
	public abstract void release();
	
	/**
	 * Finishes an IWireUpdateOperation.
	 * 
	 * This applies queued block updates.
	 * 
	 * You do <i>not</i> need to finish an operation if there was an unexpected exception while processing it.  
	 * 
	 * No further methods may be called on this object after finishing an operation
	 * (unless the same object is later reused).
	 * 
	 * Finished operation objects may be reused.
	 */
	public abstract void finish();
	
	/**
	 * Creates an IWireUpdateOperation, or reuses a cached one.
	 * 
	 * The returned object should be used in a try-with-resources block, as these
	 * objects are pooled.
	 * Failure to close an IWireUpdateOperation promptly will result in an assertion failure.
	 * 
	 * This is thread-safe.
	 */
	public static IWireUpdateOperation create(World world) {
		if(ZZ_FACTORY_METHOD == null) throw new IllegalStateException("RedLogic is not installed or not yet initialized.");
		try {
			return (IWireUpdateOperation)ZZ_FACTORY_METHOD.invoke(null, new Object[] {world});
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new AssertionError("shouldn't happen", e);
		} catch (InvocationTargetException e) {
			if(e.getCause() instanceof RuntimeException)
				throw (RuntimeException)e.getCause();
			if(e.getCause() instanceof Error)
				throw (Error)e.getCause();
			throw new AssertionError("shouldn't happen", e);
		}
	}
	
	/**
	 * Allows this class to be used in a try-with-resources statement.
	 */
	@Override
	public final void close() {
		release();
	}
	
	
	/**
	 * Obviously, writing to this if you are not RedLogic is a bad idea.
	 */
	public static Method ZZ_FACTORY_METHOD = null;
		
	protected IWireUpdateOperation() {}
		
	
	public static abstract class Factory {
		public static IWireUpdateOperation createOperation() {
			if(INSTANCE == null) throw new IllegalStateException("RedLogic is not installed.");
			return INSTANCE.createWireUpdateOperation_();
		}
		
		public static void returnOperation(IWireUpdateOperation operation) {
			if(INSTANCE == null) throw new IllegalStateException("RedLogic is not installed.");
			INSTANCE.returnWireUpdateOperation_(operation);
		}
		
		protected abstract IWireUpdateOperation createWireUpdateOperation_();
		protected abstract void returnWireUpdateOperation_(IWireUpdateOperation operation);
		
		protected static Factory INSTANCE;
		
		public static void __setInstance(Factory instance) {
			if(INSTANCE != null) throw new IllegalStateException("Already initialized.");
			INSTANCE = instance;
		}
	}
}
