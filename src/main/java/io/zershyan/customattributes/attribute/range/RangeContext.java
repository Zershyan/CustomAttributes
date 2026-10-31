package io.zershyan.customattributes.attribute.range;

/**
 * 范围表达式求值时可见的数值。
 *
 * @param current      属性最终值
 * @param base         属性基础值
 * @param defaultValue 该实体类型上的默认基础值
 * @param min          属性硬下限
 * @param max          属性硬上限
 */
public record RangeContext(double current, double base, double defaultValue, double min, double max) {
}
