package com.mcsimonflash.sponge.cmdcontrol.objects.scripts;

import com.google.common.base.Preconditions;
import com.mcsimonflash.sponge.cmdcontrol.api.ValueType;
import com.mcsimonflash.sponge.cmdcontrol.api.ValueTypeEntry;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;

public class Argument {

    private String name;
    private ValueType type;
    private CommentedConfigurationNode meta;
    private CommandElement element;

    public Argument(@Nullable String name, @Nullable ValueType type, @Nullable CommentedConfigurationNode meta) {
        Preconditions.checkNotNull(name, "Argument name cannot be null.");
        Preconditions.checkArgument(!name.isEmpty(), "Argument name cannot be empty.");
        this.name = name;
        Preconditions.checkNotNull(type, "Argument type cannot be null.");
        this.type = type;
        Preconditions.checkNotNull(meta, "Argument meta cannot be null.");
        this.meta = meta;
        this.element = type.getCmdElem(Text.of(name), meta);
    }

    public String getName() {
        return name;
    }
    public ValueType getType() {
        return type;
    }
    public CommentedConfigurationNode getMeta() {
        return meta;
    }
    public CommandElement getElement() {
        return element;
    }

    public ValueTypeEntry collectArg(CommandContext args) throws IllegalArgumentException {
        return new ValueTypeEntry(type, args.getOne(name).orElseThrow(() -> new IllegalArgumentException("No argument found for name \"" + name + "\".")));
    }

}