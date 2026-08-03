package com.breakinblocks.animusnv.gametest;

import com.breakinblocks.animusnv.gametest.base.AnimusTestRegistrar;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

public class AnimusGameTestRegistration {

    public static void registerTests(RegisterGameTestsEvent event) {
        AnimusTestRegistrar r = new AnimusTestRegistrar(event);

        AnimusRitualTests.register(r);
        AnimusDataTests.register(r);
    }
}
