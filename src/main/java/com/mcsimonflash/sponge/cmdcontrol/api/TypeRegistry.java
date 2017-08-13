package com.mcsimonflash.sponge.cmdcontrol.api;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.BoundType;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import com.mcsimonflash.sponge.cmdcontrol.objects.exceptions.ScriptConfigurationException;
import com.mcsimonflash.sponge.cmdcontrol.objects.elements.RangedDouble;
import com.mcsimonflash.sponge.cmdcontrol.objects.elements.RangedInteger;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TypeRegistry {

    private static Map<String, Entry<PluginContainer, ValueType>> registry = Maps.newHashMap();

    public static boolean containsType(String name) {
        return registry.containsKey(name);
    }

    public static ValueType getType(String name) {
        return registry.containsKey(name) ? registry.get(name).getValue() : null;
    }

    public static PluginContainer getContainer(String name) {
        return registry.containsKey(name) ? registry.get(name).getKey() : null;
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
        register(CmdControl.getContainer(), new ValueType("Boolean") {

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                return GenericArguments.onlyOne(GenericArguments.bool(key));
            }

        });
        register(CmdControl.getContainer(), new ValueType("Choices") {

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                Map<String, String> choices = Maps.newHashMap();
                Util.getObjectList(metaNode.getNode("choices"), String.class).forEach(c -> choices.put(c, c));
                return GenericArguments.onlyOne(GenericArguments.choices(key, choices, true));
            }

        });
        register(CmdControl.getContainer(), new ValueType("Double") {

            private final Pattern doubleRangePattern = Pattern.compile("(?:([-+]?[0-9]*[.]?[0-9]+)[ ]?<([=])?[ ]?)?#(?:[ ]?<([=])?[ ]?([-+]?[0-9]*[.]?[0-9]+))?");

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                CommentedConfigurationNode rangeNode = metaNode.getNode("range");
                if (rangeNode.isVirtual()) {
                    return GenericArguments.onlyOne(GenericArguments.doubleNum(key));
                }
                String rangeStr = rangeNode.getString("");
                Matcher matcher = doubleRangePattern.matcher(rangeStr);
                if (matcher.matches()) {
                    double lowerNum = matcher.group(1) != null ? Double.parseDouble(matcher.group(1)) : Double.MIN_VALUE;
                    double upperNum = matcher.group(4) != null ? Double.parseDouble(matcher.group(4)) : Double.MAX_VALUE;
                    BoundType lowerBound = matcher.group(2) != null ? BoundType.CLOSED : BoundType.OPEN;
                    BoundType upperBound = matcher.group(3) != null ? BoundType.CLOSED : BoundType.OPEN;
                    return GenericArguments.onlyOne(new RangedDouble(key, Range.range(lowerNum, lowerBound, upperNum, upperBound)));
                }
                throw new ScriptConfigurationException(rangeNode, "Invalid range format. | Range:[" + rangeStr + "]");
            }

        });
        register(CmdControl.getContainer(), new ValueType("Integer") {

            private final Pattern integerRangePattern = Pattern.compile("(?:([-+]?[0-9]+)[ ]?<([=])?[ ]?)?#(?:[ ]?<([=])?[ ]?([-+]?[0-9]+))?");

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                CommentedConfigurationNode rangeNode = metaNode.getNode("range");
                if (rangeNode.isVirtual()) {
                    return GenericArguments.onlyOne(GenericArguments.integer(key));
                }
                String rangeStr = rangeNode.getString("");
                Matcher matcher = integerRangePattern.matcher(rangeStr);
                if (matcher.matches()) {
                    int lowerNum = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : Integer.MIN_VALUE;
                    int upperNum = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : Integer.MAX_VALUE;
                    BoundType lowerBound = matcher.group(2) != null ? BoundType.CLOSED : BoundType.OPEN;
                    BoundType upperBound = matcher.group(3) != null ? BoundType.CLOSED : BoundType.OPEN;
                    return GenericArguments.onlyOne(new RangedInteger(key, Range.range(lowerNum, lowerBound, upperNum, upperBound)));
                }
                throw new ScriptConfigurationException(rangeNode, "Invalid range format. | Range:[" + rangeStr + "]");
            }

        });
        register(CmdControl.getContainer(), new ValueType("Item") {

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                return GenericArguments.onlyOne(GenericArguments.catalogedElement(key, ItemType.class));
            }

            @Override
            public String getString(Object object) {
                return object instanceof ItemType ? ((ItemType) object).getId() : super.getString(object);
            }

        });
        register(CmdControl.getContainer(), new ValueType("JoinedStrings") {

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                return GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(key));
            }

        });
        register(CmdControl.getContainer(), new ValueType("Player") {

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) {
                return GenericArguments.onlyOne(GenericArguments.player(key));
            }

            //TODO: This will not work properly for type checking
            @Override
            public Object getParam(Object object, String param) {
                if (object instanceof Player) {
                    Player player = (Player) object;
                    switch (param.toLowerCase()) {
                        case "uuid":
                            return player.getUniqueId();
                        case "world":
                            return player.getWorld();
                    }
                }
                return super.getParam(object, param);
            }

            @Override
            public String getString(Object object) {
                return object instanceof Player ? ((Player) object).getName() : super.getString(object);
            }

        });
        register(CmdControl.getContainer(), new ValueType("Position") {

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                return GenericArguments.onlyOne(GenericArguments.vector3d(key));
            }

            @Override
            public String getString(Object object) {
                return object instanceof Vector3d ? ((Vector3d) object).getX() + " " + ((Vector3d) object).getY() + " " + ((Vector3d) object).getZ() : super.getString(object);
            }

        });
        register(CmdControl.getContainer(), new ValueType("String") {

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                return GenericArguments.onlyOne(GenericArguments.string(key));
            }

        });
        register(CmdControl.getContainer(), new ValueType("User") {

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                return GenericArguments.onlyOne(GenericArguments.user(key));
            }

            @Override
            public String getString(Object object) {
                return object instanceof User ? ((User) object).getName() : super.getString(object);
            }

        });
        register(CmdControl.getContainer(), new ValueType("World") {

            @Override
            public CommandElement getCmdElem(Text key, CommentedConfigurationNode metaNode) throws ScriptConfigurationException {
                return GenericArguments.onlyOne(GenericArguments.world(key));
            }

            @Override
            public String getString(Object object) {
                return object instanceof World ? ((World) object).getName() : super.getString(object);
            }
        });
    }

}