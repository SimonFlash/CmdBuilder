package com.mcsimonflash.sponge.cmdbuilder.type;

import com.mcsimonflash.sponge.cmdcontrol.teslalibs.argument.parser.ValueParser;
import ninja.leaping.configurate.ConfigurationNode;

public abstract class ParserType<T> {

    private final String name;
    private final ValueType<T> type;

    public ParserType(String name, ValueType<T> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public abstract ValueParser<T> getParser(ConfigurationNode meta);

    public ValueType<T> getType() {
        return type;
    }

}