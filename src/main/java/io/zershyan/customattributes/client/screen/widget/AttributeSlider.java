package io.zershyan.customattributes.client.screen.widget;

import io.zershyan.customattributes.attribute.AttributeEntry;
import io.zershyan.customattributes.client.ClientAttributeData;
import io.zershyan.customattributes.util.NumberFormats;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** 一行属性滑块：拖动只改本地显示，松手时提交。区间塌缩成一个点时不可拖动，数值以旁边的输入框为准。 */
public class AttributeSlider extends AbstractSliderButton {
    private final ResourceLocation attributeId;
    private double rangeMin;
    private double rangeMax;
    private double actualValue;
    private boolean modified;
    private boolean pending;
    /** 拖动期间收到的推送先存着，提交后再应用，避免打断拖动也避免丢掉新区间 */
    private AttributeEntry deferred;
    private Runnable onValueChanged = () -> {
    };

    public AttributeSlider(AttributeEntry entry, int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), normalize(entry.value(), entry.min(), entry.max()));
        this.attributeId = entry.id();
        apply(entry);
    }

    public ResourceLocation attributeId() {
        return attributeId;
    }

    public double rangeMin() {
        return rangeMin;
    }

    public double rangeMax() {
        return rangeMax;
    }

    /** 该属性的基础值是否已被改离默认值。 */
    public boolean isModified() {
        return modified;
    }

    public double selectedValue() {
        return rangeMax > rangeMin ? rangeMin + this.value * (rangeMax - rangeMin) : actualValue;
    }

    /** 数值变化时通知同一行的输入框同步显示。 */
    public void setOnValueChanged(Runnable listener) {
        this.onValueChanged = listener;
    }

    /** 服务端推送的新数值与区间；正在拖动时留到提交后再应用。 */
    public void onServerUpdate(AttributeEntry entry) {
        if (pending) {
            this.deferred = entry;
            return;
        }
        apply(entry);
    }

    /** 提交未发送的拖动结果，并把拖动期间被搁置的服务端状态补上。 */
    public void flush() {
        if (!pending) {
            return;
        }
        pending = false;
        double target = selectedValue();
        ClientAttributeData.submit(attributeId, false, target);
        AttributeEntry queued = deferred;
        deferred = null;
        if (queued != null) {
            apply(queued);
        }
    }

    public void discardPending() {
        pending = false;
        deferred = null;
        onValueChanged.run();
    }

    @Override
    protected void updateMessage() {
        // 滑块上也显示数值：拖动时不用去看输入框，朗读提示也依赖它
        setMessage(Component.literal(NumberFormats.format(selectedValue())));
    }

    @Override
    protected void applyValue() {
        pending = true;
        onValueChanged.run();
    }

    /** 松手即提交，拖动过程只改本地显示。 */
    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        flush();
    }

    private void apply(AttributeEntry entry) {
        this.actualValue = entry.value();
        this.rangeMin = entry.min();
        this.rangeMax = entry.max();
        this.modified = entry.modified();
        this.active = rangeMin < rangeMax;
        this.value = normalize(actualValue, rangeMin, rangeMax);
        updateMessage();
        onValueChanged.run();
    }

    private static double normalize(double value, double min, double max) {
        return max > min ? Mth.clamp((value - min) / (max - min), 0.0, 1.0) : 0.0;
    }
}
