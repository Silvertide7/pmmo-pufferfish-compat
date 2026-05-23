package net.silvertide.pmmo_pufferfish_compat.bridge;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public final class BridgeCommand {
    private static final int OP_LEVEL = 2;

    private BridgeCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("pmmopufferfish")
                .requires(source -> source.hasPermission(OP_LEVEL))
                .then(Commands.literal("resync")
                        .executes(BridgeCommand::resyncSelf)
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(BridgeCommand::resyncSelected)));
    }

    private static int resyncSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Reconciler.reconcileAll(player);
        ctx.getSource().sendSuccess(
                () -> Component.literal("Resynced " + player.getGameProfile().getName()),
                true);
        return 1;
    }

    private static int resyncSelected(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "players");
        for (ServerPlayer player : players) {
            Reconciler.reconcileAll(player);
        }
        int count = players.size();
        ctx.getSource().sendSuccess(
                () -> Component.literal("Resynced " + count + " player(s)"),
                true);
        return count;
    }
}
