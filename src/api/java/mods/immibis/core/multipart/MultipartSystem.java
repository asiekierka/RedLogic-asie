package mods.immibis.core.multipart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mods.immibis.core.api.APILocator;
import mods.immibis.core.api.multipart.ICoverSystem;
import mods.immibis.core.api.multipart.IMultipartSystem;
import mods.immibis.core.api.multipart.IMultipartTile;
import mods.immibis.core.api.multipart.IPartContainer2;
import mods.immibis.core.api.multipart.PartBreakEvent;
import mods.immibis.core.api.multipart.PartCoordinates;
import mods.immibis.core.api.porting.SidedProxy;
import mods.immibis.core.util.SynchronizedWeakIdentityListMap;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MultipartSystem implements IMultipartSystem {
	// [0] = non-3d render type
	// [1] = 3d render type
	public static int[] multipartRenderType;
	
	@SideOnly(Side.CLIENT)
	public static IIcon transparentIcon;
	
	public void init() {
		multipartRenderType = new int[2];
		multipartRenderType[0] = RenderingRegistry.getNextAvailableRenderId();
		multipartRenderType[1] = RenderingRegistry.getNextAvailableRenderId();
		
		SidedProxy.instance.createSidedObject("mods.immibis.core.multipart.ClientProxy", null);
		
		MinecraftForge.EVENT_BUS.register(new EventHandler());
	}
	
	// must be public
	public static class EventHandler {
		@SubscribeEvent
		@SideOnly(Side.CLIENT)
		public void onTextureStitch(TextureStitchEvent.Pre evt) {
			if(evt.map.getTextureType() == 0)
				transparentIcon = evt.map.registerIcon("immibis_core:transparent");
		}
	}	
	
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getTransparentIcon() {
		return transparentIcon;
	}
	
	
	
	
	////////// PART BREAKING //////////
	
	/** Maps players to the part they are currently breaking.
	 * If the player is not currently breaking a part, their value is undefined
	 * (they may or may not have an entry in the map)
	 */
	private SynchronizedWeakIdentityListMap<EntityPlayer, PartCoordinates> breaking_part = new SynchronizedWeakIdentityListMap<EntityPlayer, PartCoordinates>();
	
	// only call on client
	private void playPartDestroyEffect(Object pc, int part) {
		if(pc instanceof IPartContainer2)
			((IPartContainer2)pc).addPartDestroyEffects(part, Minecraft.getMinecraft().effectRenderer);
	}
	
	@Override
	public boolean onRemoveBlockByPlayer(World w, EntityPlayer ply, int x, int y, int z) {
		
		if(ply.worldObj.isRemote && ply.capabilities.isCreativeMode)
			updateBreakingPart(x, y, z);
		
		
		// remove the part the player was breaking
		PartCoordinates coord = getBreakingPart(ply);
		breaking_part.remove(ply);
		
		if(coord == null || coord.x != x || coord.y != y || coord.z != z)
			return false;
		
		TileEntity te = w.getTileEntity(x, y, z);
		
		if(!(te instanceof IMultipartTile)) {
			return false;
		}
		
		IMultipartTile tile = (IMultipartTile)te;
		
		PartBreakEvent evt = new PartBreakEvent(w, coord, ply);
		MinecraftForge.EVENT_BUS.post(evt);
		if(evt.isCanceled()) {
			return false;
		}
		
		if(ply.worldObj.isRemote) {
			
			sendDigFinish(coord);
			
			// client-side prediction - no drops, and send a dig finish packet.
			if(!coord.isCoverSystemPart) {
				playPartDestroyEffect(tile, coord.part);
				tile.removePartByPlayer(ply, coord.part);
			} else {
				playPartDestroyEffect(tile.getCoverSystem(), coord.part);
				tile.getCoverSystem().removePartByPlayer(ply, coord.part);
			}
		
		} else {
			broken_parts.put(ply, coord);
			
			if(!coord.isCoverSystemPart)
				lastDrop = tile.removePartByPlayer(ply, coord.part);
			else
				lastDrop = tile.getCoverSystem().removePartByPlayer(ply, coord.part);
			
			if(ply.capabilities.isCreativeMode)
				lastDrop = null;
		}
		
		return true;
	}
	
	@Override
	public PartCoordinates getBreakingPart(EntityPlayer ply) {
		return breaking_part.get(ply);
	}
	
	@Override
	public void onBlockClicked(World w, int x, int y, int z, EntityPlayer ply) {
		if(w.isRemote)
			// ensures a PacketMicroblockDigStart will be sent immediately
			breaking_part.remove(ply);
	}
	
	@SideOnly(Side.CLIENT)
	private void sendDigStart() {
		PartCoordinates coord = getBreakingPart(Minecraft.getMinecraft().thePlayer);
		if(coord != null)
			APILocator.getNetManager().sendToServer(new PacketMultipartDigStart(coord));
	}
	
	// client only
	private PartCoordinates tracePlayerDirection(EntityPlayer ply) {
		MovingObjectPosition ray = ply.rayTrace(SidedProxy.instance.getPlayerReach(ply), 0);
		if(ray == null || ray.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
			return null;
		if(!SubhitValues.isCoverSystem(ray.subHit))
			return new PartCoordinates(ray.blockX, ray.blockY, ray.blockZ, SubhitValues.getTilePartIndex(ray.subHit), false);
		else
			return new PartCoordinates(ray.blockX, ray.blockY, ray.blockZ, SubhitValues.getCSPartIndex(ray.subHit), true);
	}
	
	@Override
	public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer ply, int side, float hx, float hy, float hz) {
		if(w.isRemote) {
			PartCoordinates part = tracePlayerDirection(ply);
		}
		return false;
	}
	
	/**
	 * @param x The X coordinate of the block the player should be breaking.
	 * @param y The Y coordinate of the block the player should be breaking.
	 * @param z The Z coordinate of the block the player should be breaking.
	 * @return True if the player is breaking a valid part of this block. 
	 */
	@SideOnly(Side.CLIENT)
	private boolean updateBreakingPart(int x, int y, int z) {
		
		//System.out.println("updateBreakingPart "+x+","+y+","+z);
		
		EntityPlayer ply = Minecraft.getMinecraft().thePlayer;
		PartCoordinates old = getBreakingPart(ply);
		
		MovingObjectPosition ray = ply.rayTrace(SidedProxy.instance.getPlayerReach(ply), 0);
		PartCoordinates _new = null;
		if(ray == null || ray.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || ray.blockX != x || ray.blockY != y || ray.blockZ != z) {
			breaking_part.remove(ply);
			
		} else {
			if(!SubhitValues.isCoverSystem(ray.subHit))
				breaking_part.put(ply, _new = new PartCoordinates(x, y, z, SubhitValues.getTilePartIndex(ray.subHit), false));
			else
				breaking_part.put(ply, _new = new PartCoordinates(x, y, z, SubhitValues.getCSPartIndex(ray.subHit), true));
		}
		
		boolean changed = (old == null && _new != null) || (old != null && !old.equals(_new));
		
		//if(changed) // always send update, since it's not always synced
			sendDigStart();
		if(changed)
			resetBreakProgress(ply);
		
		return _new != null;
	}
	
	private static void resetBreakProgress(EntityPlayer ply) {
		// Need to reset the block damage, but that doesn't seem to be possible
		// Even RP2's covers don't do that
		// TODO: this was not edited since 1.2.5, is it possible now?
		/*PlayerController pc = ModLoader.getMinecraftInstance().playerController;
		pc.resetBlockRemoving();
		pc.updateController();*/
	}
	
	void setBreakingPart(EntityPlayer source, PartCoordinates part) {
		if(part == null)
			breaking_part.remove(source);
		else
			breaking_part.put(source, part);
		
		if(source.capabilities.isCreativeMode) {
			onRemoveBlockByPlayer(source.worldObj, source, part.x, part.y, part.z);
		}
		
		//for(EntityPlayer pl : (List<EntityPlayer>)source.worldObj.playerEntities)
		//	APILocator.getNetManager().sendToClient(new PacketUpdateBreakingPart(part), pl);
	}

	public Iterable<Map.Entry<EntityPlayer, PartCoordinates>> getBreakingParts() {
		return breaking_part.entries();
	}
	
	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer ply, World world, int x, int y, int z) {
		if(world.isRemote)
			updateBreakingPart(x, y, z);
		
		PartCoordinates part = getBreakingPart(ply);
		if(part == null || part.x != x || part.y != y || part.z != z) {
			return 0;
		}
			
		TileEntity te = world.getTileEntity(x, y, z);
		if(te == null || !(te instanceof IMultipartTile)) {
			return 0.01f;
		}
		
		else if(!part.isCoverSystemPart) {
			return ((IMultipartTile)te).getPlayerRelativePartHardness(ply, part.part);
		
		} else {
			ICoverSystem ci = ((IMultipartTile)te).getCoverSystem();
			return ci == null ? -1 : ci.getPlayerRelativePartHardness(ply, part.part);
		}
	}
	
	////////// DROPS //////////
	private List<ItemStack> lastDrop = null;

	@Override
	public final ArrayList<ItemStack> getDrops() {
		if(lastDrop == null)
			return new ArrayList<ItemStack>();
		
		ArrayList<ItemStack> rv = new ArrayList<ItemStack>(lastDrop);
		lastDrop = null;
		return rv;
	}
	
	
	
	
	
	////////// RENDER INTERCEPTION //////////
	static boolean useWrappedRenderType = false;

	@Override
	public int overrideRenderType(int base, boolean inv3d) {
		return useWrappedRenderType || multipartRenderType == null ? base : multipartRenderType[inv3d?1:0];
	}
	
	@Override
	public void renderInvBlockUsingOverriddenRenderType(RenderBlocks render, Block block, int meta) {
		useWrappedRenderType = true;
		render.renderBlockAsItem(block, meta, 1);
		useWrappedRenderType = false;
	}
	
	@Override
	public boolean renderBlockInWorldUsingOverriddenRenderType(RenderBlocks render, Block block, int x, int y, int z) {
		useWrappedRenderType = true;
		boolean result = render.renderBlockByRenderType(block, x, y, z);
		useWrappedRenderType = false;
		return result;
	}
	
	
	
	
	
	
	
	
	////////// DIG FINISH PREDICTION //////////
	
	/** Maps players to the part they most recently broke - used for checking dig finish packets.
	 */
	private SynchronizedWeakIdentityListMap<EntityPlayer, PartCoordinates> broken_parts = new SynchronizedWeakIdentityListMap<EntityPlayer, PartCoordinates>();
	
	public boolean didClientJustBreakPart(EntityPlayer ply, PartCoordinates coord) {
		PartCoordinates broke = broken_parts.get(ply);
		broken_parts.remove(ply);
		
		return broke != null && broke.equals(coord);
	}
	
	@SideOnly(Side.CLIENT)
	private static void sendDigFinish(PartCoordinates coord) {
		APILocator.getNetManager().sendToServer(new PacketMultipartDigFinish(coord));
	}
	
	

}
