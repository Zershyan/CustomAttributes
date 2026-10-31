package io.zershyan.customattributes.registry;

import io.zershyan.customattributes.registry.config.ServerConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class CustomAttributesConfigs {

    private CustomAttributesConfigs() {
    }

    public static void doRegister(ModContainer container) {
        if (FMLLoader.getDist().isClient()) {
            registerClient(container);
        }
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    /** 服务端配置在游戏内也要能改，并显示配置语言里登记的中文名。 */
    private static void registerClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
