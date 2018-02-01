package com.mcsimonflash.sponge.cmdbuilder;

import com.google.inject.Inject;
import com.mcsimonflash.sponge.cmdbuilder.internal.Scripts;
import com.mcsimonflash.sponge.cmdbuilder.internal.Util;
import com.mcsimonflash.sponge.cmdcontrol.core.CmdPlugin;
import org.slf4j.Logger;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "cmdbuilder", name = "CmdBuilder", version = "1.1.0", dependencies = @Dependency(id="cmdcontrol"), url = "https://github.com/SimonFlash/CmdBuilder/wiki", authors = "Simon_Flash")
public class CmdBuilder extends CmdPlugin {

    private static CmdBuilder instance;

    @Inject
    public CmdBuilder(PluginContainer container) {
        super(container);
        instance = this;
    }

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path directory;
    public Path getDirectory() {
        return directory;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        Logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        Logger.info("|     CmdBuilder -- Version 1.1.0     |");
        Logger.info("|      Developed By: Simon_Flash      |");
        Logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        Util.initialize();
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        Util.initialize();
    }


    @Listener
    public void onSendCommand(SendCommandEvent event, @First CommandSource src) {
        Optional<CommandResult> result = Scripts.process(src, event.getCommand() + " " + event.getArguments());
        if (result.isPresent()) {
            event.setCancelled(true);
            event.setResult(result.get());
        }
    }

    public static CmdBuilder getInstance() {
        return instance;
    }

}