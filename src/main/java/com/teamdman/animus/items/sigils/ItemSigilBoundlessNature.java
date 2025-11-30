package com.teamdman.animus.items.sigils;

import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Sigil of Boundless Nature - LP-powered mana tablet
 *
 * This sigil acts as a Botania mana tablet, but instead of being filled by mana pools,
 * it converts Life Essence (LP) from the bound player's soul network into mana.
 *
 * Features:
 * - Stores up to 2500 mana internally
 * - When active, consumes 50 LP per second (every 20 ticks)
 * - Converts LP to mana at a configurable rate (default: 50 LP = 100 mana per second)
 * - Can be toggled on/off by shift-right-clicking
 * - Implements Botania's ManaItem interface for compatibility with mana-using tools
 * - Can export mana to both mana pools and other items
 * - Cannot receive mana from pools or spreaders (LP-powered only)
 *
 * Requires Botania to be installed to function.
 */
public class ItemSigilBoundlessNature extends AnimusSigilBase {
    private static final String MANA_KEY = "mana";
    private static final int MAX_MANA = 2500;
    private static final int TICK_RATE = 20; // Once per second
    private static final int LP_PER_CONVERSION = 50; // LP consumed per conversion cycle

    // Botania integration flag
    private static final boolean BOTANIA_LOADED = ModList.get().isLoaded("botania");

    // Cached Botania capability (loaded via reflection if available)
    private static Capability<?> MANA_ITEM_CAPABILITY = null;

    static {
        if (BOTANIA_LOADED) {
            try {
                Class<?> capsClass = Class.forName("vazkii.botania.api.BotaniaForgeCapabilities");
                java.lang.reflect.Field manaItemField = capsClass.getDeclaredField("MANA_ITEM");
                MANA_ITEM_CAPABILITY = (Capability<?>) manaItemField.get(null);
            } catch (Exception e) {
                System.err.println("[Sigil Boundless Nature] Failed to load Botania MANA_ITEM capability: " + e.getMessage());
            }
        }
    }

    public ItemSigilBoundlessNature() {
        super(Constants.Sigils.BOUNDLESS_NATURE, LP_PER_CONVERSION);
    }

    /**
     * Check if sigil is active
     */
    private boolean isActive(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        return stack.getTag().getBoolean("Active");
    }

    /**
     * Set active state
     */
    private void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean("Active", active);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player.isShiftKeyDown()) {
            // Toggle activation
            setActive(stack, !isActive(stack));
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!BOTANIA_LOADED || !isActive(stack) || !(entity instanceof Player) || entity instanceof FakePlayer) {
            return;
        }

        Player player = (Player) entity;

        // Check if sigil is bound to the player
        var binding = getBinding(stack);
        if (binding == null || !binding.getOwnerId().equals(player.getUUID())) {
            return;
        }

        // Only tick once per second (20 ticks)
        if (level.getGameTime() % TICK_RATE != 0) {
            return;
        }

        // Check if mana is already full
        int currentMana = getMana(stack);
        if (currentMana >= MAX_MANA) {
            return;
        }

        // Try to consume LP and convert to mana
        if (!level.isClientSide) {
            var network = wayoftime.bloodmagic.util.helper.NetworkHelper.getSoulNetwork(player);
            var ticket = new wayoftime.bloodmagic.core.data.SoulTicket(
                Component.translatable("item.animus.sigil_boundless_nature"),
                getLpUsed()
            );

            var syphonResult = network.syphonAndDamage(player, ticket);
            if (syphonResult.isSuccess()) {
                // Convert LP to mana based on config (bulk conversion)
                int manaPerLP = AnimusConfig.botania.LPtoManaConversionRate.get();
                int manaGained = manaPerLP * LP_PER_CONVERSION;
                addMana(stack, manaGained);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        if (!BOTANIA_LOADED) {
            tooltip.add(Component.translatable("tooltip.animus.requires_botania")
                .withStyle(ChatFormatting.RED));
            return;
        }

        // Show activation state
        if (isActive(stack)) {
            tooltip.add(Component.translatable("tooltip.animus.sigil.active")
                .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.animus.sigil.inactive")
                .withStyle(ChatFormatting.GRAY));
        }

        // Show mana storage
        int currentMana = getMana(stack);
        tooltip.add(Component.translatable("tooltip.animus.mana_stored",
                currentMana, MAX_MANA)
            .withStyle(ChatFormatting.AQUA));

        // Show conversion rate
        int conversionRate = AnimusConfig.botania.LPtoManaConversionRate.get();
        int manaPerSecond = conversionRate * LP_PER_CONVERSION;
        tooltip.add(Component.translatable("tooltip.animus.conversion_rate",
                LP_PER_CONVERSION, manaPerSecond)
            .withStyle(ChatFormatting.GRAY));
    }

    // ===== Mana Storage (NBT-based) =====

    private int getMana(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        return tag.getInt(MANA_KEY);
    }

    private void setMana(ItemStack stack, int mana) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(MANA_KEY, Math.max(0, Math.min(mana, MAX_MANA)));
    }

    private void addMana(ItemStack stack, int mana) {
        int current = getMana(stack);
        setMana(stack, current + mana);
    }

    // ===== Botania ManaItem Implementation via Capability =====

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        if (!BOTANIA_LOADED || MANA_ITEM_CAPABILITY == null) {
            return null;
        }

        return new ICapabilityProvider() {
            private final ManaItemImpl manaItem = new ManaItemImpl(stack);
            private final LazyOptional<ManaItemImpl> manaItemCap = LazyOptional.of(() -> manaItem);

            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == MANA_ITEM_CAPABILITY) {
                    return manaItemCap.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    /**
     * Implementation of Botania's ManaItem interface
     * Dynamically loaded only when Botania is present
     */
    private class ManaItemImpl implements vazkii.botania.api.mana.ManaItem {
        private final ItemStack stack;

        public ManaItemImpl(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return ItemSigilBoundlessNature.this.getMana(stack);
        }

        @Override
        public int getMaxMana() {
            return MAX_MANA;
        }

        @Override
        public void addMana(int mana) {
            ItemSigilBoundlessNature.this.addMana(stack, mana);
        }

        @Override
        public boolean canReceiveManaFromPool(BlockEntity pool) {
            // Don't accept mana from pools - this is LP-powered
            return false;
        }

        @Override
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            // Don't accept mana from items - this is LP-powered
            return false;
        }

        @Override
        public boolean canExportManaToPool(BlockEntity pool) {
            // Allow exporting mana to pools
            return true;
        }

        @Override
        public boolean canExportManaToItem(ItemStack otherStack) {
            // Allow exporting mana to other items (e.g., mana tools)
            return true;
        }

        @Override
        public boolean isNoExport() {
            // Allow mana to be used by tools
            return false;
        }
    }
}
