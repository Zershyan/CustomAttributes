package io.zershyan.customattributes.registry.packet;

import io.netty.buffer.ByteBuf;
import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.attribute.AttributeEntry;
import io.zershyan.customattributes.client.ClientAttributeData;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/** 服务端 → 客户端：订阅期间某条属性的数值或可调区间发生变化。 */
public record AttributeUpdatePacket(AttributeEntry entry) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AttributeUpdatePacket> TYPE =
            new CustomPacketPayload.Type<>(CustomAttributes.id("attribute_update"));

    public static final StreamCodec<ByteBuf, AttributeUpdatePacket> STREAM_CODEC =
            AttributeEntry.STREAM_CODEC.map(AttributeUpdatePacket::new, AttributeUpdatePacket::entry);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ClientAttributeData.update(entry));
    }
}
