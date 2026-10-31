package io.zershyan.customattributes.client.screen.widget;

import io.zershyan.customattributes.client.ClientAttributeData;
import io.zershyan.customattributes.util.NumberFormats;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** 与滑块配对的数值输入框：回车提交精确值，失焦或数值变化时回填真实值。 */
public class AttributeValueBox extends EditBox {
    private final AttributeSlider slider;

    public AttributeValueBox(Font font, AttributeSlider slider, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.empty());
        this.slider = slider;
        setMaxLength(20);
        setFilter(AttributeValueBox::looksNumeric);
        setResponder(this::submit);
        sync();
    }

    /** 把滑块当前值写进输入框；玩家正在输入时不打断。 */
    public void sync() {
        if (!isFocused()) {
            setValue(NumberFormats.format(slider.selectedValue()));
        }
    }

    @Override
    public void setFocused(boolean focused) {
        boolean wasFocused = isFocused();
        super.setFocused(focused);
        if (wasFocused && !focused) {
            // 失焦等于放弃这次编辑，回填真实值
            sync();
        }
    }

    private void submit(String text) {
        double value = parse(text);
        if (Double.isFinite(value)) {
            ClientAttributeData.submit(slider.attributeId(), false, value);
        }
        setFocused(false);
        sync();
    }

    private static double parse(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static boolean looksNumeric(String text) {
        return text.isEmpty() || text.matches("-?\\d{0,12}(\\.\\d{0,6})?");
    }
}
