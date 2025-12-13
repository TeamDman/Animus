package com.teamdman.animus.registry;

import com.mojang.serialization.Codec;
import com.teamdman.animus.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Data components for Animus items
 * Replaces NBT tags that were used in 1.20.1
 */
public class AnimusDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Constants.Mod.MODID);

    // Sigil of Transposition - stored block position to move
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> TRANSPOSITION_POS =
        DATA_COMPONENTS.registerComponentType("transposition_pos", builder ->
            builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    // Sigil of Transposition - bound teleposer position for entity teleportation
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> TELEPOSER_POS =
        DATA_COMPONENTS.registerComponentType("teleposer_pos", builder ->
            builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    // Generic sigil activation state
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SIGIL_ACTIVATED =
        DATA_COMPONENTS.registerComponentType("sigil_activated", builder ->
            builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    // Mob Soul - entity type identifier
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SOUL_ENTITY_NAME =
        DATA_COMPONENTS.registerComponentType("soul_entity_name", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // Mob Soul - entity NBT data
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> SOUL_DATA =
        DATA_COMPONENTS.registerComponentType("soul_data", builder ->
            builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    // Mob Soul - custom name
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SOUL_NAME =
        DATA_COMPONENTS.registerComponentType("soul_name", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // Ritual Designer - corner 1 position
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> RITUAL_CORNER1 =
        DATA_COMPONENTS.registerComponentType("ritual_corner1", builder ->
            builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    // Ritual Designer - corner 2 position
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> RITUAL_CORNER2 =
        DATA_COMPONENTS.registerComponentType("ritual_corner2", builder ->
            builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC));

    // Sentient Shield - demon will type
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> DEMON_WILL_TYPE =
        DATA_COMPONENTS.registerComponentType("demon_will_type", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // Bound Spear - activation state
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SPEAR_ACTIVATED =
        DATA_COMPONENTS.registerComponentType("spear_activated", builder ->
            builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    // Sigil of the Free Soul - last death prevention timestamp (milliseconds)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> LAST_DEATH_PREVENT =
        DATA_COMPONENTS.registerComponentType("last_death_prevent", builder ->
            builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    // Sigil of Equivalency - stored block ID for replacement
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> EQUIVALENCY_BLOCK =
        DATA_COMPONENTS.registerComponentType("equivalency_block", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // Sigil of Equivalency - selected blocks list (comma-separated block IDs)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> EQUIVALENCY_SELECTED_BLOCKS =
        DATA_COMPONENTS.registerComponentType("equivalency_selected_blocks", builder ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    // Sigil of Equivalency - radius setting
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> EQUIVALENCY_RADIUS =
        DATA_COMPONENTS.registerComponentType("equivalency_radius", builder ->
            builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    // Enhancement Ritual - marks items as already enhanced
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ANIMUS_ENHANCED =
        DATA_COMPONENTS.registerComponentType("animus_enhanced", builder ->
            builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
