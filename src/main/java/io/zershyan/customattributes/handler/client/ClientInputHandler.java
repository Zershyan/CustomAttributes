package io.zershyan.customattributes.handler.client;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.client.ClientAttributeData;
import io.zershyan.customattributes.registry.CustomAttributesKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** 按键回调：请求服务端下发列表，由服务端决定是否打开界面。 */
@EventBusSubscriber(modid = CustomAttributes.MODID, value = Dist.CLIENT)
public final class ClientInputHandler {

    private ClientInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (CustomAttributesKeyMappings.OPEN_SCREEN.get().consumeClick()) {
            ClientAttributeData.requestOpen();
        }
        ClientAttributeData.tickPendingOpen();
    }
}
