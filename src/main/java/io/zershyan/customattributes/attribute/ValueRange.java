package io.zershyan.customattributes.attribute;

/**
 * 一次求值得到的可调区间。
 * {@code min < max} 才能调整；{@code min == max} 表示区间塌缩成一个点，只能查看与恢复默认；{@code min > max} 属于方向不成立。
 */
public record ValueRange(double min, double max) {

    public boolean isAscending() {
        return min < max;
    }

    /** 求值出 NaN 或下界大于上界都算无效。 */
    public boolean isInverted() {
        return Double.isNaN(min) || Double.isNaN(max) || min > max;
    }

    public double clamp(double value) {
        return Math.max(min, Math.min(max, value));
    }
}
