package com.mcsimonflash.sponge.cmdcontrol.managers;

import com.google.common.collect.Lists;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.objects.configuration.ConfigWrapper;
import com.mcsimonflash.sponge.cmdcontrol.objects.enums.CmdSource;
import com.mcsimonflash.sponge.cmdcontrol.objects.enums.ArgType;
import com.mcsimonflash.sponge.cmdcontrol.objects.exceptions.ScriptConfigurationException;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.Script;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.ScriptArgument;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.ScriptExecutor;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.ScriptMetadata;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.stream.Stream;

public class Config {

    private static Path rootDir = CmdControl.getPlugin().getDirectory().resolve("cmdcontrol");
    private static Path scriptDir = rootDir.resolve("scripts");
    private static ConfigWrapper core;

    private static boolean errorComments;

    public static boolean initializeNodes() {
        try {
            Files.createDirectories(rootDir);
            core = new ConfigWrapper(rootDir.resolve("cmdcontrol.core"), true);
            if (Files.notExists(scriptDir)) {
                Files.createDirectory(scriptDir);
                Util.getLoader(scriptDir.resolve("item.script"), true);
                Util.getLoader(scriptDir.resolve("lighten.script"), true);
            }
        } catch (IOException e) {
            CmdControl.getPlugin().getLogger().error("Error initializing config nodes.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean readConfig() {
        if (initializeNodes() && Storage.initializeNodes()) {
            errorComments = core.node().getNode("config", "error-comments").getBoolean(false);
            try (Stream<Path> paths = Files.walk(scriptDir)) {
                paths.filter(Files::isRegularFile).forEach(Config::loadScript);
                return true;
            } catch (IOException e) {
                CmdControl.getPlugin().getLogger().error("An unexpected IOException occurred.");
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void loadScript(Path path) {
        String rawName = path.getFileName().toString();
        String fileName = rawName.contains(".") ? rawName.substring(0, rawName.indexOf(".")) : rawName;
        try {
            buildScript(fileName, new ConfigWrapper(path, false));
        } catch (IOException e) {
            CmdControl.getPlugin().getLogger().error("An unexpected IOException occurred.");
            e.printStackTrace();
        }
    }

    public static void buildScript(String script, ConfigWrapper config) throws IOException {
        try {
            LinkedList<ScriptArgument> arguments = Lists.newLinkedList();
            for (CommentedConfigurationNode node : config.node().getNode("arguments").getChildrenList()) {
                String name = node.getNode("name").getString("");
                if (name.isEmpty()) {
                    throw new ScriptConfigurationException(node.getNode("name"), "Empty argument name.");
                }
                String typeStr = node.getNode("type").getString("String");
                ArgType type = ArgType.getType(typeStr);
                if (type == null) {
                    throw new ScriptConfigurationException(node.getNode("type"), "Unknown value type. | Type:[" + typeStr + "]");
                }
                String typeMeta = node.getNode("type-meta").getString(type.equals(ArgType.JOINED_STRINGS) ? " " : "");
                arguments.add(new ScriptArgument(name, type, typeMeta));
            }
            LinkedList<ScriptExecutor> executors = Lists.newLinkedList();
            ScriptMetadata metadata = new ScriptMetadata();
            for (CommentedConfigurationNode node : config.node().getNode("executors").getChildrenList()) {
                String command = node.getNode("command").getString("");
                String sourceStr = node.getNode("source").getString("Sender");
                String sourceMeta = node.getNode("source-meta").getString("");
                CmdSource source = CmdSource.getType(sourceStr);
                if (source == null) {
                    throw new ScriptConfigurationException(node.getNode("source"), "Unknown source value. | Source:[" + sourceStr + "]");
                } else if (source.equals(CmdSource.PLAYER)) {
                    metadata.RequirePlayer = true;
                }
                int wait = node.getNode("wait").getInt(0);
                if (wait < 0) {
                    throw new ScriptConfigurationException(node.getNode("wait"), "Negative wait value. | Wait:[" + wait + "]");
                }
                executors.add(new ScriptExecutor(command, source, sourceMeta, wait));
            }
            CommentedConfigurationNode metaNode = config.node().getNode("metadata");
            metadata.Aliases = Util.getObjectList(metaNode.getNode("aliases"), String.class);
            metadata.Cooldown = metaNode.getNode("cooldown").getInt(0);
            metadata.Cost = metaNode.getNode("cost").getDouble(0.0);
            metadata.Description = metaNode.getNode("description").getString("");
            metadata.Override = metaNode.getNode("override").getBoolean(false);
            metadata.Permissions = Util.getObjectList(metaNode.getNode("permissions"), String.class);
            if (metadata.Cooldown < 0) {
                throw new ScriptConfigurationException(metaNode.getNode("cooldown"), "Metadata cooldown is less than 0. | Cooldown:[" + metadata.Cooldown + "]");
            }
            if (metadata.Cost < 0) {
                throw new ScriptConfigurationException(metaNode.getNode("cost"), "Metadata cost is less than 0. | Cost:[" + metadata.Cost + "]");
            }
            Storage.scriptDirectory.put(script.toLowerCase(), new Script(script, arguments, executors, metadata));
        } catch (ScriptConfigurationException e) {
            CmdControl.getPlugin().getLogger().error(e.getMessage() + " Script:[" + script + "]");
            if (errorComments) {
                e.getErrNode().setComment("ERROR: " + e.getMessage());
                config.save();
            }
        }
    }
}