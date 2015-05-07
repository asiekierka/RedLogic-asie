package mods.immibis.redlogic.nei;

import mods.immibis.redlogic.RedLogicMod;
import codechicken.nei.api.IConfigureNEI;
import cpw.mods.fml.common.Mod;

public class NEIRedLogicConfig implements IConfigureNEI {
	@Override
	public String getName() {
		return "RedLogic NEI Plugin";
	}
	
	@Override
	public String getVersion() {
		return RedLogicMod.class.getAnnotation(Mod.class).version();
	}
	
	@Override
	public void loadConfig() {
		// TODO Auto-generated method stub
		
	}
}
