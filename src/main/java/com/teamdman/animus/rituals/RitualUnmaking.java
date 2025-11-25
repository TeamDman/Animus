package com.teamdman.animus.rituals;

import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

import java.util.List;
import java.util.Map;
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

        if (currentEssence < getRefreshCost()) {
            network.causeNausea();
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

            if (stack.is(Items.ENCHANTED_BOOK)) {
                // Handle enchanted books - split enchantments
                ListTag enchants = stack.getEnchantmentTags();
                if (enchants.isEmpty()) {
                    continue;
                }

                for (int i = enchants.size() - 1; i >= 0; i--) {
                    if (books.getItem().isEmpty()) {
                        break;
                    }

                    CompoundTag enchData = enchants.getCompound(i);
                    String enchId = enchData.getString("id");
                    int enchLvl = enchData.getInt("lvl");

                    // Remove this enchantment
                    enchants.remove(i);

                    // Create two enchanted books with reduced level
                    int newLevel = enchLvl > 2 ? enchLvl - 1 : 1;
                    ItemStack enchBook = createEnchantedBook(enchId, newLevel);

                    // Spawn two copies
                    level.addFreshEntity(new ItemEntity(level, masterPos.getX() + 0.5, masterPos.getY() + 1, masterPos.getZ() + 0.5, enchBook.copy()));
                    level.addFreshEntity(new ItemEntity(level, masterPos.getX() + 0.5, masterPos.getY() + 1, masterPos.getZ() + 0.5, enchBook));

                    books.getItem().shrink(1);
                }

                stack.shrink(1);
                level.playSound(null, masterPos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.5F, 1.0F);
                mrs.stopRitual(Ritual.BreakType.DEACTIVATE);

            } else if (stack.isEnchanted()) {
                // Handle regular enchanted items
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    if (books.getItem().isEmpty()) {
                        break;
                    }

                    // Create enchanted book
                    ItemStack enchBook = new ItemStack(Items.ENCHANTED_BOOK);
                    EnchantmentHelper.setEnchantments(Map.of(entry.getKey(), entry.getValue()), enchBook);

                    // Spawn the book
                    level.addFreshEntity(new ItemEntity(level, masterPos.getX() + 0.5, masterPos.getY() + 1, masterPos.getZ() + 0.5, enchBook));

                    books.getItem().shrink(1);
                }

                // Clear enchantments from item
                EnchantmentHelper.setEnchantments(Map.of(), stack);

                level.playSound(null, masterPos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5F, 1.0F);
                mrs.stopRitual(Ritual.BreakType.DEACTIVATE);
            }
        }

        // Consume LP
        SoulTicket ticket = new SoulTicket(
            Component.translatable(Constants.Localizations.Text.TICKET_UNMAKING),
            getRefreshCost()
        );
        network.syphon(ticket, false);
    }

    /**
     * Creates an enchanted book with a specific enchantment
     */
    private ItemStack createEnchantedBook(String enchantmentId, int level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        CompoundTag tag = book.getOrCreateTag();

        ListTag storedEnchantments = new ListTag();
        CompoundTag enchantment = new CompoundTag();
        enchantment.putString("id", enchantmentId);
        enchantment.putInt("lvl", level);
        storedEnchantments.add(enchantment);

        tag.put("StoredEnchantments", storedEnchantments);

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
        addRune(components, -4, 0, -2, EnumRuneType.FIRE);
        addRune(components, -4, 0, 0, EnumRuneType.FIRE);
        addRune(components, -4, 0, 2, EnumRuneType.FIRE);
        addRune(components, -3, 0, -3, EnumRuneType.DUSK);
        addRune(components, -3, 0, -1, EnumRuneType.FIRE);
        addRune(components, -3, 0, 1, EnumRuneType.FIRE);
        addRune(components, -3, 0, 3, EnumRuneType.DUSK);
        addRune(components, -2, 0, -4, EnumRuneType.AIR);
        addRune(components, -2, 0, -2, EnumRuneType.DUSK);
        addRune(components, -2, 0, 0, EnumRuneType.FIRE);
        addRune(components, -2, 0, 2, EnumRuneType.DUSK);
        addRune(components, -2, 0, 4, EnumRuneType.EARTH);
        addRune(components, -1, 0, -3, EnumRuneType.AIR);
        addRune(components, -1, 0, -1, EnumRuneType.DUSK);
        addRune(components, -1, 0, 0, EnumRuneType.FIRE);
        addRune(components, -1, 0, 1, EnumRuneType.DUSK);
        addRune(components, -1, 0, 3, EnumRuneType.EARTH);
        addRune(components, 0, 0, -4, EnumRuneType.AIR);
        addRune(components, 0, 0, -2, EnumRuneType.AIR);
        addRune(components, 0, 0, -1, EnumRuneType.AIR);
        addRune(components, 0, 0, 1, EnumRuneType.EARTH);
        addRune(components, 0, 0, 2, EnumRuneType.EARTH);
        addRune(components, 0, 0, 4, EnumRuneType.EARTH);
        addRune(components, 1, 0, -3, EnumRuneType.AIR);
        addRune(components, 1, 0, -1, EnumRuneType.DUSK);
        addRune(components, 1, 0, 0, EnumRuneType.WATER);
        addRune(components, 1, 0, 1, EnumRuneType.DUSK);
        addRune(components, 1, 0, 3, EnumRuneType.EARTH);
        addRune(components, 2, 0, -4, EnumRuneType.AIR);
        addRune(components, 2, 0, -2, EnumRuneType.DUSK);
        addRune(components, 2, 0, 0, EnumRuneType.WATER);
        addRune(components, 2, 0, 2, EnumRuneType.DUSK);
        addRune(components, 2, 0, 4, EnumRuneType.EARTH);
        addRune(components, 3, 0, -3, EnumRuneType.DUSK);
        addRune(components, 3, 0, -1, EnumRuneType.WATER);
        addRune(components, 3, 0, 1, EnumRuneType.WATER);
        addRune(components, 3, 0, 3, EnumRuneType.DUSK);
        addRune(components, 4, 0, -2, EnumRuneType.WATER);
        addRune(components, 4, 0, 0, EnumRuneType.WATER);
        addRune(components, 4, 0, 2, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new RitualUnmaking();
    }
}
