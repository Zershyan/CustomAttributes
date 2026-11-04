package io.zershyan.customattributes.registry;

import io.zershyan.customattributes.registry.config.ServerConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

/**
 * 配置注册入口。
 *
 * <p>Forge 1.20.1 只有 COMMON / CLIENT / SERVER 三种，没有 Startup 配置。</p>
 */
public final class CustomAttributesConfigs {

    private CustomAttributesConfigs() {
    }

    public static void doRegister() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }
}
