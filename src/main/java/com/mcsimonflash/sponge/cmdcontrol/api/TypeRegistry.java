package com.mcsimonflash.sponge.cmdcontrol.api;

import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import java.lang.IllegalArgumentException;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class TypeRegistry {

    private static Map<String, Entry<PluginContainer, ValueType>> registry = Maps.newHashMap();

    public static Optional<ValueType> getType(String name) {
        Entry<PluginContainer, ValueType> type = registry.get(name.toLowerCase());
        return type != null ? Optional.of(type.getValue()) : Optional.empty();
    }

    public static ValueType getTypeOrThrow(String name) throws IllegalArgumentException {
        return getType(name).orElseThrow(() -> new IllegalArgumentException("ValueType \"" + name + "\" does not exist."));
    }

    public static Optional<PluginContainer> getContainer(String name) {
        Entry<PluginContainer, ValueType> type = registry.get(name.toLowerCase());
        return type != null ? Optional.of(type.getKey()) : Optional.empty();
    }

    public static PluginContainer getContainerOrThrow(String name) throws IllegalArgumentException {
        return getContainer(name).orElseThrow(() -> new IllegalArgumentException("Container for \"" + name + "\" does not exist."));
    }

    public static boolean register(PluginContainer container, ValueType type) {
        return registerExtended(container, type) && registerSimple(container, type);
    }

    public static boolean registerExtended(PluginContainer container, ValueType type) {
        return registry.putIfAbsent(container.getId() + ":" + type.getName().toLowerCase(), Maps.immutableEntry(container, type)) == null;
    }

    public static boolean registerSimple(PluginContainer container, ValueType type) {
        return registry.putIfAbsent(type.getName().toLowerCase(), Maps.immutableEntry(container, type)) == null;
    }

    static {
        register(CmdControl.getContainer(), ValueTypes.BOOLEAN);
        register(CmdControl.getContainer(), ValueTypes.CHOICES);
        register(CmdControl.getContainer(), ValueTypes.DOUBLE);
        register(CmdControl.getContainer(), ValueTypes.INTEGER);
        register(CmdControl.getContainer(), ValueTypes.ITEM);
        register(CmdControl.getContainer(), ValueTypes.JOINED_STRINGS);
        register(CmdControl.getContainer(), ValueTypes.PLAYER);
        register(CmdControl.getContainer(), ValueTypes.POSITION);
        register(CmdControl.getContainer(), ValueTypes.STRING);
        register(CmdControl.getContainer(), ValueTypes.TRISTATE);
        register(CmdControl.getContainer(), ValueTypes.USER);
        register(CmdControl.getContainer(), ValueTypes.UUID);
        register(CmdControl.getContainer(), ValueTypes.WORLD);
    }

}