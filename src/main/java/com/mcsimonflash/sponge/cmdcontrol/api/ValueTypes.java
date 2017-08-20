package com.mcsimonflash.sponge.cmdcontrol.api;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.BoundType;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import com.mcsimonflash.sponge.cmdcontrol.objects.elements.RangedDouble;
import com.mcsimonflash.sponge.cmdcontrol.objects.elements.RangedInteger;
import com.mcsimonflash.sponge.cmdcontrol.objects.elements.Uuid;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValueTypes {

    public static final ValueType BOOLEAN = new ValueType("Boolean") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.onlyOne(GenericArguments.bool(key));
        }

    };
    public static final ValueType CHOICES = new ValueType("Choices") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            Map<String, String> choices = Maps.newHashMap();
            Util.getObjectList(meta.getNode("choices"), String.class).forEach(c -> choices.put(c, c));
            return GenericArguments.onlyOne(GenericArguments.choices(key, choices, true));
        }

    };
    public static final ValueType DOUBLE = new ValueType("Double") {

        private final Pattern doubleRangePattern = Pattern.compile("(?:([-+]?[0-9]*[.]?[0-9]+)[ ]?<([=])?[ ]?)?#(?:[ ]?<([=])?[ ]?([-+]?[0-9]*[.]?[0-9]+))?");

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            CommentedConfigurationNode rangeNode = meta.getNode("range");
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
            throw new IllegalArgumentException("Invalid Double range format.");
        }

    };
    public static final ValueType INTEGER = new ValueType("Integer") {

        private final Pattern integerRangePattern = Pattern.compile("(?:([-+]?[0-9]+)[ ]?<([=])?[ ]?)?#(?:[ ]?<([=])?[ ]?([-+]?[0-9]+))?");

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            CommentedConfigurationNode rangeNode = meta
                    .getNode("range");
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
            throw new IllegalArgumentException("Invalid Integer range meta.");
        }

    };
    public static final ValueType ITEM = new ValueType("Item") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.onlyOne(GenericArguments.catalogedElement(key, ItemType.class));
        }

        @Override
        public String getString(Object object) {
            return object instanceof ItemType ? ((ItemType) object).getId() : super.getString(object);
        }

    };
    public static final ValueType JOINED_STRINGS = new ValueType("JoinedStrings") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(key));
        }

    };
    public static final ValueType PLAYER = new ValueType("Player") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.onlyOne(GenericArguments.player(key));
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof Player) {
                Player player = (Player) object;
                switch (param.toLowerCase()) {
                    case "uuid":
                        return new ValueTypeEntry(UUID, player.getUniqueId());
                    case "world":
                        return new ValueTypeEntry(WORLD, player.getWorld());
                }
            }
            return super.getParam(object, param);
        }

        @Override
        public String getString(Object object) {
            return object instanceof Player ? ((Player) object).getName() : super.getString(object);
        }

    };
    public static final ValueType POSITION = new ValueType("Position") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.onlyOne(GenericArguments.vector3d(key));
        }

        @Override
        public String getString(Object object) {
            return object instanceof Vector3d ? ((Vector3d) object).getX() + " " + ((Vector3d) object).getY() + " " + ((Vector3d) object).getZ() : super.getString(object);
        }
    };
    public static final ValueType STRING = new ValueType("String") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.onlyOne(GenericArguments.string(key));
        }

    };
    public static final ValueType TRISTATE = new ValueType("Tristate") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            Map<String, Tristate> tristates = Maps.newHashMap();
            tristates.put("true", Tristate.TRUE);
            tristates.put("false", Tristate.FALSE);
            tristates.put("undefined", Tristate.UNDEFINED);
            return GenericArguments.onlyOne(GenericArguments.choices(key, tristates));
        }

    };
    public static final ValueType USER = new ValueType("User") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.onlyOne(GenericArguments.user(key));
        }

        @Override
        public String getString(Object object) {
            return object instanceof User ? ((User) object).getName() : super.getString(object);
        }

    };
    public static final ValueType UUID = new ValueType("Uuid") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.onlyOne(new Uuid(key));
        }

    };
    public static final ValueType WORLD = new ValueType("World") {

        @Override
        public CommandElement getCmdElem(Text key, CommentedConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.onlyOne(GenericArguments.world(key));
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof World) {
                World world = (World) object;
                switch (param.toLowerCase()) {
                    case "uuid":
                        return new ValueTypeEntry(UUID, world.getUniqueId());
                }
            }
            return super.getParam(object, param);
        }

        @Override
        public String getString(Object object) {
            return object instanceof World ? ((World) object).getName() : super.getString(object);
        }

    };

}