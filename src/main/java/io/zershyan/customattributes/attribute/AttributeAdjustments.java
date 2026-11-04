package io.zershyan.customattributes.attribute;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

/**
 * 用一个本模组专属的修饰器把属性最终值钉在指定数值上。
 * <p>因为调整目标是绝对值而非固定偏移，装备、药水或其它模组的乘法修饰变化后，
 * 重新求解即可让最终值回到目标值，不会跟着一起漂移。</p>
 * <p>用 permanent 修饰器，随玩家 NBT 一起保存。</p>
 */
public final class AttributeAdjustments {
    /** 本模组表达调整量用的修饰器 ID，每个属性各持一份同 ID 的修饰器。 */
    private static final UUID ADJUSTMENT_ID = UUID.fromString("8d3f0b6a-7c1e-4f1a-9b0e-c0a5b7d2f3a1");
    private static final String ADJUSTMENT_NAME = "customattributes:adjustment";
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
     * 让最终值等于 {@code target}：按当前的外部修饰反解出需要的偏移量，写成 ADDITION 修饰器。
     *
     * @return 是否成功；属性被外部修饰钉死（合成系数为 0）时返回 false
     */
    public static boolean setValue(AttributeInstance instance, double target) {
        double factor = multiplier(instance);
        if (!Double.isFinite(factor) || Math.abs(factor) <= EPSILON) {
            return false;
        }
        Attribute attribute = instance.getAttribute();
        double offset = attribute.sanitizeValue(target) / factor - instance.getBaseValue() - addition(instance);
        if (Math.abs(offset) <= EPSILON) {
            clear(instance);
            return true;
        }
        instance.removeModifier(ADJUSTMENT_ID);
        instance.addPermanentModifier(
                new AttributeModifier(ADJUSTMENT_ID, ADJUSTMENT_NAME, offset, AttributeModifier.Operation.ADDITION));
        return true;
    }

    /** 按属性自身的计算公式求值，可选择是否把本模组的调整算进去。 */
    private static double evaluate(AttributeInstance instance, double base, boolean includeAdjustment) {
        double addition = 0;
        double multipliedBase = 0;
        double multipliedTotal = 1;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (!includeAdjustment && isOurs(modifier)) {
                continue;
            }
            switch (modifier.getOperation()) {
                case ADDITION -> addition += modifier.getAmount();
                case MULTIPLY_BASE -> multipliedBase += modifier.getAmount();
                case MULTIPLY_TOTAL -> multipliedTotal *= 1.0 + modifier.getAmount();
            }
        }
        double value = (base + addition) * (1.0 + multipliedBase) * multipliedTotal;
        return instance.getAttribute().sanitizeValue(value);
    }

    /** 除本模组之外所有 ADDITION 修饰之和。 */
    private static double addition(AttributeInstance instance) {
        double sum = 0;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (!isOurs(modifier) && modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                sum += modifier.getAmount();
            }
        }
        return sum;
    }

    /** 除本模组之外所有乘法修饰的合成系数。 */
    private static double multiplier(AttributeInstance instance) {
        double multipliedBase = 0;
        double multipliedTotal = 1;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (isOurs(modifier)) {
                continue;
            }
            switch (modifier.getOperation()) {
                case MULTIPLY_BASE -> multipliedBase += modifier.getAmount();
                case MULTIPLY_TOTAL -> multipliedTotal *= 1.0 + modifier.getAmount();
                case ADDITION -> {
                }
            }
        }
        return (1.0 + multipliedBase) * multipliedTotal;
    }

    private static boolean isOurs(AttributeModifier modifier) {
        return ADJUSTMENT_ID.equals(modifier.getId());
    }
}
