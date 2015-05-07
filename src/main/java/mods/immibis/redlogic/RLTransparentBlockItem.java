package mods.immibis.redlogic;

import net.minecraft.block.Block;
import mods.immibis.core.ItemCombined;

public class RLTransparentBlockItem extends ItemCombined {

	public RLTransparentBlockItem(Block block) {
		super(block, "redlogic", new String[] {
			"cleanwall",
			"cleanfilter",
			"cleanglass"
		});
	}

}
