package io.zershyan.customattributes.client.screen;

import io.zershyan.customattributes.attribute.AttributeEntry;
import io.zershyan.customattributes.client.ClientAttributeData;
import io.zershyan.customattributes.client.screen.widget.AttributeSlider;
import io.zershyan.customattributes.client.screen.widget.AttributeValueBox;
import io.zershyan.customattributes.datagen.lang.CustomAttributesKeyLang;
import io.zershyan.customattributes.util.NumberFormats;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 属性调整界面：每行一个滑块、一个精确输入框和一个重置按钮，只能在服务端给出的区间内改动。 */
public class AttributeScreen extends Screen {
    private static final int ROW_HEIGHT = 22;
    private static final int LABEL_WIDTH = 96;
    private static final int BOX_WIDTH = 52;
    private static final int RESET_WIDTH = 40;
    private static final int GAP = 4;
    private static final int WIDGET_HEIGHT = 18;

    private final List<Row> rows = new ArrayList<>();
    private int listLeft;
    private int listWidth;
    private int listTop;
    private int listBottom;
    private int scroll;
    private Button doneButton;

    public AttributeScreen() {
        super(CustomAttributesKeyLang.ScreenTitle);
    }

    @Override
    protected void init() {
        flushPending();
        rows.clear();
        this.listWidth = Math.min(this.width - 20, 420);
        this.listLeft = (this.width - listWidth) / 2;
        this.listTop = 32;
        this.listBottom = this.height - 34;

        int sliderLeft = listLeft + LABEL_WIDTH + GAP;
        int sliderWidth = Math.max(60, listWidth - LABEL_WIDTH - BOX_WIDTH - RESET_WIDTH - GAP * 3);
        int boxLeft = sliderLeft + sliderWidth + GAP;
        int resetLeft = listLeft + listWidth - RESET_WIDTH;
        int y = listTop;
        for (AttributeEntry entry : sortedEntries()) {
            AttributeSlider slider = new AttributeSlider(entry, sliderLeft, y, sliderWidth, WIDGET_HEIGHT);
            AttributeValueBox box = new AttributeValueBox(this.font, slider, boxLeft, y, BOX_WIDTH, WIDGET_HEIGHT);
            Runnable syncRow = () -> {
                box.sync();
                // 区间塌缩成一个点时没有可设的值，只留重置
                box.setEditable(slider.rangeMax() > slider.rangeMin());
            };
            slider.setOnValueChanged(syncRow);
            syncRow.run();
            Component name = attributeName(entry.id());
            Button reset = Button.builder(CustomAttributesKeyLang.ScreenReset, button -> {
                        slider.discardPending();
                        ClientAttributeData.submit(entry.id(), true, 0.0);
                    })
                    .bounds(resetLeft, y, RESET_WIDTH, WIDGET_HEIGHT)
                    .tooltip(Tooltip.create(CustomAttributesKeyLang.ScreenResetTooltip.get(name)))
                    .build();
            addRenderableWidget(slider);
            addRenderableWidget(box);
            addRenderableWidget(reset);
            rows.add(new Row(entry.id(), trim(name), slider, box, reset, y));
            y += ROW_HEIGHT;
        }

        // 底部按钮不进 renderables：它在列表裁剪区之外，由 render 单独绘制
        this.doneButton = addWidget(Button.builder(CustomAttributesKeyLang.ScreenDone, button -> this.onClose())
                .bounds(this.width / 2 - 60, this.height - 26, 120, 20)
                .build());
        this.scroll = Mth.clamp(this.scroll, 0, maxScroll());
        layoutRows();
    }

    /** 服务端重发列表时刷新整页。 */
    public void reload() {
        for (Row row : rows) {
            row.slider().discardPending();
        }
        this.rebuildWidgets();
    }

    public void onUpdated(AttributeEntry entry) {
        for (Row row : rows) {
            if (row.id().equals(entry.id())) {
                row.slider().onServerUpdate(entry);
                return;
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);

        graphics.enableScissor(listLeft, listTop, listLeft + listWidth, listBottom);
        for (Row row : rows) {
            if (row.slider().visible) {
                graphics.drawString(this.font, row.label(), listLeft, row.slider().getY() + 5, 0xE0E0E0, false);
            }
        }
        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        graphics.disableScissor();

        if (rows.isEmpty()) {
            graphics.drawCenteredString(this.font, CustomAttributesKeyLang.ScreenEmpty, this.width / 2, listTop + 8, 0xA0A0A0);
        }
        renderScrollBar(graphics);
        doneButton.render(graphics, mouseX, mouseY, partialTick);

        for (Row row : rows) {
            AttributeSlider slider = row.slider();
            if (slider.visible && slider.isMouseOver(mouseX, mouseY)) {
                graphics.renderTooltip(this.font, CustomAttributesKeyLang.ScreenRange.get(
                        NumberFormats.format(slider.rangeMin()), NumberFormats.format(slider.rangeMax())), mouseX, mouseY);
                break;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int max = maxScroll();
        if (max <= 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        int updated = Mth.clamp(scroll - (int) Math.round(scrollY * ROW_HEIGHT / 2.0), 0, max);
        if (updated != scroll) {
            this.scroll = updated;
            layoutRows();
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        flushPending();
        ClientAttributeData.close();
    }

    private void flushPending() {
        for (Row row : rows) {
            row.slider().flush();
        }
    }

    private void layoutRows() {
        for (Row row : rows) {
            int y = row.baseY() - scroll;
            boolean visible = y >= listTop - WIDGET_HEIGHT + 2 && y + WIDGET_HEIGHT <= listBottom;
            row.slider().setY(y);
            row.slider().visible = visible;
            row.box().setY(y);
            row.box().visible = visible;
            row.reset().setY(y);
            row.reset().visible = visible;
            if (!visible && row.box().isFocused()) {
                row.box().setFocused(false);
            }
        }
    }

    private void renderScrollBar(GuiGraphics graphics) {
        int max = maxScroll();
        if (max <= 0) {
            return;
        }
        int view = listBottom - listTop;
        int content = rows.size() * ROW_HEIGHT;
        int barHeight = Math.max(16, view * view / content);
        int barTop = listTop + (view - barHeight) * scroll / max;
        int x = listLeft + listWidth + 3;
        graphics.fill(x, listTop, x + 3, listBottom, 0x40000000);
        graphics.fill(x, barTop, x + 3, barTop + barHeight, 0xA0FFFFFF);
    }

    private int maxScroll() {
        return Math.max(0, rows.size() * ROW_HEIGHT - (listBottom - listTop));
    }

    private List<AttributeEntry> sortedEntries() {
        List<AttributeEntry> entries = new ArrayList<>(ClientAttributeData.entries());
        Map<ResourceLocation, String> names = new HashMap<>();
        for (AttributeEntry entry : entries) {
            names.put(entry.id(), attributeName(entry.id()).getString());
        }
        entries.sort(Comparator.comparing(entry -> names.get(entry.id()), String.CASE_INSENSITIVE_ORDER));
        return entries;
    }

    private Component attributeName(ResourceLocation id) {
        Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(id);
        return attribute == null ? Component.literal(id.toString()) : Component.translatable(attribute.getDescriptionId());
    }

    private Component trim(Component text) {
        String raw = text.getString();
        if (this.font.width(raw) <= LABEL_WIDTH - 4) {
            return text;
        }
        return Component.literal(this.font.plainSubstrByWidth(raw, LABEL_WIDTH - 12) + "...");
    }

    private record Row(ResourceLocation id, Component label, AttributeSlider slider, AttributeValueBox box,
                       Button reset, int baseY) {
    }
}
