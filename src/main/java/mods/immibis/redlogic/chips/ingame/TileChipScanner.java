package mods.immibis.redlogic.chips.ingame;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;
import mods.immibis.core.TileCombined;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.core.api.util.Colour;
import mods.immibis.core.api.util.XYZ;
import mods.immibis.redlogic.CommandDebug;
import mods.immibis.redlogic.RLMachineBlock;
import mods.immibis.redlogic.RLNormalBlock;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.api.chips.scanner.CircuitLayoutException;
import mods.immibis.redlogic.api.misc.ILampBlock;
import mods.immibis.redlogic.chips.scanner.CircuitScanner;
import mods.immibis.redlogic.chips.scanner.ScannedCircuit;

public class TileChipScanner extends TileCombined {
	
	private void doFinishScan() {
		if(numContainmentErrors > numScannedFilters / 4) {
			if(scanStartedBy != null)
				scanStartedBy.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.containmenterror", firstContainmentError.x, firstContainmentError.y, firstContainmentError.z).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			explode(firstContainmentError.x, firstContainmentError.y, firstContainmentError.z);
			return;
		}
		
		try {
			ScannedCircuit circuit = scanner.finishScan();
			
			//String className = CircuitCompiler.compile(circuit);
			
			//System.out.println("Scan and compile done, class name "+className);
			
			//worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+0.5, yCoord+1.5, zCoord, ItemCompiledCircuit.createItemStack(className)));
			
			ItemStack schematicStack = ItemSchematic.createItemStackWithNewFile(worldObj);
			File file = ItemSchematic.getFile(worldObj, schematicStack);
			
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
			try {
				out.writeObject(circuit);
			} finally {
				out.close();
			}
			
			worldObj.spawnEntityInWorld(new EntityItem(worldObj, xCoord+0.5, yCoord+1.5, zCoord, schematicStack));
		
		} catch(AssertionError e) {
			if(scanStartedBy != null) {
				scanStartedBy.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.exception1", e.toString()));
				scanStartedBy.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.exception2"));
			}
			e.printStackTrace();
			
		} catch(Exception e) {
			if(scanStartedBy != null) {
				scanStartedBy.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.exception1", e.toString()));
				scanStartedBy.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.exception2"));
			}
			e.printStackTrace();
			if(!TileChipScanner.class.desiredAssertionStatus()) {
				System.err.println("If you're a developer, you might get more information by enabling assertions.");
				System.err.println("(java option: '-ea:mods.immibis.redlogic...' without quotes)");
			}
			
		}
	}
	
	private boolean isCleanRoomWall(int x, int y, int z) {
		 Block block = worldObj.getBlock(x, y, z);
		 int meta = worldObj.getBlockMetadata(x, y, z);
		 if(block == RedLogicMod.plainBlock) 
			 return meta == RLNormalBlock.META_CLEANWALL || meta == RLNormalBlock.META_CLEANFILTER;
		 if(block == RedLogicMod.machineBlock)
			 return meta == RLMachineBlock.META_CHIP_SCANNER;
		 if(block instanceof ILampBlock) {
			 meta = ((ILampBlock)block).getColourWool(worldObj, x, y, z);
			 return meta == Colour.YELLOW.woolId() || meta == Colour.RED.woolId() || meta == Colour.ORANGE.woolId();
		 }
		 return false;
	}
	
	// coordinates of clean room interior, inclusive; initialized by findCleanRoom
	int crMinX, crMinY, crMinZ, crMaxX, crMaxY, crMaxZ;
	
	// scan progress. not saved; scan resets if chunk unloaded
	int scanTicks = -1; // ticks so far, -1 if not scanning
	int scanDir; // 0=X 1=Y 2=Z
	int scanLength; // ticks
	EntityPlayer scanStartedBy;
	int numScannedFilters;
	CircuitScanner scanner;
	XYZ firstContainmentError;
	int numContainmentErrors;
	int rotation = 0;
	
