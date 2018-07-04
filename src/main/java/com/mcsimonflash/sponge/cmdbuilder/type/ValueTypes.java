package com.mcsimonflash.sponge.cmdbuilder.type;

import com.flowpowered.math.vector.Vector3d;
import com.mcsimonflash.sponge.cmdbuilder.internal.Config;
import com.mcsimonflash.sponge.cmdbuilder.internal.Util;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.ConfigurationException;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;

import java.util.UUID;

public class ValueTypes {

    public static final ValueType<Boolean> BOOLEAN = new ValueType<Boolean>("Boolean") {

        @Override
        public Boolean deserialize(ConfigurationNode node) throws ConfigurationException {
            return node.getBoolean();
        }

    };
    public static final ValueType<Double> DOUBLE = new ValueType<Double>("Double") {

        @Override
        public Double deserialize(ConfigurationNode node) throws ConfigurationException {
            return node.getDouble();
        }

    };
    public static final ValueType<Integer> INTEGER = new ValueType<Integer>("Integer") {

        @Override
        public Integer deserialize(ConfigurationNode node) throws ConfigurationException {
            return node.getInt();
        }

    };
    public static final ValueType<ItemType> ITEM = new ValueType<ItemType>("Item") {

        @Override
        public ItemType deserialize(ConfigurationNode node) throws ConfigurationException {
            return Sponge.getRegistry().getType(ItemType.class, node.getString("")).orElseThrow(() ->
                    new ConfigurationException(node, "No item type found for id %s.", node.getString("")));
        }

        @Override
        public void serialize(ConfigurationNode node, ItemType value) {
            node.setValue(value.getId());
        }

        @Override
        public String getString(Object object) {
            return object instanceof ItemType ? ((ItemType) object).getId() : super.getString(object);
        }

    };
    public static final ValueType<ConfigurationNode> NODE = new ValueType<ConfigurationNode>("Node") {

        @Override
        public ConfigurationNode deserialize(ConfigurationNode node) throws ConfigurationException {
            return node;
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            return object instanceof ConfigurationNode ? NODE.createEntry(((ConfigurationNode) object).getNode(param)) : super.getParam(object, param);
        }

    };
    public static final ValueType<Player> PLAYER = new ValueType<Player>("Player") {

        @Override
        public Player deserialize(ConfigurationNode node) throws ConfigurationException {
            return Sponge.getServer().getPlayer(UUID.deserialize(node)).orElseThrow(() ->
                    new ConfigurationException(node, "No player with uuid %s.", node.getString("undefined")));
        }

        @Override
        public void serialize(ConfigurationNode node, Player value) {
            node.setValue(value.getUniqueId().toString());
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof Player) {
                if (param.startsWith("meta.") && param.length() > 5) {
                    return Config.getMeta(((Player) object).getUniqueId(), param.substring(5));
                }
                switch (param.toLowerCase()) {
                    case "position": return VECTOR_3D.createEntry(((Player) object).getLocation().getPosition());
                    case "uuid": return UUID.createEntry(((Player) object).getUniqueId());
                    case "world": return WORLD.createEntry(((Player) object).getWorld());
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
        public CommandSource deserialize(ConfigurationNode node) throws ConfigurationException {
            throw new UnsupportedOperationException("Cannot deserialize a Source.");
        }

        @Override
        public void serialize(ConfigurationNode node, CommandSource value) throws ConfigurationException {
            throw new UnsupportedOperationException("Cannot serialize a Source.");
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof CommandSource && param.startsWith("meta.") && param.length() > 5) {
                return Config.getMeta(object instanceof Player ? ((Player) object).getUniqueId() : Util.ZERO_UUID, param.substring(5));
            }
            return super.getParam(object, param);
        }

        @Override
        public String getString(Object object) {
            return object instanceof CommandSource ? ((CommandSource) object).getName() : super.getString(object);
        }

    };
    public static final ValueType<String> STRING = new ValueType<String>("String") {

        @Override
        public String deserialize(ConfigurationNode node) throws ConfigurationException {
            return node.getString("");
        }

    };
    public static final ValueType<Tristate> TRISTATE = new ValueType<Tristate>("Tristate") {

        @Override
        public Tristate deserialize(ConfigurationNode node) throws ConfigurationException {
            switch (node.getString("").toLowerCase()) {
                case "true": return Tristate.TRUE;
                case "false": return Tristate.FALSE;
                case "undefined": return Tristate.UNDEFINED;
                default: throw new ConfigurationException(node, "Input %s is not a Tristate.", node.getString(""));
            }
        }

        @Override
        public void serialize(ConfigurationNode node, Tristate value) throws ConfigurationException {
            node.setValue(value.name().toLowerCase());
        }

        @Override
        public String getString(Object object) {
            return object instanceof Tristate ? ((Tristate) object).name().toLowerCase() : super.getString(object);
        }

    };
    public static final ValueType<User> USER = new ValueType<User>("User") {

        @Override
        public User deserialize(ConfigurationNode node) throws ConfigurationException {
            return Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(UUID.deserialize(node)).orElseThrow(() ->
                    new ConfigurationException(node, "No user with uuid %s.", node.getString("undefined")));
        }

        @Override
        public void serialize(ConfigurationNode node, User value) throws ConfigurationException {
            node.setValue(value.getUniqueId().toString());
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof User) {
                if (param.startsWith("meta.") && param.length() > 5) {
                    return Config.getMeta(((User) object).getUniqueId(), param.substring(5));
                }
                switch (param.toLowerCase()) {
                    case "position": return VECTOR_3D.createEntry(((User) object).getPosition());
                    case "uuid": return UUID.createEntry(((User) object).getUniqueId());
                }
            }
            return super.getParam(object, param);
        }

        @Override
        public String getString(Object object) {
            return object instanceof User ? ((User) object).getName() : super.getString(object);
        }

    };
    public static final ValueType<java.util.UUID> UUID = new ValueType<java.util.UUID>("Uuid") {

        @Override
        public UUID deserialize(ConfigurationNode node) throws ConfigurationException {
            try {
                return java.util.UUID.fromString(node.getString(""));
            } catch (IllegalArgumentException e) {
                throw new ConfigurationException(node, "UUID is not in the proper form: " + e.getMessage());
            }
        }

        @Override
        public void serialize(ConfigurationNode node, UUID value) throws ConfigurationException {
            node.setValue(value.toString());
        }

    };
    public static final ValueType<Vector3d> VECTOR_3D = new ValueType<Vector3d>("Vector3d") {

        @Override
        public Vector3d deserialize(ConfigurationNode node) throws ConfigurationException {
            String[] split = node.getString().split(" ");
            if (split.length == 3) {
                try {
                    return Vector3d.from(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]));
                } catch (NumberFormatException e) {
                    throw new ConfigurationException(node, "Unable to parse vector component: %s", e.getMessage());
                }
            }
            throw new ConfigurationException(node, "Expected 3 vector components, received %s.", split.length);
        }

        @Override
        public void serialize(ConfigurationNode node, Vector3d value) throws ConfigurationException {
            node.setValue(getString(value));
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof Vector3d) {
                switch (param.toLowerCase()) {
                    case "x": return INTEGER.createEntry(((Vector3d) object).getFloorX());
                    case "y": return INTEGER.createEntry(((Vector3d) object).getFloorY());
                    case "z": return INTEGER.createEntry(((Vector3d) object).getFloorZ());
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
        public World deserialize(ConfigurationNode node) throws ConfigurationException {
            return Sponge.getServer().getWorld(UUID.deserialize(node)).orElseThrow(() ->
                    new ConfigurationException(node, "No world found with uuid %s.", node.getString("undefined")));
        }

        @Override
        public void serialize(ConfigurationNode node, World value) throws ConfigurationException {
            node.setValue(value.getUniqueId().toString());
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            return object instanceof World && param.toLowerCase().equals("uuid") ? UUID.createEntry(((World) object).getUniqueId()) : super.getParam(object, param);
        }

        @Override
        public String getString(Object object) {
            return object instanceof World ? ((World) object).getName() : super.getString(object);
        }

    };

}