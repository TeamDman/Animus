package com.breakinblocks.animusnv.compat.jei;

import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.recipes.SanguineDynamoRecipe;
import com.breakinblocks.animusnv.registry.AnimusBlocks;
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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SanguineDynamoCategory implements IRecipeCategory<SanguineDynamoRecipe> {

    public static final RecipeType<SanguineDynamoRecipe> RECIPE_TYPE =
        RecipeType.create(Constants.Mod.MODID, "sanguine_dynamo", SanguineDynamoRecipe.class);

    private static final int WIDTH = 160;
    private static final int HEIGHT = 52;

    private final IDrawable icon;
    private final IDrawable slot;
    private final Component title;

    public SanguineDynamoCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(AnimusBlocks.BLOCK_SANGUINE_DYNAMO.get()));
        this.slot = guiHelper.getSlotDrawable();
        this.title = Component.translatable("jei.animusnv.category.sanguine_dynamo");
    }

    @Override
    public RecipeType<SanguineDynamoRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, SanguineDynamoRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 18)
            .addIngredients(recipe.orb());
    }

    @Override
    public void draw(SanguineDynamoRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        slot.draw(guiGraphics, 4, 17);

        Font font = Minecraft.getInstance().font;
        int x = 28;

        guiGraphics.drawString(font,
            Component.translatable("jei.animusnv.sanguine_dynamo.ratio", String.format("%,d", recipe.fePerEv())),
            x, 6, 0x8B0000, false);
        guiGraphics.drawString(font,
            Component.translatable("jei.animusnv.sanguine_dynamo.max",
                String.format("%,d", recipe.maxEvPerTick()), String.format("%,d", recipe.maxFePerTick())),
            x, 20, 0x404040, false);
        guiGraphics.drawString(font,
            Component.translatable("jei.animusnv.sanguine_dynamo.redstone"),
            x, 34, 0x606060, false);
    }
}
