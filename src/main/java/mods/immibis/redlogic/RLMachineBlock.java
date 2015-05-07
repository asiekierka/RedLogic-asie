package mods.immibis.redlogic;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import mods.immibis.core.BlockCombined;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.api.chips.scanner.NodeType;
import mods.immibis.redlogic.chips.ingame.TileChipCompiler;
import mods.immibis.redlogic.chips.ingame.TileChipFabricator;
import mods.immibis.redlogic.chips.ingame.TileChipScanner;
import mods.immibis.redlogic.chips.ingame.TileIOMarker;

public class RLMachineBlock extends BlockCombined {
	public static final int META_CHIP_SCANNER = 0;
	public static final int META_IO_MARKER = 1;
	public static final int META_CHIP_COMPILER = 2;
	public static final int META_CHIP_FABRICATOR = 3;
	
	public RLMachineBlock() {
		super(Material.iron);
		
		setHardness(5.0F);
		setResistance(10.0F);
		setStepSound(soundTypeMetal);
		setCreativeTab(CreativeTabs.tabDecorations);
	}
	
	static int renderType = SidedProxy.instance.getUniqueBlockModelID("mods.immibis.redlogic.RLMachineBlockRenderStatic", true);

	@Override
	public int getRenderType() {
		return renderType;
	}
	
	private IIcon iChipScanner, iCompFront, iCompSide, iCompTop, iCompFrontOn, iCompTopOn, iFabSide, iFabFront, iFabTop, iChipScannerDir;
	private IIcon[] iInputPin, iOutputPin;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		iChipScanner = reg.registerIcon("redlogic:chipscanner");
		iChipScannerDir = reg.registerIcon("redlogic:chipscanner_dir");
		
		iInputPin = new IIcon[NodeType.values().length];
		iOutputPin = new IIcon[NodeType.values().length];
		for(NodeType nt : NodeType.values()) {
			iInputPin[nt.ordinal()] = reg.registerIcon("redlogic:chip/i_" + nt);
			iOutputPin[nt.ordinal()] = reg.registerIcon("redlogic:chip/o_" + nt);
		}
		
		iCompTop = reg.registerIcon("redlogic:chip/comp_top");
		iCompSide = reg.registerIcon("redlogic:chip/comp_side");
		iCompFront = reg.registerIcon("redlogic:chip/comp_front");
		iCompFrontOn = reg.registerIcon("redlogic:chip/comp_front_on");
		iCompTopOn = reg.registerIcon("redlogic:chip/comp_top_on");
		iFabSide = reg.registerIcon("redlogic:chip/fab_side");
		iFabTop = reg.registerIcon("redlogic:chip/fab_top");
		iFabFront = reg.registerIcon("redlogic:chip/fab_front");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		switch(par2) {
		case META_CHIP_SCANNER: return (par1 < 2 ? iChipScannerDir : iChipScanner);
		case META_IO_MARKER: return iInputPin[0];
		case META_CHIP_COMPILER: return (par1 == Dir.PY ? iCompTop : par1 == Dir.PZ ? iCompFront : iCompSide);
		case META_CHIP_FABRICATOR: return (par1 == Dir.PZ ? iFabFront : par1 == Dir.PY ? iFabTop : iFabSide);
		default: return null;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess par1iBlockAccess, int par2, int par3, int par4, int par5) {
		TileEntity te = par1iBlockAccess.getTileEntity(par2, par3, par4);
		if(te instanceof TileIOMarker)
			return (((TileIOMarker)te).isOutput() ? iOutputPin : iInputPin)[((TileIOMarker)te).getNodeType().ordinal()];
		if(te instanceof TileChipCompiler) {
			int front = ((TileChipCompiler)te).getFront();
			boolean on = ((TileChipCompiler)te).isRunning();
			return (par5 == Dir.PY ? (on ? iCompTopOn : iCompTop) : par5 == front ? (on ? iCompFrontOn : iCompFront) : iCompSide); 
		}
		if(te instanceof TileChipFabricator) {
			int front = ((TileChipFabricator)te).getFront();
			return (par5 == Dir.PY ? iFabTop : par5 == front ? iFabFront : iFabSide); 
		}
		return super.getIcon(par1iBlockAccess, par2, par3, par4, par5);
	}

	@Override
	public TileEntity getBlockEntity(int data) {
		switch(data) {
		case META_CHIP_SCANNER: return new TileChipScanner();
		case META_IO_MARKER: return new TileIOMarker();
		case META_CHIP_COMPILER: return new TileChipCompiler();
		case META_CHIP_FABRICATOR: return new TileChipFabricator();
		default: return null;
		}
	}

	@Override
	public void getCreativeItems(List<ItemStack> is) {
		is.add(new ItemStack(this, 1, META_CHIP_SCANNER));
		is.add(new ItemStack(this, 1, META_IO_MARKER));
		is.add(new ItemStack(this, 1, META_CHIP_COMPILER));
		is.add(new ItemStack(this, 1, META_CHIP_FABRICATOR));
	}
}

