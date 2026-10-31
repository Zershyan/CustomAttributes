package io.zershyan.customattributes.attribute;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * 下发给客户端的一条属性信息。
 *
 * @param value 服务端此刻的最终值
 * @param min   可调下界
 * @param max   可调上界
 */
public record AttributeEntry(ResourceLocation id, double value, double min, double max) {

    public static final StreamCodec<ByteBuf, AttributeEntry> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, AttributeEntry::id,
            ByteBufCodecs.DOUBLE, AttributeEntry::value,
            ByteBufCodecs.DOUBLE, AttributeEntry::min,
            ByteBufCodecs.DOUBLE, AttributeEntry::max,
            AttributeEntry::new
    );

    public static final StreamCodec<ByteBuf, List<AttributeEntry>> LIST_STREAM_CODEC =
            STREAM_CODEC.apply(ByteBufCodecs.list());
}
