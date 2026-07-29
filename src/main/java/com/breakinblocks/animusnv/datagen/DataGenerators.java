package com.breakinblocks.animusnv.datagen;

import com.klikli_dev.modonomicon.api.datagen.LanguageProviderCache;
import com.klikli_dev.modonomicon.api.datagen.NeoBookProvider;
import com.klikli_dev.modonomicon.api.datagen.research.ResearchCache;
import com.breakinblocks.animusnv.Constants;
import com.breakinblocks.animusnv.datagen.book.AnimusBookProvider;
import com.breakinblocks.animusnv.worldgen.AnimusWorldGenProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Constants.Mod.MODID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        event.createProvider(AnimusRecipeProvider.Runner::new);
        event.createProvider(AnimusLootTableProvider::new);

        event.createBlockAndItemTags(AnimusBlockTagsProvider::new, AnimusItemTagsProvider::new);

        event.createProvider(AnimusWorldGenProvider::new);

        var langCache = new LanguageProviderCache("en_us");
        var researchCache = new ResearchCache();

        generator.addProvider(true, NeoBookProvider.of(event, langCache, researchCache,
                new AnimusBookProvider()
        ));
        generator.addProvider(true, new AnimusLanguageProvider(output, langCache));
    }
}
