package io.zershyan.customattributes.registry;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.registry.packet.AttributeListPacket;
import io.zershyan.customattributes.registry.packet.AttributeUpdatePacket;
import io.zershyan.customattributes.registry.packet.ModifyAttributePacket;
import io.zershyan.customattributes.registry.packet.SubscriptionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * 网络注册入口。
 *
 * <p>Forge 1.20.1 使用 {@link SimpleChannel}，没有 NeoForge 的 PayloadRegistrar。
 */
public final class CustomAttributesPackets {
    /** 协议版本取本 mod 版本（含构建数），不硬编码；两端不一致时拒绝连接 */
    private static final String PROTOCOL_VERSION = ModList.get()
            .getModContainerById(CustomAttributes.MODID)
            .map(container -> container.getModInfo().getVersion().toString())
            .orElse("unknown");

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CustomAttributes.MODID, CustomAttributes.MODID),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextId;

    private CustomAttributesPackets() {
    }

    private static int id() {
        return nextId++;
    }

    /** SimpleChannel 的注册不依赖事件总线，故无需入参 */
    public static void doRegister() {
        INSTANCE.messageBuilder(SubscriptionPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(SubscriptionPacket::new)
                .encoder(SubscriptionPacket::encode)
                .consumerMainThread(SubscriptionPacket::handle)
                .add();

        INSTANCE.messageBuilder(ModifyAttributePacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ModifyAttributePacket::new)
                .encoder(ModifyAttributePacket::encode)
                .consumerMainThread(ModifyAttributePacket::handle)
                .add();

        INSTANCE.messageBuilder(AttributeListPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AttributeListPacket::new)
                .encoder(AttributeListPacket::encode)
                .consumerMainThread(AttributeListPacket::handle)
                .add();

        INSTANCE.messageBuilder(AttributeUpdatePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AttributeUpdatePacket::new)
                .encoder(AttributeUpdatePacket::encode)
                .consumerMainThread(AttributeUpdatePacket::handle)
                .add();
    }
}
