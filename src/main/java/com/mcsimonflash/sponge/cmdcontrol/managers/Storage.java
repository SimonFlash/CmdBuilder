package com.mcsimonflash.sponge.cmdcontrol.managers;

import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.objects.miscellaneous.ConfigContainer;
import org.spongepowered.api.entity.living.player.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Storage {

    private static Path storDir = CmdControl.getPlugin().getDirectory().resolve("cmdcontrol").resolve("storage");
    private static ConfigContainer cooldowns;


    public static boolean load() {
        try {
            Files.createDirectories(storDir);
            cooldowns = new ConfigContainer(storDir.resolve("cooldowns.stor"), false);
            return true;
        } catch (IOException e) {
            CmdControl.getPlugin().getLogger().error("Error initializing storage nodes.");
            e.printStackTrace();
            return false;
        }
    }

    public static long getCooldown(User user, String scriptName) {
        return cooldowns.node().getNode(user.getUniqueId().toString(), scriptName).getLong(0);
    }

    public static boolean setCooldown(User user, String scriptName) {
        cooldowns.node().getNode(user.getUniqueId().toString(), scriptName).setValue(System.currentTimeMillis());
        return cooldowns.save();
    }
}