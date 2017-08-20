package com.mcsimonflash.sponge.cmdcontrol.managers;

import com.google.common.collect.Lists;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.api.TypeRegistry;
import com.mcsimonflash.sponge.cmdcontrol.api.ValueType;
import com.mcsimonflash.sponge.cmdcontrol.objects.miscellaneous.ConfigContainer;
import com.mcsimonflash.sponge.cmdcontrol.objects.miscellaneous.Source;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.Argument;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.Executor;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.Metadata;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.Script;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class Config {

    private static Path rootDir = CmdControl.getPlugin().getDirectory().resolve("cmdcontrol");
    private static Path scriptDir = rootDir.resolve("scripts");
    private static ConfigContainer core;

    public static void load() {
        try {
            Files.createDirectories(rootDir);
            core = new ConfigContainer(rootDir.resolve("cmdcontrol.core"), true);
            if (Files.notExists(scriptDir)) {
                Files.createDirectory(scriptDir);
                Util.getLoader(scriptDir.resolve("item.script"), true);
                Util.getLoader(scriptDir.resolve("lighten.script"), true);
            }
            Files.walk(scriptDir).filter(Files::isRegularFile).forEach(Config::loadScript);
        } catch (IOException e) {
            CmdControl.getPlugin().getLogger().error("An unexpected IOException occurred loading files.");
            e.printStackTrace();
        }
    }

    public static void loadScript(Path path) {
        String rawName = path.getFileName().toString();
        String fileName = rawName.contains(".") ? rawName.substring(0, rawName.indexOf(".")) : rawName;
        try {
            ConfigContainer config = new ConfigContainer(path, false);
            LinkedList<Argument> arguments = Lists.newLinkedList();
            for (CommentedConfigurationNode node : config.node().getNode("arguments").getChildrenList()) {
                String name = node.getNode("name").getString();
                ValueType type = TypeRegistry.getTypeOrThrow(node.getNode("type").getString());
                CommentedConfigurationNode meta = Util.copyNode(node.getNode("meta"));
                arguments.add(new Argument(name, type, meta));
            }
            LinkedList<Executor> executors = Lists.newLinkedList();
            for (CommentedConfigurationNode node : config.node().getNode("executors").getChildrenList()) {
                String command = node.getNode("command").getString();
                Source source = Source.getSourceOrThrow(node.getNode("source").getString());
                CommentedConfigurationNode meta = Util.copyNode(node.getNode("meta"));
                int wait = node.getNode("wait").getInt();
                executors.add(new Executor(command, source, meta, wait));
            }
            CommentedConfigurationNode meta = config.node().getNode("meta");
            List<String> aliases = Util.getObjectList(meta.getNode("aliases"), String.class);
            int cooldown = meta.getNode("cooldown").getInt(0);
            double cost = meta.getNode("cost").getDouble(0.0);
            boolean override = meta.getNode("override").getBoolean(false);
            List<String> permissions = Util.getObjectList(meta.getNode("permissions"), String.class);
            boolean player = meta.getNode("player").getBoolean(false) || executors.stream().anyMatch(e -> e.getSource() == Source.PLAYER);
            Metadata metadata = new Metadata(override, player, cooldown, cost, aliases, permissions);
            Scripts.directory.put(fileName, new Script(fileName, arguments, executors, metadata));
        } catch (IOException e) {
            CmdControl.getPlugin().getLogger().error("An unexpected IOException occurred loading script \"" + fileName + "\".");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            CmdControl.getPlugin().getLogger().error("Error loading script \"" + fileName + "\": " + e.getMessage());
        }
    }

}