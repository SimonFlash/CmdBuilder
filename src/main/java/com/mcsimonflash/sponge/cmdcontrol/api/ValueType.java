package com.mcsimonflash.sponge.cmdcontrol.api;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

public abstract class ValueType {

    private final String name;
    public final String getName() {
        return name;
    }

    public ValueType(String name) {
        this.name = name;
    }

    public abstract CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException;

    public ValueTypeEntry getParam(Object object, String param) {
        return new ValueTypeEntry(ValueTypes.STRING, getString(object) + "#" + param);
    }

    public String getString(Object object) {
        return object.toString();
    }

}