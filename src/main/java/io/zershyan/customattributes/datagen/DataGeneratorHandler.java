package io.zershyan.customattributes.datagen;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.datagen.provider.ModLangProvider;
import io.zershyan.customattributes.datagen.provider.PackMetadataProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomAttributes.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneratorHandler {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), ModLangProvider.runEnUs(output));
        generator.addProvider(event.includeClient(), ModLangProvider.runZhCn(output));
        generator.addProvider(event.includeClient() || event.includeServer(), new PackMetadataProvider(output));
    }
}
