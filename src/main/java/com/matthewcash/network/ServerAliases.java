package com.matthewcash.network;

import com.velocitypowered.api.proxy.server.RegisteredServer;

public class ServerAliases {
    public static String getAlias(RegisteredServer server) {
        final String name = server.getServerInfo().getName();

        return ConfigManager.config.getOrElse("serveraliases." + name, name);
    }
}
