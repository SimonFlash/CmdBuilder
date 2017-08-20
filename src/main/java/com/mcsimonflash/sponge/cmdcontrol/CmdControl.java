package com.mcsimonflash.sponge.cmdcontrol;

import com.google.inject.Inject;
import com.mcsimonflash.sponge.cmdcontrol.managers.Util;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

@Plugin(id = "cmdcontrol", name = "CmdControl", version = "s6.0-v1.1.0-pr1", authors = "Simon_Flash")
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
        logger.info("|   CmdControl -- Version 1.1.0-pr1   |");
        logger.info("|      Developed By: Simon_Flash      |");
        logger.info("+=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=+");
        try {
            discord = new URL("https://discordapp.com/invite/4wayq37");
            wiki = new URL("https://github.com/SimonFlash/CmdControl/wiki");
        } catch (MalformedURLException e) {
            logger.error("Unable to locate discord/wiki urls.");
            e.printStackTrace();
        }
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
    public void onChangeServProv(ChangeServiceProviderEvent event) {
        if (event.getService().equals(EconomyService.class)) {
            econServ = (EconomyService) event.getNewProviderRegistration().getProvider();
        }
    }

}