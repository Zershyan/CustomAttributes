package io.zershyan.customattributes.registry.config;

/** 决定用哪份名单筛选可调属性。 */
public enum AttributeListMode {
    /** 只有白名单里的属性可调，其余一律不可调。 */
    WHITELIST,
    /** 黑名单里的属性不可调，其余按范围配置处理。 */
    BLACKLIST
}
