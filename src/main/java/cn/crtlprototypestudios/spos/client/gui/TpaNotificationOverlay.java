package cn.crtlprototypestudios.spos.client.gui;

import cn.crtlprototypestudios.spos.Spos;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class TpaNotificationOverlay {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Spos.MODID, "textures/gui/tpa_notifications.png");
    private static final List<TpaToast> activeToasts = new ArrayList<>();
    private static final int TOAST_WIDTH = 200;
    private static final int TOAST_HEIGHT = 40;

    public static void render(GuiGraphics guiGraphics, float partialTick) {
        if (activeToasts.isEmpty()) return;

        int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - TOAST_WIDTH - 10;
        int y = 10;

        Iterator<TpaToast> iterator = activeToasts.iterator();
        while (iterator.hasNext()) {
            TpaToast toast = iterator.next();
            if (toast.isExpired()) {
                iterator.remove();
                continue;
            }

            toast.render(guiGraphics, x, y);
            y += TOAST_HEIGHT + 5;
        }
    }

    public static void addRequest(UUID requestId, String requesterName, boolean isToRequest, long expirationTime) {
        activeToasts.add(new TpaToast(requestId, requesterName, isToRequest, expirationTime));
    }

    public static void removeRequest(UUID requestId) {
        activeToasts.removeIf(toast -> toast.getRequestId().equals(requestId));
    }

    public static boolean handleClick(double mouseX, double mouseY) {
        int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - TOAST_WIDTH - 10;
        int y = 10;

        for (TpaToast toast : activeToasts) {
            if (toast.onClick(mouseX, mouseY, x, y)) {
                return true;
            }
            y += TOAST_HEIGHT + 5;
        }
        return false;
    }

    private static class TpaToast {
        private final UUID requestId;
        private final String requesterName;
        private final boolean isToRequest;
        private final long expirationTime;

        public TpaToast(UUID requestId, String requesterName, boolean isToRequest, long expirationTime) {
            this.requestId = requestId;
            this.requesterName = requesterName;
            this.isToRequest = isToRequest;
            this.expirationTime = expirationTime;
        }

        public void render(GuiGraphics guiGraphics, int x, int y) {
            // Background
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, TEXTURE);
            guiGraphics.blit(TEXTURE, x, y, 0, 0, TOAST_WIDTH, TOAST_HEIGHT);

            // Progress bar
            float progress = ((expirationTime - System.currentTimeMillis()) / 30000.0F);
            int progressWidth = (int) (TOAST_WIDTH * progress);
            guiGraphics.fill(x, y, x + progressWidth, y + 2, 0xFFFFFFFF);

            // Message
            String message = isToRequest ?
                    requesterName + " wants to teleport to you" :
                    requesterName + " wants you to teleport to them";
            guiGraphics.drawString(Minecraft.getInstance().font, message, x + 10, y + 10, 0xFFFFFFFF);

            // Buttons
            guiGraphics.fill(x, y + TOAST_HEIGHT - 15, x + TOAST_WIDTH/2 - 1, y + TOAST_HEIGHT - 5, 0xFFFFFFFF);
            guiGraphics.fill(x + TOAST_WIDTH/2 + 1, y + TOAST_HEIGHT - 15, x + TOAST_WIDTH, y + TOAST_HEIGHT - 5, 0x80000000);

            guiGraphics.drawCenteredString(Minecraft.getInstance().font, "Accept", x + TOAST_WIDTH/4, y + TOAST_HEIGHT - 12, 0x00000000);
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, "Deny", x + TOAST_WIDTH*3/4, y + TOAST_HEIGHT - 12, 0xFFFFFFFF);
        }

        public boolean onClick(double mouseX, double mouseY, int x, int y) {
            if (mouseY >= y + TOAST_HEIGHT - 20 && mouseY < y + TOAST_HEIGHT) {
                if (mouseX >= x && mouseX < x + TOAST_WIDTH/2) {
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.connection.sendCommand("tpaccept " + requesterName);
                    return true;
                } else if (mouseX >= x + TOAST_WIDTH/2 && mouseX < x + TOAST_WIDTH) {
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.connection.sendCommand("tpadeny " + requesterName);
                    return true;
                }
            }
            return false;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expirationTime;
        }

        public UUID getRequestId() {
            return requestId;
        }
    }
}
