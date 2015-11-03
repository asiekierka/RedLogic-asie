package mods.immibis.redlogic.gates;

import mods.immibis.core.api.util.BaseGuiContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CounterGui extends BaseGuiContainer<CounterContainer> {

	public CounterGui(CounterContainer par1Container) {
		super(par1Container, 256, 164, new ResourceLocation("redlogic", "textures/gui/counter.png"));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
		for(int row = 0; row < 3; row++) {
			int y = guiTop + 16 + 40*row;
			int k = row * 8;
			buttonList.add(new GuiButton(k+0, guiLeft +   5, y, 40, 20, "-10"));
			buttonList.add(new GuiButton(k+1, guiLeft +  46, y, 40, 20, "-5"));
			buttonList.add(new GuiButton(k+2, guiLeft +  87, y, 40, 20, "-1"));
			buttonList.add(new GuiButton(k+3, guiLeft + 129, y, 40, 20, "+1"));
			buttonList.add(new GuiButton(k+4, guiLeft + 170, y, 40, 20, "+5"));
			buttonList.add(new GuiButton(k+5, guiLeft + 211, y, 40, 20, "+10"));
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
        
        CounterContainer container = (CounterContainer)inventorySlots;
        
        String s = "Maximum: "+container.max;
        fontRendererObj.drawString(s, guiLeft + (xSize - fontRendererObj.getStringWidth(s)) / 2, guiTop + 5, 0x404040);
        s = "Increment by: "+container.incr;
        fontRendererObj.drawString(s, guiLeft + (xSize - fontRendererObj.getStringWidth(s)) / 2, guiTop + 45, 0x404040);
        s = "Decrement by: "+container.decr;
        fontRendererObj.drawString(s, guiLeft + (xSize - fontRendererObj.getStringWidth(s)) / 2, guiTop + 85, 0x404040);
        s = "Current count: "+container.value;
        fontRendererObj.drawString(s, guiLeft + (xSize - fontRendererObj.getStringWidth(s)) / 2, guiTop + 125, 0x404040);
	}
	
	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		container.sendButtonPressed(par1GuiButton.id);
	}

}
