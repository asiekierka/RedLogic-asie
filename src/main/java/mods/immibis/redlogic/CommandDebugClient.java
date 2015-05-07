package mods.immibis.redlogic;

import mods.immibis.core.api.APILocator;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;


public class CommandDebugClient extends CommandBase {
	@Override
	public String getCommandName() {
		return "rldebugclient";
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
		return p_71519_1_ instanceof EntityPlayerMP;
	}
	
	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/rldebugclient "+CommandDebug.COMMAND_ARGS;
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(!(sender instanceof EntityPlayerMP))
			return;
		
		if(args.length != 2)
			throw new WrongUsageException(getCommandUsage(sender));
		
		String thing = args[0];
		
		boolean value;
		if(args[1].equals("on"))
			value = true;
		else if(args[1].equals("off"))
			value = false;
		else
			throw new WrongUsageException(getCommandUsage(sender));
		
		CommandDebugClientPacket p = new CommandDebugClientPacket();
		p.thing = args[0];
		p.value = value;
		APILocator.getNetManager().sendToClient(p, (EntityPlayerMP)sender);
	}
}
