package com.matthewcash.network;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.player.TabList;
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
        ProxyCore.proxy.getAllPlayers().forEach(player -> {
            TabList tabList = player.getTabList();
            Set<UUID> currentPlayerUuids = new HashSet<>();

            AtomicInteger serverIndex = new AtomicInteger(0);
            ProxyCore.proxy.getAllServers().forEach(server -> {
                int currentIndex = serverIndex.incrementAndGet();

                server.getPlayersConnected().forEach(subPlayer -> {
                    UUID subPlayerUuid = subPlayer.getUniqueId();
                    currentPlayerUuids.add(subPlayerUuid);

                    boolean isHub = server.getServerInfo().getName()
                        .equals("hub");

                    Component displayName = MiniMessage.miniMessage()
                        .deserialize(
                            "<player> <gray>@</gray> <bold><server></bold>",
                            Placeholder.unparsed(
                                "server", ServerAliases.getAlias(server)
                            ),
                            Placeholder
                                .unparsed("player", subPlayer.getUsername())
                        );

                    int listOrder = isHub ? 0xff : currentIndex;
                    int latency = (int) subPlayer.getPing();

                    Optional<TabListEntry> existingEntry = tabList
                        .getEntry(subPlayerUuid);

                    if (existingEntry.isPresent()) {
                        // Update existing entry to preserve gamemode
                        TabListEntry entry = existingEntry.get();
                        entry.setDisplayName(displayName);
                        entry.setListOrder(listOrder);
                        entry.setLatency(latency);
                    } else {
                        // Add new entry if player wasn't in the list
                        TabListEntry entry = TabListEntry
                            .builder()
                            .tabList(tabList)
                            .profile(subPlayer.getGameProfile())
                            .listOrder(listOrder)
                            .latency(latency)
                            .displayName(displayName)
                            .build();

                        tabList.addEntry(entry);
                    }
                });
            });

            // Remove players who are no longer online
            tabList.getEntries().stream()
                .map(entry -> entry.getProfile().getId())
                .filter(uuid -> !currentPlayerUuids.contains(uuid))
                .toList()
                .forEach(tabList::removeEntry);
        });
    }
}
