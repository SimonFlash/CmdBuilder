package com.mcsimonflash.sponge.cmdbuilder.script;

import com.mcsimonflash.sponge.cmdbuilder.type.ParserType;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.NodeUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;

import static com.google.common.base.Preconditions.checkArgument;

public class Argument<T> {

    private String name;
    private ParserType<T> parser;
    private ConfigurationNode meta;
    private CommandElement element;

    private Argument(String name, ParserType<T> parser, ConfigurationNode meta) throws IllegalArgumentException {
        this.name = name;
        this.parser = parser;
        this.meta = meta;
        this.element = parser.getParser(meta).toElement(name);
    }

    public ValueTypeEntry<T> collectArg(CommandContext args) throws IllegalArgumentException {
        return parser.getType().createEntry(args.<T>getOne(name).orElseThrow(() -> new IllegalArgumentException("No argument found for name '" + name + "'.")));
    }

    public String getName() {
        return name;
    }
    public ParserType<T> getParser() {
        return parser;
    }
    public ConfigurationNode getMeta() {
        return NodeUtils.copy(meta);
    }
    public CommandElement getElement() {
        return element;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {

        private String name;
        private ParserType<T> parser;
        private ConfigurationNode meta;

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> parser(ParserType<T> parser) {
            this.parser = parser;
            return this;
        }

        public Builder<T> meta(ConfigurationNode meta) {
            this.meta = NodeUtils.copy(meta);
            return this;
        }

        public Argument<T> build() {
            checkArgument(name != null && !name.isEmpty(), "'name' must be defined");
            checkArgument(parser != null, "'parser' must be defined.");
            return new Argument<>(name, parser, meta != null ? meta : SimpleConfigurationNode.root());
        }

    }

}