	private boolean findCleanRoom() {
		int x = xCoord;
		int y = yCoord-1;
		int z = zCoord;
		
		crMinX = crMaxX = x;
		crMinY = crMaxY = y;
		crMinZ = crMaxZ = z;
		
		while(true) {
			if(!worldObj.blockExists(crMinX, y, z))
				return false;
			if(isCleanRoomWall(crMinX, y, z))
				break;
			if(worldObj.getBlock(crMinX, y, z).isOpaqueCube())
				return false;
			crMinX--;
		}
		
		while(true) {
			if(!worldObj.blockExists(x, crMinY, z))
				return false;
			if(isCleanRoomWall(x, crMinY, z))
				break;
			if(worldObj.getBlock(x, crMinY, z).isOpaqueCube())
				return false;
			crMinY--;
		}
		
		while(true) {
			if(!worldObj.blockExists(x, y, crMinZ))
				return false;
			if(isCleanRoomWall(x, y, crMinZ))
				break;
			if(worldObj.getBlock(x, y, crMinZ).isOpaqueCube())
				return false;
			crMinZ--;
		}
		
		while(true) {
			if(!worldObj.blockExists(crMaxX, y, z))
				return false;
			if(isCleanRoomWall(crMaxX, y, z))
				break;
			if(worldObj.getBlock(crMaxX, y, z).isOpaqueCube())
				return false;
			crMaxX++;
		}
		
		while(true) {
			if(!worldObj.blockExists(x, crMaxY, z))
				return false;
			if(isCleanRoomWall(x, crMaxY, z))
				break;
			if(worldObj.getBlock(x, crMaxY, z).isOpaqueCube())
				return false;
			crMaxY++;
		}
		
		while(true) {
			if(!worldObj.blockExists(x, y, crMaxZ))
				return false;
			if(isCleanRoomWall(x, y, crMaxZ))
				break;
			if(worldObj.getBlock(x, y, crMaxZ).isOpaqueCube())
				return false;
			crMaxZ++;
		}
		
		crMinX++; crMinY++; crMinZ++;
		crMaxX--; crMaxY--; crMaxZ--;
		
		return true;
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(scanTicks >= 0) {
			if(!worldObj.isRemote && (scanTicks % TICKS_PER_SCAN_BLOCK) == 0) {
				// process layer
				int x, y, z;
				try {
					switch(scanDir) {
					case 0:
						x = crMinX + scanTicks / TICKS_PER_SCAN_BLOCK;
						for(y = crMinY; y <= crMaxY; y++)
							for(z = crMinZ; z <= crMaxZ && scanTicks >= 0; z++)
								processScanBlock(x, y, z);
						for(y = crMinY; y <= crMaxY && scanTicks >= 0; y++) {
							processEdgeBlock(x, y, crMinZ-1);
							processEdgeBlock(x, y, crMaxZ+1);
						}
						for(z = crMinZ - 1; z <= crMaxZ + 1 && scanTicks >= 0; z++) {
							processEdgeBlock(x, crMinY-1, z);
							processEdgeBlock(x, crMaxY+1, z);
						}
						break;
					case 1:
						y = crMinY + scanTicks / TICKS_PER_SCAN_BLOCK;
						for(x = crMinX; x <= crMaxX; x++)
							for(z = crMinZ; z <= crMaxZ && scanTicks >= 0; z++)
								processScanBlock(x, y, z);
						for(x = crMinX; x <= crMaxX && scanTicks >= 0; x++) {
							processEdgeBlock(x, y, crMinZ-1);
							processEdgeBlock(x, y, crMaxZ+1);
						}
						for(z = crMinZ - 1; z <= crMaxZ + 1 && scanTicks >= 0; z++) {
							processEdgeBlock(crMinX-1, y, z);
							processEdgeBlock(crMaxX+1, y, z);
						}
						break;
					case 2:
						z = crMinZ + scanTicks / TICKS_PER_SCAN_BLOCK;
						for(y = crMinY; y <= crMaxY; y++)
							for(x = crMinX; x <= crMaxX && scanTicks >= 0; x++)
								processScanBlock(x, y, z);
						for(y = crMinY; y <= crMaxY && scanTicks >= 0; y++) {
							processEdgeBlock(crMinX-1, y, z);
							processEdgeBlock(crMaxX+1, y, z);
						}
						for(x = crMinX - 1; x <= crMaxX + 1 && scanTicks >= 0; x++) {
							processEdgeBlock(x, crMinY-1, z);
							processEdgeBlock(x, crMaxY+1, z);
						}
						break;
					}
				} catch(CircuitLayoutException e) {
					if(scanStartedBy != null)
						scanStartedBy.addChatMessage(e.getDisplayMessage());
					else if(!SidedProxy.instance.isDedicatedServer())
						e.printStackTrace();
					abortScan();
				}
				
			}
			
			if(scanTicks == scanLength - 1 && !worldObj.isRemote) {
				// scan far end wall
				switch(scanDir) {
				case 0:
					for(int y = crMinY-1; y <= crMaxY-1; y++)
					for(int z = crMinZ-1; z <= crMaxZ-1 && scanTicks >= 0; z++) {
						processEdgeBlock(crMaxX+1, y, z);
					}
					break;
				case 1:
					for(int z = crMinZ-1; z <= crMaxZ-1; z++)
					for(int x = crMinX-1; x <= crMaxX-1 && scanTicks >= 0; x++) {
						processEdgeBlock(x, crMaxY+1, z);
					}
					break;
				case 2:
					for(int y = crMinY-1; y <= crMaxY-1; y++)
					for(int x = crMinX-1; x <= crMaxX-1 && scanTicks >= 0; x++) {
						processEdgeBlock(x, y, crMaxZ+1);
					}
					break;
				}
			}
			
			scanTicks++;
			if(scanTicks >= scanLength) {
				if(!worldObj.isRemote)
					doFinishScan();
				scanTicks = -1;
				firstContainmentError = null;
				scanStartedBy = null;
				worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
			}
			
			if(scanStartedBy != null && scanStartedBy.isDead)
				scanStartedBy = null;
		}
	}
	
