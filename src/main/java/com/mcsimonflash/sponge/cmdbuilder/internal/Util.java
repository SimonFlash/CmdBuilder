package com.mcsimonflash.sponge.cmdbuilder.internal;

import com.google.common.collect.Lists;
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
import java.util.stream.Collectors;

public class Util {

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

}