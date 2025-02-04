package cn.crtlprototypestudios.spos.command;

import cn.crtlprototypestudios.spos.data.waypoint.Waypoint;
import cn.crtlprototypestudios.spos.manager.TeleportManager;
import cn.crtlprototypestudios.spos.manager.waypoint.WaypointManager;
import cn.crtlprototypestudios.spos.utility.LocalizationHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class WaypointCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("waypoint")
                .then(Commands.literal("add")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> addWaypoint(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> removeWaypoint(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("goto")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> gotoWaypoint(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("override")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(context -> overrideWaypoint(context.getSource(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("list")
                        .executes(WaypointCommand::listWaypoints)));
    }

    private static int addWaypoint(CommandSourceStack source, String name) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        if (WaypointManager.getInstance().addWaypoint(player, name)) {
            source.sendSuccess(() -> LocalizationHelper.getComponent("waypoint.added", name), false);
            return 1;
        } else {
            source.sendFailure(LocalizationHelper.getComponent("waypoint.exists", name));
            return 0;
        }
    }

    private static int removeWaypoint(CommandSourceStack source, String name) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        if (WaypointManager.getInstance().removeWaypoint(player.getUUID(), name)) {
            source.sendSuccess(() -> LocalizationHelper.getComponent("waypoint.removed", name), false);
            return 1;
        } else {
            source.sendFailure(LocalizationHelper.getComponent("waypoint.not_found", name));
            return 0;
        }
    }

    private static int gotoWaypoint(CommandSourceStack source, String name) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Optional<Waypoint> waypoint = WaypointManager.getInstance().getWaypoint(player.getUUID(), name);

        if (waypoint.isEmpty()) {
            source.sendFailure(LocalizationHelper.getComponent("waypoint.not_found", name));
            return 0;
        }

        Waypoint wp = waypoint.get();
        ResourceKey<Level> dimension = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("dimension")),
                new ResourceLocation(wp.getWorld()));

        TeleportManager.Location location = new TeleportManager.Location(
                dimension, wp.getX(), wp.getY(), wp.getZ(), 0, 0
        );

        TeleportManager.teleport(player, location);
        return 1;
    }

    private static int overrideWaypoint(CommandSourceStack source, String name) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        if (WaypointManager.getInstance().overrideWaypoint(player, name)) {
            source.sendSuccess(() -> LocalizationHelper.getComponent("waypoint.overridden", name), false);
            return 1;
        } else {
            source.sendFailure(LocalizationHelper.getComponent("waypoint.not_found", name));
            return 0;
        }
    }

    private static int listWaypoints(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        List<Waypoint> waypoints = WaypointManager.getInstance().getAllWaypoints(player.getUUID());

        if (waypoints.isEmpty()) {
            context.getSource().sendSuccess(() -> LocalizationHelper.getComponent("waypoint.list.empty"), false);
            return 0;
        }

        context.getSource().sendSuccess(() -> LocalizationHelper.getComponent("waypoint.list.header"), false);
        for (Waypoint wp : waypoints) {
            context.getSource().sendSuccess(() -> LocalizationHelper.getComponent("waypoint.list.entry",
                    wp.getName(), wp.getWorld(),
                    String.format("%.2f", wp.getX()),
                    String.format("%.2f", wp.getY()),
                    String.format("%.2f", wp.getZ())), false);
        }
        return waypoints.size();
    }
}
