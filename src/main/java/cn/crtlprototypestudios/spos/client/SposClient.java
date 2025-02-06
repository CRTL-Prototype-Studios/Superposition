package cn.crtlprototypestudios.spos.client;

import cn.crtlprototypestudios.ovsr.client.api.OverseerHUD;
import cn.crtlprototypestudios.spos.client.data.TpaNotification;
import cn.crtlprototypestudios.spos.client.hud.TpaNotificationHud;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@OnlyIn(Dist.CLIENT)
public class SposClient {
    public static final TpaNotificationHud TPA_NOTIF_HUD = new TpaNotificationHud();
    public static final List<TpaNotification> notifications = new CopyOnWriteArrayList<>();

    public static void init(final FMLClientSetupEvent event){
        OverseerHUD.addElement(TPA_NOTIF_HUD);
    }
}
