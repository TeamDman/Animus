package com.breakinblocks.animusnv.gametest.base;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;
import java.util.stream.Stream;

public final class AnimusInlineTest extends GameTestInstance {

    private static final MapCodec<AnimusInlineTest> CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }

        @Override
        public <T> DataResult<AnimusInlineTest> decode(DynamicOps<T> ops, MapLike<T> input) {
            return DataResult.error(() -> "AnimusInlineTest is registered programmatically, not decoded");
        }

        @Override
        public <T> RecordBuilder<T> encode(AnimusInlineTest input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix;
        }

        @Override
        public String toString() {
            return "AnimusInlineTest";
        }
    };

    private final Consumer<GameTestHelper> body;

    public AnimusInlineTest(TestData<Holder<TestEnvironmentDefinition<?>>> info, Consumer<GameTestHelper> body) {
        super(info);
        this.body = body;
    }

    @Override
    public void run(GameTestHelper helper) {
        body.accept(helper);
    }

    @Override
    public MapCodec<? extends GameTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("animusnv:inline");
    }
}
