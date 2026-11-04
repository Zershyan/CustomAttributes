package io.zershyan.customattributes.client;

import io.zershyan.customattributes.attribute.AttributeEntry;
import io.zershyan.customattributes.client.screen.AttributeScreen;
import io.zershyan.customattributes.registry.CustomAttributesPackets;
import io.zershyan.customattributes.registry.packet.ModifyAttributePacket;
import io.zershyan.customattributes.registry.packet.SubscriptionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 客户端侧缓存服务端下发的属性数据，并把界面操作转成请求。 */
public final class ClientAttributeData {
    private static final Map<ResourceLocation, AttributeEntry> ENTRIES = new LinkedHashMap<>();
    /** 服务端下发列表时玩家正处在别的界面，等他自己关掉再打开 */
    private static boolean pendingOpen;

    private ClientAttributeData() {
    }

    public static void requestOpen() {
        if (Minecraft.getInstance().getConnection() != null) {
            CustomAttributesPackets.INSTANCE.sendToServer(new SubscriptionPacket(true));
        }
    }

    public static void close() {
        pendingOpen = false;
        ENTRIES.clear();
        if (Minecraft.getInstance().getConnection() != null) {
            CustomAttributesPackets.INSTANCE.sendToServer(new SubscriptionPacket(false));
        }
    }

    public static void submit(ResourceLocation id, boolean reset, double value) {
        if (Minecraft.getInstance().getConnection() != null) {
            CustomAttributesPackets.INSTANCE.sendToServer(new ModifyAttributePacket(id, reset, value));
        }
    }

    public static List<AttributeEntry> entries() {
        return List.copyOf(ENTRIES.values());
    }

    /** 收到完整列表：写入缓存并打开（或刷新）界面，不抢占玩家正在使用的其他界面。 */
    public static void open(List<AttributeEntry> entries) {
        ENTRIES.clear();
        for (AttributeEntry entry : entries) {
            ENTRIES.put(entry.id(), entry);
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof AttributeScreen screen) {
            screen.reload();
        } else if (minecraft.screen == null) {
            minecraft.setScreen(new AttributeScreen());
        } else {
            pendingOpen = minecraft.player != null;
        }
    }

    public static void update(AttributeEntry entry) {
        ENTRIES.put(entry.id(), entry);
        if (Minecraft.getInstance().screen instanceof AttributeScreen screen) {
            screen.onUpdated(entry);
        }
    }

    /** 由客户端 tick 调用：等玩家关掉手头的界面后再补开。 */
    public static void tickPendingOpen() {
        if (!pendingOpen) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            // 已退出世界，别把这次请求带到下一次进服
            pendingOpen = false;
            return;
        }
        if (minecraft.screen == null) {
            pendingOpen = false;
            minecraft.setScreen(new AttributeScreen());
        }
    }
}
