package io.zershyan.customattributes.registry.packet;

import io.netty.buffer.ByteBuf;
import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.attribute.AttributeEntry;
import io.zershyan.customattributes.client.ClientAttributeData;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** 服务端 → 客户端：完整属性列表，收到即打开界面。 */
public record AttributeListPacket(List<AttributeEntry> entries) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AttributeListPacket> TYPE =
            new CustomPacketPayload.Type<>(CustomAttributes.id("attribute_list"));

    public static final StreamCodec<ByteBuf, AttributeListPacket> STREAM_CODEC =
            AttributeEntry.LIST_STREAM_CODEC.map(AttributeListPacket::new, AttributeListPacket::entries);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 只在客户端侧执行；客户端只做展示。 */
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ClientAttributeData.open(entries));
    }
}
