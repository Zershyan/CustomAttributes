package io.zershyan.customattributes.registry.packet;

import io.netty.buffer.ByteBuf;
import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.attribute.AttributeManager;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/** 客户端 → 服务端：订阅（同时索取列表）或退订。 */
public record SubscriptionPacket(boolean subscribe) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SubscriptionPacket> TYPE =
            new CustomPacketPayload.Type<>(CustomAttributes.id("subscription"));

    public static final StreamCodec<ByteBuf, SubscriptionPacket> STREAM_CODEC =
            ByteBufCodecs.BOOL.map(SubscriptionPacket::new, SubscriptionPacket::subscribe);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (subscribe) {
                AttributeManager.open(player);
            } else {
                AttributeManager.close(player);
            }
        });
    }
}
