package io.zershyan.customattributes.datagen.util;

import io.zershyan.customattributes.CustomAttributes;

/** 配置项语言键：<modid>.configuration.<name> */
public record ConfigLangEntry(String name) {
    public String key() {
        return CustomAttributes.MODID + ".configuration." + name;
    }
}
