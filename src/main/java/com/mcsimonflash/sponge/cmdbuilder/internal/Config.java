package com.mcsimonflash.sponge.cmdbuilder.internal;

import com.mcsimonflash.sponge.cmdbuilder.CmdBuilder;
import com.mcsimonflash.sponge.cmdbuilder.miscellaneous.Source;
import com.mcsimonflash.sponge.cmdbuilder.script.Argument;
import com.mcsimonflash.sponge.cmdbuilder.script.Executor;
import com.mcsimonflash.sponge.cmdbuilder.script.Metadata;
import com.mcsimonflash.sponge.cmdbuilder.script.Script;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypeEntry;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.ConfigHolder;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.ConfigurationException;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Collectors;

public class Config {

    private static Path rootDir = CmdBuilder.get().getDirectory(), scriptDir = rootDir.resolve("scripts"), storageDir = rootDir.resolve("storage");
    private static ConfigHolder<CommentedConfigurationNode> core, users;

    public static void load() {
        try {
            core = getLoader(rootDir, "cmdbuilder.conf", true);
            users = getLoader(storageDir, "users.conf", false);
            if (Files.notExists(scriptDir)) {
                getLoader(scriptDir, "item.script", true);
                getLoader(scriptDir, "lighten.script", true);
                getLoader(scriptDir, "magic.script", true);
            }
            Files.walk(scriptDir).filter(Files::isRegularFile).forEach(Config::loadScript);
        } catch (IOException e) {
            CmdBuilder.get().getLogger().error("An unexpected IOException occurred loading files.");
            e.printStackTrace();
        }
    }

    private static ConfigHolder<CommentedConfigurationNode> getLoader(Path dir, String name, boolean asset) throws IOException {
        try {
            Path path = dir.resolve(name);
            if (Files.notExists(path)) {
                Files.createDirectories(dir);
                if (asset) {
                    CmdBuilder.get().getContainer().getAsset(name).get().copyToFile(path);
                } else {
                    Files.createFile(path);
                }
            }
            return ConfigHolder.of(HoconConfigurationLoader.builder().setPath(path).build());
        } catch (IOException e) {
            CmdBuilder.get().getLogger().error("Unable to load config file " + name + ".");
            throw e;
        }
    }

    public static void loadScript(Path path) {
        String name = path.getFileName().toString();
        name = name.contains(".") ? name.substring(0, name.indexOf(".")) : name;
        try {
            ConfigHolder config = ConfigHolder.of(HoconConfigurationLoader.builder().setPath(path).build());
            Scripts.register(Script.builder()
                    .name(name)
                    .arguments(config.getNode("arguments").getChildrenList().stream().map(a -> Argument.builder()
                            .name(a.getNode("name").getString(""))
                            .parser(CmdBuilder.PARSER_TYPES.getValue((a.getNode("type").getString(""))).orElseThrow(() -> new IllegalArgumentException("Unknown parser type " + a.getNode("type").getString("undefined"))))
                            .meta(a.getNode("meta"))
                            .build()).collect(Collectors.toList()))
                    .executors(config.getNode("executors").getChildrenList().stream().map(e -> Executor.builder()
                            .delay(e.getNode("delay").getLong(0))
                            .command(e.getNode("command").getString(""))
                            .source(Source.getSourceOrThrow(e.getNode("source").getString("server")))
                            .build()).collect(Collectors.toList()))
                    .metadata(Metadata.builder()
                            .aliases(Util.getObjectList(config.getNode("metadata", "aliases"), String.class))
                            .cooldown(config.getNode("metadata", "cooldown").getLong(0))
                            .cost(config.getNode("metadata", "cost").getDouble(0.0))
                            .override(config.getNode("metadata", "override").getBoolean())
                            .build())
                    .build());
        } catch (IOException e) {
            CmdBuilder.get().getLogger().error("An unexpected IOException occurred loading script '" + name + "'.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            CmdBuilder.get().getLogger().error("Error loading script '" + name + "': " + e.getMessage());
        }
    }

    public static long getCooldown(UUID uuid, Script script) {
        return users.getNode(uuid.toString(), "cooldowns", script.getName()).getLong(0);
    }

    public static boolean setCooldown(UUID uuid, Script script, long cooldown) {
        users.getNode(uuid.toString(), "cooldowns", script.getName()).setValue(cooldown);
        return users.save();
    }

    public static ValueTypeEntry getMeta(UUID uuid, String name) throws ConfigurationException {
        ConfigurationNode node = users.getNode(uuid.toString(), "meta").getNode((Object[]) name.split("\\."));
        return CmdBuilder.VALUE_TYPES.getValue(node.getNode("type").getString("")).orElseThrow(() ->
                new ConfigurationException(node, "Unknown value type %s.", node.getNode("type").getString("undefined")))
                .deserializeEntry(node.getNode("value"));
    }

    public static boolean setMeta(UUID uuid, String name, ValueTypeEntry entry) {
        ConfigurationNode node = users.getNode(uuid.toString(), "meta").getNode((Object[]) name.split("\\."));
        node.getNode("type").setValue(entry.getType().getName());
        entry.getType().serialize(node.getNode("value"), entry.getValue());
        return users.save();
    }

}