package com.mcsimonflash.sponge.cmdcontrol.api;

public class ValueTypeEntry {

    private ValueType type;
    private Object value;

    public ValueTypeEntry(ValueType type, Object value) {
        this.type = type;
        this.value = value;
    }

    public ValueType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String getString() {
        return type.getString(value);
    }

    public ValueTypeEntry getParam(String param) {
        return type.getParam(value, param);
    }

}