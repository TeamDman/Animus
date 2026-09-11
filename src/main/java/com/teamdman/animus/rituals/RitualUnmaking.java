package com.teamdman.animus.rituals;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.core.data.SoulNetwork;
import wayoftime.bloodmagic.core.data.SoulTicket;
import wayoftime.bloodmagic.ritual.*;
import wayoftime.bloodmagic.ritual.EnumRuneType;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Ritual of Unmaking - Extracts enchantments from items
 * Place books and enchanted items near the ritual to extract enchantments
 * Consumes books to create enchanted books with the extracted enchantments
 * Activation Cost: 3000 LP
 * Refresh Cost: Configurable (default: 0 LP; one-time use, deactivates after)
 * Refresh Time: Configurable (default: 20 ticks)
 */
@RitualRegister(Constants.Rituals.UNMAKING)
public class RitualUnmaking extends Ritual {
    public static final String EFFECT_RANGE = "effect";

    public RitualUnmaking() {
        super(Constants.Rituals.UNMAKING, 0, 3000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.UNMAKING);

        addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-2, -2, -2), 5));
        setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 8, 8);
    }

    @Override
    public void performRitual(IMasterRitualStone mrs) {
        Level level = mrs.getWorldObj();
        SoulNetwork network = NetworkHelper.getSoulNetwork(mrs.getOwner());
        int currentEssence = network.getCurrentEssence();
        BlockPos masterPos = mrs.getMasterBlockPos();

        if (level.isClientSide) {
            return;
        }
        // Check if ritual is enabled
        if (!AnimusConfig.rituals.unmakingEnabled.get()) {
            return;
        }


        if (currentEssence < getRefreshCost()) {
            network.causeNausea();
            return;
        }

        // Find all item entities in range
        AreaDescriptor effectRange = mrs.getBlockRange(EFFECT_RANGE);
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

        boolean changed = false;
        for (ItemEntity itemEntity : itemList) {
            if (books.getItem().isEmpty()) {
                break;
            }
            if (itemEntity.isRemoved()) {
                continue;
            }
            ItemStack stack = itemEntity.getItem();
            Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
            if (enchantments.isEmpty()) {
                continue;
            }

            if (stack.is(Items.ENCHANTED_BOOK)) {
                // The original book plus blank books supply both copies of each
                // enchantment. Leave the original intact if the full split cannot fit.
                int requiredBooks = enchantments.size() * 2 - 1;
                if (books.getItem().getCount() < requiredBooks) {
                    continue;
                }
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    int newLevel = Math.max(1, entry.getValue() - 1);
                    spawnEnchantedBook(level, masterPos, entry.getKey(), newLevel);
                    spawnEnchantedBook(level, masterPos, entry.getKey(), newLevel);
                }
                books.getItem().shrink(requiredBooks);
                stack.shrink(1);
                level.playSound(null, masterPos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.5F, 1.0F);
                changed = true;
            } else {
                var iterator = enchantments.entrySet().iterator();
                while (iterator.hasNext() && !books.getItem().isEmpty()) {
                    Map.Entry<Enchantment, Integer> entry = iterator.next();
                    spawnEnchantedBook(level, masterPos, entry.getKey(), entry.getValue());
                    books.getItem().shrink(1);
                    iterator.remove();
                    changed = true;
                }
                EnchantmentHelper.setEnchantments(enchantments, stack);
                level.playSound(null, masterPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            itemEntity.setItem(stack.copy());
        }
        if (!changed) {
            return;
        }
        books.setItem(books.getItem().copy());
        mrs.stopRitual(Ritual.BreakType.DEACTIVATE);

        // Consume LP
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_UNMAKING),
            getRefreshCost()
        );
        network.syphon(ticket, false);
    }

    private void spawnEnchantedBook(Level level, BlockPos pos, Enchantment enchantment, int enchantmentLevel) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        EnchantmentHelper.setEnchantments(Map.of(enchantment, enchantmentLevel), book);
        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, book));
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
