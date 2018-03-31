package com.mcsimonflash.sponge.cmdbuilder.type;

import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.args.CommandElement;

public abstract class ValueType<T> {

    private final String name;

    public ValueType(String name) {
        this.name = name;
    }

    public abstract CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException;

    public ValueTypeEntry getParam(Object object, String param) {
        return ValueTypes.STRING.createEntry(getString(object) + "#" + param);
    }

    public String getString(Object object) {
        return object.toString();
    }

    public final String getName() {
        return name;
    }

    public final ValueTypeEntry<T> createEntry(T value) {
        return new ValueTypeEntry<>(this, value);
    }

}