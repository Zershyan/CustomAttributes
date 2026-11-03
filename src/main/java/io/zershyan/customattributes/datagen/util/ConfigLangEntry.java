package io.zershyan.customattributes.datagen.util;

import io.zershyan.customattributes.CustomAttributes;

/**
 * 配置项语言键：<modid>.configuration.<name>。
 * {@code name} 同时用作 {@code define(...)} 的配置键，是全模组唯一的写法来源。
 */
public record ConfigLangEntry(String name) {
    public String key() {
        return CustomAttributes.MODID + ".configuration." + name;
    }

    /** 配置界面的悬停说明由 NeoForge 追加 .tooltip 后缀，这里跟着同一个 name 派生。 */
    public String tooltipName() {
        return name + ".tooltip";
    }
}
