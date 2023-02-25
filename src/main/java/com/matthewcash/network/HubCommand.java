package com.matthewcash.network;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class HubCommand {
    public static final String aliases[] = { "hub", "lobby" };

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
            .requires(source -> {
                if (source instanceof Player == false)
                    return false;

                final Player player = (Player) source;

                if (!player.getCurrentServer().isPresent())
                    return false;

                if (player.getCurrentServer().get().getServerInfo().getName()
                    .equals("hub"))
                    return false;

                return true;
            })
            .executes(context -> {
                final Player player = (Player) context.getSource();

                player
                    .createConnectionRequest(
                        ProxyCore.proxy.getServer("hub").get()
                    )
                    .fireAndForget();

                return Command.SINGLE_SUCCESS;
            })
            .build();

        return new BrigadierCommand(commandNode);
    }
}
