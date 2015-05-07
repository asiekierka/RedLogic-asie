package mods.immibis.redlogic.chips.ingame;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.Vec3;
import mods.immibis.core.ImmibisCore;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.RedLogicMod;

public class TileChipFabricator extends TilePoweredBase implements ISidedInventory {
	public TileChipFabricator() {
		super(4, "chip fabricator");
	}
	
	public static final int SLOT_PHOTOMASK = 0;
	public static final int SLOT_OUT = 1;
	public static final int SLOT_REDSTONE = 2;
	public static final int SLOT_STONE = 3;
	
	private boolean has(int slot, Item item, int amount) {
		return inv.contents[slot] != null && inv.contents[slot].getItem().equals(item) && inv.contents[slot].stackSize >= amount;
	}
	
	private void sub(int slot, int amount) {
		inv.decrStackSize(slot, amount);
	}
	
	private boolean notfull(int slot) {
		return inv.contents[slot] == null || inv.contents[slot].stackSize < inv.contents[slot].getMaxStackSize();
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setByte("v", front);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}
	
	@Override
	public void onDataPacket(S35PacketUpdateTileEntity packet) {
		front = (byte)packet.func_148857_g().getByte("v");
	}
	
	private byte front;
	
	private static final int REDSTONE_AMOUNT = 4;
	private static final int STONE_AMOUNT = 8;
	
	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j) {
		if(i == SLOT_REDSTONE)
			return itemstack != null && itemstack.getItem() == Items.redstone;
		if(i == SLOT_STONE)
			return itemstack != null && itemstack.getItem() == Item.getItemFromBlock(Blocks.stone);
		return false;
	}
	
	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j) {
		return i == SLOT_OUT;
	}
	
	private final static int[] acc_slots = {SLOT_OUT, SLOT_REDSTONE, SLOT_STONE}; 
	@Override
	public int[] getAccessibleSlotsFromSide(int var1) {
		return acc_slots;
	}
	
	private int POWER_PER_TICK = 80; // RF/t cost
	private int TICKS_PER_OPERATION = 400;
	private int progress;
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("progress", progress);
		nbttagcompound.setByte("front", front);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		progress = nbttagcompound.getInteger("progress");
		front = nbttagcompound.getByte("front");
		if(front == 0)
			front = 2;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(worldObj.isRemote)
			return;
		
		if(has(SLOT_PHOTOMASK, RedLogicMod.photomaskItem, 1) && has(SLOT_REDSTONE, Items.redstone, REDSTONE_AMOUNT) && has(SLOT_STONE, Item.getItemFromBlock(Blocks.stone), STONE_AMOUNT) && notfull(SLOT_OUT)) {
			String className = ItemPhotomask.getClassName(inv.contents[SLOT_PHOTOMASK]);
			if(className == null) {
				// invalid photomask item
				inv.contents[SLOT_PHOTOMASK] = null;
				progress = 0;
			} else {
				
				if(energyConsumer.useEnergy(POWER_PER_TICK, POWER_PER_TICK) >= POWER_PER_TICK) {
					progress++;
				}
				
				if(progress >= TICKS_PER_OPERATION) {
					ItemStack outStack = ItemCustomCircuit.createItemStack(className);
					progress = 0;
					if(inv.contents[SLOT_OUT] == null)
						inv.contents[SLOT_OUT] = outStack;
					else if(ImmibisCore.areItemsEqual(inv.contents[SLOT_OUT], outStack))
						inv.contents[SLOT_OUT].stackSize++;
					else
						return;
					
					sub(SLOT_REDSTONE, REDSTONE_AMOUNT);
					sub(SLOT_STONE, STONE_AMOUNT);
				}
			}
		} else
			progress = 0;
	}
	
	@Override
	public void onPlaced(EntityLivingBase player, int unused) {
		Vec3 look = player.getLook(1.0f);
		
        double absx = Math.abs(look.xCoord);
        double absz = Math.abs(look.zCoord);
        
        if(absx > absz) {
        	if(look.xCoord < 0)
        		front = Dir.PX;
        	else
        		front = Dir.NX;
        } else {
        	if(look.zCoord < 0)
        		front = Dir.PZ;
        	else
        		front = Dir.NZ;
        }
	}
	
	@Override
	public boolean onBlockActivated(EntityPlayer player) {
		if(!worldObj.isRemote)
			player.openGui(RedLogicMod.instance, RedLogicMod.GUI_CHIP_FABRICATOR, worldObj, xCoord, yCoord, zCoord);
		return true;
	}

	public float getProgress() {
		return progress / (float)TICKS_PER_OPERATION;
	}

	public int getFront() {
		return front;
	}
}
