package io.zershyan.customattributes.handler.common;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.attribute.AttributeManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 服务端驱动的同步：订阅期间属性变化即时推送给本人，玩家退出时清理订阅。 */
@Mod.EventBusSubscriber(modid = CustomAttributes.MODID)
public final class AttributeSyncHandler {

    private AttributeSyncHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        AttributeManager.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AttributeManager.close(player);
        }
    }
}
