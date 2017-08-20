package com.mcsimonflash.sponge.cmdcontrol.managers;

import com.google.common.reflect.TypeToken;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.api.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdcontrol.api.ValueTypes;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static final Pattern argumentPattern = Pattern.compile("(<(?:([@])?([A-Za-z0-9]+))(?:[#]([A-Za-z0-9]+))?>)");
    public static final Text prefix = toText("&4[&6CmdControl&4] &e");

    public static void initialize() {
        Config.load();
        Storage.load();
        Scripts.register();
    }

    public static Text toText(String msg) {
        return TextSerializers.FORMATTING_CODE.deserialize(msg);
    }

    public static HoconConfigurationLoader getLoader(Path path, boolean asset) throws IOException {
        try {
            if (Files.notExists(path)) {
                if (asset) {
                    Sponge.getAssetManager().getAsset(CmdControl.getPlugin(), path.getFileName().toString()).get().copyToFile(path);
                } else {
                    Files.createFile(path);
                }
            }
            return HoconConfigurationLoader.builder().setPath(path).build();
        } catch (IOException e) {
            CmdControl.getPlugin().getLogger().error("Error loading file \"" + path.getFileName().toString() + "\"");
            throw e;
        }
    }

    public static CommentedConfigurationNode copyNode(CommentedConfigurationNode node) {
        return SimpleCommentedConfigurationNode.root().setValue(node);
    }

    public static <T> List<T> getObjectList(CommentedConfigurationNode node, Class<T> clazz) throws IllegalArgumentException {
        try {
            return node.getList(TypeToken.of(clazz));
        } catch (ObjectMappingException e) {
            throw new IllegalArgumentException("Unable to load list of type \"" + clazz.getSimpleName() + "\" from node \"" + node.getKey() + "\".");
        }
    }

    public static String insertArguments(String string, CommandSource src, Map<String, ValueTypeEntry> arguments) {
        Matcher matcher = argumentPattern.matcher(string);
        while (matcher.find()) {
            ValueTypeEntry entry;
            if (matcher.group(1) != null) {
                entry = getAttribute(matcher.group(2), src);
            } else if (arguments.containsKey(matcher.group(2))) {
                entry = arguments.get(matcher.group(2));
            } else continue;
            if (matcher.group(3) != null && matcher.group(3).length() > 1) {
                String[] params = matcher.group(3).substring(1).split("#");
                for (String param : params) {
                    entry = entry.getParam(param);
                }
            }
            string = matcher.replaceAll(entry.getString());
        }
        return string;
    }

    public static ValueTypeEntry getAttribute(String attribute, CommandSource src) {
        switch (attribute.toLowerCase()) {
            case "sender":
                return new ValueTypeEntry(ValueTypes.PLAYER, src);
            default:
                return new ValueTypeEntry(ValueTypes.STRING, attribute);
        }
    }

}