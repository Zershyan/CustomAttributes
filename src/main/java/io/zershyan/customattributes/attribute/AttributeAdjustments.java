package io.zershyan.customattributes.attribute;

import io.zershyan.customattributes.CustomAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 用一个本模组专属的修饰器把属性最终值钉在指定数值上。
 * <p>因为调整目标是绝对值而非固定偏移，装备、药水或其它模组的乘法修饰变化后，
 * 重新求解即可让最终值回到目标值，不会跟着一起漂移。</p>
 * <p>用 permanent 修饰器，随玩家 NBT 一起保存。</p>
 */
public final class AttributeAdjustments {
    /** 本模组表达调整量用的修饰器 ID，每个属性各持一份同 ID 的修饰器。 */
    private static final ResourceLocation ADJUSTMENT_ID = CustomAttributes.id("adjustment");
    private static final double EPSILON = 1.0e-9;

    private AttributeAdjustments() {
    }

    /** 不含本模组调整的最终值：玩家怎么调都返回同一个数，只有装备、药水等外部修饰会改变它。 */
    public static double unadjustedValue(AttributeInstance instance) {
        return evaluate(instance, instance.getBaseValue(), false);
    }

    /** 撤掉调整，回到未调整状态。 */
    public static void clear(AttributeInstance instance) {
        instance.removeModifier(ADJUSTMENT_ID);
    }

    /**
     * 让最终值等于 {@code target}：按当前的外部修饰反解出需要的偏移量，写成 ADD_VALUE 修饰器。
     *
     * @return 是否成功；属性被外部修饰钉死（合成系数为 0）时返回 false
     */
    public static boolean setValue(AttributeInstance instance, double target) {
        double factor = multiplier(instance);
        if (!Double.isFinite(factor) || Math.abs(factor) <= EPSILON) {
            return false;
        }
        Attribute attribute = instance.getAttribute().value();
        double offset = attribute.sanitizeValue(target) / factor - instance.getBaseValue() - addition(instance);
        if (Math.abs(offset) <= EPSILON) {
            clear(instance);
            return true;
        }
        instance.addOrReplacePermanentModifier(
                new AttributeModifier(ADJUSTMENT_ID, offset, AttributeModifier.Operation.ADD_VALUE));
        return true;
    }

    /** 按属性自身的计算公式求值，可选择是否把本模组的调整算进去。 */
    private static double evaluate(AttributeInstance instance, double base, boolean includeAdjustment) {
        double addition = 0;
        double multipliedBase = 0;
        double multipliedTotal = 1;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (!includeAdjustment && modifier.id().equals(ADJUSTMENT_ID)) {
                continue;
            }
            switch (modifier.operation()) {
                case ADD_VALUE -> addition += modifier.amount();
                case ADD_MULTIPLIED_BASE -> multipliedBase += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> multipliedTotal *= 1.0 + modifier.amount();
            }
        }
        double value = (base + addition) * (1.0 + multipliedBase) * multipliedTotal;
        return instance.getAttribute().value().sanitizeValue(value);
    }

    /** 除本模组之外所有 ADD_VALUE 修饰之和。 */
    private static double addition(AttributeInstance instance) {
        double sum = 0;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (!modifier.id().equals(ADJUSTMENT_ID) && modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                sum += modifier.amount();
            }
        }
        return sum;
    }

    /** 除本模组之外所有乘法修饰的合成系数。 */
    private static double multiplier(AttributeInstance instance) {
        double multipliedBase = 0;
        double multipliedTotal = 1;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.id().equals(ADJUSTMENT_ID)) {
                continue;
            }
            switch (modifier.operation()) {
                case ADD_MULTIPLIED_BASE -> multipliedBase += modifier.amount();
                case ADD_MULTIPLIED_TOTAL -> multipliedTotal *= 1.0 + modifier.amount();
                case ADD_VALUE -> {
                }
            }
        }
        return (1.0 + multipliedBase) * multipliedTotal;
    }
}
