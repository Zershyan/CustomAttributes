package io.zershyan.customattributes.registry.packet;

import io.netty.buffer.ByteBuf;
import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.attribute.AttributeManager;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/** 客户端 → 服务端：把某个属性改成目标值，或恢复默认。 */
public record ModifyAttributePacket(ResourceLocation attribute, boolean reset, double value) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ModifyAttributePacket> TYPE =
            new CustomPacketPayload.Type<>(CustomAttributes.id("modify_attribute"));

    public static final StreamCodec<ByteBuf, ModifyAttributePacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ModifyAttributePacket::attribute,
            ByteBufCodecs.BOOL, ModifyAttributePacket::reset,
            ByteBufCodecs.DOUBLE, ModifyAttributePacket::value,
            ModifyAttributePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            // 服务端基于自身配置重新求值范围，不信任客户端提交的上下界与数值
            AttributeManager.apply(player, attribute, reset, value);
        });
    }
}
