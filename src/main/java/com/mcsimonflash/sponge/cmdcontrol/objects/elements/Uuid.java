package com.mcsimonflash.sponge.cmdcontrol.objects.elements;

import com.google.common.collect.Lists;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class Uuid extends CommandElement {

    public Uuid(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String arg = args.next();
        try {
            arg = arg.length() == 32 ? arg = arg.substring(0, 8) + "-" + arg.substring(8, 12) + "-" + arg.substring(12, 16) + "-" + arg.substring(16, 20) + "-" + arg.substring(20) : arg;
            return UUID.fromString(arg);
        } catch (IllegalArgumentException ignored) {
            throw args.createError(Text.of("Argument \"" + arg + "\" is not a valid Uuid."));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Lists.newArrayList();
    }

}
