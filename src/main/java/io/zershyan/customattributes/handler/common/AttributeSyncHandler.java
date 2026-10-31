package io.zershyan.customattributes.handler.common;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.attribute.AttributeManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** 服务端驱动的同步：订阅期间属性变化即时推送给本人，玩家退出时清理订阅。 */
@EventBusSubscriber(modid = CustomAttributes.MODID)
public final class AttributeSyncHandler {

    private AttributeSyncHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        AttributeManager.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AttributeManager.close(player);
        }
    }
}
