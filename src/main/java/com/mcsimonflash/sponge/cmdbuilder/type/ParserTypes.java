package com.mcsimonflash.sponge.cmdbuilder.type;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableMap;
import com.mcsimonflash.sponge.cmdbuilder.internal.Util;
import com.mcsimonflash.sponge.cmdbuilder.script.Argument;
import com.mcsimonflash.sponge.cmdcontrol.command.parser.SourceParser;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.argument.Arguments;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.argument.parser.CatalogTypeParser;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.argument.parser.ValueParser;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.*;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.*;

public class ParserTypes {

    public static final ParserType<Boolean> BOOLEAN = new ParserType<Boolean>("Boolean", ValueTypes.BOOLEAN) {

        @Override
        public ValueParser<Boolean> getParser(ConfigurationNode meta) {
            return Arguments.booleanObj();
        }

    };
    public static final ParserType<String> CHOICES = new ParserType<String>("Choices", ValueTypes.STRING) {

        @Override
        public ValueParser<String> getParser(ConfigurationNode meta) {
            ConfigurationNode choices = meta.getNode("choices");
            Collector<ConfigurationNode, ?, Map<String, String>> collector = Collectors.toMap(choices.hasListChildren() ? c -> c.getString("") : c -> (String) c.getKey(), c -> c.getString(""));
            return Arguments.choices((choices.hasListChildren() ? choices.getChildrenList() : choices.getChildrenMap().values()).stream().collect(collector), ImmutableMap.of("no-choice", meta.getNode("messages", "no-choice").getString("No choice available for <key>.")));
        }

    };
    public static final ParserType<Double> DOUBLE = new ParserType<Double>("Double", ValueTypes.DOUBLE) {

        @Override
        public ValueParser<Double> getParser(ConfigurationNode meta) {
            ConfigurationNode range = meta.getNode("range");
            if (range.isVirtual()) return Arguments.doubleObj();
            return Arguments.doubleObj().inRange(Util.range(Util.DOUBLE_RANGE, range.getString(""), Double::parseDouble, Double.MIN_VALUE, Double.MAX_VALUE).orElseThrow(() ->
                    new ConfigurationException(range, "Invalid Integer range format: %s", range.getString(""))));
        }
    };
    public static final ParserType<Integer> INTEGER = new ParserType<Integer>("Integer", ValueTypes.INTEGER) {

        @Override
        public ValueParser<Integer> getParser(ConfigurationNode meta) {
            ConfigurationNode range = meta.getNode("range");
            if (range.isVirtual()) return Arguments.intObj();
            return Arguments.intObj().inRange(Util.range(Util.INTEGER_RANGE, range.getString(""), Integer::parseInt, Integer.MIN_VALUE, Integer.MAX_VALUE).orElseThrow(() ->
                    new ConfigurationException(range, "Invalid Integer range format: %s", range.getString(""))));
        }

    };
    public static final ParserType<ItemType> ITEM = new ParserType<ItemType>("Item", ValueTypes.ITEM) {

        private final CatalogTypeParser<ItemType> PARSER = Arguments.catalogType(ItemType.class, ImmutableMap.of());

        @Override
        public ValueParser<ItemType> getParser(ConfigurationNode meta) {
            return PARSER;
        }

    };
    public static final ParserType<String> JOINED_STRINGS = new ParserType<String>("JoinedStrings", ValueTypes.STRING) {

        @Override
        public ValueParser<String> getParser(ConfigurationNode meta) {
            return Arguments.remainingStrings();
        }

    };
    public static final ParserType<Player> PLAYER = new ParserType<Player>("Player", ValueTypes.PLAYER) {

        @Override
        public ValueParser<Player> getParser(ConfigurationNode meta) {
            return Arguments.player();
        }

    };
    public static final ParserType<CommandSource> SOURCE = new ParserType<CommandSource>("Source", ValueTypes.SOURCE) {

        @Override
        public ValueParser<CommandSource> getParser(ConfigurationNode meta) {
            return SourceParser.PARSER;
        }

    };
    public static final ParserType<String> STRING = new ParserType<String>("String", ValueTypes.STRING) {

        @Override
        public ValueParser<String> getParser(ConfigurationNode meta) {
            return Arguments.string();
        }

    };
    public static final ParserType<Tristate> TRISTATE = new ParserType<Tristate>("Tristate", ValueTypes.TRISTATE) {

        @Override
        public ValueParser<Tristate> getParser(ConfigurationNode meta) {
            return Arguments.tristate();
        }

    };
    public static final ParserType<User> USER = new ParserType<User>("User", ValueTypes.USER) {

        @Override
        public ValueParser<User> getParser(ConfigurationNode meta) {
            return Arguments.user();
        }

    };
    public static final ParserType<Vector3d> POSITION = new ParserType<Vector3d>("Position", ValueTypes.VECTOR_3D) {

        @Override
        public ValueParser<Vector3d> getParser(ConfigurationNode meta) {
            return Arguments.position();
        }

    };
    public static final ParserType<World> WORLD = new ParserType<World>("World", ValueTypes.WORLD) {

        @Override
        public ValueParser<World> getParser(ConfigurationNode meta) {
            return Arguments.world();
        }

    };

}