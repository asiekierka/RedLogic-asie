package mods.immibis.redlogic.chips.ingame;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.Vec3;
import mods.immibis.core.api.util.Dir;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.redlogic.chips.compiler.CircuitCompiler;

public class TileChipCompiler extends TilePoweredBase {
	// slot 0: schematic
	// slot 1: photomask
	
	private int POWER_PER_TICK = 240; // RF/t cost
	public final int BYTES_PER_TICK = 1024;
	
	public TileChipCompiler() {
		super(2, "chip compiler");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		
		nbttagcompound.setByte("front", front);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		front = nbttagcompound.getByte("front");
	}
	
	byte visualState;
	byte front;
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setByte("v", visualState);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}
	
	@Override
	public void onDataPacket(S35PacketUpdateTileEntity packet) {
		this.visualState = (byte)packet.func_148857_g().getByte("v");
		this.front = (byte)(this.visualState >> 3);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	// not saved in NBT
	private int ticksLeft = 0;
	private boolean isRunning;
	private CompileThread compileThread;
	private String errorMessage = null;
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		if(worldObj.isRemote)
			return;
		
		int oldVS = visualState;
		visualState = (byte)((inv.contents[0] != null ? 1 : 0) | (inv.contents[1] != null ? 2 : 0) | (isRunning ? 4 : 0) | (front << 3));
		if(visualState != oldVS)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		
		if(inv.contents[0] != null && inv.contents[0].getItem() == RedLogicMod.schematicItem && inv.contents[1] == null && errorMessage == null) {
			if(!isRunning) {
				File file = ItemSchematic.getFile(worldObj, inv.contents[0]);
				if(file != null) {
					ticksLeft = (int)(file.length() / BYTES_PER_TICK);
					isRunning = true;
					compileThread = new CompileThread(file);
				}
			}
			
			if(ticksLeft > 0 && energyConsumer.useEnergy(POWER_PER_TICK, POWER_PER_TICK) >= POWER_PER_TICK) {
				ticksLeft--;
			}
			
			if(ticksLeft == 0 && compileThread.isDone()) {
				
				String className = compileThread.getResult();
				if(className != null)
					inv.contents[1] = ItemPhotomask.createItemStack(className);
				else {
					String error;
					if (compileThread.exception != null) {
						error = compileThread.exception.toString();
					} else {
						error = "Unknown error";
					}
					
					EntityPlayer ply = (activatingPlayer == null ? null : activatingPlayer.get());
					if (ply != null)
						ply.addChatMessage(new ChatComponentTranslation("redlogic.chipcompiler.error", error));
					
					isRunning = false;
					errorMessage = error; // don't keep restarting the process
				}
			}
		} else {
			if(isRunning) {
				compileThread.stop();
				compileThread = null;
			}
			isRunning = false;
		}
	}
	
	private WeakReference<EntityPlayer> activatingPlayer;
	
	@Override
	public boolean onBlockActivated(EntityPlayer ply) {
		if(worldObj.isRemote)
			return true;
		
		activatingPlayer = new WeakReference<>(ply);
		
		ItemStack holding = ply.getCurrentEquippedItem();
		
		if(holding == null) {
			if(isRunning && errorMessage == null) {
				ply.addChatMessage(new ChatComponentTranslation("redlogic.chipcompiler.status", new Object[]{ticksLeft}));
			} else if(inv.contents[0] != null) {
				if(errorMessage != null) {
					ply.addChatMessage(new ChatComponentTranslation("redlogic.chipcompiler.error", errorMessage));
					errorMessage = null;
				}
				ply.inventory.setInventorySlotContents(ply.inventory.currentItem, inv.contents[0]);
				inv.contents[0] = null;
			} else if(inv.contents[1] != null) {
				ply.inventory.setInventorySlotContents(ply.inventory.currentItem, inv.contents[1]);
				inv.contents[1] = null;
			}
			return true;
		}
		
		if(holding.getItem().equals(RedLogicMod.schematicItem) && inv.contents[0] == null) {
			inv.contents[0] = holding;
			ply.inventory.setInventorySlotContents(ply.inventory.currentItem, null);
		}
		
		return true;
	}

	public boolean isRunning() {
		return (visualState & 4) != 0;
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
	
	private static class CompileThread {
		private File schematicFile;
		
		private class Task implements Callable<String> {
			@Override
			public String call() throws Exception {
				return CircuitCompiler.compile(ItemSchematic.loadCircuit(schematicFile));
			}
		}
		
		private FutureTask<String> future;
		private Thread thread;
		Throwable exception;
		
		public CompileThread(File schematicFile) {
			this.schematicFile = schematicFile;
			this.future = new FutureTask<String>(new Task());
			this.thread = new Thread(this.future);
			this.thread.setName("RedLogic compiling "+schematicFile.getName());
			this.thread.setPriority(Thread.MIN_PRIORITY);
			this.thread.setDaemon(true);
			this.thread.start();
		}
		
		public void stop() {
			this.thread.interrupt();
		}

		private boolean isDone() {return future.isDone();}
		private String getResult() {
			try {
				return future.get();
			} catch(ExecutionException e) {
				this.exception = e.getCause();
				new Exception("Schematic compiling failed", e).printStackTrace();
				return null;
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
	}

	public int getFront() {
		return front;
	}
}
