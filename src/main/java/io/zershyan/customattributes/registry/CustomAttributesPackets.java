package io.zershyan.customattributes.registry;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.registry.packet.AttributeListPacket;
import io.zershyan.customattributes.registry.packet.AttributeUpdatePacket;
import io.zershyan.customattributes.registry.packet.ModifyAttributePacket;
import io.zershyan.customattributes.registry.packet.SubscriptionPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jetbrains.annotations.NotNull;

public final class CustomAttributesPackets {
    /** 协议版本取本 mod 版本（含构建数），不硬编码；两端不一致时 NeoForge 拒绝连接 */
    @NotNull
    private static final String PROTOCOL_VERSION = ModList.get()
            .getModContainerById(CustomAttributes.MODID)
            .map(ModContainer::getModInfo)
            .map(IModInfo::getVersion)
            .map(Object::toString)
            .orElse("unknown");

    private CustomAttributesPackets() {
    }

    public static void doRegister(IEventBus modEventBus) {
        modEventBus.addListener(CustomAttributesPackets::register);
    }

    private static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(SubscriptionPacket.TYPE, SubscriptionPacket.STREAM_CODEC, SubscriptionPacket::handle);
        registrar.playToServer(ModifyAttributePacket.TYPE, ModifyAttributePacket.STREAM_CODEC, ModifyAttributePacket::handle);
        registrar.playToClient(AttributeListPacket.TYPE, AttributeListPacket.STREAM_CODEC, AttributeListPacket::handle);
        registrar.playToClient(AttributeUpdatePacket.TYPE, AttributeUpdatePacket.STREAM_CODEC, AttributeUpdatePacket::handle);
    }
}
