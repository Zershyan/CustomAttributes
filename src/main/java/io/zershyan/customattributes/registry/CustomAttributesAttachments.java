package io.zershyan.customattributes.registry;

import com.mojang.serialization.Codec;
import io.zershyan.customattributes.CustomAttributes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class CustomAttributesAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CustomAttributes.MODID);

    private static final Supplier<Map<ResourceLocation, Double>> NEW_TARGETS = HashMap::new;

    /** 玩家自己设定的目标最终值：属性ID → 目标值。死亡重生后保留。 */
    public static final Supplier<AttachmentType<Map<ResourceLocation, Double>>> TARGETS =
            ATTACHMENT_TYPES.register("targets", () -> AttachmentType
                    .builder(NEW_TARGETS)
                    .serialize(Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE))
                    .copyOnDeath()
                    .build());

    private CustomAttributesAttachments() {
    }

    public static void doRegister(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
