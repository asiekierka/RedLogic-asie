package mods.immibis.redlogic.gates;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public abstract class GateLogic {
	
	public World world; // undefined for Stateless logics
	
	// For redstone connections, inputs and outputs are signal strength 0-255.
	// For bundled connections, inputs and outputs are a bitmask, with bit 0 = white.
	
	public abstract void update(short[] inputs, short[] outputs, int gateSettings);
	public abstract boolean getInputID(int side, int gateSettings);
	public abstract boolean getOutputID(int side, int gateSettings);
	
	public int getRenderState(short[] inputs, short[] outputs, int gateSettings) {return 0;}
	public int configure(int gateSettings) {return gateSettings;}
	public void write(NBTTagCompound tag) {}
	public void read(NBTTagCompound tag) {}
	

	public boolean connectsToDirection(int side, int gateSettings) {
		return true;
	}
	
	public static interface WithBundledConnections {
		public boolean isBundledConnection(int side);
	}
	
	/**
	 * Marker interface for gate-logic classes which do not store any state and can thus be shared
	 * between multiple gates.
	 * This does not mean the gate has no state - for example RS latches are Stateless, but they store
	 * their only state in the input and output arrays.
	 */
	public static interface Stateless {}
	
	/**
	 * Marker interface for gates which can be horizontally flipped by shift-clicking with a screwdriver.
	 */
	public static interface Flippable {}
	
	public static interface WithRightClickAction {
		public void onRightClick(EntityPlayer ply, GateTile tile);
	}
	
	public static interface WithPointer {
		public int getPointerPosition(); // degrees
		public float getPointerSpeed(); // degrees per tick
	}
	
	/**
	 * Not the best way to implement timer sounds, but it works for now.
	 * Non-Stateless GateLogics can set this to true in their update method, and then
	 * the gate will play a click sound.
	 */
	public boolean tickSoundPending;
}


