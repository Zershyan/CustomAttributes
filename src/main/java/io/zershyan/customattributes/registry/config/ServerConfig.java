package io.zershyan.customattributes.registry.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.Blacklist;
import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.DefaultRange;
import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.Enabled;
import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.Entries;
import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.General;
import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.ListMode;
import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.Ranges;
import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.SyncIntervalTicks;
import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.Whitelist;

/** 服务端权威配置：哪些属性可调、可调区间是什么。 */
public final class ServerConfig {
    /** 默认放行的属性：都是"调低只会更难受、不会让人卡死"的那一类。 */
    private static final List<String> DEFAULT_WHITELIST = List.of(
            // 移动与手感
            "minecraft:generic.movement_speed",
            "minecraft:generic.jump_strength",
            "minecraft:generic.step_height",
            "minecraft:generic.safe_fall_distance",
            "minecraft:player.sneaking_speed",
            "neoforge:swim_speed",
            "minecraft:generic.movement_efficiency",
            "minecraft:generic.water_movement_efficiency",
            // 战斗
            "minecraft:generic.attack_damage",
            "minecraft:generic.attack_speed",
            "minecraft:generic.attack_knockback",
            "minecraft:player.sweeping_damage_ratio",
            "minecraft:generic.armor",
            "minecraft:generic.armor_toughness",
            "minecraft:generic.max_health",
            // 交互与采集
            "minecraft:player.block_interaction_range",
            "minecraft:player.entity_interaction_range",
            "minecraft:player.block_break_speed",
            "minecraft:player.submerged_mining_speed",
            "minecraft:player.mining_efficiency"
    );

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.EnumValue<AttributeListMode> LIST_MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_RANGE;
    public static final ModConfigSpec.IntValue SYNC_INTERVAL_TICKS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> RANGE_OVERRIDES;

    static {
        // 配置键与语言条目共用同一个 name，避免两处手写字符串写岔
        BUILDER.translation(General.key()).comment("通用设置").push(General.name());
        ENABLED = BUILDER.translation(Enabled.key())
                .comment("是否启用本模组的功能")
                .define(Enabled.name(), true);
        LIST_MODE = BUILDER.translation(ListMode.key())
                .comment("用哪份名单筛选可调属性：WHITELIST 只有名单内的可调，BLACKLIST 只有名单内的不可调")
                .defineEnum(ListMode.name(), AttributeListMode.WHITELIST);
        WHITELIST = BUILDER.translation(Whitelist.key())
                .comment("白名单模式下只有这些属性显示且可调，其余一律不可调；清空列表即什么都不放开")
                .defineListAllowEmpty(Whitelist.name(), DEFAULT_WHITELIST, () -> "", o -> o instanceof String);
        BLACKLIST = BUILDER.translation(Blacklist.key())
                .comment("黑名单模式下这些属性完全不显示、也不可修改；白名单模式下本项被忽略")
                .defineListAllowEmpty(Blacklist.name(), List.<String>of(), () -> "", o -> o instanceof String);
        DEFAULT_RANGE = BUILDER.translation(DefaultRange.key())
                .comment("没有单独配置的属性使用这个范围；留空表示这些属性不出现在界面里")
                .define(DefaultRange.name(), "[min, current]");
        SYNC_INTERVAL_TICKS = BUILDER.translation(SyncIntervalTicks.key())
                .comment("属性检查间隔（刻）：把越界的数值夹回区间，并把数值/区间的变化推送给打开界面的玩家")
                .defineInRange(SyncIntervalTicks.name(), 5, 1, 20);
        BUILDER.pop();

        BUILDER.translation(Ranges.key()).comment("逐个属性覆盖范围").push(Ranges.name());
        RANGE_OVERRIDES = BUILDER.translation(Entries.key())
                .comment("每行一条，格式：属性ID=[下界, 上界]")
                .defineListAllowEmpty(Entries.name(), List.<String>of(), () -> "", o -> o instanceof String);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private ServerConfig() {
    }

    public static boolean isEnabled() {
        return SPEC.isLoaded() && ENABLED.get();
    }

    public static AttributeListMode listMode() {
        return SPEC.isLoaded() ? LIST_MODE.get() : AttributeListMode.WHITELIST;
    }

    public static int syncInterval() {
        return SPEC.isLoaded() ? SYNC_INTERVAL_TICKS.get() : 1;
    }

    public static String defaultRange() {
        return SPEC.isLoaded() ? DEFAULT_RANGE.get() : "";
    }

    public static List<? extends String> whitelist() {
        return SPEC.isLoaded() ? WHITELIST.get() : List.of();
    }

    public static List<? extends String> blacklist() {
        return SPEC.isLoaded() ? BLACKLIST.get() : List.of();
    }

    public static List<? extends String> rangeOverrides() {
        return SPEC.isLoaded() ? RANGE_OVERRIDES.get() : List.of();
    }
}
