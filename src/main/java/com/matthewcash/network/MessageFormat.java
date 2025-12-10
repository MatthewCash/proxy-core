package com.matthewcash.network;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class MessageFormat {
    public static Component getDisplayName(Player player) {
        final User user = LuckPermsProvider.get().getPlayerAdapter(Player.class)
            .getUser(player);
        final String rawPrefix = user.getCachedData().getMetaData().getPrefix();
        final Component prefix = MiniMessage.miniMessage().deserialize(
            rawPrefix
        );

        return MiniMessage.miniMessage()
            .deserialize(
                ConfigManager.config.get("messages.display_name"),
                Placeholder.component(
                    "prefix", prefix
                ),
                Placeholder.unparsed(
                    "prefixspace", rawPrefix.length() > 0 ? " " : ""
                ),
                Placeholder.unparsed(
                    "username", player.getUsername()
                )
            );
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerChat(PlayerChatEvent event) {
        final String text = event.getMessage();
        final Player player = event.getPlayer();

        final boolean formattedMessages = player.hasPermission(
            "mcash.messages.formatted"
        );

        final var content = formattedMessages
            ? Placeholder.component(
                "content", MiniMessage.miniMessage()
                    .deserialize(
                        text
                    )
            )
            : Placeholder.unparsed(
                "content", text
            );

        final Component displayName = getDisplayName(
            player
        );

        final Component message = MiniMessage.miniMessage()
            .deserialize(
                ConfigManager.config.get("messages.chat"),
                Placeholder.component(
                    "displayname", displayName
                ),
                content
            );

        ProxyCore.proxy.sendMessage(
            message
        );
    }
}
