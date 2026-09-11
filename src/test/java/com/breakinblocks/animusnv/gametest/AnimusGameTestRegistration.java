package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public class AnimusGameTestRegistration {

    public static void registerTests(RegisterGameTestsEvent event) {
        AnimusTestRegistrar r = new AnimusTestRegistrar(event);

        AnimusRitualTests.register(r);
        AnimusDataTests.register(r);
        AnimusRegressionTests.register(r);
        AnimusTransactionTests.register(r);
        AnimusLifecycleTests.register(r);
        registerOptional(r, "com.breakinblocks.animusnv.gametest.AnimusSourceTests");
        registerOptional(r, "com.breakinblocks.animusnv.gametest.AnimusSpellTests");
    }

    private static void registerOptional(AnimusTestRegistrar r, String className) {
        try {
            Class.forName(className).getMethod("register", AnimusTestRegistrar.class).invoke(null, r);
        } catch (ClassNotFoundException absent) {
            return;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register " + className, e);
        }
    }
}
