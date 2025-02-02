package cn.crtlprototypestudios.spos.command;

import cn.crtlprototypestudios.spos.Config;
import cn.crtlprototypestudios.spos.data.TeleportRequest;
import cn.crtlprototypestudios.spos.handler.TeleportHandler;
import cn.crtlprototypestudios.spos.manager.TeleportManager;
import cn.crtlprototypestudios.spos.manager.tpa.TpaManager;
import cn.crtlprototypestudios.spos.utility.LocalizationHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Optional;

public class TpaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tpa")
                .then(Commands.literal("to")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> tpaTo(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                .then(Commands.literal("from")
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> tpaFrom(context.getSource(), EntityArgument.getPlayer(context, "target")))))
                .then(Commands.literal("accept")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> tpaAccept(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("decline")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> tpaDecline(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("always")
                        .then(Commands.literal("allow")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> tpaAlways(context.getSource(), EntityArgument.getPlayers(context, "players"), true))))
                        .then(Commands.literal("decline")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> tpaAlways(context.getSource(), EntityArgument.getPlayers(context, "players"), false))))));
    }

    private static int tpaTo(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        if (!Config.allowCommandTpa) {
            source.sendFailure(LocalizationHelper.getComponent("command.disabled"));
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        if (player.getUUID().equals(target.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.self"));
            return 0;
        }

        if (TpaManager.getInstance().isAlwaysDeclined(player.getUUID(), target.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.always_declined"));
            return 0;
        }

        // Check if there's already an active request
        Optional<TeleportRequest> existingRequest = TeleportManager.getRequest(target.getUUID());
        if (existingRequest.isPresent() && existingRequest.get().getFrom().equals(player.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.request_pending"));
            return 0;
        }

        if (TpaManager.getInstance().isAlwaysAllowed(player.getUUID(), target.getUUID())) {
            // Auto-accept if player is in always-allow list
            TeleportManager.Location destination = TeleportManager.Location.fromEntity(target);
            TeleportHandler.scheduleTeleport(player, destination);
            target.sendSystemMessage(LocalizationHelper.getComponent("tpa.auto_accepted", player.getName()));
            return 1;
        }

        TeleportManager.createRequest(player.getUUID(), target.getUUID(), true);
        source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.request_sent", target.getName()), false);
        target.sendSystemMessage(LocalizationHelper.getComponent("tpa.request_received", player.getName()));
        return 1;
    }

    private static int tpaFrom(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        if (!Config.allowCommandTpa) {
            source.sendFailure(LocalizationHelper.getComponent("command.disabled"));
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        if (player.getUUID().equals(target.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.self"));
            return 0;
        }

        // Check if there's already an active request
        Optional<TeleportRequest> existingRequest = TeleportManager.getRequest(target.getUUID());
        if (existingRequest.isPresent() && existingRequest.get().getFrom().equals(player.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.request_pending"));
            return 0;
        }

        TeleportManager.createRequest(player.getUUID(), target.getUUID(), false);
        source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.request_sent_from", target.getName()), false);
        target.sendSystemMessage(LocalizationHelper.getComponent("tpa.request_received_from", player.getName()));
        return 1;
    }

    private static int tpaAccept(CommandSourceStack source, ServerPlayer requester) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<TeleportRequest> request = TeleportManager.getRequest(player.getUUID());

        if (request.isEmpty() || !request.get().getFrom().equals(requester.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.no_request", requester.getName()));
            return 0;
        }

        TeleportRequest req = request.get();
        TeleportManager.removeRequest(player.getUUID());

        ServerPlayer fromPlayer = source.getServer().getPlayerList().getPlayer(req.getFrom());
        if (fromPlayer == null) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.player_offline"));
            return 0;
        }

        if (req.isToRequest()) {
            // Requester wants to teleport to accepter
            TeleportHandler.scheduleTeleport(fromPlayer, TeleportManager.Location.fromEntity(player));
        } else {
            // Requester wants accepter to teleport to them
            TeleportHandler.scheduleTeleport(player, TeleportManager.Location.fromEntity(fromPlayer));
        }

        source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.accepted", requester.getName()), false);
        fromPlayer.sendSystemMessage(LocalizationHelper.getComponent("tpa.request_accepted", player.getName()));
        return 1;
    }

    private static int tpaDecline(CommandSourceStack source, ServerPlayer requester) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<TeleportRequest> request = TeleportManager.getRequest(player.getUUID());

        if (request.isEmpty() || !request.get().getFrom().equals(requester.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.no_request", requester.getName()));
            return 0;
        }

        TeleportManager.removeRequest(player.getUUID());

        ServerPlayer fromPlayer = source.getServer().getPlayerList().getPlayer(request.get().getFrom());
        if (fromPlayer != null) {
            fromPlayer.sendSystemMessage(LocalizationHelper.getComponent("tpa.request_declined", player.getName()));
        }

        source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.declined", requester.getName()), false);
        return 1;
    }

    private static int tpaAlways(CommandSourceStack source, Collection<ServerPlayer> targets, boolean allow) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int count = 0;

        for (ServerPlayer target : targets) {
            if (target.getUUID().equals(player.getUUID())) continue;

            if (allow) {
                TpaManager.getInstance().setAlwaysAllow(player.getUUID(), target.getUUID());
            } else {
                TpaManager.getInstance().setAlwaysDecline(player.getUUID(), target.getUUID());
            }
            count++;
        }

        if (count > 0) {
            int finalCount = count;
            source.sendSuccess(() -> LocalizationHelper.getComponent(
                    allow ? "tpa.always_allow_set" : "tpa.always_decline_set",
                    finalCount
            ), false);
        }

        return count;
    }
}

