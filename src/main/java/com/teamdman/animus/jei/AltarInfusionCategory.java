package com.teamdman.animus.jei;

import org.jetbrains.annotations.Nullable;
import com.teamdman.animus.Constants;
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
import wayoftime.bloodmagic.common.block.BloodMagicBlocks;

/**
 * JEI Recipe Category for special Blood Altar infusions
 * Shows Blood-Infused Spellbook and Sanguine Scroll creation processes
 */
public class AltarInfusionCategory implements IRecipeCategory<AltarInfusionDisplay> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Constants.Mod.MODID, "altar_infusion");
    public static final RecipeType<AltarInfusionDisplay> RECIPE_TYPE = RecipeType.create(Constants.Mod.MODID, "altar_infusion", AltarInfusionDisplay.class);

    private static final int WIDTH = 170;
    private static final int HEIGHT = 85;

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public AltarInfusionCategory(IGuiHelper guiHelper) {
        // Use a blank drawable for background
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        // Use Blood Altar as the icon
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
            new ItemStack(BloodMagicBlocks.BLOOD_ALTAR.get()));
        this.title = Component.translatable("jei.animus.category.altar_infusion");
    }

    @Override
    public RecipeType<AltarInfusionDisplay> getRecipeType() {
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
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AltarInfusionDisplay recipe, IFocusGroup focuses) {
        if (recipe.isSpellbookType()) {
            // Spellbook infusion layout:
            // [Input] -> [Altar] -> [Output]

            // Input slot (left)
            builder.addSlot(RecipeIngredientRole.INPUT, 20, 35)
                .addItemStack(recipe.getAltarInput());

            // Blood Altar (center)
            builder.addSlot(RecipeIngredientRole.CATALYST, 77, 35)
                .addItemStack(new ItemStack(BloodMagicBlocks.BLOOD_ALTAR.get()));

            // Output slot (right)
            builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 35)
                .addItemStacks(recipe.getOutputs());

        } else if (recipe.isSanguineScrollType()) {
            // Sanguine Scroll layout - compact horizontal:
            // [Scroll] + [Slate] → [Altar] → [Output]

            // Main hand input - the Iron's Spells scroll
            builder.addSlot(RecipeIngredientRole.INPUT, 10, 35)
                .addItemStack(recipe.getMainHandInput());

            // Offhand input - the slate
            builder.addSlot(RecipeIngredientRole.INPUT, 35, 35)
                .addItemStack(recipe.getOffHandInput());

            // Blood Altar (center)
            builder.addSlot(RecipeIngredientRole.CATALYST, 77, 35)
                .addItemStack(new ItemStack(BloodMagicBlocks.BLOOD_ALTAR.get()));

            // Output slot (right)
            builder.addSlot(RecipeIngredientRole.OUTPUT, 134, 35)
                .addItemStacks(recipe.getOutputs());
        }
    }

    @Override
    public void draw(AltarInfusionDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;

        // Draw title at top
        String titleStr = recipe.getTitle().getString();
        int titleWidth = font.width(titleStr);
        guiGraphics.drawString(font, titleStr, (WIDTH - titleWidth) / 2, 5, 0x8B0000, false);

        if (recipe.isSpellbookType()) {
            // Draw subtitle explaining interaction
            String subtitle = "Right-click altar with spellbook";
            int subtitleWidth = font.width(subtitle);
            guiGraphics.drawString(font, subtitle, (WIDTH - subtitleWidth) / 2, 18, 0x606060, false);

            // Draw arrows for spellbook layout (items at y=35, so arrows at y=38)
            guiGraphics.drawString(font, "→", 42, 38, 0x404040, false);
            guiGraphics.drawString(font, "→", 115, 38, 0x404040, false);

            // Draw LP cost below items (items end at y=51)
            String lpText = String.format("%,d LP", recipe.getLpCost());
            int lpWidth = font.width(lpText);
            guiGraphics.drawString(font, lpText, (WIDTH - lpWidth) / 2, 58, 0xAA0000, false);

            // Draw tier requirement
            String tierReq = "Requires Tier 3+ Altar";
            int tierWidth = font.width(tierReq);
            guiGraphics.drawString(font, tierReq, (WIDTH - tierWidth) / 2, 70, 0x606060, false);

        } else if (recipe.isSanguineScrollType()) {
            // Draw subtitle explaining interaction
            String subtitle = "Hold scroll + slate, right-click altar";
            int subtitleWidth = font.width(subtitle);
            guiGraphics.drawString(font, subtitle, (WIDTH - subtitleWidth) / 2, 16, 0x606060, false);

            // Draw + between inputs and → arrows
            guiGraphics.drawString(font, "+", 28, 38, 0x404040, false);
            guiGraphics.drawString(font, "→", 57, 38, 0x404040, false);
            guiGraphics.drawString(font, "→", 115, 38, 0x404040, false);

            // Draw LP cost below items
            String lpText = String.format("%,d LP*", recipe.getLpCost());
            int lpWidth = font.width(lpText);
            guiGraphics.drawString(font, lpText, (WIDTH - lpWidth) / 2, 58, 0xAA0000, false);

            // Draw note about LP varying
            String note = "*LP varies by spell level";
            int noteWidth = font.width(note);
            guiGraphics.drawString(font, note, (WIDTH - noteWidth) / 2, 70, 0x606060, false);
        }
    }
}
