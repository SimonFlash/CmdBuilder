package com.mcsimonflash.sponge.cmdbuilder;

import com.google.inject.Inject;
import com.mcsimonflash.sponge.cmdbuilder.internal.Scripts;
import com.mcsimonflash.sponge.cmdbuilder.internal.Util;
import com.mcsimonflash.sponge.cmdcontrol.core.CmdPlugin;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

@Plugin(id = "cmdbuilder", name = "CmdBuilder", version = "1.2.0", dependencies = @Dependency(id="cmdcontrol"), url = "https://ore.spongepowered.org/Simon_Flash/CmdBuilder", authors = "Simon_Flash")
public class CmdBuilder extends CmdPlugin {

    private static CmdBuilder instance;

    @Inject
    public CmdBuilder(PluginContainer container) {
        super(container);
        instance = this;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
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

    public static CmdBuilder get() {
        return instance;
    }

}