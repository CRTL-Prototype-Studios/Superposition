package cn.crtlprototypestudios.spos.client.slateui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class ToastButton extends UIComponent {
    private final Component text;
    private final OnPress onPress;
    private boolean hovered;
    private boolean enabled = true;

    public interface OnPress {
        void onPress(ToastButton button);
    }

    public ToastButton(int x, int y, int width, int height, Component text, OnPress onPress) {
        super(x, y, width, height);
        this.text = text;
        this.onPress = onPress;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        this.hovered = isHovered(mouseX, mouseY) && enabled;

        // Calculate alpha for the button based on parent's alpha if it exists
        float effectiveAlpha = alpha;
        if (this.getParent() instanceof UIComponent parent) {
            effectiveAlpha *= parent.alpha;
        }

        // Background color based on state
        int backgroundColor;
        if (!enabled) {
            backgroundColor = 0x80808080;
        } else if (hovered) {
            backgroundColor = 0xFF3C3C3C;
        } else {
            backgroundColor = 0xFF2C2C2C;
        }

        // Apply alpha to background color
        backgroundColor = applyAlpha(backgroundColor, effectiveAlpha);

        // Render button background
        graphics.fill(x, y, x + width, y + height, backgroundColor);

        // Draw border
        int borderColor = applyAlpha(hovered ? 0xFFFFFFFF : 0xFF999999, effectiveAlpha);
        graphics.fill(x, y, x + width, y + 1, borderColor); // Top
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor); // Bottom
        graphics.fill(x, y, x + 1, y + height, borderColor); // Left
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor); // Right

        // Center the text
        int textWidth = Minecraft.getInstance().font.width(String.valueOf(text));
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;

        // Draw text with shadow
        int textColor = applyAlpha(enabled ? (hovered ? 0xFFFFFF : 0xE0E0E0) : 0x808080, effectiveAlpha);
        graphics.drawString(Minecraft.getInstance().font, text, textX, textY, textColor);
    }

    private int applyAlpha(int color, float alpha) {
        int a = (int)((color >> 24 & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (enabled && isHovered(mouseX, mouseY) && button == 0) {
            this.playDownSound();
            this.onPress.onPress(this);
            return true;
        }
        return false;
    }

    protected void playDownSound() {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)
        );
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
    }
}

