package com.mcsimonflash.sponge.cmdbuilder.type;

import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.ConfigurationException;
import ninja.leaping.configurate.ConfigurationNode;

public abstract class ValueType<T> {

    private final String name;

    public ValueType(String name) {
        this.name = name;
    }

    public abstract T deserialize(ConfigurationNode node) throws ConfigurationException;

    public void serialize(ConfigurationNode node, T value) throws ConfigurationException {
        node.setValue(value);
    }

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

    public final ValueTypeEntry<T> deserializeEntry(ConfigurationNode node) {
        return new ValueTypeEntry<>(this, deserialize(node));
    }

}