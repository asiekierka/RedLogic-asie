package mods.immibis.redlogic.chips.ingame;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.redlogic.RedLogicMod;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemChip extends Item {

	public ItemChip() {
		super();
		
		setMaxStackSize(64);
		setUnlocalizedName("redlogic.old-circuit");
		setTextureName("redlogic:chip");
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer ply, List lines, boolean showIDs) {
		super.addInformation(stack, ply, lines, showIDs);
		
		lines.add(I18n.format("item.redlogic.old-circuit.line1", new Object[0]));
		lines.add(I18n.format("item.redlogic.old-circuit.line2", new Object[0]));
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		ItemStack n = new ItemStack(RedLogicMod.customCircuitBlock, 1, par1ItemStack.getItemDamage());
		n.setTagCompound(par1ItemStack.getTagCompound());
		return n;
	}
	
	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer ply, World w, int x, int y, int z, int side, float subX, float subY, float subZ) {
		// TODO: Cannot replace items in stacks anymore
		return true;
	}

}
