package com.breakinblocks.animusnv.compat.jei;

import org.jetbrains.annotations.Nullable;
import com.breakinblocks.animusnv.Constants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import com.breakinblocks.neovitae.common.block.NVBlocks;

/**
 * JEI Recipe Category for special Ara Vitae infusions
 * Shows Blood-Infused Spellbook and Sanguine Scroll creation processes
 */
public class AltarInfusionCategory implements IRecipeCategory<AltarInfusionDisplay> {

    public static final Identifier UID = Identifier.fromNamespaceAndPath(Constants.Mod.MODID, "altar_infusion");
    public static final IRecipeType<AltarInfusionDisplay> RECIPE_TYPE = IRecipeType.create(Constants.Mod.MODID, "altar_infusion", AltarInfusionDisplay.class);

    private static final int WIDTH = 170;
    private static final int HEIGHT = 100;

    private final IDrawable icon;
    private final Component title;

    public AltarInfusionCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(NVBlocks.ARA_VITAE.block().get()));
        this.title = Component.translatable("jei.animusnv.category.altar_infusion");
    }

    @Override
    public IRecipeType<AltarInfusionDisplay> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AltarInfusionDisplay recipe, IFocusGroup focuses) {
        if (recipe.isSpellbookType()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 20, 40)
                .addItemStack(recipe.getAltarInput());

            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 75, 40)
                .addItemStack(new ItemStack(NVBlocks.ARA_VITAE.block().get()));

            builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 40)
                .addItemStacks(recipe.getOutputs());

        } else if (recipe.isSpellbookUpgradeType()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 20, 40)
                .addItemStack(recipe.getAltarInput());

            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 75, 40)
                .addItemStack(new ItemStack(NVBlocks.ARA_VITAE.block().get()));

            builder.addSlot(RecipeIngredientRole.OUTPUT, 130, 40)
                .addItemStacks(recipe.getOutputs());

        } else if (recipe.isSanguineScrollType()) {
            builder.addSlot(RecipeIngredientRole.INPUT, 35, 28)
                .addItemStack(recipe.getMainHandInput());

            builder.addSlot(RecipeIngredientRole.INPUT, 115, 28)
                .addItemStack(recipe.getOffHandInput());

            builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 75, 52)
                .addItemStack(new ItemStack(NVBlocks.ARA_VITAE.block().get()));

            builder.addSlot(RecipeIngredientRole.OUTPUT, 75, 80)
                .addItemStacks(recipe.getOutputs());
        }
    }

    @Override
    public void draw(AltarInfusionDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        String titleStr = recipe.getTitle().getString();
        int titleWidth = font.width(titleStr);
        guiGraphics.text(font, titleStr, (WIDTH - titleWidth) / 2, 2, 0x8B0000, false);

        if (recipe.isSpellbookType()) {
            guiGraphics.text(font, "→", 45, 43, 0x404040, false);
            guiGraphics.text(font, "→", 105, 43, 0x404040, false);

            String evText = String.format("%,d EV", recipe.getEvCost());
            int evWidth = font.width(evText);
            guiGraphics.text(font, evText, (WIDTH - evWidth) / 2, 60, 0xAA0000, false);

            drawWrappedText(guiGraphics, font, recipe.getDescription().getString(), 5, 75, 160, 0x606060);

        } else if (recipe.isSpellbookUpgradeType()) {
            guiGraphics.text(font, "→", 45, 43, 0x404040, false);
            guiGraphics.text(font, "→", 105, 43, 0x404040, false);

            String subtitle = "Tier " + recipe.getFromTier() + " → Tier " + recipe.getToTier();
            int subtitleWidth = font.width(subtitle);
            guiGraphics.text(font, subtitle, (WIDTH - subtitleWidth) / 2, 14, 0x8B0000, false);

            String evText = String.format("%,d EV", recipe.getEvCost());
            int evWidth = font.width(evText);
            guiGraphics.text(font, evText, (WIDTH - evWidth) / 2, 60, 0xAA0000, false);

            String orbReq = "Requires: " + recipe.getRequiredOrb();
            int orbWidth = font.width(orbReq);
            guiGraphics.text(font, orbReq, (WIDTH - orbWidth) / 2, 72, 0x606060, false);

            drawWrappedText(guiGraphics, font, recipe.getDescription().getString(), 5, 85, 160, 0x606060);

        } else if (recipe.isSanguineScrollType()) {
            guiGraphics.text(font, "Main Hand", 23, 16, 0x404040, false);
            guiGraphics.text(font, "Offhand", 107, 16, 0x404040, false);

            guiGraphics.text(font, "↘", 55, 46, 0x404040, false);
            guiGraphics.text(font, "↙", 105, 46, 0x404040, false);
            guiGraphics.text(font, "↓", 80, 70, 0x404040, false);

            String evText = String.format("%,d EV*", recipe.getEvCost());
            guiGraphics.text(font, evText, 5, 56, 0xAA0000, false);
        }
    }

    private void drawWrappedText(GuiGraphicsExtractor guiGraphics, Font font, String text, int x, int y, int maxWidth, int color) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        int yOffset = y;
        int lineHeight = 9;

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int width = font.width(testLine);

            if (width > maxWidth && currentLine.length() > 0) {
                guiGraphics.text(font, currentLine.toString(), x, yOffset, color, false);
                yOffset += lineHeight;
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }

            if (yOffset > y + lineHeight * 2) break;
        }

        if (currentLine.length() > 0 && yOffset <= y + lineHeight * 2) {
            guiGraphics.text(font, currentLine.toString(), x, yOffset, color, false);
        }
    }
}
