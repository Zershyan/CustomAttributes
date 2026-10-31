package io.zershyan.customattributes.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 属性数值的显示格式：最多四位小数并去掉尾随零。 */
public final class NumberFormats {

    private NumberFormats() {
    }

    public static String format(double value) {
        if (!Double.isFinite(value) || value == 0.0) {
            return value == 0.0 ? "0" : Double.toString(value);
        }
        return BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
