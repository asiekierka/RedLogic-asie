package mods.immibis.redlogic;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;

public class CommandDebug extends CommandBase {
	/**
	 * If true:
	 * Fire particles indicate block updates.
	 * Bonemeal particles indicate packets sent.
	 */
	public static boolean WIRE_LAG_PARTICLES;
	
	/**
	 * If true, bonemeal particles indicate wire strength updates.
	 */
	public static boolean WIRE_DEBUG_PARTICLES;
	
	/**
	 * If true, right-click a wire for signal strength.
	 * Will not work properly in SMP.
	 */
	public static boolean WIRE_READING;
	
	/**
	 * If true, chip scanning is much faster.
	 */
	public static boolean FAST_SCAN;
	
	/**
	 * If true, wire update details get printed to the console.
	 */
	public static boolean WIRE_UPDATE_CONSOLE_SPAM;
	
	/**
	 * If true, wires and gates won't cause chunk updates (resulting in stale rendering).
	 */
	public static boolean DONT_RERENDER;
	
	/**
	 * If true, wires and gates won't send packets just because of state changes.
	 * (Rotation, microblock placement, etc. still will).
	 * Currently not accessible.
	 */
	public static boolean DONT_UPDATE_CLIENTS;
	
	
	static final String COMMAND_ARGS = "{wire-lag-particles|wire-debug-particles|wire-reading|fast-scan|print-wire-updates|no-wire-chunk-updates} {on|off}";

	@Override
	public String getCommandName() {
		return "rldebug";
	}
	
	@Override
	public String getCommandUsage(ICommandSender par1iCommandSender) {
		return "/"+getCommandName()+" "+COMMAND_ARGS;
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		if(astring.length != 2)
			throw new WrongUsageException(getCommandUsage(icommandsender));
		
		String thing = astring[0];
		boolean on;
		
		if(astring[1].equals("on"))
			on = true;
		else if(astring[1].equals("off"))
			on = false;
		else
			throw new WrongUsageException(getCommandUsage(icommandsender));
		
		set(thing, on, getCommandName(), icommandsender);
	}
	
	public static void set(String thing, boolean on, String commandName, ICommandSender sender) {
		if(thing.equals("wire-lag-particles"))
			WIRE_LAG_PARTICLES = on;
		else if(thing.equals("wire-debug-particles"))
			WIRE_DEBUG_PARTICLES = on;
		else if(thing.equals("wire-reading"))
			WIRE_READING = on;
		else if(thing.equals("fast-scan"))
			FAST_SCAN = on;
		else if(thing.equals("print-wire-updates"))
			WIRE_UPDATE_CONSOLE_SPAM = on;
		else if(thing.equals("no-wire-chunk-updates"))
			DONT_RERENDER = on;
		else
			throw new WrongUsageException("/"+commandName+" "+COMMAND_ARGS);
		
		String side;
		if(FMLLaunchHandler.side().isClient())
			if(Minecraft.getMinecraft().isIntegratedServerRunning())
				side = "client and server";
			else
				side = "client";
		else
			side = "server";
		
		sender.addChatMessage(new ChatComponentText("RedLogic debug feature '" + thing + "' is now " + (on?"on":"off")+" ("+side+")"));
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1iCommandSender) {
		// TODO Auto-generated method stub
		return super.canCommandSenderUseCommand(par1iCommandSender);
	}
	
    @Override
	public int compareTo(Object par1Obj)
    {
        return ((ICommand)par1Obj).getCommandName().compareTo(this.getCommandName());
    }
}
