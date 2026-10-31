package io.zershyan.customattributes.datagen.provider;

import io.zershyan.customattributes.datagen.lang.CustomAttributesKeyLang;
import net.minecraft.DetectedVersion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

public class PackMetadataProvider extends PackMetadataGenerator {
    public PackMetadataProvider(PackOutput output) {
        super(output);
        add(PackMetadataSection.TYPE, new PackMetadataSection(
                CustomAttributesKeyLang.Resource,
                DetectedVersion.BUILT_IN.getPackVersion(PackType.SERVER_DATA)
        ));
    }
}
