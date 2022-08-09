package com.matthewcash.network;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class MessageFormat {
    @Subscribe(order = PostOrder.LAST)
    public void onMessage(PlayerChatEvent event) {
        final String text = event.getMessage();
        final Player player = event.getPlayer();

        final User user = LuckPermsProvider.get().getPlayerAdapter(Player.class).getUser(player);

        final String rawPrefix = user.getCachedData().getMetaData().getPrefix();
        final Component prefix = MiniMessage.miniMessage().deserialize(rawPrefix);

        final boolean formattedMessages = player.hasPermission("mcash.messages.formatted");

        final var content = formattedMessages
            ? Placeholder.component("content", MiniMessage.miniMessage()
                .deserialize(text))
            : Placeholder.unparsed("content", text);

        final Component message = MiniMessage.miniMessage()
            .deserialize("<prefix><prefixspace><gray><username></gray> <content>",
                Placeholder.component("prefix", prefix),
                Placeholder.unparsed("prefixspace", rawPrefix.length() > 0 ? " " : ""),
                Placeholder.unparsed("username", player.getUsername()), content);

        ProxyCore.proxy.sendMessage(message);

        if (player.getProtocolVersion().compareTo(ProtocolVersion.MINECRAFT_1_19_1) >= 0) {
            player.disconnect(Component.text("1.19.1+ players cannot send chat messages at this time!"));
        }

        event.setResult(PlayerChatEvent.ChatResult.denied());
    }
}
