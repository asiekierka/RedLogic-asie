package mods.immibis.redlogic;

import net.minecraft.block.Block;
import mods.immibis.core.ItemCombined;

public class RLNormalBlockItem extends ItemCombined {

	public RLNormalBlockItem(Block block) {
		super(block, "redlogic", new String[] {
			"cleanwall",
			"cleanfilter",
			"cleanglass"
		});
	}

}
