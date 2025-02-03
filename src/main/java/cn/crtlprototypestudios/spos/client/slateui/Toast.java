package cn.crtlprototypestudios.spos.client.slateui;

import cn.crtlprototypestudios.spos.Spos;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class Toast extends UIContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Spos.MODID, "textures/gui/tpa_notifications.png");
    private final Component message;
    private final long expirationTime;

    public Toast(int x, int y, Component message, long durationMs) {
        super(x, y, 200, 30);
        this.message = message;
        this.expirationTime = System.currentTimeMillis() + durationMs;

        // Add slide-in and fade-in animations
        addAnimation(new SlideAnimation(this, 0.3f, true)); // true for slide in
        addAnimation(new FadeAnimation(this, 0.3f, 1.0f));
    }

    public void startExitAnimation(Runnable onComplete) {
        // Slide out to the right and fade out
        addAnimation(new SlideAnimation(this, 0.3f, false) { // false for slide out
            @Override
            protected void animate(float progress) {
                super.animate(progress);
                if (progress >= 1.0f) {
                    onComplete.run();
                }
            }
        });
        addAnimation(new FadeAnimation(this, 0.3f, 0.0f));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        // Render background with alpha
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(TEXTURE, x, y, 0, 0, width, height);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // Reset color

        // Render progress bar
        float progress = (expirationTime - System.currentTimeMillis()) / 30000.0F;
        int progressWidth = (int)(width * Math.max(0, Math.min(1, progress)));
        graphics.fill(x, y, x + progressWidth, y + 2,
                ((int)(alpha * 255) << 24) | 0xFFFFFF);

        // Render message
        graphics.drawString(Minecraft.getInstance().font, message,
                x + 10, y + 10,
                ((int)(alpha * 255) << 24) | 0xFFFFFF);

        // Render children (buttons)
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    public void addButton(ToastButton button) {
        super.addChild(button);
        // Store initial button position relative to toast
        button.setInitialX(button.x - this.x);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }
}

