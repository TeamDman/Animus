package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Data generation for Animus mod
 * Run with: ./gradlew runData
 */
@Mod.EventBusSubscriber(modid = Constants.Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();

        // Client-side data generators
        generator.addProvider(event.includeClient(), new AnimusBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new AnimusItemModelProvider(output, existingFileHelper));

        // Server-side data generators
        generator.addProvider(event.includeServer(), new AnimusRecipeProvider(output));
        generator.addProvider(event.includeServer(), new AnimusLootTableProvider(output));

        // Tags - block tags must be added before item tags for proper dependency
        var blockTagsProvider = generator.addProvider(event.includeServer(),
            new AnimusBlockTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
            new AnimusItemTagsProvider(output, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
    }
}
