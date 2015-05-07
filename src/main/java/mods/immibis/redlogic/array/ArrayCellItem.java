package mods.immibis.redlogic.array;

import java.util.Collection;
import java.util.Collections;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.IMicroblockSupporterTile;
import mods.immibis.microblocks.api.MicroblockAPIUtils;
import mods.immibis.microblocks.api.Part;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class ArrayCellItem extends ItemBlock {
	public ArrayCellItem(Block block) {
		super(block);
		setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack is) {
		if(is.getItemDamage() < 0 || is.getItemDamage() >= ArrayCellType.VALUES.length)
			return "item.redlogic.invalid";
		else
			return "tile.redlogic.array." + is.getItemDamage();
	}
	
	public boolean canMergeIntoMicroblockContainer(World w, int x, int y, int z, int side, ItemStack stack) {
		Block mbcb = MicroblockAPIUtils.getMicroblockContainerBlock();
		if(mbcb != null) {
			Block block = w.getBlock(x, y, z);
			if(block == mbcb) {
				ArrayCellTile testTile = new ArrayCellTile();
				testTile.init(ArrayCellType.VALUES[stack.getItemDamage()], side, (side + 2) % 6);
				for(Part p : ((IMicroblockSupporterTile)w.getTileEntity(x, y, z)).getCoverSystem().getAllParts())
					if(testTile.isPlacementBlocked(p.type, p.pos))
						return false;
				return true;
			}
		}
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean func_150936_a(World w, int x, int y, int z, int side, EntityPlayer par6EntityPlayer, ItemStack par7ItemStack) {
		Block mbcb = MicroblockAPIUtils.getMicroblockContainerBlock();
		if(mbcb != null) {
			ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side];
			if(canMergeIntoMicroblockContainer(w, x+fd.offsetX, y+fd.offsetY, z+fd.offsetZ, side^1, par7ItemStack))
				return true;
		}
		return super.func_150936_a(w, x, y, z, side, par6EntityPlayer, par7ItemStack);
	}
	
	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		Block block = par3World.getBlock(par4, par5, par6);

        if (block.equals(Blocks.snow_layer) && (par3World.getBlockMetadata(par4, par5, par6) & 7) < 1)
        {
            par7 = 1;
        }
        else if (block != Blocks.vine && block != Blocks.tallgrass && block != Blocks.deadbush
                && (block == null || !block.isReplaceable(par3World, par4, par5, par6)))
        {
            if (par7 == 0)
            {
                --par5;
            }

            if (par7 == 1)
            {
                ++par5;
            }

            if (par7 == 2)
            {
                --par6;
            }

            if (par7 == 3)
            {
                ++par6;
            }

            if (par7 == 4)
            {
                --par4;
            }

            if (par7 == 5)
            {
                ++par4;
            }
        }

        if (par1ItemStack.stackSize == 0)
        {
            return false;
        }
        else if (!par2EntityPlayer.canPlayerEdit(par4, par5, par6, par7, par1ItemStack))
        {
            return false;
        }
        else if (par3World.canPlaceEntityOnSide(Block.getBlockFromItem(this), par4, par5, par6, false, par7, par2EntityPlayer, par1ItemStack) || canMergeIntoMicroblockContainer(par3World, par4, par5, par6, par7^1, par1ItemStack))
        {
            Block blockk = Block.getBlockFromItem(this);
            int j1 = this.getMetadata(par1ItemStack.getItemDamage());
            int k1 = blockk.onBlockPlaced(par3World, par4, par5, par6, par7, par8, par9, par10, j1);

            if (placeBlockAt(par1ItemStack, par2EntityPlayer, par3World, par4, par5, par6, par7, par8, par9, par10, k1))
            {
                par3World.playSoundEffect((double)((float)par4 + 0.5F), (double)((float)par5 + 0.5F), (double)((float)par6 + 0.5F), block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
                --par1ItemStack.stackSize;
            }

            return true;
        }
        else
        {
            return false;
        }
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
		int meta = stack.getItemDamage();
		if(meta < 0 || meta >= ArrayCellType.VALUES.length)
			return false;
		
		if(!ArrayCellTile.checkCanStay(world, x, y, z, side^1))
			return false;
		
		Block mbcb = MicroblockAPIUtils.getMicroblockContainerBlock();
		
		Collection<Part> parts = Collections.emptyList();
		
		if(mbcb != null && world.getBlock(x, y, z) == mbcb) {
			parts = ((IMicroblockSupporterTile)world.getTileEntity(x, y, z)).getCoverSystem().getAllParts();
		}
		
		if(!super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata))
			return false;
		
		int front = (side + 2) % 6; // unpredictable direction not parallel to side
				
		Vec3 look = player.getLook(1.0f);
        double absx = Math.abs(look.xCoord);
        double absy = Math.abs(look.yCoord);
        double absz = Math.abs(look.zCoord);
		switch(side) {
		case Dir.PX: case Dir.NX:
			if(absy > absz)
				front = look.yCoord > 0 ? Dir.PY : Dir.NY;
			else
				front = look.zCoord > 0 ? Dir.PZ : Dir.NZ;
			break;
		case Dir.PY: case Dir.NY:
			if(absx > absz)
				front = look.xCoord > 0 ? Dir.PX : Dir.NX;
			else
				front = look.zCoord > 0 ? Dir.PZ : Dir.NZ;
			break;
		case Dir.PZ: case Dir.NZ:
			if(absy > absx)
				front = look.yCoord > 0 ? Dir.PY : Dir.NY;
			else
				front = look.xCoord > 0 ? Dir.PX : Dir.NX;
			break;
		}
		
		if(world.getBlock(x,y,z) instanceof ArrayCellBlock) {
			ArrayCellTile te = (ArrayCellTile)world.getTileEntity(x, y, z);
			te.init(ArrayCellType.VALUES[meta], side ^ 1, front);
			for(Part p : parts)
				te.getCoverSystem().addPart(p);
			world.notifyBlocksOfNeighborChange(x, y, z, Block.getBlockFromItem(this));
			world.notifyBlockOfNeighborChange(x, y, z, Block.getBlockFromItem(this));
		}

		return true;
	}
	
	@Override
	public int getMetadata(int par1) {
		return par1;
	}
}
