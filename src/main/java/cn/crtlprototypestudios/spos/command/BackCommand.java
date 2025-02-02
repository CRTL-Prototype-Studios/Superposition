package cn.crtlprototypestudios.spos.command;

import cn.crtlprototypestudios.spos.Config;
import cn.crtlprototypestudios.spos.manager.TeleportManager;
import cn.crtlprototypestudios.spos.utility.LocalizationHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class BackCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("back")
                .executes(BackCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!Config.allowCommandBack) {
            context.getSource().sendFailure(LocalizationHelper.getComponent("command.disabled"));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayerOrException();
        Optional<TeleportManager.Location> lastLoc = TeleportManager.getLastLocation(player.getUUID());
        Optional<TeleportManager.Location> deathLoc = TeleportManager.getDeathLocation(player.getUUID());

        if(deathLoc.isEmpty()) {
            if (lastLoc.isEmpty()) {
                context.getSource().sendFailure(LocalizationHelper.getComponent("back.no_location"));
                return 0;
            }
            TeleportManager.teleport(player, lastLoc.get(), false);
            TeleportManager.clearLastLocation(player.getUUID());
        } else {
            if (!Config.allowDeathBack) {
                context.getSource().sendFailure(LocalizationHelper.getComponent("back.no_location"));
                return 0;
            }
            TeleportManager.teleport(player, deathLoc.get(), false);
            TeleportManager.clearDeathLocation(player.getUUID());
        }

//        context.getSource().sendSuccess(() -> LocalizationHelper.getComponent("back.success"), false);
        return 1;
    }
}

