package com.matthewcash.network;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class SwitchMessages {
    @Subscribe(order = PostOrder.NORMAL)
    public void onServerConnected(ServerConnectedEvent event) {
        if (event.getPreviousServer().isEmpty())
            return;

        final String username = event.getPlayer().getUsername();
        final String serverName = ServerAliases.getAlias(event.getServer());

        final Component message = MiniMessage.miniMessage()
            .deserialize(
                "<bold><yellow><username></yellow> <gray>has moved to</gray> <yellow><server></yellow></bold>",
                Placeholder.unparsed("username", username),
                Placeholder.unparsed("server", serverName));

        ProxyCore.proxy.sendMessage(message);
    }
}
