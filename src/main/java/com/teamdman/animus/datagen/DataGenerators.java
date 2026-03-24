package com.teamdman.animus.datagen;

import com.teamdman.animus.Constants;
import com.teamdman.animus.worldgen.AnimusWorldGenProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new AnimusBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new AnimusItemModelProvider(output, existingFileHelper));

        generator.addProvider(event.includeServer(), new AnimusRecipeProvider(output, lookupProvider));
        generator.addProvider(event.includeServer(), new AnimusLootTableProvider(output, lookupProvider));

        // Block tags must be registered before item tags (dependency)
        var blockTagsProvider = generator.addProvider(event.includeServer(),
            new AnimusBlockTagsProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(),
            new AnimusItemTagsProvider(output, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));

        generator.addProvider(event.includeServer(), new AnimusWorldGenProvider(output, lookupProvider));
    }
}
