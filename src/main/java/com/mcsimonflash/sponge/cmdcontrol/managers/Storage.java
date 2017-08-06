package com.mcsimonflash.sponge.cmdcontrol.managers;

import com.google.common.collect.Maps;
import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.objects.configuration.ConfigWrapper;
import com.mcsimonflash.sponge.cmdcontrol.objects.scripts.Script;
import org.spongepowered.api.entity.living.player.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Storage {

    private static Path storDir = CmdControl.getPlugin().getDirectory().resolve("cmdcontrol").resolve("storage");
    private static ConfigWrapper cooldowns;

    public static Map<String, Script> scriptDirectory = Maps.newHashMap();
    public static Map<String, String> aliasRegistry = Maps.newHashMap();

    public static boolean initializeNodes() {
        try {
            Files.createDirectories(storDir);
            cooldowns = new ConfigWrapper(storDir.resolve("cooldowns.stor"), false);
        } catch (IOException e) {
            CmdControl.getPlugin().getLogger().error("Error initializing storage nodes.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static long getCooldown(User user, String scriptName) {
        return cooldowns.node().getNode(user.getUniqueId().toString(), scriptName).getLong(0);
    }

    public static boolean setCooldown(User user, String scriptName) {
        cooldowns.node().getNode(user.getUniqueId().toString(), scriptName).setValue(System.currentTimeMillis());
        return cooldowns.save();
    }
}