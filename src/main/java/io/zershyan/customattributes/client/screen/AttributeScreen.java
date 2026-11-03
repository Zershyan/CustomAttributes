package io.zershyan.customattributes.client.screen;

import io.zershyan.customattributes.attribute.AttributeEntry;
import io.zershyan.customattributes.client.ClientAttributeData;
import io.zershyan.customattributes.client.screen.widget.AttributeSlider;
import io.zershyan.customattributes.client.screen.widget.AttributeValueBox;
import io.zershyan.customattributes.datagen.lang.CustomAttributesKeyLang;
import io.zershyan.customattributes.util.NumberFormats;
import net.minecraft.ChatFormatting;
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
    private static final int PANEL_PADDING = 6;
    private static final int LABEL_COLOR = 0xE0E0E0;
    private static final int LABEL_MODIFIED_COLOR = 0xFFD98A;

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
        this.listWidth = Math.min(this.width - 32, 420);
        this.listLeft = (this.width - listWidth) / 2;
        this.listTop = 42;
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
            reset.active = entry.modified();

            addRenderableWidget(slider);
            addRenderableWidget(box);
            addRenderableWidget(reset);
            rows.add(new Row(entry.id(), name, trim(name), slider, box, reset, y));
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
                row.reset().active = row.slider().isModified();
                return;
            }
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        renderFrame(graphics);

        graphics.enableScissor(listLeft, listTop, listLeft + listWidth, listBottom);
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (!row.slider().visible) {
                continue;
            }
            int rowTop = row.slider().getY() - 2;
            if ((i & 1) == 0) {
                graphics.fill(listLeft - 2, rowTop, listLeft + listWidth + 2, rowTop + ROW_HEIGHT, 0x14FFFFFF);
            }
            graphics.drawString(this.font, row.label(), listLeft, row.slider().getY() + 5,
                    row.slider().isModified() ? LABEL_MODIFIED_COLOR : LABEL_COLOR, false);
        }
        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTick);
        }
        graphics.disableScissor();

        if (rows.isEmpty()) {
            graphics.drawCenteredString(this.font, CustomAttributesKeyLang.ScreenEmpty, this.width / 2, listTop + 12, 0xA0A0A0);
        }
        renderScrollBar(graphics);
        doneButton.render(graphics, mouseX, mouseY, partialTick);
        renderRowTooltip(graphics, mouseX, mouseY);
    }

    /** 标题、操作提示与列表面板，都在裁剪区之外画一次。 */
    private void renderFrame(GuiGraphics graphics) {
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, CustomAttributesKeyLang.ScreenHint, this.width / 2, 26, 0x909090);

        int left = listLeft - PANEL_PADDING;
        int right = listLeft + listWidth + PANEL_PADDING + 8;
        int top = listTop - PANEL_PADDING;
        int bottom = listBottom + PANEL_PADDING;
        graphics.fill(left, top, right, bottom, 0x66000000);
        graphics.renderOutline(left, top, right - left, bottom - top, 0x40FFFFFF);
    }

    private void renderRowTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (Row row : rows) {
            if (!row.slider().visible) {
                continue;
            }
            int rowTop = row.slider().getY();
            if (mouseY >= rowTop && mouseY < rowTop + WIDGET_HEIGHT
                    && mouseX >= listLeft && mouseX < listLeft + LABEL_WIDTH) {
                // 名称可能被截断，悬停给出完整名称与属性ID，方便对着写配置
                graphics.renderComponentTooltip(this.font, List.of(
                        row.fullName(),
                        Component.literal(row.id().toString()).withStyle(ChatFormatting.GRAY)), mouseX, mouseY);
                return;
            }
            if (row.slider().isMouseOver(mouseX, mouseY)) {
                graphics.renderTooltip(this.font, CustomAttributesKeyLang.ScreenRange.get(
                        NumberFormats.format(row.slider().rangeMin()),
                        NumberFormats.format(row.slider().rangeMax())), mouseX, mouseY);
                return;
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
        int x = listLeft + listWidth + PANEL_PADDING + 2;
        graphics.fill(x, listTop, x + 4, listBottom, 0x30FFFFFF);
        graphics.fill(x, barTop, x + 4, barTop + barHeight, 0xB0FFFFFF);
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

    private record Row(ResourceLocation id, Component fullName, Component label, AttributeSlider slider,
                       AttributeValueBox box, Button reset, int baseY) {
    }
}
