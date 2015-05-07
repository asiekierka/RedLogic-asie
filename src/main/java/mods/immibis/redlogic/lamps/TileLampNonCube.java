package mods.immibis.redlogic.lamps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.api.misc.ILampBlock.LampType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;

public class TileLampNonCube extends TileEntity {
	// LSB first:
	// 1 bit active flag
	// 4 bits colour
	// 3 bits side
	// 1 bit initialized flag
	// 2 bits lamp type
	// 4 bits model
	// 1 bit unused
	private short data;
	
	@Override public boolean canUpdate() {return false;}
	
	// initialized by static renderer
	@SideOnly(Side.CLIENT)
	AxisAlignedBB[] haloBBs;
	
	private void setIsInitialised() {
		data |= 0x100;
	}
	
	public boolean getIsInitialised() {
		return (data & 0x100) != 0;
	}
	
	public void setIsActive(boolean a) {
		if(a)
			data |= 1;
		else
			data &= ~1;
		
		if(getType() != LampType.Indicator)
			for(EnumSkyBlock e: EnumSkyBlock.values())
				worldObj.updateLightByType(e, xCoord, yCoord, zCoord);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public boolean getIsActive() {
		return (data & 1) != 0 || getType() == LampType.Decorative;
	}
	
	public void setColour(int woolId) {
		assert woolId >= 0 && woolId <= 15;
		data &= ~0x1E;
		data |= woolId << 1;
	}
	
	public int getColour() {
		return (data >> 1) & 15;
	}
	
	public void setSide(int side) {
		assert side >= 0 && side <= 5;
		data &= ~0xE0;
		data |= side << 5;
	}
	
	public int getSide() {
		return (data >> 5) & 7;
	}
	
	private static final LampType[] TYPES = LampType.values();
	public LampType getType() {
		int i = (data >> 9) & 3;
		if(i == 3) i = 0;
		return TYPES[i];
	}
	
	public void setType(LampType type) {
		data &= ~0x0600;
		data |= type.ordinal() << 9;
	}
	
	public void setModel(int model) {
		assert model >= 0 && model <= 15;
		data &= ~0x7800;
		data |= model << 11;
	}
	
	public int getModel() {
		return (data >> 11) & 15;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		data = par1nbtTagCompound.getShort("lamp");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setShort("lamp", data);
	}

	public void initFromItemDamage(int dmg) {
		int type = (dmg >> 4) & 3;
		setColour(dmg & 15);
		setType(TYPES[type == 3 ? 0 : type]);
		setModel((dmg >> 6) & 15);
		setIsInitialised();
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setShort("d", data);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		data = pkt.func_148857_g().getShort("d");
		if(getType() != LampType.Indicator) {
			for(EnumSkyBlock e: EnumSkyBlock.values())
				worldObj.updateLightByType(e, xCoord, yCoord, zCoord);
		}
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public ItemStack getDroppedItem() {
		return new ItemStack(RedLogicMod.lampNonCube, 1, (getModel() << 6) | (getType().ordinal() << 4) | getColour());
	}

	public AxisAlignedBB getCollisionBoxFromPool() {
		double radius = 4, height = 16;
		switch(getModel()) {
		case 0: radius = 8; height = 16; break;
		case 1: radius = 4; height = 14; break;
		case 2: radius = 7; height = 4; break;
		}
		
		AxisAlignedBB bb = null;
		
		double minr = 0.5-radius/16, maxr = 0.5+radius/16;
		double maxh = height/16, minh = 1-maxh;
		
		switch(getSide()) {
		case Dir.NX: bb = AxisAlignedBB.getBoundingBox(0, minr, minr, maxh, maxr, maxr); break;
		case Dir.NY: bb = AxisAlignedBB.getBoundingBox(minr, 0, minr, maxr, maxh, maxr); break;
		case Dir.NZ: bb = AxisAlignedBB.getBoundingBox(minr, minr, 0, maxr, maxr, maxh); break;
		case Dir.PX: bb = AxisAlignedBB.getBoundingBox(minh, minr, minr, 1, maxr, maxr); break;
		case Dir.PY: bb = AxisAlignedBB.getBoundingBox(minr, minh, minr, maxr, 1, maxr); break;
		case Dir.PZ: bb = AxisAlignedBB.getBoundingBox(minr, minr, minh, maxr, maxr, 1); break;
		}
		bb.offset(xCoord, yCoord, zCoord);
		
		return bb;
	}
	
}
