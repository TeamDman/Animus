package com.teamdman.animus.recipes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
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
public abstract class ImperfectRitualRecipe implements Recipe<net.minecraft.world.Container> {
    private final ResourceLocation id;
    private final String ritualKey;
    private final BlockState triggerBlock;
    private final int lpCost;

    protected ImperfectRitualRecipe(ResourceLocation id, String ritualKey, BlockState triggerBlock, int lpCost) {
        this.id = id;
        this.ritualKey = ritualKey;
        this.triggerBlock = triggerBlock;
        this.lpCost = lpCost;
    }

    @Override
    public ResourceLocation getId() {
        return id;
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
    public boolean matches(net.minecraft.world.Container container, Level level) {
        return false; // Not used for this recipe type
    }

    @Override
    public ItemStack assemble(net.minecraft.world.Container container, net.minecraft.core.RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(net.minecraft.core.RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeType<?> getType() {
        return AnimusRecipeTypes.IMPERFECT_RITUAL_TYPE.get();
    }

    @Override
    public abstract RecipeSerializer<?> getSerializer();
}
