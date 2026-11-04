package io.zershyan.customattributes.registry.packet;

import io.zershyan.customattributes.attribute.AttributeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 客户端 → 服务端：订阅（同时索取列表）或退订。 */
public class SubscriptionPacket {
    private final boolean subscribe;

    public SubscriptionPacket(boolean subscribe) {
        this.subscribe = subscribe;
    }

    public SubscriptionPacket(FriendlyByteBuf buf) {
        this.subscribe = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(subscribe);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ServerPlayer player = supplier.get().getSender();
        if (player == null) return;
        if (subscribe) {
            AttributeManager.open(player);
        } else {
            AttributeManager.close(player);
        }
    }
}
