package cn.crtlprototypestudios.spos.client.slateui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class ToastButton extends UIComponent {
    private static final int COLOR_NORMAL = 0xFF2C2C2C;
    private static final int COLOR_HOVERED = 0xFF3C3C3C;
    private static final int COLOR_DISABLED = 0x80808080;

    private static final int BORDER_NORMAL = 0xFF999999;
    private static final int BORDER_HOVERED = 0xFFFFFFFF;

    private static final int TEXT_NORMAL = 0xE0E0E0;
    private static final int TEXT_HOVERED = 0xFFFFFF;
    private static final int TEXT_DISABLED = 0x808080;

    private final Component text;
    private final OnPress onPress;
    private boolean hovered;
    private boolean enabled = true;
    private float hoverProgress = 0.0f; // Add this for smooth transition

    private int initialX; // Store initial X position relative to parent

    public void setInitialX(int initialX) {
        this.initialX = initialX;
    }

    public interface OnPress {
        void onPress(ToastButton button);
    }

    public ToastButton(int x, int y, int width, int height, Component text, OnPress onPress) {
        super(x, y, width, height);
        this.text = text;
        this.onPress = onPress;
    }

    @Override
    public void tick() {
        super.tick();
        // Smooth hover transition
        if (hovered && hoverProgress < 1.0f) {
            hoverProgress = Math.min(1.0f, hoverProgress + 0.2f);
        } else if (!hovered && hoverProgress > 0.0f) {
            hoverProgress = Math.max(0.0f, hoverProgress - 0.2f);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;

        // Update position based on parent's position
        if (getParent() != null) {
            this.x = getParent().x + initialX;
        }

        this.hovered = isHovered(mouseX, mouseY) && enabled;

        // Calculate effective alpha
        float effectiveAlpha = this.getParent() != null ? alpha * this.getParent().alpha : alpha;

        // Interpolate colors based on hover progress
        int backgroundColor = lerpColor(
                COLOR_NORMAL,
                COLOR_HOVERED,
                enabled ? hoverProgress : 0f
        );

        int borderColor = lerpColor(
                BORDER_NORMAL,
                BORDER_HOVERED,
                enabled ? hoverProgress : 0f
        );

        int textColor = lerpColor(
                TEXT_NORMAL,
                TEXT_HOVERED,
                enabled ? hoverProgress : 0f
        );

        if (!enabled) {
            backgroundColor = COLOR_DISABLED;
            borderColor = BORDER_NORMAL;
            textColor = TEXT_DISABLED;
        }

        // Apply alpha to colors
        backgroundColor = applyAlpha(backgroundColor, effectiveAlpha);
        borderColor = applyAlpha(borderColor, effectiveAlpha);
        textColor = applyAlpha(textColor, effectiveAlpha);

        // Render button background
        graphics.fill(x, y, x + width, y + height, backgroundColor);

        // Draw border
        graphics.fill(x, y, x + width, y + 1, borderColor); // Top
        graphics.fill(x, y + height - 1, x + width, y + height, borderColor); // Bottom
        graphics.fill(x, y, x + 1, y + height, borderColor); // Left
        graphics.fill(x + width - 1, y, x + width, y + height, borderColor); // Right

        // Center the text
        int textWidth = Minecraft.getInstance().font.width(text);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;

        // Draw text
        graphics.drawString(Minecraft.getInstance().font, text, textX, textY, textColor);
    }

    private int lerpColor(int color1, int color2, float progress) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a1 = (color1 >> 24) & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;

        int r = (int) (r1 + (r2 - r1) * progress);
        int g = (int) (g1 + (g2 - g1) * progress);
        int b = (int) (b1 + (b2 - b1) * progress);
        int a = (int) (a1 + (a2 - a1) * progress);

        return (a << 24) | (r << 16) | (g << 8) | b;
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

