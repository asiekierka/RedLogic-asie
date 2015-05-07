package mods.immibis.redlogic.chips.ingame;

import mods.immibis.core.TileBasicInventory;
import mods.immibis.core.api.traits.IEnergyConsumerTrait;
import mods.immibis.core.api.traits.IEnergyConsumerTraitUser;
import mods.immibis.core.api.traits.TraitField;
import mods.immibis.core.api.traits.UsesTraits;
import mods.immibis.core.api.traits.IEnergyConsumerTrait.EnergyUnit;
import net.minecraft.nbt.NBTTagCompound;


class TilePoweredBaseBase extends TileBasicInventory {
	public TilePoweredBaseBase(int size, String name) {
		super(size, name);
	}
	
	// 1 EU = 2 units
	// 1 MJ = 5 units
	// 1 RF = 0.5 units
	//protected int powerStorage;
	//protected int maxPowerStorage = 2000;
	
	
}

@UsesTraits
public class TilePoweredBase extends TileBasicInventory implements IEnergyConsumerTraitUser {
	
	@TraitField
	protected IEnergyConsumerTrait energyConsumer;
	
	public TilePoweredBase(int size, String name) {
		super(size, name);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		energyConsumer.readFromNBT(tag);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		energyConsumer.writeToNBT(tag);
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		energyConsumer.onInvalidate();
	}
	
	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		energyConsumer.onChunkUnload();
	}
	
	@Override
	public void validate() {
		super.validate();
		energyConsumer.onValidate();
	}
	
	@Override
	public double EnergyConsumer_getPreferredBufferSize() {
		return 10000;
	}
	
	@Override
	public EnergyUnit EnergyConsumer_getPreferredUnit() {
		return EnergyUnit.RF;
	}
	
	@Override
	public boolean EnergyConsumer_isBufferingPreferred() {
		return false;
	}
}
