package io.zershyan.customattributes.registry.packet;

import io.zershyan.customattributes.attribute.AttributeEntry;
import io.zershyan.customattributes.client.ClientAttributeData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** 服务端 → 客户端：完整属性列表，收到即打开界面。 */
public class AttributeListPacket {
    private final List<AttributeEntry> entries;

    public AttributeListPacket(List<AttributeEntry> entries) {
        this.entries = entries;
    }

    public AttributeListPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<AttributeEntry> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(AttributeEntry.read(buf));
        }
        this.entries = list;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entries.size());
        for (AttributeEntry entry : entries) {
            entry.write(buf);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        // 只在客户端侧执行；客户端只做展示
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientAttributeData.open(entries));
    }
}
