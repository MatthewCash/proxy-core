package com.matthewcash.network;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class KickToHub {
    @Subscribe(order = PostOrder.LAST)
    public void onKick(KickedFromServerEvent event) {
        final Component reason = event.getServerKickReason()
            .orElse(Component.text("No Reason Supplied"));

        final Component message = MiniMessage.miniMessage()
            .deserialize("<aqua><bold>Kicked</bold></aqua> > <gray><reason></gray>",
                Placeholder.component("reason", reason));

        if (event.kickedDuringServerConnect()
            || event.getServer().getServerInfo().getName().equals("hub")) {
            event.setResult(KickedFromServerEvent.Notify
                .create(message));
        } else {
            event.setResult(KickedFromServerEvent.RedirectPlayer
                .create(ProxyCore.proxy.getServer("hub").get(), message));
        }
    }
}
