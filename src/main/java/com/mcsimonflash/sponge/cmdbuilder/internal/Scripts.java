package com.mcsimonflash.sponge.cmdbuilder.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.cmdbuilder.CmdBuilder;
import com.mcsimonflash.sponge.cmdbuilder.miscellaneous.Mapping;
import com.mcsimonflash.sponge.cmdbuilder.script.Script;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.*;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.SpongeApiTranslationHelper;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Scripts {

    public static final Pattern ARGUMENT = Pattern.compile("<([@])?([A-Za-z0-9]+)((?:[#][A-Za-z0-9]+)+)?>");

    public static final Map<String, Script> directory = Maps.newHashMap();

    public static void register(Script script) {
        directory.put(script.getName().toLowerCase(), script);
        script.getMetadata().getAliases().forEach(a -> {
            String[] split = a.split(" ");
            Mapping mapping = Mapping.ROOT;
            for (String name : split) {
                Mapping child = mapping.Children.get(name.toLowerCase());
                mapping = child == null ? new Mapping(name, mapping) : child;
            }
            if (mapping.Script != null) {
                CmdBuilder.get().getLogger().error("Script " + script.getName() + " attempted to register alias " + a + "already registered to " + mapping.Script.getName() + "!");
            }
            mapping.Script = script;
        });
    }

    public static Mapping.Result getMapping(String path) {
        Mapping mapping = Mapping.ROOT;
        String[] split = path.split(" ", -1);
        int i = 0;
        while (i < split.length) {
            Mapping child = mapping.Children.get(split[i].toLowerCase());
            if (child == null) {
                break;
            }
            mapping = child;
            i++;
        }
        return new Mapping.Result(mapping, split, i);
    }

    public static Optional<CommandResult> process(CommandSource src, String arguments) {
        Mapping.Result result = getMapping(arguments);
        CommandException exception = null;
        Mapping mapping = result.getMapping();
        int i = result.getStart();
        while (i >= 0) {
            while (mapping.Script == null) {
                mapping = mapping.Parent;
                if (mapping == null) {
                    return Optional.empty();
                }
                i--;
            }
            String argument = String.join(" ", Arrays.copyOfRange(result.getSplit(), i, result.getSplit().length));
            try {
                return Optional.of(mapping.Script.getSpec().process(src, argument));
            } catch (CommandException e) {
                exception = exception == null ? e : exception;
            }
            i--;
        }
        if (exception != null && exception.getText() != null) {
            src.sendMessage(CommandMessageFormatting.error(exception.getText()));
            CommandMapping backup = Sponge.getCommandManager().get(result.getSplit()[0]).orElse(null);
            if (backup == null || result.getMapping().Script.getMetadata().getOverride()) {
                return Optional.of(CommandResult.empty());
            }
        }
        return Optional.empty();
    }

    public static List<String> complete(CommandSource src, String arguments, @Nullable Location<World> position) {
        Mapping.Result result = getMapping(arguments);
        Mapping mapping = result.getMapping();
        List<String> suggestions = Lists.newArrayList();
        if (result.getStart() == result.getSplit().length - 1) {
            String last = result.getSplit()[result.getStart()].toLowerCase();
            suggestions = mapping.Children.keySet().stream().filter(n -> n.toLowerCase().startsWith(last)).collect(Collectors.toList());
        }
        boolean override = false;
        int i = result.getStart();
        while (i >= 0 && !override) {
            while (mapping.Script == null) {
                mapping = mapping.Parent;
                if (mapping == null) {
                    return suggestions;
                }
                i--;
            }
            try {
                suggestions.addAll(mapping.Script.getSpec().getSuggestions(src, String.join(" ", Arrays.copyOfRange(result.getSplit(), result.getStart(), result.getSplit().length)), position));
                override = mapping.Script.getMetadata().getOverride();
                i--;
                mapping = mapping.Parent;
                if (mapping == null) {
                    break;
                }
            } catch (CommandException e) {
                src.sendMessage(CommandMessageFormatting.error(SpongeApiTranslationHelper.t("Error getting suggestions: %s", e.getText())));
                return ImmutableList.of();
            }
        }
        return override ? ImmutableList.copyOf(suggestions) : suggestions;
    }

    public static String insertArguments(String string, CommandSource src, Map<String, ValueTypeEntry> arguments) {
        Matcher matcher = ARGUMENT.matcher(string);
        while (matcher.find()) {
            ValueTypeEntry entry;
            if (matcher.group(1) != null) {
                entry = getAttribute(src, matcher.group(2));
            } else if (arguments.containsKey(matcher.group(2))) {
                entry = arguments.get(matcher.group(2));
            } else continue;
            if (matcher.group(3) != null) {
                String[] params = matcher.group(3).substring(1).split("#");
                for (String param : params) {
                    entry = entry.getParam(param);
                }
            }
            string = string.replace(matcher.group(), entry.getString());
        }
        return string;
    }

    public static ValueTypeEntry getAttribute(CommandSource src, String attribute) {
        switch (attribute.toLowerCase()) {
            case "player":
                return src instanceof Player ? ValueTypes.PLAYER.createEntry((Player) src) : ValueTypes.STRING.createEntry(src.getName());
            case "sender":
                return ValueTypes.STRING.createEntry(src.getName());
            case "meta":
                return ValueTypes.STRING.createEntry(src.getName());
            default:
                return ValueTypes.STRING.createEntry(attribute);
        }
    }

}