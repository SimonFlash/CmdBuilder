package com.mcsimonflash.sponge.cmdbuilder.type;

import com.google.common.collect.Sets;
import com.mcsimonflash.sponge.cmdbuilder.CmdBuilder;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.argument.parser.ValueParser;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.ConfigurationException;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.util.Tuple;

import java.util.Optional;

public class OptionalTypes {

    public static void initialize() {
        Sets.newHashSet(CmdBuilder.VALUE_TYPES.getDistinct()).stream()
                .map(Tuple::getFirst)
                .forEach(v -> CmdBuilder.registerValueType(new OptionalValueType(v), CmdBuilder.get().getContainer()));
        Sets.newHashSet(CmdBuilder.PARSER_TYPES.getDistinct()).stream()
                .map(Tuple::getFirst)
                .forEach(p -> CmdBuilder.registerParserType(
                        new OptionalParserType((OptionalValueType) CmdBuilder.VALUE_TYPES.getValue("cmdbuilder:optional" + p.getType().getName()).get(), p),
                        CmdBuilder.get().getContainer()));
    }

    public static class OptionalValueType<T> extends ValueType<Optional<T>> {

        private final ValueType<T> type;

        public OptionalValueType(ValueType<T> type) {
            super("Optional" + type.getName());
            this.type = type;
        }

        @Override
        public Optional<T> deserialize(ConfigurationNode node) throws ConfigurationException {
            return node.getValue() == null ? Optional.empty() : Optional.of(type.deserialize(node));
        }

        @Override
        public void serialize(ConfigurationNode node, Optional<T> value) throws ConfigurationException {
            if (value.isPresent()) {
                type.serialize(node, value.get());
            } else {
                node.setValue(null);
            }
        }

        @Override
        public String getString(Object object) {
            if (object instanceof Optional) {
                return ((Optional<?>) object).map(type::getString).orElse("");
            } else {
                return super.getString(object);
            }
        }

        @Override
        public ValueTypeEntry getParam(Object object, String param) {
            if (object instanceof Optional) {
                return ((Optional<?>) object).map(o -> type.getParam(o, param)).orElse(new ValueTypeEntry(ValueTypes.STRING, ""));
            } else {
                return super.getParam(object, param);
            }
        }

    }

    public static class OptionalParserType<T> extends ParserType<Optional<T>> {

        private final ParserType<T> parser;

        public OptionalParserType(ValueType<Optional<T>> type, ParserType<T> parser) {
            super("Optional" + parser.getName(), type);
            this.parser = parser;
        }

        @Override
        public ValueParser<Optional<T>> getParser(ConfigurationNode meta) {
            return (src, args) -> parser.getParser(meta).optional().parseValue(src, args);
        }

    }

}
