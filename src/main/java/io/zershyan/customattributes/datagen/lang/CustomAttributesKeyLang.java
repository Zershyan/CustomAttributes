package io.zershyan.customattributes.datagen.lang;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.datagen.util.LazyComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class CustomAttributesKeyLang extends CustomAttributesLang {
    private static final List<FinalEntry<String>> TranslatableLang = new ArrayList<>();
    private static final String ModId = CustomAttributes.MODID;
    private static final String ModName = "Custom Attributes";

    public static final MutableComponent Resource = entry(ModId + ".resources", ModName + " resources", ModName + "资源");

    private static final String ScreenPrefix = "screen." + ModId + ".";
    private static final String CommandPrefix = "command." + ModId + ".";

    public static final String KeyCategory = entryString(getKeyDesc("category"), ModName, "自定义属性");
    public static final String KeyOpen = entryString(getKeyDesc("open"), "Open Attribute Screen", "打开属性调整界面");

    public static final MutableComponent ScreenTitle = entry(ScreenPrefix + "title", "Attributes", "属性调整");
    public static final MutableComponent ScreenHint = entry(ScreenPrefix + "hint",
            "Drag or type a value; Reset restores the default.",
            "拖动滑块，或在输入框填数值后回车。重置恢复默认值。");
    public static final MutableComponent ScreenEmpty = entry(ScreenPrefix + "empty", "No attribute can be edited", "没有可调整的属性");
    public static final MutableComponent ScreenReset = entry(ScreenPrefix + "reset", "Reset", "重置");
    public static final MutableComponent ScreenDone = entry(ScreenPrefix + "done", "Done", "完成");
    public static final LazyComponent ScreenRange = entryLazy(ScreenPrefix + "range", "Editable range: %s ~ %s", "可调范围：%s ~ %s");
    public static final LazyComponent ScreenResetTooltip = entryLazy(ScreenPrefix + "reset.tooltip",
            "Restore the base value of %s to its default (equipment and potion modifiers stay)",
            "把「%s」的基础值恢复为默认值（装备与药水的修饰不受影响）");

    public static final MutableComponent CommandPlayerOnly = entry(CommandPrefix + "player_only",
            "Only a player can open the attribute screen",
            "只有玩家可以打开属性调整界面");
    public static final MutableComponent CommandOpened = entry(CommandPrefix + "opened",
            "Attribute screen opened",
            "已打开属性调整界面");
    public static final MutableComponent CommandDisabled = entry(CommandPrefix + "disabled",
            "Custom attributes are disabled on this server",
            "服务端已关闭自定义属性功能");

    private static String entryString(String key, String enUs, String zhCn) {
        TranslatableLang.add(new FinalEntry<>(key, enUs, zhCn));
        return key;
    }

    private static MutableComponent entry(String key, String enUs, String zhCn) {
        return Component.translatable(entryString(key, enUs, zhCn));
    }

    private static LazyComponent entryLazy(String key, String enUs, String zhCn) {
        return new LazyComponent(entryString(key, enUs, zhCn));
    }

    @Override
    protected List<Entry> init(List<Entry> entries) {
        entries.addAll(TranslatableLang);
        return entries;
    }
}
