package cn.crtlprototypestudios.spos.command;

import cn.crtlprototypestudios.spos.Config;
import cn.crtlprototypestudios.spos.data.TeleportRequest;
import cn.crtlprototypestudios.spos.manager.TeleportManager;
import cn.crtlprototypestudios.spos.manager.tpa.TpaManager;
import cn.crtlprototypestudios.spos.utility.LocalizationHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TpaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Base /tpa command
        dispatcher.register(Commands.literal("tpa")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> tpaTo(context.getSource(), EntityArgument.getPlayer(context, "target")))));

        // /tpahere command
        dispatcher.register(Commands.literal("tpahere")
                .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> tpaHere(context.getSource(), EntityArgument.getPlayers(context, "players")))));

        // /tpaccept command
        dispatcher.register(Commands.literal("tpaccept")
                .executes(context -> acceptLatest(context.getSource()))
                .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> acceptMultiple(context.getSource(), EntityArgument.getPlayers(context, "players")))));

        // /tpadeny command
        dispatcher.register(Commands.literal("tpadeny")
                .executes(context -> denyLatest(context.getSource()))
                .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> denyMultiple(context.getSource(), EntityArgument.getPlayers(context, "players")))));

        // /tpasetting command
        dispatcher.register(Commands.literal("tpasetting")
                .then(Commands.literal("alwaysAllow")
                        .then(Commands.literal("list")
                                .executes(context -> listSettings(context.getSource(), true)))
                        .then(Commands.literal("add")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> modifySettings(context.getSource(), EntityArgument.getPlayers(context, "players"), true, SettingAction.ADD))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> modifySettings(context.getSource(), EntityArgument.getPlayers(context, "players"), true, SettingAction.REMOVE))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> modifySettings(context.getSource(), EntityArgument.getPlayers(context, "players"), true, SettingAction.SET)))))
                .then(Commands.literal("alwaysDeny")
                        .then(Commands.literal("list")
                                .executes(context -> listSettings(context.getSource(), false)))
                        .then(Commands.literal("add")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> modifySettings(context.getSource(), EntityArgument.getPlayers(context, "players"), false, SettingAction.ADD))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> modifySettings(context.getSource(), EntityArgument.getPlayers(context, "players"), false, SettingAction.REMOVE))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("players", EntityArgument.players())
                                        .executes(context -> modifySettings(context.getSource(), EntityArgument.getPlayers(context, "players"), false, SettingAction.SET))))));
    }

    private enum SettingAction {
        ADD, REMOVE, SET
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

        if (TpaManager.getInstance().isAlwaysDenied(player.getUUID(), target.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.always_denied"));
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
            TeleportManager.teleport(player, destination);
            target.sendSystemMessage(LocalizationHelper.getComponent("tpa.auto_accepted", player.getName()));
            return 1;
        }

        TeleportManager.createRequest(player.getUUID(), target.getUUID(), true);
        source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.request_sent", target.getName()), false);
        target.sendSystemMessage(LocalizationHelper.getComponent("tpa.request_received", player.getName()));
        return 1;
    }

    private static int tpaHere(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        if (!Config.allowCommandTpa) {
            source.sendFailure(LocalizationHelper.getComponent("command.disabled"));
            return 0;
        }

        ServerPlayer player = source.getPlayerOrException();
        int successCount = 0;

        for (ServerPlayer target : targets) {
            if (target.getUUID().equals(player.getUUID())) continue;

            if (TpaManager.getInstance().isAlwaysDenied(player.getUUID(), target.getUUID())) {
                source.sendFailure(LocalizationHelper.getComponent("tpa.always_denied.single", target.getName()));
                continue;
            }

            Optional<TeleportRequest> existingRequest = TeleportManager.getRequest(target.getUUID());
            if (existingRequest.isPresent() && existingRequest.get().getFrom().equals(player.getUUID())) {
                source.sendFailure(LocalizationHelper.getComponent("tpa.request_pending.single", target.getName()));
                continue;
            }

            if (TpaManager.getInstance().isAlwaysAllowed(player.getUUID(), target.getUUID())) {
                TeleportManager.teleport(target, TeleportManager.Location.fromEntity(player));
                target.sendSystemMessage(LocalizationHelper.getComponent("tpa.auto_accepted.from", player.getName()));
                successCount++;
                continue;
            }

            TeleportManager.createRequest(player.getUUID(), target.getUUID(), false);
            target.sendSystemMessage(LocalizationHelper.getComponent("tpa.request_received.from", player.getName()));
            successCount++;
        }

        if (successCount > 0) {
            int finalSuccessCount = successCount;
            source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.request_sent.multiple", finalSuccessCount), false);
        }

        return successCount;
    }

    private static int acceptLatest(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<UUID> latestRequest = TpaManager.getInstance().getLatestRequest(player.getUUID());

        if (latestRequest.isEmpty()) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.no_request"));
            return 0;
        }

        ServerPlayer requester = source.getServer().getPlayerList().getPlayer(latestRequest.get());
        if (requester == null) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.player_offline"));
            return 0;
        }

        return acceptRequest(source, player, requester);
    }

    private static int acceptMultiple(CommandSourceStack source, Collection<ServerPlayer> requesters) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int successCount = 0;

        for (ServerPlayer requester : requesters) {
            successCount += acceptRequest(source, player, requester);
        }

        return successCount;
    }

    private static int acceptRequest(CommandSourceStack source, ServerPlayer player, ServerPlayer requester) {
        Optional<TeleportRequest> request = TeleportManager.getRequest(player.getUUID());

        if (request.isEmpty() || !request.get().getFrom().equals(requester.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.no_request.single", requester.getName()));
            return 0;
        }

        TeleportRequest req = request.get();
        TeleportManager.removeRequest(player.getUUID());

        if (req.isToRequest()) {
            TeleportManager.teleport(requester, TeleportManager.Location.fromEntity(player));
        } else {
            TeleportManager.teleport(player, TeleportManager.Location.fromEntity(requester));
        }

        source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.accepted", requester.getName()), false);
        requester.sendSystemMessage(LocalizationHelper.getComponent("tpa.request_accepted", player.getName()));
        return 1;
    }

    private static int denyLatest(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<UUID> latestRequest = TpaManager.getInstance().getLatestRequest(player.getUUID());

        if (latestRequest.isEmpty()) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.no_request"));
            return 0;
        }

        ServerPlayer requester = source.getServer().getPlayerList().getPlayer(latestRequest.get());
        if (requester == null) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.player_offline"));
            return 0;
        }

        return denyRequest(source, player, requester);
    }

    private static int denyMultiple(CommandSourceStack source, Collection<ServerPlayer> requesters) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int successCount = 0;

        for (ServerPlayer requester : requesters) {
            successCount += denyRequest(source, player, requester);
        }

        return successCount;
    }

    private static int denyRequest(CommandSourceStack source, ServerPlayer player, ServerPlayer requester) {
        Optional<TeleportRequest> request = TeleportManager.getRequest(player.getUUID());

        if (request.isEmpty() || !request.get().getFrom().equals(requester.getUUID())) {
            source.sendFailure(LocalizationHelper.getComponent("tpa.no_request.single", requester.getName()));
            return 0;
        }

        TeleportManager.removeRequest(player.getUUID());
        source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.denied", requester.getName()), false);
        requester.sendSystemMessage(LocalizationHelper.getComponent("tpa.request_denied", player.getName()));
        return 1;
    }

    private static int listSettings(CommandSourceStack source, boolean isAllow) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Set<UUID> players = isAllow
                ? TpaManager.getInstance().getAlwaysAllowPlayers(player.getUUID())
                : TpaManager.getInstance().getAlwaysDenyPlayers(player.getUUID());

        if (players.isEmpty()) {
            source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.settings.list.empty",
                    isAllow ? "allow" : "deny"), false);
            return 0;
        }

        StringBuilder playersString = new StringBuilder();
        int count = 0;
        for (
                UUID uuid : players) {
            if (count++ > 0) playersString.append(", ");
            String name = Optional.ofNullable(source.getServer().getPlayerList().getPlayer(uuid))
                    .map(ServerPlayer::getName)
                    .map(Component::getString)
                    .orElse(uuid.toString());
            playersString.append(name);
        }
        Component playerList = Component.literal(playersString.toString());

        source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.settings.list",
                isAllow ? "allow" : "deny", playerList), false);
        return players.size();
    }

    private static int modifySettings(CommandSourceStack source, Collection<ServerPlayer> targets,
                                      boolean isAllow, SettingAction action) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        TpaManager manager = TpaManager.getInstance();

        Set<UUID> targetUUIDs = targets.stream()
                .map(ServerPlayer::getUUID)
                .collect(Collectors.toSet());

        switch (action) {
            case ADD -> {
                if (isAllow) {
                    targetUUIDs.forEach(uuid -> manager.addAlwaysAllowPlayer(player.getUUID(), uuid));
                } else {
                    targetUUIDs.forEach(uuid -> manager.addAlwaysDenyPlayer(player.getUUID(), uuid));
                }
            }
            case REMOVE -> {
                if (isAllow) {
                    targetUUIDs.forEach(uuid -> manager.removeAlwaysAllowPlayer(player.getUUID(), uuid));
                } else {
                    targetUUIDs.forEach(uuid -> manager.removeAlwaysDenyPlayer(player.getUUID(), uuid));
                }
            }
            case SET -> {
                if (isAllow) {
                    manager.setAlwaysAllowPlayers(player.getUUID(), targetUUIDs);
                } else {
                    manager.setAlwaysDenyPlayers(player.getUUID(), targetUUIDs);
                }
            }
        }

        source.sendSuccess(() -> LocalizationHelper.getComponent("tpa.settings.modified",
                action.name().toLowerCase(), isAllow ? "allow" : "deny", targets.size()), false);
        return targets.size();
    }
}
