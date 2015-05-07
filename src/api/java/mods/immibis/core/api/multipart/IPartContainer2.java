package mods.immibis.core.api.multipart;

import net.minecraft.client.particle.EffectRenderer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Part containers (tiles and cover systems) should implement this to get
 * part destroy/hit effects.
 */
public interface IPartContainer2 extends IPartContainer {
	/**
	 * Creates particles when a part is destroyed.
	 * If the part number is invalid, returns false.
	 * Otherwise, creates particles if applicable, and returns true.
	 */
	@SideOnly(Side.CLIENT)
	public boolean addPartDestroyEffects(int part, EffectRenderer er);

	/**
	 * Creates particles when a part is punched.
	 * If the part number is invalid, returns false.
	 * Otherwise, creates particles if applicable, and returns true.
	 */
	@SideOnly(Side.CLIENT)
	public boolean addPartHitEffects(int part, int sideHit, EffectRenderer er);
}
