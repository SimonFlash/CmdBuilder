package com.mcsimonflash.sponge.cmdbuilder.internal;

import com.mcsimonflash.sponge.cmdbuilder.CmdBuilder;
import com.mcsimonflash.sponge.cmdbuilder.miscellaneous.Source;
import com.mcsimonflash.sponge.cmdbuilder.script.Argument;
import com.mcsimonflash.sponge.cmdbuilder.script.Executor;
import com.mcsimonflash.sponge.cmdbuilder.script.Metadata;
import com.mcsimonflash.sponge.cmdbuilder.script.Script;
import com.mcsimonflash.sponge.cmdbuilder.type.ValueTypeRegistry;
import com.mcsimonflash.sponge.cmdcontrol.teslalibs.configuration.ConfigHolder;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Collectors;

public class Config {

    private static Path rootDir = CmdBuilder.getInstance().Directory, scriptDir = rootDir.resolve("scripts"), storageDir = rootDir.resolve("storage");
    private static ConfigHolder<CommentedConfigurationNode> core, users;

    public static void load() {
        try {
            core = getLoader(rootDir, "cmdbuilder.conf", true);
            users = getLoader(storageDir, "users.conf", false);
            if (Files.notExists(scriptDir)) {
                getLoader(scriptDir, "item.script", true);
                getLoader(scriptDir, "lighten.script", true);
            }
            if (core.getNode("-legacy").getBoolean()) {
                CmdBuilder.getInstance().Logger.warn("Attempting to migrate legacy scripts from CmdControl.");
                Path legacyScriptDir = rootDir.getParent().resolve("cmdcontrol").resolve("scripts");
                if (Files.exists(legacyScriptDir)) {
                    Files.walk(legacyScriptDir).filter(Files::isRegularFile).forEach(Config::legacyScript);
                    CmdBuilder.getInstance().Logger.warn("Finished migrating legacy scripts from CmdControl.");
                } else {
                    CmdBuilder.getInstance().Logger.warn("The folder /config/cmdcontrol/scripts does not exist; ending legacy migration.");
                }
                core.getNode("-legacy").setValue(false);
                core.save();
            }
            Files.walk(scriptDir).filter(Files::isRegularFile).forEach(Config::loadScript);
        } catch (IOException e) {
            CmdBuilder.getInstance().Logger.error("An unexpected IOException occurred loading files.");
            e.printStackTrace();
        }
    }

    private static ConfigHolder<CommentedConfigurationNode> getLoader(Path dir, String name, boolean asset) throws IOException {
        try {
            Path path = dir.resolve(name);
            if (Files.notExists(path)) {
                Files.createDirectories(dir);
                if (asset) {
                    CmdBuilder.getInstance().Container.getAsset(name).get().copyToFile(path);
                } else {
                    Files.createFile(path);
                }
            }
            return ConfigHolder.of(HoconConfigurationLoader.builder().setPath(path).build());
        } catch (IOException e) {
            CmdBuilder.getInstance().Logger.error("Unable to load config file " + name + ".");
            throw e;
        }
    }

    public static void loadScript(Path path) {
        String fileName = path.getFileName().toString();
        fileName = fileName.contains(".") ? fileName.substring(0, fileName.indexOf(".")) : fileName;
        try {
            ConfigHolder config = ConfigHolder.of(HoconConfigurationLoader.builder().setPath(path).build());
            Scripts.register(Script.builder()
                    .name(fileName)
                    .arguments(config.getNode("arguments").getChildrenList().stream().map(a -> Argument.builder()
                            .name(a.getNode("name").getString())
                            .type(ValueTypeRegistry.getTypeOrThrow(a.getNode("type").getString()))
                            .meta(a.getNode("meta"))
                            .build()).collect(Collectors.toList()))
                    .executors(config.getNode("executors").getChildrenList().stream().map(e -> Executor.builder()
                            .delay(e.getNode("delay").getLong())
                            .command(e.getNode("command").getString())
                            .source(Source.getSourceOrThrow(e.getNode("source").getString("server")))
                            .build()).collect(Collectors.toList()))
                    .metadata(Metadata.builder()
                            .aliases(Util.getObjectList(config.getNode("metadata", "aliases"), String.class))
                            .cooldown(config.getNode("metadata", "cooldown").getLong())
                            .cost(config.getNode("metadata", "cost").getDouble())
                            .override(config.getNode("metadata", "override").getBoolean())
                            .build())
                    .build());
        } catch (IOException e) {
            CmdBuilder.getInstance().Logger.error("An unexpected IOException occurred loading script '" + fileName + "'.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            CmdBuilder.getInstance().Logger.error("Error loading script '" + fileName + "': " + e.getMessage());
        }
    }

    public static void legacyScript(Path path) {
        String name = FilenameUtils.getBaseName(path.getFileName().toString());
        try {
            ConfigHolder<CommentedConfigurationNode> legacy = ConfigHolder.of(HoconConfigurationLoader.builder().setPath(path).build());
            int delay = 0;
            for (ConfigurationNode executor : legacy.getNode("executors").getChildrenList()) {
                if (delay != 0) {
                    executor.getNode("delay").setValue(delay);
                }
                delay += executor.getNode("wait").getInt();
                executor.getNode("wait").setValue(null);
            }
            legacy.getNode("metadata", "player").setComment("Deprecated - now done automatically.");
            legacy.getNode("metadata", "permissions").setComment("Deprecated - if your player has permission for this script, make sure they have permission for any commands they would run using it.");
            ConfigHolder config = ConfigHolder.of(HoconConfigurationLoader.builder().setPath(scriptDir.resolve(path.getFileName())).build());
            config.getNode().setValue(legacy.getNode());
        } catch (IOException e) {
            CmdBuilder.getInstance().Logger.error("An unexpected IOException occurred loading script '" + name + "'.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            CmdBuilder.getInstance().Logger.error("Error loading script '" + name + "': " + e.getMessage());
        }
    }

    public static long getCooldown(UUID uuid, Script script) {
        return users.getNode(uuid.toString(), "cooldowns", script.getName()).getLong(0);
    }

    public static boolean setCooldown(UUID uuid, Script script, long cooldown) {
        users.getNode(uuid.toString(), "cooldowns", script.getName()).setValue(cooldown);
        return users.save();
    }

    /*public static CommentedConfigurationNode getMeta(UUID uuid) {
        return NodeUtils.copy(users.getNode(uuid.toString(), "meta"));
    }

    public static boolean setMeta(UUID uuid, String[] path, Object value) {
        users.getNode(uuid.toString(), "meta").getNode((Object[]) path).setValue(value);
        return users.save();
    }*/

}