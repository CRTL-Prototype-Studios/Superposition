package cn.crtlprototypestudios.spos.handler;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.manager.TeleportManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Spos.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            TeleportHandler.checkPlayerMovement((ServerPlayer) event.player);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            TeleportManager.saveDeathLocation(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        // Cancel any pending teleports when changing dimensions
        if (event.getEntity() instanceof ServerPlayer player) {
            TeleportHandler.cancelPendingTeleport(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clean up when player logs out
        if (event.getEntity() instanceof ServerPlayer player) {
            TeleportHandler.cancelPendingTeleport(player);
        }
    }
}