	private void processEdgeBlock(int x, int y, int z) {
		if(!worldObj.blockExists(x, y, z)) {
			if(scanStartedBy != null)
				scanStartedBy.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.chunkloaderror", x,y,z).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			abortScan();
		} else if(!isCleanRoomWall(x, y, z)) {
			if(firstContainmentError == null)
				firstContainmentError = new XYZ(x, y, z);
			numContainmentErrors++;
		} else if(worldObj.getBlock(x, y, z) == RedLogicMod.plainBlock && worldObj.getBlockMetadata(x, y, z) == RLNormalBlock.META_CLEANFILTER) {
			numScannedFilters++;
		}
	}

	private void explode(int x, int y, int z) {
		worldObj.newExplosion(null, x+0.5, y+0.5, z+0.5, 2.0f, false, true);
	}

	private void processScanBlock(int x, int y, int z) throws CircuitLayoutException {
		scanner.scanBlock(x, y, z);
	}
	
	private static final int MAX_SCAN_BLOCKS_TOTAL = 50*50*50;

	private void abortScan() {
		scanTicks = -2;
		scanStartedBy = null;
		
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
	}

	private void startScan() {
		int xlen = crMaxX - crMinX + 1, ylen = crMaxY - crMinY + 1, zlen = crMaxZ - crMinZ + 1;
		
		TICKS_PER_SCAN_BLOCK = CommandDebug.FAST_SCAN ? 1 : NORMAL_TICKS_PER_SCAN_BLOCK;
		
		if(xlen * ylen * zlen > MAX_SCAN_BLOCKS_TOTAL) {
			if(scanStartedBy != null)
				scanStartedBy.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.toobig", xlen*ylen*zlen, MAX_SCAN_BLOCKS_TOTAL).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			return;
		}
		
		if(xlen < 0 || ylen < 0 || zlen < 0) {
			if(scanStartedBy != null)
				scanStartedBy.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.invalidrange").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			return;
		}
		
		if(xlen > ylen && xlen > zlen) {
			scanDir = 0;
			scanLength = xlen;
		} else if(ylen > zlen) {
			scanDir = 1;
			scanLength = ylen;
		} else {
			scanDir = 2;
			scanLength = zlen;
		}
		
		scanTicks = 0;
		scanLength *= TICKS_PER_SCAN_BLOCK;
		numScannedFilters = 0;
		numContainmentErrors = 0;
		firstContainmentError = null;
		
		if(!worldObj.isRemote) {
			switch(scanDir) {
			case 0:
				for(int y = crMinY-1; y <= crMaxY-1; y++)
				for(int z = crMinZ-1; z <= crMaxZ-1 && scanTicks >= 0; z++) {
					processEdgeBlock(crMinX-1, y, z);
				}
				break;
			case 1:
				for(int z = crMinZ-1; z <= crMaxZ-1; z++)
				for(int x = crMinX-1; x <= crMaxX-1 && scanTicks >= 0; x++) {
					processEdgeBlock(x, crMinY-1, z);
				}
				break;
			case 2:
				for(int y = crMinY-1; y <= crMaxY-1; y++)
				for(int x = crMinX-1; x <= crMaxX-1 && scanTicks >= 0; x++) {
					processEdgeBlock(x, y, crMinZ-1);
				}
				break;
			}
		}
		if(scanTicks < 0)
			return;
		
		scanner = new CircuitScanner(worldObj, crMinX, crMinY, crMinZ, crMaxX, crMaxY, crMaxZ, rotation);
		
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		worldObj.markTileEntityChunkModified(xCoord, yCoord, zCoord, this);
	}
	
