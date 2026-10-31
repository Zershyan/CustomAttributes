package io.zershyan.customattributes.registry;

import com.mojang.blaze3d.platform.InputConstants;
import io.zershyan.customattributes.datagen.lang.CustomAttributesKeyLang;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class CustomAttributesKeyMappings {
    private static final List<Lazy<KeyMapping>> KEYS = new ArrayList<>();

    /** 打开属性调整界面 */
    public static final Lazy<KeyMapping> OPEN_SCREEN =
            registerKeyMapping(CustomAttributesKeyLang.KeyOpen, GLFW.GLFW_KEY_K);

    private CustomAttributesKeyMappings() {
    }

    private static Lazy<KeyMapping> registerKeyMapping(String name, int keyValue) {
        Lazy<KeyMapping> mapping = Lazy.of(() -> new KeyMapping(
                name, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, keyValue, CustomAttributesKeyLang.KeyCategory));
        KEYS.add(mapping);
        return mapping;
    }

    public static void doRegister(IEventBus modEventBus) {
        modEventBus.addListener(CustomAttributesKeyMappings::register);
    }

    private static void register(RegisterKeyMappingsEvent event) {
        KEYS.stream().map(Lazy::get).forEach(event::register);
    }
}
