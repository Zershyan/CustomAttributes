package io.zershyan.customattributes.registry.packet;

import io.zershyan.customattributes.attribute.AttributeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 客户端 → 服务端：把某个属性设成目标值，或恢复未调整状态。 */
public class ModifyAttributePacket {
    private final ResourceLocation attribute;
    private final boolean reset;
    private final double value;

    public ModifyAttributePacket(ResourceLocation attribute, boolean reset, double value) {
        this.attribute = attribute;
        this.reset = reset;
        this.value = value;
    }

    public ModifyAttributePacket(FriendlyByteBuf buf) {
        this.attribute = buf.readResourceLocation();
        this.reset = buf.readBoolean();
        this.value = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(attribute);
        buf.writeBoolean(reset);
        buf.writeDouble(value);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player == null) return;
        // 服务端基于自身配置重新求值范围，不信任客户端提交的上下界与数值
        AttributeManager.apply(player, attribute, reset, value);
    }
}
