package com.breakinblocks.animusnv.rituals;

import com.breakinblocks.animusnv.AnimusConfig;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.registry.AnimusDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import com.breakinblocks.animusnv.util.AnimusRitualHelper;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.ritual.AreaDescriptor;
import com.breakinblocks.neovitae.ritual.*;
import com.breakinblocks.neovitae.ritual.EnumRuneType;

import java.util.List;
import java.util.function.Consumer;

/**
 * Ritual of Unmaking - Extracts enchantments from items
 * Place books and enchanted items near the ritual to extract enchantments
 * Consumes books to create enchanted books with the extracted enchantments
 * Activation Cost: 3000 EV
 * Refresh Cost: 0 EV (one-time use, deactivates after)
 * Refresh Time: 20 ticks
 */
public class RitualUnmaking extends Ritual {
    public static final String EFFECT_RANGE = "effect";

    public RitualUnmaking() {
        super(Constants.Rituals.UNMAKING, 0, 3000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.UNMAKING);

        addBlockRange(EFFECT_RANGE, RitualAreaDescriptors.smallCube5());
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 8, 8);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        IAnima network = AnimusRitualHelper.getOwnerNetwork(mrs);
        if (network == null) {
            return;
        }
        int currentEV = network.getCurrentEV();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide()) {
            return;
        }

        if (currentEV < getRefreshCost()) {
            return;
        }

        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        AABB aabb = effectRange.getAABB(masterPos);
        List<ItemEntity> itemList = level.getEntitiesOfClass(ItemEntity.class, aabb);

        if (itemList.isEmpty()) {
            return;
        }

        List<ItemEntity> books = itemList.stream()
            .filter(e -> !e.isRemoved())
            .filter(e -> e.getItem().is(Items.BOOK))
            .toList();

        if (books.isEmpty()) {
            return;
        }

        boolean processed = false;

        for (ItemEntity itemEntity : itemList) {
            ItemStack stack = itemEntity.getItem();

            if (AnimusConfig.rituals.unmakingDisallowEnhanced.get() && isEnhancedItem(stack)) {
                continue;
            }

            if (stack.is(Items.ENCHANTED_BOOK)) {
                ItemEnchantments enchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
                if (enchants == null || enchants.isEmpty()) {
                    continue;
                }

                if (!consumeBooks(books, enchants.size())) {
                    continue;
                }
                boolean processedAny = false;
                for (Holder<Enchantment> enchHolder : enchants.keySet()) {
                    int enchLvl = enchants.getLevel(enchHolder);

                    int newLevel = enchLvl > 2 ? enchLvl - 1 : 1;
                    ItemStack enchBook = createEnchantedBook(level, enchHolder, newLevel);

                    level.addFreshEntity(new ItemEntity(level, masterPos.getX() + 0.5, masterPos.getY() + 1, masterPos.getZ() + 0.5, enchBook.copy()));
                    level.addFreshEntity(new ItemEntity(level, masterPos.getX() + 0.5, masterPos.getY() + 1, masterPos.getZ() + 0.5, enchBook));

                    processedAny = true;
                }

                if (processedAny) {
                    stack.shrink(1);
                    level.playSound(null, masterPos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.5F, 1.0F);
                    processed = true;
                }

            } else if (stack.isEnchanted()) {
                ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
                if (enchantments == null || enchantments.isEmpty()) {
                    continue;
                }

                if (!consumeBooks(books, enchantments.size())) {
                    continue;
                }
                for (Holder<Enchantment> enchHolder : enchantments.keySet()) {
                    int enchLevel = enchantments.getLevel(enchHolder);

                    ItemStack enchBook = createEnchantedBook(level, enchHolder, enchLevel);
                    level.addFreshEntity(new ItemEntity(level, masterPos.getX() + 0.5, masterPos.getY() + 1, masterPos.getZ() + 0.5, enchBook));
                }

                stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

                level.playSound(null, masterPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5F, 1.0F);
                processed = true;
            }
        }

        if (processed) {
            network.syphon(AnimaTicket.create(getRefreshCost()));
            mrs.stopRitual(Ritual.BreakType.DEACTIVATE);
        }
    }

    private static boolean consumeBooks(List<ItemEntity> books, int count) {
        if (books.stream().mapToInt(e -> e.getItem().getCount()).sum() < count) {
            return false;
        }
        for (ItemEntity entity : books) {
            int consumed = Math.min(count, entity.getItem().getCount());
            entity.getItem().shrink(consumed);
            count -= consumed;
            if (count == 0) {
                break;
            }
        }
        return true;
    }

    private boolean isEnhancedItem(ItemStack stack) {
        Boolean enhanced = stack.get(AnimusDataComponents.ANIMUS_ENHANCED.get());
        return enhanced != null && enhanced;
    }

    private ItemStack createEnchantedBook(Level level, Holder<Enchantment> enchantment, int enchLevel) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(enchantment, enchLevel);
        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());

        return book;
    }

    @Override
    public int getRefreshCost() {
        return AnimusConfig.rituals.unmakingRefreshCost.get();
    }

    @Override
    public int getRefreshTime() {
        return AnimusConfig.rituals.unmakingRefreshTime.get();
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, -1, 0, 0, EnumRuneType.TENEBRAE);
        addRune(components, 0, 0, -1, EnumRuneType.TENEBRAE);
        addRune(components, 0, 0, 1, EnumRuneType.TENEBRAE);
        addRune(components, 1, 0, 0, EnumRuneType.TENEBRAE);
        addRune(components, -2, 1, -2, EnumRuneType.AIR);
        addRune(components, -2, 1, 2, EnumRuneType.EARTH);
        addRune(components, 2, 1, -2, EnumRuneType.WATER);
        addRune(components, 2, 1, 2, EnumRuneType.FIRE);
    }


    @Override
    public Ritual getNewCopy() {
        return new RitualUnmaking();
    }
}
