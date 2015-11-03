package mods.immibis.redlogic.gates;

import mods.immibis.core.api.util.BaseGuiContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TimerGui extends BaseGuiContainer<TimerContainer> {

	public TimerGui(TimerContainer par1Container) {
		super(par1Container, 256, 84, new ResourceLocation("redlogic", "textures/gui/timer.png"));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		
		buttonList.add(new GuiButton(0, guiLeft +   5, guiTop + 52, 40, 20, "-10s"));
		buttonList.add(new GuiButton(1, guiLeft +  46, guiTop + 52, 40, 20, "-1s"));
		buttonList.add(new GuiButton(2, guiLeft +  87, guiTop + 52, 40, 20, "-50ms"));
		buttonList.add(new GuiButton(3, guiLeft + 129, guiTop + 52, 40, 20, "+50ms"));
		buttonList.add(new GuiButton(4, guiLeft + 170, guiTop + 52, 40, 20, "+1s"));
		buttonList.add(new GuiButton(5, guiLeft + 211, guiTop + 52, 40, 20, "+10s"));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		super.drawGuiContainerBackgroundLayer(var1, var2, var3);
        
        String s = "Timer interval: "+String.format("%.2f", ((TimerContainer)inventorySlots).intervalTicks * 0.05)+"s";
        
        int name_w = fontRendererObj.getStringWidth(s);
        fontRendererObj.drawString(s, guiLeft + (xSize - name_w) / 2, guiTop + 8, 0x404040);
	}
	
	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		((TimerContainer)inventorySlots).sendButtonPressed(par1GuiButton.id);
	}

}
