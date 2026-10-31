package io.zershyan.customattributes.registry;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.zershyan.customattributes.CustomAttributes;
import io.zershyan.customattributes.registry.command.AttributesCommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static net.minecraft.commands.Commands.literal;

public final class CustomAttributesCommands {

    private CustomAttributesCommands() {
    }

    public static void doRegister(IEventBus neoBus) {
        neoBus.addListener(CustomAttributesCommands::register);
    }

    private static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = literal(CustomAttributes.MODID);
        AttributesCommand.register(builder);
        event.getDispatcher().register(builder);
    }
}
