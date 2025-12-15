package com.matthewcash.network;

import java.io.IOException;
import java.nio.file.Path;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import org.slf4j.Logger;

public class ProxyCore {
    public static ProxyCore plugin;
    public static ProxyServer proxy;
    public static Logger logger;
    public static Path dataDirectory;

    @Inject
    public ProxyCore(
        ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory
    ) {

        ProxyCore.plugin = this;
        ProxyCore.proxy = proxy;
        ProxyCore.logger = logger;
        ProxyCore.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        ConfigManager.loadConfig();

        try {
            ServerListEvent.loadFavicon();
        } catch (IOException e) {
            logger.error("Error occurred loading favicon!");
            e.printStackTrace();
        }

        proxy.getEventManager().register(this, new ServerListEvent());
        proxy.getEventManager().register(this, new KickToHub());
        proxy.getEventManager().register(this, new MessageFormat());
        proxy.getEventManager().register(this, new SwitchMessages());
        proxy.getEventManager().register(this, new JoinLeaveMessages());
        proxy.getEventManager().register(this, new TablistManager());

        proxy.getCommandManager().register(
            HubCommand.createCommandMeta(),
            HubCommand.createBrigadierCommand()
        );

        proxy.getCommandManager().register(
            SendCommand.createCommandMeta(),
            SendCommand.createBrigadierCommand()
        );

        TablistManager.startTablistTask();

        logger.info("Enabled ProxyCore!");
    }
}
