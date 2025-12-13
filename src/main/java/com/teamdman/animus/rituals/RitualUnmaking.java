package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.common.datacomponent.SoulNetwork;
import wayoftime.bloodmagic.util.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.SoulNetworkHelper;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Ritual of Unmaking - Extracts enchantments from items
 * Place books and enchanted items near the ritual to extract enchantments
 * Consumes books to create enchanted books with the extracted enchantments
 * Activation Cost: 3000 LP
 * Refresh Cost: 0 LP (one-time use, deactivates after)
 * Refresh Time: 20 ticks
 */
public class RitualUnmaking extends Ritual {
    public static final String EFFECT_RANGE = "effect";

    public RitualUnmaking() {
        super(Constants.Rituals.UNMAKING, 0, 3000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.UNMAKING);

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-2, -2, -2), 5, 5, 5));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 8, 8);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        SoulNetwork network = SoulNetworkHelper.getSoulNetwork(mrs.getOwner());
        int currentEssence = network.getCurrentEssence();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }

        if (currentEssence < getRefreshCost()) {
            // Note: causeNausea removed in BM 4.0
            return;
        }

        // Find all item entities in range
        AreaDescriptor effectRange = getBlockRange(EFFECT_RANGE);
        AABB aabb = effectRange.getAABB(masterPos);
        List<ItemEntity> itemList = level.getEntitiesOfClass(ItemEntity.class, aabb);

        if (itemList.isEmpty()) {
            return;
        }

        // Find books
        Optional<ItemEntity> booksOpt = itemList.stream()
            .filter(e -> !e.isRemoved())
            .filter(e -> e.getItem().is(Items.BOOK))
            .findFirst();

        if (!booksOpt.isPresent()) {
            return;
        }

        ItemEntity books = booksOpt.get();

        // Process enchanted items
        for (ItemEntity itemEntity : itemList) {
            ItemStack stack = itemEntity.getItem();

            // Skip items enhanced by the Imperfect Ritual of Enhancement if config is enabled
            if (AnimusConfig.rituals.unmakingDisallowEnhanced.get() && isEnhancedItem(stack)) {
                continue;
            }

            if (stack.is(Items.ENCHANTED_BOOK)) {
                // Handle enchanted books - split enchantments using 1.21 API
                ItemEnchantments enchants = stack.get(DataComponents.STORED_ENCHANTMENTS);
                if (enchants == null || enchants.isEmpty()) {
                    continue;
                }

                boolean processedAny = false;
                for (Holder<Enchantment> enchHolder : enchants.keySet()) {
                    if (books.getItem().isEmpty()) {
                        break;
                    }

                    int enchLvl = enchants.getLevel(enchHolder);

                    // Create two enchanted books with reduced level
                    int newLevel = enchLvl > 2 ? enchLvl - 1 : 1;
                    ItemStack enchBook = createEnchantedBook(level, enchHolder, newLevel);

                    // Spawn two copies
                    level.addFreshEntity(new ItemEntity(level, masterPos.getX() + 0.5, masterPos.getY() + 1, masterPos.getZ() + 0.5, enchBook.copy()));
                    level.addFreshEntity(new ItemEntity(level, masterPos.getX() + 0.5, masterPos.getY() + 1, masterPos.getZ() + 0.5, enchBook));

                    books.getItem().shrink(1);
                    processedAny = true;
                }

                if (processedAny) {
                    stack.shrink(1);
                    level.playSound(null, masterPos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.5F, 1.0F);
                    mrs.stopRitual(Ritual.BreakType.DEACTIVATE);
                }

            } else if (stack.isEnchanted()) {
                // Handle regular enchanted items using 1.21 API
                ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
                if (enchantments == null || enchantments.isEmpty()) {
                    continue;
                }

                for (Holder<Enchantment> enchHolder : enchantments.keySet()) {
                    if (books.getItem().isEmpty()) {
                        break;
                    }

                    int enchLevel = enchantments.getLevel(enchHolder);

                    // Create enchanted book
                    ItemStack enchBook = createEnchantedBook(level, enchHolder, enchLevel);

                    // Spawn the book
                    level.addFreshEntity(new ItemEntity(level, masterPos.getX() + 0.5, masterPos.getY() + 1, masterPos.getZ() + 0.5, enchBook));

                    books.getItem().shrink(1);
                }

                // Clear enchantments from item using 1.21 API
                stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);

                level.playSound(null, masterPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5F, 1.0F);
                mrs.stopRitual(Ritual.BreakType.DEACTIVATE);
            }
        }

        // Consume LP
        SoulTicket ticket = SoulTicket.create(getRefreshCost());
        network.syphon(ticket);
    }

    /**
     * Checks if an item has been enhanced by the Imperfect Ritual of Enhancement
     */
    private boolean isEnhancedItem(ItemStack stack) {
        // In 1.21+, check custom data component
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        return customData.copyTag().getBoolean("AnimusEnhanced");
    }

    /**
     * Creates an enchanted book with a specific enchantment using 1.21 API
     */
    private ItemStack createEnchantedBook(Level level, Holder<Enchantment> enchantment, int enchLevel) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);

        // Use EnchantmentHelper to set stored enchantments on the book
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(enchantment, enchLevel);
        book.set(DataComponents.STORED_ENCHANTMENTS, mutable.toImmutable());

        return book;
    }

    @Override
    public int getRefreshCost() {
        return 0;
    }

    @Override
    public int getRefreshTime() {
        return 20;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addRune(components, -1, 0, 0, EnumRuneType.DUSK);
        addRune(components, 0, 0, -1, EnumRuneType.DUSK);
        addRune(components, 0, 0, 1, EnumRuneType.DUSK);
        addRune(components, 1, 0, 0, EnumRuneType.DUSK);
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
