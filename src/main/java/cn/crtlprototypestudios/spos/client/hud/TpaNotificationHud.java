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

            ImGui.pushID(i);

            // Style for the container
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12, 8);
            ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 10);
            ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 10);
            ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.2f, 0.2f, 0.2f, 0.9f);

            // Progress bar at the top
            float progress = (notification.expirationTime() - System.currentTimeMillis()) /
                    (float) (Config.tpaRequestExpirationTime * 1000L);
            ImGui.pushStyleColor(ImGuiCol.PlotHistogram, 1f, 1f, 1f, 0.8f);
            ImGui.pushStyleColor(ImGuiCol.FrameBg, 0, 0, 0, 0); // Transparent background
            ImGui.progressBar(progress, -1, 4, "");
            ImGui.popStyleColor(2);

            // Notification text
            String message = notification.isToRequest() ?
                    LocalizationHelper.getString("tpa.request_received", notification.requesterName()) :
                    LocalizationHelper.getString("tpa.request_received.from", notification.requesterName());
            ImGui.text(message);

            // Calculate button sizes and positions
            float windowWidth = 160;
            float buttonWidth = 30; // Fixed button width
            float buttonSpacing = 10;
            float buttonsStartX = windowWidth - (buttonWidth * 2 + buttonSpacing);

            // Button styling
            ImGui.pushStyleColor(ImGuiCol.Button, 0.2f, 0.8f, 0.2f, 0.8f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.3f, 0.9f, 0.3f, 0.9f);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.2f, 0.7f, 0.2f, 1.0f);

            // Position cursor for the accept button
            ImGui.setCursorPosX(buttonsStartX);
            if (ImGui.button(OverseerUtility.hiddenIndexString(
                    LocalizationHelper.getString("text.accept"), i), buttonWidth, 0)) {
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.connection.sendCommand(
                        "tpaccept " + notification.requesterName());
                SposClient.notifications.remove(notification);
                ImGui.popStyleColor(3);
                ImGui.popStyleColor();
                ImGui.popStyleVar(3);
                ImGui.popID();
                continue;
            }
            ImGui.popStyleColor(3);

            // Deny button styling and positioning
            ImGui.pushStyleColor(ImGuiCol.Button, 0.8f, 0.2f, 0.2f, 0.8f);
            ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 0.9f, 0.3f, 0.3f, 0.9f);
            ImGui.pushStyleColor(ImGuiCol.ButtonActive, 0.7f, 0.2f, 0.2f, 1.0f);

            ImGui.sameLine();
            if (ImGui.button(OverseerUtility.hiddenIndexString(
                    LocalizationHelper.getString("text.deny"), i), buttonWidth, 0)) {
                assert Minecraft.getInstance().player != null;
                Minecraft.getInstance().player.connection.sendCommand(
                        "tpadeny " + notification.requesterName());
                SposClient.notifications.remove(notification);
                ImGui.popStyleColor(3);
                ImGui.popStyleColor();
                ImGui.popStyleVar(3);
                ImGui.popID();
                continue;
            }
            ImGui.popStyleColor(3);

            ImGui.popStyleColor(); // WindowBg
            ImGui.popStyleVar(3); // Window styling
            ImGui.popID();

            ImGui.dummy(0, 8); // Space between notifications
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
