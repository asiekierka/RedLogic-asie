package mods.immibis.redlogic.wires;

import com.google.common.collect.ImmutableBiMap;

import mods.immibis.core.RenderUtilsIC;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// Damage value for wire items is the ordinal of one of these, but metadata in the world
// is different, and depends on the TE class (for client sync purposes).
// Metadata mapping is in CLASS_TO_META and META_TO_CLASS.

public enum EnumWireType {
	RED_ALLOY("wire-red-alloy", "", 2, 2, PlainRedAlloyTile.class, 0x800000),
	INSULATED_0("wire-insulated!", "0", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_1("wire-insulated!", "1", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_2("wire-insulated!", "2", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_3("wire-insulated!", "3", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_4("wire-insulated!", "4", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_5("wire-insulated!", "5", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_6("wire-insulated!", "6", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_7("wire-insulated!", "7", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_8("wire-insulated!", "8", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_9("wire-insulated!", "9", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_A("wire-insulated!", "10", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_B("wire-insulated!", "11", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_C("wire-insulated!", "12", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_D("wire-insulated!", "13", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_E("wire-insulated!", "14", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	INSULATED_F("wire-insulated!", "15", 3, 4, InsulatedRedAlloyTile.class, 0xFFFFFF),
	BUNDLED("wire-bundled!", "", 4, 6, BundledTile.class, 0xFFFFFF),
	;
	
		
	
	public static final int PLAIN_RED_ALLOY_META = 0;
	public static final int INSULATED_RED_ALLOY_META = 1;
	public static final int BUNDLED_META = 2;
	
	public static ImmutableBiMap<Class<? extends WireTile>, Integer> CLASS_TO_META = ImmutableBiMap.<Class<? extends WireTile>, Integer>builder()
		.put(PlainRedAlloyTile.class, PLAIN_RED_ALLOY_META)
		.put(InsulatedRedAlloyTile.class, INSULATED_RED_ALLOY_META)
		.put(BundledTile.class, BUNDLED_META)
		.build();
	
	public static ImmutableBiMap<Integer, Class<? extends WireTile>> META_TO_CLASS = CLASS_TO_META.inverse();
	
	
	public static EnumWireType[] INSULATED_WIRE = {
		INSULATED_0, INSULATED_1, INSULATED_2, INSULATED_3,
		INSULATED_4, INSULATED_5, INSULATED_6, INSULATED_7,
		INSULATED_8, INSULATED_9, INSULATED_A, INSULATED_B,
		INSULATED_C, INSULATED_D, INSULATED_E, INSULATED_F,
	};
	
	public boolean hasJacketedForm() {
		//return this == RED_ALLOY || this == BUNDLED;
		return true;
	}
	
	
	public final String textureName, texNameSuffix;
	public final double thickness, width;
	public final Class<? extends WireTile> teclass;
	public final int itemColour;
	
	@SideOnly(Side.CLIENT)
	public IIcon texture_cross, texture_straight_x, texture_straight_z, texture_none,
		texture_tee_nz, texture_tee_pz, texture_tee_nx, texture_tee_px,
		texture_corner_nn, texture_corner_np, texture_corner_pn, texture_corner_pp,
		texture_end_nx, texture_end_px, texture_end_nz, texture_end_pz,
		
		texture_jacketed, texture_jacketed_end, texture_jacketed_cross;
	
	private EnumWireType(String texName, String texSuffix, int thicknessPixels, int widthPixels, Class<? extends WireTile> teclass, int itemColour) {
		this.textureName = "redlogic:wire/" + texName;
		this.texNameSuffix = texSuffix;
		this.thickness = thicknessPixels / 16.0;
		this.width = widthPixels / 16.0;
		this.teclass = teclass;
		this.itemColour = itemColour;
	}
	
	public static final EnumWireType[] VALUES = values();

	@SideOnly(Side.CLIENT)
	public void loadTextures(IIconRegister reg, String base, String suffix) {
		if(!base.endsWith("!")) {
			IIcon i = reg.registerIcon(base);
			texture_cross = i;
			texture_straight_x = i;
			texture_straight_z = i;
			texture_tee_nz = i;
			texture_tee_pz = i;
			texture_tee_nx = i;
			texture_tee_px = i;
			texture_corner_nn = i;
			texture_corner_np = i;
			texture_corner_pn = i;
			texture_corner_pp = i;
			texture_none = i;
			texture_end_px = i;
			texture_end_nx = i;
			texture_end_pz = i;
			texture_end_nz = i;
			
		} else {
			texture_cross = RenderUtilsIC.loadIcon(reg, base + "cross" + suffix);
			texture_straight_x = RenderUtilsIC.loadIcon(reg, base + "straight-x" + suffix);
			texture_straight_z = RenderUtilsIC.loadIcon(reg, base + "straight-z" + suffix);
			texture_tee_nz = RenderUtilsIC.loadIcon(reg, base + "tee-nz" + suffix);
			texture_tee_pz = RenderUtilsIC.loadIcon(reg, base + "tee-pz" + suffix);
			texture_tee_nx = RenderUtilsIC.loadIcon(reg, base + "tee-nx" + suffix);
			texture_tee_px = RenderUtilsIC.loadIcon(reg, base + "tee-px" + suffix);
			texture_corner_nn = RenderUtilsIC.loadIcon(reg, base + "corner-nn" + suffix);
			texture_corner_np = RenderUtilsIC.loadIcon(reg, base + "corner-np" + suffix);
			texture_corner_pn = RenderUtilsIC.loadIcon(reg, base + "corner-pn" + suffix);
			texture_corner_pp = RenderUtilsIC.loadIcon(reg, base + "corner-pp" + suffix);
			texture_none = RenderUtilsIC.loadIcon(reg, base + "none" + suffix);
			texture_end_px = RenderUtilsIC.loadIcon(reg, base + "end-px" + suffix);
			texture_end_nx = RenderUtilsIC.loadIcon(reg, base + "end-nx" + suffix);
			texture_end_pz = RenderUtilsIC.loadIcon(reg, base + "end-pz" + suffix);
			texture_end_nz = RenderUtilsIC.loadIcon(reg, base + "end-nz" + suffix);
		}
		
		if(hasJacketedForm()) {
			String jacketedBase;
			if(base.endsWith("!")) {
				jacketedBase = base + "jacketed";
			} else {
				jacketedBase = base + "-j";
			}
			texture_jacketed = RenderUtilsIC.loadIcon(reg, jacketedBase + suffix);
			texture_jacketed_end = RenderUtilsIC.loadIcon(reg, jacketedBase + "-end" + suffix);
			texture_jacketed_cross = RenderUtilsIC.loadIcon(reg, jacketedBase + "-cross" + suffix);
		}		
	}
}
