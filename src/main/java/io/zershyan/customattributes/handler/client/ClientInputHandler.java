package io.zershyan.customattributes.handler.client;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.client.ClientAttributeData;
import io.zershyan.customattributes.registry.CustomAttributesKeyMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 按键回调：请求服务端下发列表，由服务端决定是否打开界面。 */
@Mod.EventBusSubscriber(modid = CustomAttributes.MODID, value = Dist.CLIENT)
public final class ClientInputHandler {

    private ClientInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        while (CustomAttributesKeyMappings.OPEN_SCREEN.get().consumeClick()) {
            ClientAttributeData.requestOpen();
        }
        ClientAttributeData.tickPendingOpen();
    }
}
