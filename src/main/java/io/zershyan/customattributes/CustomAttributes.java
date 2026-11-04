package io.zershyan.customattributes;

import io.zershyan.customattributes.registry.CustomAttributesAttachments;
import io.zershyan.customattributes.registry.CustomAttributesCommands;
import io.zershyan.customattributes.registry.CustomAttributesConfigs;
import io.zershyan.customattributes.registry.CustomAttributesKeyMappings;
import io.zershyan.customattributes.registry.CustomAttributesPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CustomAttributes.MODID)
public class CustomAttributes {
    public static final String MODID = "customattributes";
    public static final Logger LOGGER = LoggerFactory.getLogger(CustomAttributes.class);

    public CustomAttributes() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        CustomAttributesAttachments.doRegister(modBus);
        CustomAttributesPackets.doRegister();
        CustomAttributesConfigs.doRegister();
        CustomAttributesCommands.doRegister(forgeBus);

        // Forge 1.20.1 没有 Dist 构造器参数，客户端注册必须经 DistExecutor 隔离
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CustomAttributesKeyMappings.doRegister(modBus));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
