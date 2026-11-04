package io.zershyan.customattributes.registry.packet;

import io.zershyan.customattributes.attribute.AttributeEntry;
import io.zershyan.customattributes.client.ClientAttributeData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 服务端 → 客户端：订阅期间某条属性的数值或可调区间发生变化。 */
public class AttributeUpdatePacket {
    private final AttributeEntry entry;

    public AttributeUpdatePacket(AttributeEntry entry) {
        this.entry = entry;
    }

    public AttributeUpdatePacket(FriendlyByteBuf buf) {
        this.entry = AttributeEntry.read(buf);
    }

    public void encode(FriendlyByteBuf buf) {
        entry.write(buf);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAttributeData.update(entry));
    }
}
