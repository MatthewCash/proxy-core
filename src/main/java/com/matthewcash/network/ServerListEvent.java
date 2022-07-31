package com.matthewcash.network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.proxy.server.ServerPing.SamplePlayer;
import com.velocitypowered.api.util.Favicon;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ServerListEvent {
    private static Favicon favicon;

    public static void loadFavicon() throws IOException {
        Path faviconPath = ProxyCore.dataDirectory.resolve("favicon.png");

        favicon = new Favicon(
            Base64.getEncoder().encodeToString(Files.readAllBytes(faviconPath)));
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPing(ProxyPingEvent event) {
        final ServerPing ping = event.getPing();

        ServerPing.Builder builder = ping.asBuilder();
        builder.version(new ServerPing.Version(ping.getVersion().getProtocol(),
            ConfigManager.config.get("serverlist.version")));

        builder.description(MiniMessage.miniMessage()
            .deserialize(ConfigManager.config.get("serverlist.description")));

        builder.onlinePlayers(ping.getPlayers().get().getOnline());
        builder.maximumPlayers(20000);

        builder.clearSamplePlayers();

        final List<String> samplePlayerLines = ConfigManager.config
            .get("serverlist.sample_players");

        builder.samplePlayers(
            samplePlayerLines.stream()
                .map(line -> new ServerPing.SamplePlayer(
                    LegacyComponentSerializer.legacySection()
                        .serialize(MiniMessage.miniMessage().deserialize(line)),
                    UUID.randomUUID()))
                .toArray(SamplePlayer[]::new));

        builder.favicon(favicon);

        event.setPing(builder.build());
    }
}