	static final int NORMAL_TICKS_PER_SCAN_BLOCK = 12;
	
	private int TICKS_PER_SCAN_BLOCK = NORMAL_TICKS_PER_SCAN_BLOCK;
	
	@Override
	public boolean onBlockActivated(EntityPlayer player) {
		if(worldObj.isRemote)
			return true;
		
		if(findCleanRoom()) {
			player.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.roomcoords", crMinX, crMinY, crMinZ, crMaxX, crMaxY, crMaxZ));
			scanStartedBy = player;
			startScan();
		}
		else
			player.addChatMessage(new ChatComponentTranslation("redlogic.chipscanner.cantfindroom"));
		
		return true;
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("-x", crMinX);
		tag.setInteger("-y", crMinY);
		tag.setInteger("-z", crMinZ);
		tag.setInteger("+x", crMaxX);
		tag.setInteger("+y", crMaxY);
		tag.setInteger("+z", crMaxZ);
		tag.setInteger("st", scanTicks);
		tag.setInteger("sd", scanDir);
		tag.setInteger("sl", scanLength);
		tag.setInteger("rot", rotation);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}
	
	@Override
	public void onDataPacket(S35PacketUpdateTileEntity p) {
		NBTTagCompound data = p.func_148857_g();
		crMinX = data.getInteger("-x");
		crMinY = data.getInteger("-y");
		crMinZ = data.getInteger("-z");
		crMaxX = data.getInteger("+x");
		crMaxY = data.getInteger("+y");
		crMaxZ = data.getInteger("+z");
		scanTicks = data.getInteger("st");
		scanDir = data.getInteger("sd");
		scanLength = data.getInteger("sl");
		rotation = data.getInteger("rot");
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
		rotation = par1nbtTagCompound.getInteger("rot");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		par1nbtTagCompound.setInteger("rot", rotation);
	}
	
	public void rotate() {
		rotation = (rotation + 1) & 3;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if(scanTicks < 0)
			return super.getRenderBoundingBox();
		return INFINITE_EXTENT_AABB;
	}

	public int getRotation() {
		return rotation;
	}

	public void setInitialRotation(EntityPlayer player) {
		Vec3 look = player.getLook(1.0f);
			
        double absx = Math.abs(look.xCoord);
        double absz = Math.abs(look.zCoord);
        
        if(absx > absz)
        	if(look.xCoord < 0)
        		rotation = 3;
        	else
        		rotation = 1;
        else
        	if(look.zCoord < 0)
        		rotation = 0;
        	else
        		rotation = 2;
        
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}
