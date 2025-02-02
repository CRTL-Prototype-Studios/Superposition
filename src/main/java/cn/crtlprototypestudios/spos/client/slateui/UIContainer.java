package cn.crtlprototypestudios.spos.client.slateui;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class UIContainer extends UIComponent {
    private List<UIAnimation> activeAnimations = new ArrayList<>();

    public UIContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        children.forEach(child -> child.render(graphics, mouseX, mouseY, partialTicks));
    }

    public void addAnimation(UIAnimation animation) {
        activeAnimations.add(animation);
    }

    @Override
    public void tick() {
        super.tick();
        activeAnimations.removeIf(UIAnimation::update);
    }

    // Override addChild to ensure proper parent-child relationship
    @Override
    public void addChild(UIComponent child) {
        super.addChild(child);
        if (child instanceof ToastButton) {
            // Ensure buttons inherit container properties
            child.alpha = this.alpha;
        }
    }
}

