package com.mcsimonflash.sponge.cmdbuilder.type;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.mcsimonflash.sponge.cmdcontrol.command.parser.SourceParser;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.argument.Arguments;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ValueTypes {

    public static final ValueType<Boolean> BOOLEAN = new ValueType<Boolean>("Boolean") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.booleanObj().toElement(key);
        }

    };
    public static final ValueType<List<String>> CHOICES = new ValueType<List<String>>("Choices") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.choices(meta.getNode("choices").getChildrenMap().entrySet().stream().collect(Collectors.toMap(e -> (String) e.getKey(), e -> e.getValue().getString((String) e.getKey()))), ImmutableMap.of("no-choice", meta.getNode("messages", "no-choice").getString("No choice available for <key>."))).toElement(key);
        }

    };
    public static final ValueType<Double> DOUBLE = new ValueType<Double>("Double") {

        private final Pattern RANGE_PATTERN = Pattern.compile("([(\\[])(\\*|[-+]?[0-9]*[.]?[0-9]+),(\\*|[-+]?[0-9]*[.]?[0-9]+)([)\\]])");

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            ConfigurationNode rangeNode = meta.getNode("range");
            if (rangeNode.isVirtual()) {
                return Arguments.doubleObj().toElement(key);
            }
            String rangeStr = rangeNode.getString("");
            Matcher matcher = RANGE_PATTERN.matcher(rangeStr);
            if (matcher.matches()) {
                return Arguments.doubleObj().inRange(Range.range(
                        matcher.group(2).equals("*") ? Double.MIN_VALUE : Double.parseDouble(matcher.group(2)),
                        matcher.group(1).equals("(") ? BoundType.OPEN : BoundType.CLOSED,
                        matcher.group(3).equals("*") ? Double.MAX_VALUE : Double.parseDouble(matcher.group(2)),
                        matcher.group(4).equals(")") ? BoundType.OPEN : BoundType.CLOSED))
                        .toElement(key);
            }
            throw new IllegalArgumentException("Invalid Double range format.");
        }

    };
    public static final ValueType<Integer> INTEGER = new ValueType<Integer>("Integer") {

        private final Pattern RANGE_PATTERN = Pattern.compile("([(\\[])(\\*|[-+]?[0-9]+),(\\*|[-+]?[0-9]+)([)\\]])");

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            ConfigurationNode rangeNode = meta.getNode("range");
            if (rangeNode.isVirtual()) {
                return Arguments.intObj().toElement(key);
            }
            String range = rangeNode.getString("");
            Matcher matcher = RANGE_PATTERN.matcher(range);
            if (matcher.matches()) {
                return Arguments.intObj().inRange(Range.range(
                        matcher.group(2).equals("*") ? Integer.MIN_VALUE : Integer.parseInt(matcher.group(2)),
                        matcher.group(1).equals("(") ? BoundType.OPEN : BoundType.CLOSED,
                        matcher.group(3).equals("*") ? Integer.MAX_VALUE : Integer.parseInt(matcher.group(3)),
                        matcher.group(4).equals(")") ? BoundType.OPEN : BoundType.CLOSED))
                        .toElement(key);
            }
            throw new IllegalArgumentException("Invalid Integer range meta: " + range);
        }

    };
    public static final ValueType<ItemType> ITEM = new ValueType<ItemType>("Item") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return GenericArguments.catalogedElement(Text.of(key), ItemType.class);
        }

        @Override
        public String getString(Object object) {
            return object instanceof ItemType ? ((ItemType) object).getId() : super.getString(object);
        }

    };
    public static final ValueType<String> JOINED_STRINGS = new ValueType<String>("JoinedStrings") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.remainingStrings().toElement(key);
        }

    };
    public static final ValueType<Player> PLAYER = new ValueType<Player>("Player") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.player().toElement(key);
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof Player) {
                Player player = (Player) object;
                switch (param.toLowerCase()) {
                    case "position":
                        return VECTOR_3D.createEntry(player.getLocation().getPosition());
                    case "uuid":
                        return UUID.createEntry(player.getUniqueId());
                    case "world":
                        return WORLD.createEntry(player.getWorld());
                }
            }
            return super.getParam(object, param);
        }

        @Override
        public String getString(Object object) {
            return object instanceof Player ? ((Player) object).getName() : super.getString(object);
        }

    };
    public static final ValueType<CommandSource> SOURCE = new ValueType<CommandSource>("Source") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return SourceParser.PARSER.toElement(key);
        }

        @Override
        public String getString(Object object) {
            return object instanceof CommandSource ? ((CommandSource) object).getName() : super.getString(object);
        }

    };
    public static final ValueType<String> STRING = new ValueType<String>("String") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.string().toElement(key);
        }

    };
    public static final ValueType<Tristate> TRISTATE = new ValueType<Tristate>("Tristate") {

        private final Map<String, Tristate> tristates = ImmutableMap.<String, Tristate>builder()
                .put("true", Tristate.TRUE).put("t", Tristate.TRUE).put("1", Tristate.TRUE)
                .put("false", Tristate.FALSE).put("f", Tristate.FALSE).put("0", Tristate.FALSE)
                .put("undefined", Tristate.UNDEFINED).put("u", Tristate.UNDEFINED).put("1/2", Tristate.UNDEFINED)
                .build();

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.tristate().toElement(key);
        }

        @Override
        public String getString(Object obj) {
            return obj instanceof Tristate ? ((Tristate) obj).name().toLowerCase() : super.getString(obj);
        }

    };
    public static final ValueType<?> UNKNOWN = new ValueType<Object>("Unknown") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.string().toElement(key);
        }

    };
    public static final ValueType<User> USER = new ValueType<User>("User") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.user().toElement(key);
        }

        @Override
        public String getString(Object object) {
            return object instanceof User ? ((User) object).getName() : super.getString(object);
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof User) {
                User user = (User) object;
                switch (param.toLowerCase()) {
                    case "uuid":
                        return UUID.createEntry(user.getUniqueId());
                }
            }
            return super.getParam(object, param);
        }
    };
    public static final ValueType<java.util.UUID> UUID = new ValueType<java.util.UUID>("Uuid") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.user().toUuid().toElement(key);
        }

    };
    public static final ValueType<Vector3d> VECTOR_3D = new ValueType<Vector3d>("Vector3d") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.position().toElement(key);
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof Vector3d) {
                Vector3d vector3d = (Vector3d) object;
                switch (param.toLowerCase()) {
                    case "x":
                        return INTEGER.createEntry(vector3d.getFloorX());
                    case "y":
                        return INTEGER.createEntry(vector3d.getFloorY());
                    case "z":
                        return INTEGER.createEntry(vector3d.getFloorZ());
                }
            }
            return super.getParam(object, param);
        }

        @Override
        public String getString(Object object) {
            return object instanceof Vector3d ? ((Vector3d) object).getX() + " " + ((Vector3d) object).getY() + " " + ((Vector3d) object).getZ() : super.getString(object);
        }

    };
    public static final ValueType<World> WORLD = new ValueType<World>("World") {

        @Override
        public CommandElement getCmdElem(String key, ConfigurationNode meta) throws IllegalArgumentException {
            return Arguments.world().toElement(key);
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof World) {
                World world = (World) object;
                switch (param.toLowerCase()) {
                    case "uuid":
                        return UUID.createEntry(world.getUniqueId());
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