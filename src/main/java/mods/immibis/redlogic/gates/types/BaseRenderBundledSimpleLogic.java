package mods.immibis.redlogic.gates.types;

import mods.immibis.redlogic.RotatedTessellator;
import mods.immibis.redlogic.gates.GateRendering;
import mods.immibis.redlogic.rendering.WireRenderer;
import mods.immibis.redlogic.wires.EnumWireType;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.util.ForgeDirection;

public class BaseRenderBundledSimpleLogic extends GateRendering {
	private boolean connBack, connLeft, connRight, connFront;
	
	protected BaseRenderBundledSimpleLogic(String texname)
	{
		segmentCol = new int[] {0xFFFFFF};
		segmentTex = new String[] {texname};
	}
	
	@Override
	public void set(int renderState) {
		connFront = (renderState & 1) != 0;
		connBack = (renderState & 2) != 0;
		connLeft = (renderState & 4) != 0;
		connRight = (renderState & 8) != 0;
	}
	
	@Override
	public void setItemRender() {
		connFront = connBack = connLeft = connRight = true;
	}
	
	@Override
	public void customRender(RotatedTessellator rt, RenderBlocks render) {
		ForgeDirection up = ForgeDirection.VALID_DIRECTIONS[rt.side ^ 1];
		
		// draw a bundled cable, offset away from the base of the gate
		double offset = 0; // was 0.125
		double dx = up.offsetX * offset, dy = up.offsetY * offset, dz = up.offsetZ * offset;
		rt.x += dx; rt.y += dy; rt.z += dz;
		WireRenderer.renderWireSide(rt, render, EnumWireType.BUNDLED, connFront, connBack, connLeft, connRight, false, false, false, false, false, false, false, false, false, false, false, false, null, null, null, null, true, false);
		rt.x -= dx; rt.y -= dy; rt.z -= dz;
		
		renderRaisedSquare(rt, render, segmentIcons[0], 4, 4, 12, 12, 3 + (int)(offset * 16 + 0.5));
	}
}
