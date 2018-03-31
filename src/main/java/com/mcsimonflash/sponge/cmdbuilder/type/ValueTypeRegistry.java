package com.mcsimonflash.sponge.cmdbuilder.type;

import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.cmdbuilder.CmdBuilder;
import java.lang.IllegalArgumentException;

import org.spongepowered.api.plugin.PluginContainer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class ValueTypeRegistry {

    private static Map<String, Entry<PluginContainer, ValueType>> registry = Maps.newHashMap();

    public static <T> Optional<ValueType<T>> getType(String name) {
        Entry<PluginContainer, ValueType> type = registry.get(name.toLowerCase());
        return type != null ? Optional.of(type.getValue()) : Optional.empty();
    }

    public static <T> ValueType<T> getTypeOrThrow(String name) throws IllegalArgumentException {
        return ValueTypeRegistry.<T>getType(name).orElseThrow(() -> new IllegalArgumentException("ValueType '" + name + "' does not exist."));
    }

    public static Optional<PluginContainer> getContainer(String name) {
        Entry<PluginContainer, ValueType> type = registry.get(name.toLowerCase());
        return type != null ? Optional.of(type.getKey()) : Optional.empty();
    }

    public static PluginContainer getContainerOrThrow(String name) throws IllegalArgumentException {
        return getContainer(name).orElseThrow(() -> new IllegalArgumentException("Container for '" + name + "' does not exist."));
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
        register(CmdBuilder.get().getContainer(), ValueTypes.BOOLEAN);
        register(CmdBuilder.get().getContainer(), ValueTypes.CHOICES);
        register(CmdBuilder.get().getContainer(), ValueTypes.DOUBLE);
        register(CmdBuilder.get().getContainer(), ValueTypes.INTEGER);
        register(CmdBuilder.get().getContainer(), ValueTypes.ITEM);
        register(CmdBuilder.get().getContainer(), ValueTypes.JOINED_STRINGS);
        register(CmdBuilder.get().getContainer(), ValueTypes.PLAYER);
        register(CmdBuilder.get().getContainer(), ValueTypes.SOURCE);
        register(CmdBuilder.get().getContainer(), ValueTypes.STRING);
        register(CmdBuilder.get().getContainer(), ValueTypes.TRISTATE);
        register(CmdBuilder.get().getContainer(), ValueTypes.UNKNOWN);
        register(CmdBuilder.get().getContainer(), ValueTypes.USER);
        register(CmdBuilder.get().getContainer(), ValueTypes.UUID);
        register(CmdBuilder.get().getContainer(), ValueTypes.VECTOR_3D);
        register(CmdBuilder.get().getContainer(), ValueTypes.WORLD);
    }

}