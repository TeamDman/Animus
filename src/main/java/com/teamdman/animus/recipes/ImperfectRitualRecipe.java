package com.teamdman.animus.recipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.teamdman.animus.registry.AnimusRecipeTypes;

/**
 * Recipe for imperfect rituals
 * Defines which block triggers which ritual effect and the LP cost
 */
public abstract class ImperfectRitualRecipe implements Recipe<ImperfectRitualInput> {
    private final String ritualKey;
    private final BlockState triggerBlock;
    private final int lpCost;

    protected ImperfectRitualRecipe(String ritualKey, BlockState triggerBlock, int lpCost) {
        this.ritualKey = ritualKey;
        this.triggerBlock = triggerBlock;
        this.lpCost = lpCost;
    }

    public String getRitualKey() {
        return ritualKey;
    }

    public BlockState getTriggerBlock() {
        return triggerBlock;
    }

    public int getLpCost() {
        return lpCost;
    }

    public boolean matches(BlockState state) {
        return state.getBlock() == triggerBlock.getBlock();
    }

    /**
     * Execute the ritual effect
     * @param level The world
     * @param stonePos Position of the ritual stone
     * @param triggerPos Position of the trigger block (above stone)
     * @param player The player activating the ritual
     * @return true if ritual succeeded
     */
    public abstract boolean onActivate(ServerLevel level, BlockPos stonePos, BlockPos triggerPos, ServerPlayer player);

    @Override
    public boolean matches(ImperfectRitualInput input, Level level) {
        return false; // Not used for this recipe type - matching done via block state
    }

    @Override
    public ItemStack assemble(ImperfectRitualInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeType<?> getType() {
        return AnimusRecipeTypes.IMPERFECT_RITUAL_TYPE.get();
    }

    @Override
    public abstract RecipeSerializer<?> getSerializer();
}
