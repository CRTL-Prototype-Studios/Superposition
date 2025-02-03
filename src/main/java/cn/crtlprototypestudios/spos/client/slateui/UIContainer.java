package cn.crtlprototypestudios.spos.client.slateui;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class UIContainer extends UIComponent {

    public UIContainer(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!visible) return;
        children.forEach(child -> child.render(graphics, mouseX, mouseY, partialTicks));
    }

    // Only one addChild override
    @Override
    public void addChild(UIComponent child) {
        super.addChild(child);
        if (child instanceof ToastButton) {
            // Ensure buttons inherit container properties
            child.alpha = this.alpha;
        }
    }

    @Override
    public void tick() {
        super.tick();
        activeAnimations.removeIf(UIAnimation::update);
    }
}

