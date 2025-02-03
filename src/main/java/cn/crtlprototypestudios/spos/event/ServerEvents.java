package cn.crtlprototypestudios.spos.event;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.manager.TeleportManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = Spos.MODID)
public class ServerEvents {
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) {
                TeleportManager.tick(server);
            }
        }
    }
}

