package mods.immibis.redlogic.interaction;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

public enum LumarButtonModel {
	Button(3/16f, 2/16f, "redlogic:lumar-button"),
	Plate(7/16f, 7/16f, "redlogic:lumar-plate");
	
	private LumarButtonModel(double hsize, double vsize, String frontTex) {
		this.hsize = hsize;
		this.vsize = vsize;
		this.glowIconName = frontTex;
	}
	
	public final double hsize, vsize;
	public final String glowIconName;
	@SideOnly(Side.CLIENT) public IIcon glowIcon;
	
	public static final LumarButtonModel[] VALUES = values();
}
