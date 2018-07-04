package com.mcsimonflash.sponge.cmdbuilder.internal;

import com.google.common.collect.BoundType;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.reflect.TypeToken;
import com.mcsimonflash.sponge.cmdbuilder.CmdBuilder;
import com.mcsimonflash.sponge.cmdbuilder.miscellaneous.Mapping;
import com.mcsimonflash.sponge.cmdbuilder.script.Script;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {

    public static final UUID ZERO_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final Pattern DOUBLE_RANGE = Pattern.compile("([(\\[])(\\*|[-+]?[0-9]*[.]?[0-9]+),(\\*|[-+]?[0-9]*[.]?[0-9]+)([)\\]])");
    public static final Pattern INTEGER_RANGE = Pattern.compile("([(\\[])(\\*|[-+]?[0-9]+),(\\*|[-+]?[0-9]+)([)\\]])");

    public static void initialize() {
        Scripts.directory.clear();
        Mapping.ROOT.Children.clear();
        Sponge.getCommandManager().get("script").ifPresent(Sponge.getCommandManager()::removeMapping);
        Config.load();
        Sponge.getCommandManager().register(CmdBuilder.get().getContainer(), CommandSpec.builder()
                .children(Scripts.directory.values().stream().collect(Collectors.toMap(s -> Lists.newArrayList(s.getName()), Script::getSpec)))
                .build(), "script");
    }

    public static Text toText(String msg) {
        return TextSerializers.FORMATTING_CODE.deserialize(msg);
    }

    public static <T> List<T> getObjectList(ConfigurationNode node, Class<T> clazz) throws IllegalArgumentException {
        try {
            return node.getList(TypeToken.of(clazz));
        } catch (ObjectMappingException e) {
            throw new IllegalArgumentException("Unable to load list of type '" + clazz.getSimpleName() + "' from node '" + node.getKey() + "'.");
        }
    }

    public static <T extends Comparable> Optional<Range<T>> range(Pattern pattern, String range, Function<String, T> parser, T min, T max) {
        Matcher matcher = pattern.matcher(range);
        return matcher.matches() ? Optional.of(Range.range(
                matcher.group(2).equals("*") ? min : parser.apply(matcher.group(2)),
                matcher.group(1).equals("(") ? BoundType.OPEN : BoundType.CLOSED,
                matcher.group(3).equals("*") ? max : parser.apply(matcher.group(3)),
                matcher.group(4).equals(")") ? BoundType.OPEN : BoundType.CLOSED)) : Optional.empty();
    }

}