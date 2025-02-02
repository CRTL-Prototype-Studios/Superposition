package cn.crtlprototypestudios.spos.command;

import cn.crtlprototypestudios.spos.Config;
import cn.crtlprototypestudios.spos.handler.TeleportHandler;
import cn.crtlprototypestudios.spos.manager.TeleportManager;
import cn.crtlprototypestudios.spos.utility.LocalizationHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawn")
                .executes(SpawnCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!Config.allowCommandSpawn) {
            context.getSource().sendFailure(LocalizationHelper.getComponent("command.disabled"));
            return 0;
        }

        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = player.getServer().getLevel(Level.OVERWORLD);
        if (level == null) {
            context.getSource().sendFailure(LocalizationHelper.getComponent("spawn.error"));
            return 0;
        }

        BlockPos spawnPos = level.getSharedSpawnPos();
        TeleportManager.Location spawnLocation = new TeleportManager.Location(
                Level.OVERWORLD,
                spawnPos.getX() + 0.5,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5,
                0,
                0
        );

        TeleportManager.teleport(player, spawnLocation);
        return 1;
    }
}

