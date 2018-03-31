package com.mcsimonflash.sponge.cmdbuilder.script;

import com.mcsimonflash.sponge.cmdbuilder.type.ValueType;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.NodeUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import static com.google.common.base.Preconditions.checkArgument;

public class Argument<T> {

    private String name;
    private ValueType<T> type;
    private ConfigurationNode meta;
    private CommandElement element;

    private Argument(String name, ValueType<T> type, ConfigurationNode meta) throws IllegalArgumentException {
        this.name = name;
        this.type = type;
        this.meta = meta;
        this.element = type.getCmdElem(name, meta);
    }

    public ValueTypeEntry<T> collectArg(CommandContext args) throws IllegalArgumentException {
        return type.createEntry(args.<T>getOne(name).orElseThrow(() -> new IllegalArgumentException("No argument found for name '" + name + "'.")));
    }

    public String getName() {
        return name;
    }
    public ValueType getType() {
        return type;
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
        private ValueType<T> type;
        private ConfigurationNode meta = SimpleConfigurationNode.root();

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> type(ValueType<T> type) {
            this.type = type;
            return this;
        }

        public Builder<T> meta(ConfigurationNode meta) {
            this.meta = NodeUtils.copy(meta);
            return this;
        }

        public Argument<T> build() {
            checkArgument(name != null && !name.isEmpty(), "'name' must be defined");
            checkArgument(type != null, "'type' must be defined.");
            return new Argument<>(name, type, meta);
        }

    }

}