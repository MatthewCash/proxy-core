package com.matthewcash.network;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

public class SendCommand {
    public static final String aliases[] = { "send", "sendtoserver" };

    public static CommandMeta createCommandMeta() {
        return ProxyCore.proxy.getCommandManager().metaBuilder(aliases[0])
            .aliases(aliases)
            .build();
    }

    public static BrigadierCommand createBrigadierCommand() {
        LiteralCommandNode<CommandSource> commandNode = LiteralArgumentBuilder
            .<CommandSource>literal(
                aliases[0]
            )

            .requires(source -> source.hasPermission("mcash.proxy.send"))
            .executes(context -> {
                context.getSource().sendMessage(
                    MiniMessage.miniMessage()
                        .deserialize(
                            "<bold><dark_red>ERROR</dark_red></bold> <red>Missing required arguments!</red>"
                        )
                );

                return Command.SINGLE_SUCCESS;
            })
            .then(
                RequiredArgumentBuilder
                    .<CommandSource, String>argument(
                        "player", StringArgumentType.string()
                    )
                    .suggests(
                        SendCommand::suggestPlayer
                    )
                    .then(
                        RequiredArgumentBuilder
                            .<CommandSource, String>argument(
                                "server",
                                StringArgumentType
                                    .string()
                            )
                            .suggests(
                                SendCommand::suggestServer
                            )
                            .executes(SendCommand::execute)
                    )
            )

            .build();

        return new BrigadierCommand(commandNode);
    }

    static CompletableFuture<Suggestions> suggestPlayer(
        CommandContext<CommandSource> context, SuggestionsBuilder builder
    ) {
        ProxyCore.proxy.getAllPlayers()
            .forEach(
                player -> builder
                    .suggest(
                        player.getUsername()
                            .toLowerCase(),
                        VelocityBrigadierMessage
                            .tooltip(
                                MessageFormat
                                    .getDisplayName(
                                        player
                                    )
                            )
                    )
            );

        builder.suggest("all");
        return builder.buildFuture();
    }

    static CompletableFuture<Suggestions> suggestServer(
        CommandContext<CommandSource> context, SuggestionsBuilder builder
    ) {
        ProxyCore.proxy.getAllServers()
            .forEach(
                server -> builder
                    .suggest(
                        server.getServerInfo()
                            .getName(),
                        VelocityBrigadierMessage
                            .tooltip(
                                MiniMessage
                                    .miniMessage()
                                    .deserialize(
                                        ServerAliases
                                            .getAlias(
                                                server
                                            )
                                    )
                            )
                    )
            );

        return builder.buildFuture();
    }

    static int execute(CommandContext<CommandSource> context) {
        String playerString = context
            .getArgument(
                "player",
                String.class
            );
        String serverString = context
            .getArgument(
                "server",
                String.class
            );

        Boolean sendAllPlayers = playerString
            .equals("all");
        Player player = ProxyCore.proxy
            .getPlayer(playerString)
            .orElse(null);

        if (
            !sendAllPlayers
                && player == null
        ) {
            context.getSource()
                .sendMessage(
                    MiniMessage.miniMessage()
                        .deserialize(
                            "<bold><dark_red>ERROR</dark_red></bold> <red>Player not found!</red>"
                        )
                );
            return Command.SINGLE_SUCCESS;
        }

        RegisteredServer server = ProxyCore.proxy
            .getServer(serverString)
            .orElse(null);

        if (server == null) {
            context.getSource()
                .sendMessage(
                    MiniMessage.miniMessage()
                        .deserialize(
                            "<bold><dark_red>ERROR</dark_red></bold> <red>Server not found!</red>"
                        )
                );
            return Command.SINGLE_SUCCESS;
        }

        Collection<Player> playersToSend = sendAllPlayers
            ? ProxyCore.proxy
                .getAllPlayers()
            : Arrays.asList(player);

        playersToSend.forEach(
            sendPlayer -> sendPlayer
                .createConnectionRequest(
                    server
                )
                .fireAndForget()
        );

        String playerName = player == null
            ? "All Players"
            : player.getUsername();
        context.getSource()
            .sendMessage(
                MiniMessage.miniMessage()
                    .deserialize(
                        "<bold><aqua>Sent</aqua> <white><player></white> <aqua>to</aqua> <white><server></white><aqua>!</aqua></bold>",
                        Placeholder
                            .unparsed(
                                "player",
                                playerName
                            ),
                        Placeholder.unparsed(
                            "server",
                            ServerAliases.getAlias(
                                server
                            )
                        )
                    )
            );

        return Command.SINGLE_SUCCESS;
    }
}
