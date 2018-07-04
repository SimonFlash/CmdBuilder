package com.mcsimonflash.sponge.cmdbuilder.command.parser;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mcsimonflash.sponge.cmdbuilder.type.ParserType;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.argument.parser.StandardParser;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public class MetaParser extends StandardParser<ValueTypeEntry> {

    public static final MetaParser PARSER = new MetaParser(ImmutableMap.of());
    private static final ConfigurationNode ROOT = SimpleConfigurationNode.root();

    private MetaParser(ImmutableMap<String, String> messages) {
        super(messages);
    }

    @Override
    public void parse(Text key, CommandSource src, CommandArgs args, CommandContext ctx) throws ArgumentParseException {
        ctx.putArg(key, ctx.<ParserType>getOne("type").get().getParser(ROOT).parseValue(src, args));
    }

    @Override
    public ValueTypeEntry parseValue(CommandSource src, CommandArgs args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ImmutableList<String> complete(CommandSource src, CommandArgs args, CommandContext ctx) {
        return ctx.<ParserType>getOne("type").get().getParser(ROOT).complete(src, args, ctx);
    }

}