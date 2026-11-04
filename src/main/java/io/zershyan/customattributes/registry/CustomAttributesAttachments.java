package io.zershyan.customattributes.registry;

import com.mojang.serialization.Codec;
import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.forgeattachment.attachment.AttachmentType;
import io.zershyan.forgeattachment.registry.ForgeAttachmentRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 附件注册入口。
 *
 * <p>Forge 1.20.1 没有原生的 AttachmentType，由其依赖 forgeattachment 提供等价能力。</p>
 */
public final class CustomAttributesAttachments {
    private static final DeferredRegister<AttachmentType<?>> REGISTER =
            DeferredRegister.create(ForgeAttachmentRegistries.ATTACHMENT_TYPES_KEY, CustomAttributes.MODID);

    private static final Supplier<Map<ResourceLocation, Double>> NEW_TARGETS = HashMap::new;

    /** 玩家自己设定的目标最终值：属性ID → 目标值。死亡重生后保留。 */
    public static final RegistryObject<AttachmentType<Map<ResourceLocation, Double>>> TARGETS =
            REGISTER.register("targets", CustomAttributesAttachments::createTargets);

    private CustomAttributesAttachments() {
    }

    private static AttachmentType<Map<ResourceLocation, Double>> createTargets() {
        return AttachmentType.builder(NEW_TARGETS)
                .serialize(Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE))
                .copyOnDeath()
                .build();
    }

    public static void doRegister(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
