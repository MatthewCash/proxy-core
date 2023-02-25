package com.matthewcash.network;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class TablistManager {
    @Subscribe(order = PostOrder.NORMAL)
    public void onServerConnected(ServerConnectedEvent event) {
        Component tabHeader = MiniMessage.miniMessage()
            .deserialize(
                "<bold> <yellow>Matthew_Cash</yellow> <gray>Network</gray></bold>"
            );
        Component tabFooter = MiniMessage.miniMessage()
            .deserialize("www.matthew-cash.com");

        event.getPlayer().sendPlayerListHeaderAndFooter(tabHeader, tabFooter);
    }
}
