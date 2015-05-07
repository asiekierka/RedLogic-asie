package mods.immibis.redlogic.interaction;

public enum LumarButtonType {
	/** Normal button. Stays pressed for one second. Lights up when unpressed and not receiving a signal. */
	Normal,
	/** Latchable button. Stays pressed until not receiving a signal. Lights up when pressed or receiving a signal. */
	Latch,
	/** Self-latching button. Stays pressed until receiving a signal. Output signal is inverted. Lights up when pressed. */
	SelfLatch,
	/** Toggle button. Stays pressed until pressed again. Lights up when pressed. */
	Toggle;
	
	public static final LumarButtonType[] VALUES = values();
}
