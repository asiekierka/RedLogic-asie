package mods.immibis.redlogic;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class RLNormalBlock extends Block {
	public static final int META_CLEANWALL = 0;
	public static final int META_CLEANFILTER = 1;
	
	public RLNormalBlock() {
		super(Material.iron);
		
		setHardness(5.0F);
		setResistance(10.0F);
		setStepSound(soundTypeMetal);
		setCreativeTab(CreativeTabs.tabBlock);
	}
	
	private IIcon[] icons;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		icons = new IIcon[16];
		icons[META_CLEANWALL] = reg.registerIcon("redlogic:cleanwall");
		icons[META_CLEANFILTER] = reg.registerIcon("redlogic:cleanfilter");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List list) {
		list.add(new ItemStack(this, 1, META_CLEANWALL));
		list.add(new ItemStack(this, 1, META_CLEANFILTER));
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return new ItemStack(this, 1, world.getBlockMetadata(x, y, z));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return icons[meta];
	}
}
