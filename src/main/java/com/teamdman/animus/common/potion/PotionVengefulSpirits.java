package com.teamdman.animus.common.potion;

import com.teamdman.animus.Animus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class PotionVengefulSpirits extends Potion {


	//@SideOnly(Side.CLIENT)
	static final ResourceLocation rl = new ResourceLocation(Animus.MODID, "textures/misc/vengefulpotion.png");

	@Override
	public boolean hasStatusIcon() {
		return false;
	}

	public PotionVengefulSpirits() {
		super(false, 0x000000);
		this.setBeneficial();

	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

	@Override
	public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
		//Do stuff
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
		Tessellator tes     = Tessellator.getInstance();
		double      wh      = 18;
		double      offsetX = 6;
		double      offsetY = 7;
		Color       c       = new Color(getLiquidColor());
		float       r       = ((float) c.getRed()) / 255F;
		float       g       = ((float) c.getGreen()) / 255F;
		float       b       = ((float) c.getBlue()) / 255F;

		mc.renderEngine.bindTexture(rl);
		BufferBuilder vb = tes.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		vb.pos(x + offsetX, y + offsetY, 0).tex(0, 0).color(r, g, b, 1F).endVertex();
		vb.pos(x + offsetX, y + offsetY + wh, 0).tex(0, 1).color(r, g, b, 1F).endVertex();
		vb.pos(x + offsetX + wh, y + offsetY + wh, 0).tex(1, 1).color(r, g, b, 1F).endVertex();
		vb.pos(x + offsetX + wh, y + offsetY, 0).tex(1, 0).color(r, g, b, 1F).endVertex();

		tes.draw();

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
		Tessellator tes     = Tessellator.getInstance();
		double      wh      = 18;
		double      offsetX = 3;
		double      offsetY = 3;
		Color       c       = new Color(getLiquidColor());
		float       r       = ((float) c.getRed()) / 255F;
		float       g       = ((float) c.getGreen()) / 255F;
		float       b       = ((float) c.getBlue()) / 255F;

		mc.renderEngine.bindTexture(rl);
		BufferBuilder vb = tes.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		vb.pos(x + offsetX, y + offsetY, 0).tex(0, 0).color(r, g, b, alpha).endVertex();
		vb.pos(x + offsetX, y + offsetY + wh, 0).tex(0, 1).color(r, g, b, alpha).endVertex();
		vb.pos(x + offsetX + wh, y + offsetY + wh, 0).tex(1, 1).color(r, g, b, alpha).endVertex();
		vb.pos(x + offsetX + wh, y + offsetY, 0).tex(1, 0).color(r, g, b, alpha).endVertex();

		tes.draw();

	}

}
