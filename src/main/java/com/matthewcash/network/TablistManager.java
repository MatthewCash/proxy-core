package com.matthewcash.network;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TablistManager {
    @Subscribe(order = PostOrder.NORMAL)
    public void onServerConnected(ServerConnectedEvent event) {
        final Component tabHeader = MiniMessage.miniMessage()
            .deserialize(
                ConfigManager.config.get("tablist.header")
            );
        final Component tabFooter = MiniMessage.miniMessage()
            .deserialize(ConfigManager.config.get("tablist.footer"));

        event.getPlayer().sendPlayerListHeaderAndFooter(tabHeader, tabFooter);
    }
}
