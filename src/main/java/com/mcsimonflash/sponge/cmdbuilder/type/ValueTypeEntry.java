package com.mcsimonflash.sponge.cmdbuilder.type;

public class ValueTypeEntry<T> {

    private final ValueType<T> type;
    private final T value;

    public ValueTypeEntry(ValueType<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public ValueType<T> getType() {
        return type;
    }

    public T getValue() {
        return value;
    }

    public String getString() {
        return type.getString(value);
    }

    public ValueTypeEntry getParam(String param) {
        return type.getParam(value, param);
    }

}