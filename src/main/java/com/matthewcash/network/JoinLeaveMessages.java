package com.matthewcash.network;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class JoinLeaveMessages {
    @Subscribe(order = PostOrder.NORMAL)
    public void onJoin(PostLoginEvent event) {
        final String username = event.getPlayer().getUsername();

        final Component message = MiniMessage.miniMessage()
            .deserialize(
                "<gray>Join ></gray> <yellow><bold><username></bold></yellow>",
                Placeholder.unparsed("username", username));

        ProxyCore.proxy.sendMessage(message);
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onLeave(DisconnectEvent event) {
        final String username = event.getPlayer().getUsername();

        final Component message = MiniMessage.miniMessage()
            .deserialize(
                "<gray>Quit ></gray> <yellow><bold><username></bold></yellow>",
                Placeholder.unparsed("username", username));

        ProxyCore.proxy.sendMessage(message);
    }
}
