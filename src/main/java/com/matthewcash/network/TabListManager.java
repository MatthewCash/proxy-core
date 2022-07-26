package com.matthewcash.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabList;
import com.velocitypowered.api.proxy.player.TabListEntry;
import com.velocitypowered.api.util.GameProfile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class TabListManager {
    static final GameProfile serverGameProfile = new GameProfile(
        UUID.fromString("7f368e1c-a1b5-4f75-b697-701270ac1aeb"), "01",
        new ArrayList<GameProfile.Property>());
    static final GameProfile pingGameProfile = new GameProfile(
        UUID.fromString("a2631d1d-6c6d-4c7e-9caa-d311b823c6b2"), "00",
        new ArrayList<GameProfile.Property>());

    static Map<String, GameProfile> serverProfiles = new HashMap<>();

    @Subscribe(order = PostOrder.NORMAL)
    public void onServerConnected(ServerPostConnectEvent event) {
        Player player = event.getPlayer();

        TabList tablist = player.getTabList();

        sendInitialTabList(player);
        sendServerList();

        tablist.removeEntry(serverGameProfile.getId());

        String serverName = ServerAliases
            .getAlias(event.getPlayer().getCurrentServer().get().getServer());

        tablist.addEntry(TabListEntry.builder().tabList(tablist)
            .profile(serverGameProfile)
            .displayName(MiniMessage.miniMessage().deserialize(
                "<gray>Server</gray> <bold><yellow><server></yellow></bold>",
                Placeholder.unparsed("server", serverName)))
            .build());

    }

    public static void updatePing() {
        var players = ProxyCore.proxy.getAllPlayers();

        players.forEach(player -> {
            TabList tablist = player.getTabList();

            tablist.removeEntry(pingGameProfile.getId());

            long ping = player.getPing();

            String pingColor = ping < 100 ? "green" : ping < 200 ? "yellow" : "red";

            tablist.addEntry(TabListEntry.builder().tabList(tablist).profile(pingGameProfile)
                .displayName(MiniMessage.miniMessage().deserialize(
                    String.format("<gray>Ping</gray> <%s><ping></%s>", pingColor, pingColor),
                    Placeholder.unparsed("ping", String.valueOf(ping))))
                .build());
        });
    }

    private static GameProfile generateFakeProfile(String name) {
        return new GameProfile(UUID.randomUUID(), name, new ArrayList<GameProfile.Property>());
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void onLogin(LoginEvent event) {
        // sendInitialTabList(event.getPlayer());
    }

    public static void sendServerList() {
        var players = ProxyCore.proxy.getAllPlayers();

        players.forEach(player -> {
            var servers = ProxyCore.proxy.getAllServers();
            TabList tablist = player.getTabList();

            servers.forEach(server -> {
                // if (server.getPlayersConnected().size() < 1)
                // return;
                String name = ServerAliases.getAlias(server);
                var serverPlayers = server.getPlayersConnected();

                GameProfile serverProfile = serverProfiles.getOrDefault(name,
                    generateFakeProfile("02" + name));
                serverProfiles.putIfAbsent(name, serverProfile);

                tablist.removeEntry(serverProfile.getId());

                tablist
                    .addEntry(TabListEntry.builder().tabList(tablist)
                        .profile(
                            serverProfile)
                        .displayName(
                            MiniMessage.miniMessage()
                                .deserialize(
                                    "<bold><yellow><name></yellow> <gray><count></gray></bold>",
                                    Placeholder.unparsed("name", name),
                                    Placeholder.unparsed("count",
                                        String.valueOf(serverPlayers.size()))))
                        .build());

                serverPlayers.forEach(serverPlayer -> {
                    String username = serverPlayer.getUsername();
                    long ping = serverPlayer.getPing();

                    String playerPingColor = ping < 100 ? "green" : ping < 200 ? "yellow" : "red";

                    ProxyCore.logger
                        .info("Updating tablist for " + player + ", user: " + serverPlayer);
                    ProxyCore.logger
                        .info("UUID Check " + serverPlayer.getUniqueId() + " - " + new GameProfile(
                            serverPlayer.getUniqueId(), "02" + name + "1",
                            new ArrayList<GameProfile.Property>()).getId());

                    tablist.removeEntry(serverPlayer.getUniqueId());

                    tablist
                        .addEntry(TabListEntry.builder().tabList(tablist)
                            .profile(new GameProfile(serverPlayer.getUniqueId(), "02" + name + "1",
                                new ArrayList<GameProfile.Property>()))
                            .displayName(MiniMessage.miniMessage()
                                .deserialize(
                                    String.format("<gray>-</gray> <username> <%s><ping></%s>",
                                        playerPingColor, playerPingColor),
                                    Placeholder.unparsed("username", username),
                                    Placeholder.unparsed("ping", String.valueOf(ping))))
                            .latency((int) ping).build());
                });
            });
        });
    }

    public static void sendInitialTabList(Player player) {
        Component tabHeader = MiniMessage.miniMessage()
            .deserialize("<bold> <yellow>Matthew_Cash</yellow> <gray>Network</gray></bold>");
        Component tabFooter = MiniMessage.miniMessage().deserialize("www.matthew-cash.com");

        player.sendPlayerListHeaderAndFooter(tabHeader, tabFooter);

        player.getTabList().getEntries()
            .forEach(entry -> player.getTabList().removeEntry(entry.getProfile().getId()));

        TabList tablist = player.getTabList();

        tablist.addEntry(TabListEntry
            .builder().tabList(tablist).profile(serverGameProfile)
            .displayName(MiniMessage.miniMessage()
                .deserialize("<gray>Server</gray> <bold><yellow>Unknown</yellow></bold>"))
            .build());

        tablist.addEntry(TabListEntry.builder().tabList(tablist).profile(pingGameProfile)
            .displayName(
                MiniMessage.miniMessage().deserialize("<gray>Ping</gray> <green>0</green>"))
            .build());
    }
}
