package mods.immibis.redlogic.interaction;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.immibis.core.api.util.Dir;
import mods.immibis.microblocks.api.EnumAxisPosition;
import mods.immibis.microblocks.api.EnumPosition;
import mods.immibis.microblocks.api.IMicroblockIntegratedTile;
import mods.immibis.microblocks.api.PartType;
import mods.immibis.redlogic.RedLogicMod;
import mods.immibis.microblocks.api.util.TileCoverableMultipartBase;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.util.ForgeDirection;

public class TileLumarButton extends TileCoverableMultipartBase implements IMicroblockIntegratedTile {
	
	private static enum LightMode {
		Default, Inverted, Always;
		
		public static final LightMode[] VALUES = values();
	}
	
	private boolean initialized = false;
	private boolean pressed, lit;
	private byte side;
	private byte colour;
	private byte pressTicks;
	private LightMode lightMode = LightMode.Default;
	private LumarButtonType type = LumarButtonType.Normal;
	private LumarButtonModel model = LumarButtonModel.Button;
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		
		lit = tag.getBoolean("lit");
		pressed = tag.getBoolean("pressed");
		side = tag.getByte("side");
		colour = tag.getByte("colour");
		pressTicks = tag.getByte("pressTicks");
		
		LumarButtonType[] types = LumarButtonType.VALUES;
		type = types[tag.getByte("type") % types.length];
		
		LumarButtonModel[] models = LumarButtonModel.VALUES;
		model = models[tag.getByte("model") % models.length];
		
		lightMode = LightMode.VALUES[tag.getByte("light") % LightMode.VALUES.length];

		initialized = true;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		
		tag.setBoolean("lit", lit);
		tag.setBoolean("pressed", pressed);
		tag.setByte("light", (byte)lightMode.ordinal());
		tag.setByte("side", side);
		tag.setByte("colour", colour);
		tag.setByte("type", (byte)type.ordinal());
		tag.setByte("model", (byte)model.ordinal());
		tag.setByte("pressTicks", (byte)pressTicks);
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();
		
		if(worldObj.isRemote)
			return;
		
