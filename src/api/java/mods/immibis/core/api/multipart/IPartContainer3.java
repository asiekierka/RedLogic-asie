package mods.immibis.core.api.multipart;

import net.minecraft.client.renderer.RenderBlocks;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IPartContainer3 extends IPartContainer {
	/**
	 * Renders all the parts.
	 * This will always be called instead of {@link IPartContainer#render(RenderBlocks)} on part containers implementing IPartContainer3.
	 * @param render The render context.
	 * @return True if anything was rendered.
	 */
	@SideOnly(Side.CLIENT)
	public boolean render2(RenderBlocks render);
	
	/**
	 * Renders one part.
	 * This will always be called instead of {@link IPartContainer#render(RenderBlocks)} on part containers implementing IPartContainer3.
	 * @param render The render context.
	 * @param part The part number.
	 * @return True if anything was rendered.
	 */
	@SideOnly(Side.CLIENT)
	public boolean renderPart2(RenderBlocks render, int part);
}
