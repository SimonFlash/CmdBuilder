package com.mcsimonflash.sponge.cmdcontrol.objects.elements;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;

public class RangedDouble extends CommandElement {

    private Range<Double> range;

    public RangedDouble(@Nullable Text key, Range<Double> range) {
        super(key);
        this.range = range;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String arg = args.next();
        try {
            double num = Double.parseDouble(arg);
            if (range.contains(num)) {
                return num;
            }
            throw args.createError(Text.of("Integer %s is not within range %s.", num, range));
        } catch (NumberFormatException ignored) {
            throw args.createError(Text.of("Argument %s is not an Integer.", arg));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Lists.newArrayList();
    }

}