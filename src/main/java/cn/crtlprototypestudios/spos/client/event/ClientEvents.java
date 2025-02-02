package cn.crtlprototypestudios.spos.client.event;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.client.gui.TpaNotificationOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Spos.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        TpaNotificationOverlay.render(event.getGuiGraphics(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (TpaNotificationOverlay.handleClick(event.getMouseX(), event.getMouseY())) {
            event.setCanceled(true);
        }
    }
}
