package io.zershyan.customattributes;

import io.zershyan.customattributes.registry.CustomAttributesAttachments;
import io.zershyan.customattributes.registry.CustomAttributesCommands;
import io.zershyan.customattributes.registry.CustomAttributesConfigs;
import io.zershyan.customattributes.registry.CustomAttributesKeyMappings;
import io.zershyan.customattributes.registry.CustomAttributesPackets;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CustomAttributes.MODID)
public class CustomAttributes {
    public static final String MODID = "customattributes";
    public static final Logger LOGGER = LoggerFactory.getLogger(CustomAttributes.class);

    public CustomAttributes(IEventBus modEventBus, Dist dist, ModContainer modContainer) {
        CustomAttributesPackets.doRegister(modEventBus);
        CustomAttributesAttachments.doRegister(modEventBus);
        CustomAttributesConfigs.doRegister(modContainer);
        CustomAttributesCommands.doRegister(NeoForge.EVENT_BUS);

        if (dist.isClient()) {
            CustomAttributesKeyMappings.doRegister(modEventBus);
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
