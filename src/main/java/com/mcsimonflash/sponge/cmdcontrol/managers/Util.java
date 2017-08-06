package com.mcsimonflash.sponge.cmdcontrol.managers;

import com.google.common.reflect.TypeToken;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.objects.exceptions.ScriptExecutionException;
import com.mcsimonflash.sponge.cmdcontrol.objects.exceptions.ScriptConfigurationException;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.Script;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static final Pattern argPattern = Pattern.compile("(<(?:([@])?([A-Za-z0-9]+))(?:[#]([A-Za-z0-9]+))?>)");
    public static final Pattern doubleRangePattern = Pattern.compile("(?:([-+]?[0-9]*[.]?[0-9]+)[ ]?<([=])?[ ]?)?#(?:[ ]?<([=])?[ ]?([-+]?[0-9]*[.]?[0-9]+))?");
    public static final Pattern integerRangePattern = Pattern.compile("(?:([-+]?[0-9]+)[ ]?<([=])?[ ]?)?#(?:[ ]?<([=])?[ ]?([-+]?[0-9]+))?");
    public static final Pattern separatorPattern = Pattern.compile(", |,| ");
    public static final Text msgPrefix = toText("&4[&6CmdControl&4] &e");

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
            CmdControl.getPlugin().getLogger().error("Error loading file! File:[" + path.getFileName().toString() + "]");
            throw e;
        }
    }

    public static <T> List<T> getObjectList(CommentedConfigurationNode node, Class<T> clazz) throws ScriptConfigurationException {
        try {
            return node.getList(TypeToken.of(clazz));
        } catch (ObjectMappingException e) {
            throw new ScriptConfigurationException(node, "Unable to load list. | Type:[" + clazz.getSimpleName() + "] List:[" + node.getKey() + "]");
        }
    }

    public static void registerAliases() {
        Storage.aliasRegistry.clear();
        Storage.scriptDirectory.values().forEach(Script::registerAliases);
    }

    public static String getParsedValue(CommandSource src, String string, Map<String, Object> arguments) throws ScriptExecutionException {
        String modifiedString = string;
        Matcher matcher = Util.argPattern.matcher(string);
        while (matcher.find()) {
            if (matcher.group(2) != null) {
                if (!matcher.group(3).equalsIgnoreCase("sender")) {
                    throw new ScriptExecutionException("Unknown dynamic argument. | Argument:[" + matcher.group(3) + "]");
                }
                modifiedString = modifiedString.replace(matcher.group(1), src.getName());
            } else {
                if (!arguments.containsKey(matcher.group(3))) {
                    throw new ScriptExecutionException("Unknown argument. | Argument:[" + matcher.group(3) + "]");
                }
                modifiedString = modifiedString.replace(matcher.group(1), arguments.get(matcher.group(3)).toString());
            }
        }
        return modifiedString;
    }
}