package io.zershyan.customattributes.datagen;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.datagen.provider.ModLangProvider;
import io.zershyan.customattributes.datagen.provider.PackMetadataProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = CustomAttributes.MODID)
public class DataGeneratorHandler {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.createProvider(ModLangProvider::runEnUs);
        event.createProvider(ModLangProvider::runZhCn);
        event.createProvider(PackMetadataProvider::new);
    }
}
