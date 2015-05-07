package mods.immibis.redlogic.chips.ingame;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import mods.immibis.core.api.util.BaseGuiContainer;

public class GuiChipFabricator extends BaseGuiContainer<ContainerChipFabricator> {
	public GuiChipFabricator(ContainerChipFabricator container) {
		super(container, 186, 152, new ResourceLocation("redlogic", "textures/gui/chipfab.png"));
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		drawTexturedModalRect(guiLeft+35, guiTop+45, 0, 152, container.progress, 16);
		drawStringWithoutShadow(I18n.format("gui.redlogic.chipfabricator", new Object[0]), 5, 3, 0x404040);
	}
}
