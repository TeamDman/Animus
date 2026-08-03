package com.breakinblocks.animusnv.gametest.base;

import com.breakinblocks.animusnv.Constants;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

import java.util.List;
import java.util.function.Consumer;

public final class AnimusTestRegistrar {

    private static final Identifier STRUCTURE = id("empty_5x5x7");

    private final RegisterGameTestsEvent event;
    private final Holder<TestEnvironmentDefinition<?>> environment;

    public AnimusTestRegistrar(RegisterGameTestsEvent event) {
        this.event = event;
        this.environment = event.registerEnvironment(id("default"), new TestEnvironmentDefinition.AllOf(List.of()));
    }

    public void add(String name, Consumer<GameTestHelper> body) {
        add(name, 100, 0, body);
    }

    public void add(String name, int maxTicks, int setupTicks, Consumer<GameTestHelper> body) {
        TestData<Holder<TestEnvironmentDefinition<?>>> info = new TestData<>(
                environment,
                STRUCTURE,
                maxTicks,
                setupTicks,
                true,
                Rotation.NONE
        );
        try {
            event.registerTest(id(name), new AnimusInlineTest(info, body));
        } catch (Throwable t) {
            System.err.println("[AnimusGameTest] Failed to register " + name + ": " + t);
            t.printStackTrace();
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.Mod.MODID, path);
    }
}
