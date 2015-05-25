package com.TeamDman.nova.Gui;

import com.TeamDman.nova.Containers.ContainerLightManipulator;
import com.TeamDman.nova.Tiles.TileLightManipulator;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLightManipulator extends GuiContainer {

  private static final ResourceLocation
      GuiTextures =
      new ResourceLocation("NOVA:textures/gui/blockLightManipulator.png");
  private TileLightManipulator tileLightManipulator;

  public GuiLightManipulator(InventoryPlayer inventoryPlayer,
                             TileLightManipulator tileEntityLightManipulator) {
    super(new ContainerLightManipulator(inventoryPlayer, tileEntityLightManipulator));
    // public GuiHopper(InventoryPlayer par1InventoryPlayer, IInventory
    // par2IInventory)
    // super(new ContainerHopper(par1InventoryPlayer, par2IInventory));
    this.tileLightManipulator = tileEntityLightManipulator;
  }

  protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_,
                                                 int p_146976_3_) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(GuiTextures);
    int k = (this.width - this.xSize) / 2;
    int l = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
    int i1;
    //
    // i1 = 1;//this.tileFurnace.getCookProgressScaled(24);
    // this.drawTexturedModalRect(k + 79, l + 34, 176, 14, i1 + 1, 16);
  }

  protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
    String
        s =
        this.tileLightManipulator.hasCustomInventoryName() ? this.tileLightManipulator
            .getInventoryName() : I18n.format(this.tileLightManipulator.getInventoryName());
    this.fontRendererObj
        .drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
    // this.fontRendererObj.drawString(I18n.format("container.inventory",new
    // Object[0]), 8, this.ySize - 96 + 2 - 51 + 96 - 2, 4210752);
  }
}
