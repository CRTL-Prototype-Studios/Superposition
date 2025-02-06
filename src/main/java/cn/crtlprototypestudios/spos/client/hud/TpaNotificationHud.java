package cn.crtlprototypestudios.spos.client.hud;

import cn.crtlprototypestudios.ovsr.client.api.OverseerHUD;
import cn.crtlprototypestudios.ovsr.client.api.OverseerUtility;
import cn.crtlprototypestudios.ovsr.client.impl.interfaces.Theme;
import cn.crtlprototypestudios.ovsr.client.impl.theme.ImGuiDarkTheme;
import cn.crtlprototypestudios.spos.Config;
import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.client.SposClient;
import cn.crtlprototypestudios.spos.client.data.TpaNotification;
import cn.crtlprototypestudios.spos.utility.LocalizationHelper;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class TpaNotificationHud extends OverseerHUD.HUDElement {

    public TpaNotificationHud() {
        super("spos.tpa_notifications_hud", new ImGuiDarkTheme());
        removeFlags(ImGuiWindowFlags.NoInputs);
        addFlags(ImGuiWindowFlags.NoMove);
        setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.TOP)
                .setOffset(-10, 10);
    }

    @Override
    protected boolean shouldRender(Minecraft mc) {
        // Call parent check first
        if (!super.shouldRender(mc)) return false;

        // Add custom conditions
        return !SposClient.notifications.isEmpty();
    }

    @Override
    protected void renderContent() {
        for (int i = 0; i < SposClient.notifications.size(); i++) {
            TpaNotification notification = SposClient.notifications.get(i);

            if (notification.isExpired()) {
                SposClient.notifications.remove(notification);
                continue;
            }

            ImGui.pushID(i); // Push unique ID for this notification group
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 10);

            ImGui.text(notification.isToRequest() ?
                    LocalizationHelper.getString("tpa.request_received", notification.requesterName()) :
                    LocalizationHelper.getString("tpa.request_received.from", notification.requesterName()));

            if (ImGui.button(OverseerUtility.hiddenIndexString(LocalizationHelper.getString("text.accept"), i))) { // Add index to button label
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.connection.sendCommand("tpaccept " + notification.requesterName());
                SposClient.notifications.remove(notification);
                ImGui.popStyleVar();
                ImGui.popID();
                continue;
            }
            ImGui.sameLine();
            if (ImGui.button(OverseerUtility.hiddenIndexString(LocalizationHelper.getString("text.deny"), i))) { // Add index to button label
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.connection.sendCommand("tpadeny " + notification.requesterName());
                SposClient.notifications.remove(notification);
                ImGui.popStyleVar();
                ImGui.popID();
                continue;
            }

            ImGui.pushStyleColor(ImGuiCol.PlotHistogram, 1f, 1f, 1f, 0.8f);
            ImGui.progressBar((notification.expirationTime() - System.currentTimeMillis()) / (float) (Config.tpaRequestExpirationTime * 1000L), ImGui.getContentRegionMaxX(), 4, "");
            ImGui.popStyleVar();

            ImGui.popStyleVar();
            ImGui.popID();
            ImGui.separator();
        }
    }

    public void addNotification(TpaNotification notification) {
        SposClient.notifications.add(notification);
        Spos.LOGGER.debug("request from {}", notification.requesterName());
        Spos.LOGGER.info("request from {}", notification.requesterName());
        Spos.LOGGER.debug("requests {}", SposClient.notifications);
        Spos.LOGGER.info("requests {}", SposClient.notifications);
    }

    public void removeNotification(UUID requestId) {
        SposClient.notifications.removeIf(i -> i.requestId().equals(requestId));
        Spos.LOGGER.debug("requests {}, after removing {}", SposClient.notifications, requestId);
        Spos.LOGGER.info("requests {}, after removing {}", SposClient.notifications, requestId);
    }
}
