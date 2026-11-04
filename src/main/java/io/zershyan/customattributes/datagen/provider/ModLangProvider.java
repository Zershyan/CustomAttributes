package io.zershyan.customattributes.datagen.provider;

import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.datagen.lang.CustomAttributesLang;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLangProvider extends LanguageProvider {
    private final boolean chinese;

    private ModLangProvider(PackOutput output, String locale, boolean chinese) {
        super(output, CustomAttributes.MODID, locale);
        this.chinese = chinese;
    }

    public static ModLangProvider runZhCn(PackOutput output) {
        return new ModLangProvider(output, "zh_cn", true);
    }

    public static ModLangProvider runEnUs(PackOutput output) {
        return new ModLangProvider(output, "en_us", false);
    }

    @Override
    protected void addTranslations() {
        for (CustomAttributesLang.FinalEntry<?> entry : CustomAttributesLang.getAllLang()) {
            addTranslation(entry.key(), chinese ? entry.lang().zhCn() : entry.lang().enUs());
        }
    }

    /** 按模板约定分发对象类型：将来补 RegistryLang 时对象键也能直接落地。 */
    private void addTranslation(Object key, String description) {
        if (key instanceof Item item) {
            add(item, description);
        } else if (key instanceof Block block) {
            add(block, description);
        } else if (key instanceof ItemStack stack) {
            add(stack.getItem(), description);
        } else if (key instanceof MobEffect effect) {
            add(effect, description);
        } else if (key instanceof EntityType<?> type) {
            add(type, description);
        } else if (key instanceof String text) {
            add(text, description);
        } else {
            CustomAttributes.LOGGER.error("无法写入语言项，键类型不支持: {}", key.getClass());
            add(String.valueOf(key), description);
        }
    }
}
