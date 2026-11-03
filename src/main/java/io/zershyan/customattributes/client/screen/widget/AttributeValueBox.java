package io.zershyan.customattributes.client.screen.widget;

import io.zershyan.customattributes.client.ClientAttributeData;
import io.zershyan.customattributes.util.NumberFormats;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * 与滑块配对的数值输入框：回车提交精确值，失焦或数值变化时回填真实值。
 * <p>不用 {@code setResponder}：它每次文本变化都会回调，边打字边提交，而且回填文本时还会再触发一次。</p>
 */
public class AttributeValueBox extends EditBox {
    private final AttributeSlider slider;
    private boolean committing;

    public AttributeValueBox(Font font, AttributeSlider slider, int x, int y, int width, int height) {
        super(font, x, y, width, height, Component.empty());
        this.slider = slider;
        setMaxLength(20);
        setFilter(AttributeValueBox::looksNumeric);
        sync();
    }

    /** 把滑块当前值写进输入框；玩家正在输入时不打断。 */
    public void sync() {
        if (!isFocused()) {
            setValue(NumberFormats.format(slider.selectedValue()));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            commit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void setFocused(boolean focused) {
        boolean wasFocused = isFocused();
        super.setFocused(focused);
        if (wasFocused && !focused && !committing) {
            // 失焦等于放弃这次编辑，回填真实值
            sync();
        }
    }

    private void commit() {
        double value = parse(getValue());
        if (Double.isFinite(value)) {
            ClientAttributeData.submit(slider.attributeId(), false, value);
        }
        // 提交后保留玩家输入的内容，等服务端推送回来校正
        committing = true;
        setFocused(false);
        committing = false;
    }

    private static double parse(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private static boolean looksNumeric(String text) {
        return text.matches("-?\\d{0,12}(\\.\\d{0,6})?");
    }
}
