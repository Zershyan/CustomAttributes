package io.zershyan.customattributes.attribute;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * 下发给客户端的一条属性信息。
 *
 * @param value    服务端此刻的最终值
 * @param min      可调下界
 * @param max      可调上界
 * @param modified 玩家是否给这个属性设定过目标值，界面据此显示"已调整"并启用重置
 */
public record AttributeEntry(ResourceLocation id, double value, double min, double max, boolean modified) {

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeDouble(value);
        buf.writeDouble(min);
        buf.writeDouble(max);
        buf.writeBoolean(modified);
    }

    public static AttributeEntry read(FriendlyByteBuf buf) {
        return new AttributeEntry(buf.readResourceLocation(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean());
    }
}
