package io.zershyan.customattributes.registry.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.zershyan.customattributes.attribute.AttributeManager;
import io.zershyan.customattributes.datagen.lang.CustomAttributesKeyLang;
import io.zershyan.customattributes.registry.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/** /customattributes —— 给执行命令的玩家打开属性调整界面。 */
public final class AttributesCommand {

    private AttributesCommand() {
    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> parent) {
        parent.executes(AttributesCommand::open);
    }

    private static int open(CommandContext<CommandSourceStack> context) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (CommandSyntaxException e) {
            context.getSource().sendFailure(CustomAttributesKeyLang.CommandPlayerOnly);
            return 0;
        }
        if (!ServerConfig.isEnabled()) {
            context.getSource().sendFailure(CustomAttributesKeyLang.CommandDisabled);
            return 0;
        }
        AttributeManager.open(player);
        context.getSource().sendSuccess(() -> CustomAttributesKeyLang.CommandOpened, false);
        return 1;
    }
}
