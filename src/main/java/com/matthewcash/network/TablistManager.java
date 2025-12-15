package com.matthewcash.network;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.player.TabListEntry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class TablistManager {
    @Subscribe(order = PostOrder.LAST)
    public void onServerConnected(ServerPostConnectEvent event) {
        final Component tabHeader = MiniMessage.miniMessage()
            .deserialize(
                ConfigManager.config.get("tablist.header")
            );
        final Component tabFooter = MiniMessage.miniMessage()
            .deserialize(ConfigManager.config.get("tablist.footer"));

        event.getPlayer().sendPlayerListHeaderAndFooter(tabHeader, tabFooter);

        // Without delay tablist may not get set
        ProxyCore.proxy.getScheduler()
            .buildTask(ProxyCore.plugin, TablistManager::updateAllTabLists)
            .delay(500L, TimeUnit.MILLISECONDS).schedule();
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPlayerDisconnect(DisconnectEvent event) {
        updateAllTabLists();
    }

    public static void startTablistTask() {
        ProxyCore.proxy.getScheduler()
            .buildTask(ProxyCore.plugin, TablistManager::updateAllTabLists)
            .repeat(5L, TimeUnit.SECONDS)
            .schedule();
    }

    private static void updateAllTabLists() {
        // TODO: is there a non (On^2) way to do this?
        ProxyCore.proxy.getAllPlayers().forEach(player -> {
            player.getTabList().clearAll();

            AtomicInteger serverIndex = new AtomicInteger(0);
            ProxyCore.proxy.getAllServers().forEach(server -> {
                int currentIndex = serverIndex.incrementAndGet();

                server.getPlayersConnected().forEach(subPlayer -> {
                    boolean isHub = server.getServerInfo().getName()
                        .equals("hub");

                    final TabListEntry entry = TabListEntry
                        .builder()
                        .tabList(player.getTabList())
                        .profile(subPlayer.getGameProfile())
                        .listOrder(isHub ? 0xff : currentIndex)
                        .latency((int) subPlayer.getPing())
                        .displayName(
                            MiniMessage.miniMessage().deserialize(
                                "<player> <gray>@</gray> <bold><server></bold>",
                                Placeholder.unparsed(
                                    "server", ServerAliases.getAlias(server)
                                ),
                                Placeholder
                                    .unparsed("player", subPlayer.getUsername())
                            )
                        )
                        .build();

                    player.getTabList().addEntry(entry);
                });
            });
        });
    }
}
