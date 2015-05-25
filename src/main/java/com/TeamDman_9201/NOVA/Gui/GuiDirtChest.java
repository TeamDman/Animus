package com.TeamDman.nova.Gui;

import com.TeamDman.nova.Containers.ContainerDirtChest;
import com.TeamDman.nova.NOVA;
import com.TeamDman.nova.Tiles.TileDirtChest;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Created by TeamDman on 2015-04-04.
 */
@SideOnly(Side.CLIENT)
public class GuiDirtChest extends GuiContainer {

  private static final ResourceLocation
      texture =
      new ResourceLocation(NOVA.MODID + ":textures/gui/blockDirtChest.png");
  private TileDirtChest tile;

  public GuiDirtChest(InventoryPlayer inventoryPlayer, TileDirtChest tile) {
    super(new ContainerDirtChest(inventoryPlayer, tile));
    this.tile = tile;
  }

  protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
    String s = I18n.format(this.tile.getInventoryName(), new Object[0]);
    this.fontRendererObj.drawString(s, 8, 6, 4210752);
    this.fontRendererObj
        .drawString(I18n.format("container.inventory", new Object[0]), 8, 35, 4210752);
  }

  protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_,
                                                 int p_146976_3_) {
    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    this.mc.getTextureManager().bindTexture(texture);
    int k = (this.width - this.xSize) / 2;
    int l = (this.height - this.ySize) / 2;
    this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
  }
}
