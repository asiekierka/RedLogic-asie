package mods.immibis.microblocks.api;

public interface IMicroblockSupporterTile2 extends IMicroblockSupporterTile {
	/**
	 * Called after microblocks are added or removed.
	 */
	public void onMicroblocksChanged();
}
