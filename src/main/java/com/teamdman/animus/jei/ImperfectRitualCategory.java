package com.teamdman.animus.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.teamdman.animus.Constants;
import com.teamdman.animus.registry.AnimusBlocks;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI Recipe Category for Imperfect Rituals
 * Shows the ritual stone, catalyst block, LP cost, and effect description
 */
public class ImperfectRitualCategory implements IRecipeCategory<ImperfectRitualDisplay> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "imperfect_ritual");
    public static final RecipeType<ImperfectRitualDisplay> RECIPE_TYPE = RecipeType.create(Constants.Mod.MODID, "imperfect_ritual", ImperfectRitualDisplay.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public ImperfectRitualCategory(IGuiHelper guiHelper) {
        // Create a simple background - 160x80 pixels
        this.background = guiHelper.createBlankDrawable(160, 85);
        // Use the imperfect ritual stone as the icon
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(AnimusBlocks.BLOCK_IMPERFECT_RITUAL_STONE.get()));
        this.title = Component.translatable("jei.animus.category.imperfect_ritual");
    }

    @Override
    public RecipeType<ImperfectRitualDisplay> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ImperfectRitualDisplay recipe, IFocusGroup focuses) {
        // Ritual Stone slot (bottom)
        builder.addSlot(RecipeIngredientRole.CATALYST, 5, 45)
            .addItemStack(new ItemStack(AnimusBlocks.BLOCK_IMPERFECT_RITUAL_STONE.get()));

        // Trigger/Catalyst block slot (top, above ritual stone)
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 22)
            .addItemStack(recipe.getTriggerItemStack());
    }

    @Override
    public void draw(ImperfectRitualDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        PoseStack poseStack = guiGraphics.pose();

        // Draw ritual name
        guiGraphics.drawString(font, recipe.getName(), 28, 5, 0x8B0000, false);

        // Draw LP cost
        String lpText = String.format("Cost: %,d LP", recipe.getLpCost());
        guiGraphics.drawString(font, lpText, 28, 18, 0x404040, false);

        // Draw description (wrapped if needed)
        Component desc = recipe.getDescription();
        String descText = desc.getString();

        // Simple word wrapping for description
        int maxWidth = 130;
        int yOffset = 32;
        int lineHeight = 10;

        // Split into words and wrap
        String[] words = descText.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int width = font.width(testLine);

            if (width > maxWidth && currentLine.length() > 0) {
                guiGraphics.drawString(font, currentLine.toString(), 28, yOffset, 0x606060, false);
                yOffset += lineHeight;
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }

            // Limit to 3 lines
            if (yOffset > 52) break;
        }

        // Draw remaining text
        if (currentLine.length() > 0 && yOffset <= 52) {
            guiGraphics.drawString(font, currentLine.toString(), 28, yOffset, 0x606060, false);
        }

        // Draw required mod info if applicable
        if (recipe.hasRequiredMod()) {
            String modText = "Requires: " + recipe.getRequiredMod();
            guiGraphics.drawString(font, modText, 28, 62, 0xAA6600, false);
        }

        // Draw arrow or indicator between blocks
        guiGraphics.drawString(font, "↑", 10, 36, 0x404040, false);
    }
}
