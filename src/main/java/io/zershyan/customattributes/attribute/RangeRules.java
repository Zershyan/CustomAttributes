package io.zershyan.customattributes.attribute;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.attribute.range.RangeContext;
import io.zershyan.customattributes.attribute.range.RangeParser;
import io.zershyan.customattributes.registry.config.AttributeListMode;
import io.zershyan.customattributes.registry.config.ServerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang.DefaultRange;

/**
 * 把配置文本翻译成某个属性当前的 {@link ValueRange}。
 * 解析结果常驻内存，由调用方在配置可能变化时调用 {@link #reload()} 刷新，
 * 这样逐属性求值只剩字段访问与一次表达式计算。
 */
public final class RangeRules {

    private static List<? extends String> cachedOverrides;
    private static Map<ResourceLocation, RangeParser.ParsedRange> overrides = Map.of();
    private static String cachedDefault;
    private static RangeParser.ParsedRange defaultRange;
    private static AttributeListMode cachedMode;
    private static AttributeListMode mode = AttributeListMode.WHITELIST;
    private static List<? extends String> cachedWhitelist;
    private static Set<ResourceLocation> whitelist = Set.of();
    private static List<? extends String> cachedBlacklist;
    private static Set<ResourceLocation> blacklist = Set.of();
    /** 夹一次也进不了区间的属性：配置自相矛盾或数值被修饰锁死，停用直到配置改动 */
    private static final Set<ResourceLocation> unsatisfiable = new HashSet<>();
    private static boolean loaded;

    private RangeRules() {
    }

    /** 配置可能变化时调用：内容没变则什么都不做。 */
    public static void reload() {
        boolean changed = false;

        AttributeListMode rawMode = ServerConfig.listMode();
        if (rawMode != cachedMode) {
            cachedMode = rawMode;
            mode = rawMode;
            changed = true;
        }

        List<? extends String> rawWhitelist = ServerConfig.whitelist();
        if (!rawWhitelist.equals(cachedWhitelist)) {
            cachedWhitelist = List.copyOf(rawWhitelist);
            whitelist = parseIds(cachedWhitelist, "白名单");
            changed = true;
        }

        List<? extends String> rawBlacklist = ServerConfig.blacklist();
        if (!rawBlacklist.equals(cachedBlacklist)) {
            cachedBlacklist = List.copyOf(rawBlacklist);
            blacklist = parseIds(cachedBlacklist, "黑名单");
            changed = true;
        }

        String rawDefault = ServerConfig.defaultRange();
        if (!rawDefault.equals(cachedDefault)) {
            cachedDefault = rawDefault;
            defaultRange = parseOrNull(rawDefault, DefaultRange.name());
            changed = true;
        }

        List<? extends String> rawOverrides = ServerConfig.rangeOverrides();
        if (!rawOverrides.equals(cachedOverrides)) {
            cachedOverrides = List.copyOf(rawOverrides);
            Map<ResourceLocation, RangeParser.ParsedRange> parsed = new HashMap<>();
            for (String line : cachedOverrides) {
                parseOverride(line, parsed);
            }
            overrides = parsed;
            changed = true;
        }

        if (changed) {
            unsatisfiable.clear();
        }
        loaded = true;
    }

    /** 记录一个无论怎么调都进不了区间的属性，配置改动后自动重新尝试。 */
    public static void markUnsatisfiable(ResourceLocation id) {
        if (unsatisfiable.add(id)) {
            CustomAttributes.LOGGER.warn("属性 {} 的区间无法通过调整基础值满足，已停止对它的限制；修改相关配置后会重新尝试", id);
        }
    }

    /**
     * @param id           属性ID，由调用方缓存好，避免每次求值都从 Holder 上取键
     * @param defaultValue 该实体类型上的默认基础值，用于 {@code default} 通配符
     * @return 可调区间；名单未放行、属性非区间属性或配置算出的方向反过来（下界大于上界）时为空
     */
    public static Optional<ValueRange> rangeOf(AttributeInstance instance, ResourceLocation id, double defaultValue) {
        if (!loaded) {
            reload();
        }
        if (unsatisfiable.contains(id) || !isAllowed(id)) {
            return Optional.empty();
        }
        if (!(instance.getAttribute().value() instanceof RangedAttribute ranged)) {
            return Optional.empty();
        }
        RangeParser.ParsedRange parsed = overrides.get(id);
        if (parsed == null) {
            parsed = defaultRange;
        }
        if (parsed == null) {
            return Optional.empty();
        }

        RangeContext context = new RangeContext(
                instance.getValue(), instance.getBaseValue(), defaultValue, ranged.getMinValue(), ranged.getMaxValue());
        double lower = Math.max(parsed.lower().evaluate(context), ranged.getMinValue());
        double upper = Math.min(parsed.upper().evaluate(context), ranged.getMaxValue());
        ValueRange range = new ValueRange(lower, upper);
        // 下界大于上界属于"大到小"，按约定直接禁用该属性
        return range.isInverted() ? Optional.empty() : Optional.of(range);
    }

    /** 白名单模式只放行名单内的属性，黑名单模式只拦名单内的属性。 */
    private static boolean isAllowed(ResourceLocation id) {
        return mode == AttributeListMode.WHITELIST ? whitelist.contains(id) : !blacklist.contains(id);
    }

    private static Set<ResourceLocation> parseIds(List<? extends String> entries, String source) {
        Set<ResourceLocation> ids = new HashSet<>();
        for (String entry : entries) {
            ResourceLocation id = ResourceLocation.tryParse(entry.trim());
            if (id != null) {
                ids.add(id);
            } else {
                CustomAttributes.LOGGER.warn("忽略无效的{}条目（不是合法的属性ID）: {}", source, entry);
            }
        }
        return ids;
    }

    private static void parseOverride(String line, Map<ResourceLocation, RangeParser.ParsedRange> target) {
        int separator = line.indexOf('=');
        if (separator <= 0) {
            CustomAttributes.LOGGER.warn("忽略无效的属性范围配置（缺少属性ID或'='）: {}", line);
            return;
        }
        ResourceLocation id = ResourceLocation.tryParse(line.substring(0, separator).trim());
        if (id == null) {
            CustomAttributes.LOGGER.warn("忽略无效的属性范围配置（属性ID非法）: {}", line);
            return;
        }
        RangeParser.ParsedRange parsed = parseOrNull(line.substring(separator + 1).trim(), "属性 " + id);
        if (parsed != null) {
            target.put(id, parsed);
        }
    }

    private static RangeParser.ParsedRange parseOrNull(String text, String source) {
        if (text.isBlank()) {
            return null;
        }
        try {
            return RangeParser.parse(text);
        } catch (IllegalArgumentException e) {
            CustomAttributes.LOGGER.warn("忽略无法解析的范围（{}）: {}", source, e.getMessage());
            return null;
        }
    }
}
