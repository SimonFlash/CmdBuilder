package com.mcsimonflash.sponge.cmdcontrol;

import com.google.inject.Inject;
import com.mcsimonflash.sponge.cmdcontrol.commands.ExecuteScript;
import com.mcsimonflash.sponge.cmdcontrol.managers.Config;
import com.mcsimonflash.sponge.cmdcontrol.managers.Storage;
import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

@Plugin(id = "cmdcontrol", name = "CmdControl", version = "s6.0-v1.0.1", authors = "Simon_Flash")
public class CmdControl {

    private static CmdControl plugin;
    public static CmdControl getPlugin() {
        return plugin;
    }

    private static URL discord;
    public static URL getDiscord() {
        return discord;
    }

    private static URL wiki;
    public static URL getWiki() {
        return wiki;
    }

    private static EconomyService econServ;
    public static EconomyService getEconServ() {
        return econServ;
    }

    @Inject
    private Logger logger;
    public Logger getLogger() {
        return logger;
    }

    @Inject
    private static PluginContainer container;
    public static PluginContainer getContainer() {
        return container;
    }

    @Inject
    @ConfigDir(sharedRoot = true)
    private Path directory;
    public Path getDirectory() {
        return directory;
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        plugin = this;
        logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        logger.info("|     CmdControl -- Version 1.0.1     |");
        logger.info("|      Developed By: Simon_Flash      |");
        logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        try {
            discord = new URL("https://discordapp.com/invite/4wayq37");
            wiki = new URL("https://github.com/SimonFlash/CmdControl/wiki");
        } catch (MalformedURLException e) {
            logger.error("Unable to locate discord/wiki urls.");
            e.printStackTrace();
        }
        Config.readConfig();
        CommandSpec ExecuteScript = CommandSpec.builder()
                .permission("cmdcontrol.executescript.base")
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("script-name"))),
                        GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("arguments"))))
                .executor(new ExecuteScript())
                .build();
        Sponge.getCommandManager().register(plugin, ExecuteScript, "ExecuteScript", "Script");
    }

    @Listener
    public void onStart(GameStartedServerEvent event) {
        Util.registerAliases();
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        Config.readConfig();
        Util.registerAliases();
    }

    @Listener
    public void onChangeServProv(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            econServ = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

    @Listener(order = Order.FIRST)
    public void onSendCommand(SendCommandEvent event, @Root CommandSource src) {
        String name = Storage.aliasRegistry.get(event.getCommand());
        if (name != null) {
            event.setCancelled(true);
            Storage.scriptDirectory.get(name).process(src, event.getArguments());
        }
    }
}
