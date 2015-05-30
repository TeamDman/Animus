package com.teamdman_9201.nova.gui;//net.minecraft.client.gui.inventory;

import com.teamdman_9201.nova.containers.ContainerBrickFurnace;
import com.teamdman_9201.nova.tiles.TileBrickFurnace;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBrickFurnace extends GuiContainer {

    private static final ResourceLocation furnaceGuiTextures = new ResourceLocation
            ("textures/gui/container/furnace.png");
    private static final String           __OBFID            = "CL_00000758";
    private TileBrickFurnace tileFurnace;

    public GuiBrickFurnace(InventoryPlayer inventoryPlayer, TileBrickFurnace
            tileEntityBrickFurnace) {
        super(new ContainerBrickFurnace(inventoryPlayer, tileEntityBrickFurnace));
        this.tileFurnace = tileEntityBrickFurnace;
    }

    protected void drawGuiContainerBackgroundLayer(float p_146976_1_, int p_146976_2_, int
            p_146976_3_) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(furnaceGuiTextures);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
        int i1;

        if (this.tileFurnace.isBurning()) {
            i1 = this.tileFurnace.getBurnTimeRemainingScaled(12);
            this.drawTexturedModalRect(k + 56, l + 36 + 12 - i1, 176, 12 - i1, 14, i1 + 2);
        }

        i1 = this.tileFurnace.getCookProgressScaled(24);
        this.drawTexturedModalRect(k + 79, l + 34, 176, 14, i1 + 1, 16);
    }

    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        String s = this.tileFurnace.hasCustomInventoryName() ? this.tileFurnace.getInventoryName
                () : I18n.format(this.tileFurnace.getInventoryName());
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth
                (s) / 2, 6, 4210752);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 +
                2, 4210752);
    }
}