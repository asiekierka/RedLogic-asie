package mods.immibis.redlogic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import mods.immibis.core.api.net.IPacket;

public class CommandDebugClientPacket implements IPacket {

	@Override
	public byte getID() {
		return RedLogicMod.PKT_COMMAND_DEBUG_CLIENT;
	}

	@Override
	public String getChannel() {
		return RedLogicMod.CHANNEL;
	}
	
	String thing;
	boolean value;

	@Override
	public void read(DataInputStream in) throws IOException {
		thing = in.readUTF();
		value = in.readBoolean();
	}

	@Override
	public void write(DataOutputStream out) throws IOException {
		out.writeUTF(thing);
		out.writeBoolean(value);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onReceived(EntityPlayer source) {
		try {
			CommandDebug.set(thing, value, "rldebugclient", Minecraft.getMinecraft().thePlayer);
		} catch(WrongUsageException e) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(e.getMessage()));
		}
	}

}
