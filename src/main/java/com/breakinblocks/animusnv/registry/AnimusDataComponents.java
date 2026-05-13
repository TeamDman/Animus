package com.breakinblocks.animusnv.registry;

import com.mojang.serialization.Codec;
import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AnimusDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Constants.Mod.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> TRANSPOSITION_POS =
        DATA_COMPONENTS.registerComponentType("transposition_pos", builder ->
            builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> TELEPOSER_POS =
        DATA_COMPONENTS.registerComponentType("teleposer_pos", builder ->
            builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SIGIL_ACTIVATED =
        DATA_COMPONENTS.registerComponentType("sigil_activated", builder ->
            builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SOUL_ENTITY_NAME =
        DATA_COMPONENTS.registerComponentType("soul_entity_name", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> SOUL_DATA =
        DATA_COMPONENTS.registerComponentType("soul_data", builder ->
            builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SOUL_NAME =
        DATA_COMPONENTS.registerComponentType("soul_name", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SPIRITUS_TYPE =
        DATA_COMPONENTS.registerComponentType("demon_will_type", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SPEAR_ACTIVATED =
        DATA_COMPONENTS.registerComponentType("spear_activated", builder ->
            builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> LAST_DEATH_PREVENT =
        DATA_COMPONENTS.registerComponentType("last_death_prevent", builder ->
            builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> EQUIVALENCY_BLOCK =
        DATA_COMPONENTS.registerComponentType("equivalency_block", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> EQUIVALENCY_SELECTED_BLOCKS =
        DATA_COMPONENTS.registerComponentType("equivalency_selected_blocks", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> EQUIVALENCY_RADIUS =
        DATA_COMPONENTS.registerComponentType("equivalency_radius", builder ->
            builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ANIMUS_ENHANCED =
        DATA_COMPONENTS.registerComponentType("animusnv_enhanced", builder ->
            builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> CACHED_SOULS =
        DATA_COMPONENTS.registerComponentType("cached_souls", builder ->
            builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> BINDING_OWNER_UUID =
        DATA_COMPONENTS.registerComponentType("binding_owner_uuid", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> BINDING_OWNER_NAME =
        DATA_COMPONENTS.registerComponentType("binding_owner_name", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SPELL_ID =
        DATA_COMPONENTS.registerComponentType("spell_id", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SPELL_LEVEL =
        DATA_COMPONENTS.registerComponentType("spell_level", builder ->
            builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> INFUSION_TIER =
        DATA_COMPONENTS.registerComponentType("infusion_tier", builder ->
            builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
