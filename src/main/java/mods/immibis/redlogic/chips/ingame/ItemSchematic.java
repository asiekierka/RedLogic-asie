package mods.immibis.redlogic.chips.ingame;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.chips.scanner.ScannedCircuit;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSchematic extends Item {
	public ItemSchematic() {
		super();
		
		setMaxStackSize(1);
		setUnlocalizedName("redlogic.schematic");
		setTextureName("redlogic:schematic");
	}
	
	private static File getSaveFolder(World world) {
		File f = world.getSaveHandler().getMapFileFromName("redlogic-schematic-cache");
		if(!f.exists())
			f.mkdirs();
		if(!f.isDirectory())
			throw new RuntimeException("Failed to create directory: "+f);
		return f;
	}
	
	@Override
	public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_) {
	}
	
	public static File getFile(World world, ItemStack stack) {
		if(stack.getItem() != RedLogicMod.schematicItem)
			return null;
		if(!stack.hasTagCompound())
			return null;
		if(!stack.stackTagCompound.hasKey("filename"))
			return null;
		String fn = stack.stackTagCompound.getString("filename");
		for(int k = 0; k < fn.length(); k++) {
			char ch = fn.charAt(k);
			if(!(ch >= 'a' && ch <= 'f') && !(ch >= 'A' && ch <= 'F') && !(ch >= '0' && ch <= '9'))
				return null;
		}
		return new File(getSaveFolder(world), fn);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer ply, List lines, boolean showIDs) {
		super.addInformation(stack, ply, lines, showIDs);
		
		if(showIDs && stack.stackTagCompound != null) {
			lines.add("File name: "+stack.stackTagCompound.getString("filename"));
		}
	}

	public static ItemStack createItemStack(String filename) {
		ItemStack st = new ItemStack(RedLogicMod.schematicItem);
		st.stackTagCompound = new NBTTagCompound();
		st.stackTagCompound.setString("filename", filename);
		return st;
	}
	
	public static ItemStack createItemStackWithNewFile(World world) throws IOException {
		File dir = getSaveFolder(world);
		while(true) {
			String name = String.format("%016X", System.nanoTime());
			File f = new File(dir, name);
			if(f.createNewFile())
				return createItemStack(name);
		}
	}

	public static ScannedCircuit loadCircuit(File f) throws IOException {
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
		try {
			return (ScannedCircuit)in.readObject();
		} catch (Exception e) {
			throw new IOException("Failed to load ScannedCircuit from file "+f, e);
		} finally {
			in.close();
		}
	}
	
}
