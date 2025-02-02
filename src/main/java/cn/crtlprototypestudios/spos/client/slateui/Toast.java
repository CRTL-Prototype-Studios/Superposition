package cn.crtlprototypestudios.spos.client.slateui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class Toast extends UIContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation("your_mod", "textures/gui/toast.png");
    private final Component message;
    private final long expirationTime;
    private List<ToastButton> buttons = new ArrayList<>();

    public Toast(int x, int y, Component message, long durationMs) {
        super(x, y, 200, 40);
        this.message = message;
        this.expirationTime = System.currentTimeMillis() + durationMs;

        // Add fade-in animation
        addAnimation(new FadeAnimation(this, 0.3f, 1.0f));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        // Render background with alpha
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(TEXTURE, x, y, 0, 0, width, height);

        // Render progress bar
        float progress = (expirationTime - System.currentTimeMillis()) / 30000.0F;
        int progressWidth = (int)(width * progress);
        graphics.fill(x, y, x + progressWidth, y + 2,
                ((int)(alpha * 255) << 24) | 0xFFFFFF);

        // Render message
        graphics.drawString(Minecraft.getInstance().font, message,
                x + 10, y + 10,
                ((int)(alpha * 255) << 24) | 0xFFFFFF);

        // Render buttons
        buttons.forEach(button -> button.render(graphics, mouseX, mouseY, partialTicks));
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    public void addButton(ToastButton button) {
        addChild(button); // This will handle setting the parent automatically
    }
}

