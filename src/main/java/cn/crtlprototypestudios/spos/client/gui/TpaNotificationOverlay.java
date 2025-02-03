package cn.crtlprototypestudios.spos.client.gui;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.client.slateui.*;
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
    private static final UIContainer container = new UIContainer(0, 0, 0, 0);
    private static final int TOAST_SPACING = 45;

    public static void render(GuiGraphics graphics, float partialTick) {
        container.render(graphics, 0, 0, partialTick);
    }

    public static boolean handleClick(double mouseX, double mouseY) {
        for (UIComponent child : container.getChildren()) {
            if (child instanceof Toast toast) {
                for (UIComponent button : toast.getChildren()) {
                    if (button instanceof ToastButton toastButton) {
                        if (toastButton.isHovered(mouseX, mouseY)) {
                            return toastButton.mouseClicked(mouseX, mouseY, 0);
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void addRequest(UUID requestId, String requesterName, boolean isToRequest, long expirationTime) {
        int x = Minecraft.getInstance().getWindow().getGuiScaledWidth() - 210;
        int y = 10 + container.getChildren().size() * TOAST_SPACING;

        Component message = Component.literal(requesterName + (isToRequest ?
                " wants to teleport to you" :
                " wants you to teleport to them"));

        Toast toast = new Toast(x, y, message, 30000);

        // Accept button
        ToastButton acceptButton = new ToastButton(
                x, y + 30, 100, 20,
                Component.literal("Accept"),
                button -> {
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.connection.sendCommand("tpaccept " + requesterName);
                    toast.startExitAnimation(() -> container.removeChild(toast));
                }
        );

        // Deny button
        ToastButton denyButton = new ToastButton(
                x + 100, y + 30, 100, 20,
                Component.literal("Deny"),
                button -> {
                    assert Minecraft.getInstance().player != null;
                    Minecraft.getInstance().player.connection.sendCommand("tpadeny " + requesterName);
                    toast.startExitAnimation(() -> container.removeChild(toast));
                }
        );

        toast.addButton(acceptButton);
        toast.addButton(denyButton);
        container.addChild(toast);
    }



    public static void removeRequest(UUID requestId) {
        container.getChildren().stream()
                .filter(child -> child instanceof Toast)
                .findFirst()
                .ifPresent(toast -> ((Toast) toast).startExitAnimation(() -> {
                    container.removeChild(toast);
                    repositionRemainingToasts();
                }));
    }

    public static void tick() {
        container.tick();

        // Create a list of toasts to remove
        List<UIComponent> toastsToRemove = new ArrayList<>();

        // Check for expired toasts
        for (UIComponent child : container.getChildren()) {
            if (child instanceof Toast toast && toast.isExpired()) {
                toast.startExitAnimation(() -> {
                    container.removeChild(toast);
                    repositionRemainingToasts();
                });
                toastsToRemove.add(child);
            }
        }

        // Remove the expired toasts
        toastsToRemove.forEach(container::removeChild);

        if (!toastsToRemove.isEmpty()) {
            repositionRemainingToasts();
        }
    }

    private static void repositionRemainingToasts() {
        int y = 10;
        for (UIComponent child : container.getChildren()) {
            if (child instanceof Toast toast) {
                final int targetY = y;
                child.addAnimation(new UIAnimation(child, 0.3f) {
                    private final int startY = child.getY();
                    @Override
                    protected void animate(float progress) {
                        child.setY((int) (startY + (targetY - startY) * easeOutCubic(progress)));
                    }
                    private float easeOutCubic(float x) {
                        return 1 - (float)Math.pow(1 - x, 3);
                    }
                });
                y += TOAST_SPACING;
            }
        }
    }
}

