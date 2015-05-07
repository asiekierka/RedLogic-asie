package mods.immibis.redlogic.gates;

import mods.immibis.core.api.util.BaseContainer;
import mods.immibis.redlogic.RedLogicMod;
import net.minecraft.entity.player.EntityPlayer;

public class TimerContainer extends BaseContainer<GateTile> {
	
	public int intervalTicks = 4; // game ticks
	
	private TimedGateLogic timer;
	
	public TimerContainer(EntityPlayer player, GateTile tile) {
		super(player, tile);
		
		timer = tile.getWorldObj().isRemote ? null : (TimedGateLogic)tile.getLogic();
	}
	
	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		
		setProgressBar((short)0, (short)timer.getInterval());
	}
	
	@Override
	public void updateProgressBar(int par1, int par2) {
		if(par1 == 0)
			intervalTicks = par2 & 0xFFFF;
	}
	
	@Override
	public void onButtonPressed(int id) {
		intervalTicks = timer.getInterval();
		
		switch(id) {
		case 0: intervalTicks -= 200; break;
		case 1: intervalTicks -= 20; break;
		case 2: intervalTicks -= 1; break;
		case 3: intervalTicks += 1; break;
		case 4: intervalTicks += 20; break;
		case 5: intervalTicks += 200; break;
		}
		if(intervalTicks < RedLogicMod.minTimerTicks)
			intervalTicks = RedLogicMod.minTimerTicks;
		if(intervalTicks > 65535)
			intervalTicks = 65535;
		
		timer.setInterval(intervalTicks);
	}
}
