package com.mcsimonflash.sponge.cmdcontrol.objects.configuration;

import com.mcsimonflash.sponge.cmdcontrol.CmdControl;
import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

public class ConfigWrapper {

    private HoconConfigurationLoader loader;
    private CommentedConfigurationNode node;

    public ConfigWrapper(Path path, boolean asset) throws IOException {
        loader = Util.getLoader(path, asset);
        node = loader.load();
    }

    public CommentedConfigurationNode node() {
        return node;
    }

    public boolean save() {
        try {
            loader.save(node);
        } catch (IOException ex) {
            CmdControl.getPlugin().getLogger().error("Unable to attach error comment!");
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