		if(pressTicks > 0) {
			switch(type) {
				case Normal:
					--pressTicks;
					if(pressTicks == 2)
						updateNeighbours();
					if(--pressTicks == 0)
						setPressed(false, isReceivingPower());
					break;
				case Latch:
					--pressTicks;
					if(pressTicks == 2)
						updateNeighbours();
					if(pressTicks == 0)
						setPressed(isReceivingPower(), isReceivingPower());
					break;
				default:
					break;
			}
		}
	}
	
	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setByte("a", (byte)(colour | (side << 4) | (pressed ? 0x80 : 0)));
		tag.setByte("b", (byte)(type.ordinal() | (isLit() ? 0x80 : 0)));
		tag.setByte("c", (byte)model.ordinal());
		if(getCoverSystem() != null)
			tag.setByteArray("M", getCoverSystem().writeDescriptionBytes());
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		NBTTagCompound data = pkt.func_148857_g();
		byte a = data.getByte("a");
		byte b = data.getByte("b");
		byte c = data.getByte("c");
		colour = (byte)(a & 15);
		side = (byte)((a >> 4) & 7);
		pressed = (a & 0x80) != 0;
		lit = (b & 0x80) != 0;
		type = LumarButtonType.VALUES[b & 0x7F];
		model = LumarButtonModel.VALUES[c];
		
		if(getCoverSystem() != null)
			getCoverSystem().readDescriptionBytes(data.getByteArray("M"), 0);
		
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
	
	public static LumarButtonType getTypeFromDamage(int dmg) {
		LumarButtonType[] types = LumarButtonType.VALUES;
		return types[((dmg >> 4) & 15) % types.length];
	}
	
	public static LumarButtonModel getModelFromDamage(int dmg) {
		LumarButtonModel[] models = LumarButtonModel.VALUES;
		return models[((dmg >> 8) & 15) % models.length];
	}

	public void initializeFromDamageValue(int dmg, int side) {
		pressed = false;
		colour = (byte)(dmg & 15);
		this.side = (byte)(side^1);
		this.type = getTypeFromDamage(dmg);
		this.model = getModelFromDamage(dmg);
		
		initialized = true;
		
		updateNeighbours();
		onNeighbourChange();
	}
	
	public static int getDamageValue(int colour, LumarButtonType type, LumarButtonModel model) {
		return colour | (type.ordinal() << 4) | (model.ordinal() << 8);
	}

	public boolean isProperlyAttached() {
		return !initialized || BlockLumarButton.canBeAttached(worldObj, xCoord, yCoord, zCoord, side^1, model, true);
	}

	public void press() {
		if(!pressed || type == LumarButtonType.Toggle)
			pressAndHold();
	}
	
	public void pressAndHold() {
		switch(type) {
		case Normal:
			pressTicks = 22;
			setPressed(true, false);
			break;
		case Latch:
			pressTicks = 5;
			setPressed(true, true);
			break;
		case SelfLatch:
			setPressed(true, true);
			break;
		case Toggle:
			setPressed(!pressed, !pressed);
			break;
		}
	}

	public AxisAlignedBB getBoundingBox() {
		return getBoundingBox(side, pressed, model);
	}
	
	public static AxisAlignedBB getBoundingBox(int side, boolean pressed, LumarButtonModel model) {
		AxisAlignedBB bb = null;
		
		final double hsize = model.hsize, vsize = model.vsize;
		final double hmin = 0.5-hsize, hmax = 0.5+hsize, vmin=0.5-vsize, vmax=0.5+vsize; 
		final double thick = (pressed ? 1/16f : 2/16f);
		
		switch(side) {
		case Dir.NX: bb = AxisAlignedBB.getBoundingBox(0, vmin, hmin, thick, vmax, hmax); break;
		case Dir.NY: bb = AxisAlignedBB.getBoundingBox(hmin, 0, vmin, hmax, thick, vmax); break;
		case Dir.NZ: bb = AxisAlignedBB.getBoundingBox(hmin, vmin, 0, hmax, vmax, thick); break;
		case Dir.PX: bb = AxisAlignedBB.getBoundingBox(1-thick, vmin, hmin, 1, vmax, hmax); break;
		case Dir.PY: bb = AxisAlignedBB.getBoundingBox(hmin, 1-thick, vmin, hmax, 1, vmax); break;
		case Dir.PZ: bb = AxisAlignedBB.getBoundingBox(hmin, vmin, 1-thick, hmax, vmax, 1); break;
		default: bb = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1); break;
		}
		
		return bb;
	}

	public int getColour() {
		return colour;
	}
	
	public int getSide() {
		return side;
	}
	
	public boolean isPressed() {
		return pressed;
	}
	
	private boolean recursiveBlockUpdate = false;
	
	private void setPressed(boolean nv, boolean lit) {
		if(nv == pressed && lit == this.lit) return;
		if(nv != pressed)
			worldObj.playSoundEffect(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5, "random.click", 0.3F, nv ? 0.6F : 0.5F);
		pressed = nv;
		this.lit = lit;
		updateNeighbours();
	}
	
	private void updateNeighbours() {
		recursiveBlockUpdate = true;
		
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());
		
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side];
		int x = xCoord + fd.offsetX;
		int y = yCoord + fd.offsetY;
		int z = zCoord + fd.offsetZ;
		worldObj.notifyBlocksOfNeighborChange(x, y, z, getBlockType());
		
		recursiveBlockUpdate = false;
	}
	
	private boolean isReceivingPower() {
		ForgeDirection fd = ForgeDirection.VALID_DIRECTIONS[side];
		int x = xCoord + fd.offsetX;
		int y = yCoord + fd.offsetY;
		int z = zCoord + fd.offsetZ;
		return worldObj.getBlockPowerInput(x, y, z) > 0;
	}

	public void onNeighbourChange() {
		switch(type) {
		case Normal:
			setPressed(pressed, !pressed && !isReceivingPower());
			break;
		case Latch:
			if(recursiveBlockUpdate) return;
			if(!isReceivingPower())
				setPressed(false, false);
			else
				setPressed(pressed, true);
			break;
		case SelfLatch:
			if(recursiveBlockUpdate) return;
			if(isReceivingPower())
				setPressed(false, false);
			break;
		}
	}

	public boolean isPowering() {
		switch(type) {
		case Normal: return pressed && pressTicks > 2;
		case Latch: return pressed && pressTicks > 2;
		case SelfLatch: return !pressed;
		case Toggle: return pressed;
		default: return false;
		}
	}

	public boolean isLit() {
		switch(lightMode) {
		case Default: return lit;
		case Inverted: return !lit;
		case Always: return true;
		}
		return false;
	}

	public void configure(EntityPlayer ply) {
		lightMode = LightMode.VALUES[(lightMode.ordinal() + 1) % LightMode.VALUES.length];
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		ply.addChatMessage(new ChatComponentTranslation("redlogic.button.lightmode."+lightMode.ordinal()));
	}

	public ItemStack getItemDropped() {
		return new ItemStack(RedLogicMod.lumarButton, 1, getDamageValue(colour, type, model));
	}

	public LumarButtonModel getModel() {
		return model;
	}

	public AxisAlignedBB getMobSensitiveBoundingBox() {
		return getBoundingBox(side, false, model);
	}

	

	
	
	@Override
	public EnumPosition getPartPosition(int subHit) {
		return EnumPosition.getFacePosition(side);
	}

	@Override
	public boolean isPlacementBlocked(PartType<?> type, EnumPosition pos) {
		switch(pos.clazz) {
		case Face:
			if(pos == EnumPosition.getFacePosition(side ^ 1))
				return false;
			if(pos == EnumPosition.getFacePosition(side))
				return true;
			return type.getSize() > 2/16f;
			
		case Edge: case Corner:
			if(type.getSize() <= 2/16f)
				return false;
			switch(side) {
			case Dir.NX: return pos.x == EnumAxisPosition.Negative;
			case Dir.PX: return pos.x == EnumAxisPosition.Positive;
			case Dir.NY: return pos.y == EnumAxisPosition.Negative;
			case Dir.PY: return pos.y == EnumAxisPosition.Positive;
			case Dir.NZ: return pos.z == EnumAxisPosition.Negative;
			case Dir.PZ: return pos.z == EnumAxisPosition.Positive;
			}
			break;
			
		case Centre: case Post:
			return false;
		}
		return true;
	}

	@Override
	public boolean isPositionOccupied(EnumPosition pos) {
		return pos == getPartPosition(0);
	}

	@Override
	public float getPlayerRelativePartHardness(EntityPlayer ply, int part) {
		return ply.getCurrentPlayerStrVsBlock(RedLogicMod.lumarButton, false) / RedLogicMod.lumarButton.hardness / 30F;
	}

	@Override
	public ItemStack pickPart(MovingObjectPosition rayTrace, int part) {
		if(part == 0)
			return getItemDropped();
		
		return null;
	}

	@Override
	public boolean isPartContainerSideSolid(ForgeDirection side) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderPartContainer(RenderBlocks render) {
		return RenderLumarButtonStatic.actuallyRenderBlock(worldObj, xCoord, yCoord, zCoord, RedLogicMod.lumarButton, 0, render);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean renderPart(RenderBlocks render, int part) {
		return renderPartContainer(render);
	}

	@Override
	public void removePartByPlayer(EntityPlayer ply, int part, boolean harvest) {
		
		if(harvest)
			RedLogicMod.lumarButton.dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, getItemDropped());
		
		convertToCoverContainerBlock();
	}
	
	@Override
	public boolean canPlayerHarvestPart(EntityPlayer entityPlayer, int part) {
		return true;
	}
	
	@Override
	public void getPartContainerDrops(List<ItemStack> drops, int fortune) {
		drops.add(getItemDropped());
	}
	
	@Override
	public boolean addPartDestroyEffects(int part, EffectRenderer er) {
		return false;
	}
	
	@Override
	public boolean addPartHitEffects(int part, int sideHit, EffectRenderer er) {
		return false;
	}

	@Override
	public AxisAlignedBB getPartAABBFromPool(int part) {
		return getBoundingBox();
	}

	@Override
	protected int getNumTileOwnedParts() {
		return 1;
	}
	
	@Override
	public void getCollidingBoundingBoxes(AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity) {
		// does not collide
	}

	public static ItemStack getItemStack(int colour, LumarButtonType type, LumarButtonModel model) {
		return new ItemStack(RedLogicMod.lumarButton, 1, getDamageValue(colour, type, model));
	}
}
