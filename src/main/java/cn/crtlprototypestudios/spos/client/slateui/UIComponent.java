package cn.crtlprototypestudios.spos.client.slateui;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public abstract class UIComponent {
    protected int x, y, width, height;
    protected float alpha = 1.0f;
    protected boolean visible = true;
    protected List<UIComponent> children = new ArrayList<>();
    protected UIComponent parent; // Add parent reference

    public UIComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks);

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width &&
                mouseY >= y && mouseY <= y + height;
    }

    public void tick() {
        children.forEach(UIComponent::tick);
    }

    // Add child handling methods
    public void addChild(UIComponent child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(UIComponent child) {
        children.remove(child);
        child.setParent(null);
    }

    public void setParent(UIComponent parent) {
        this.parent = parent;
    }

    public UIComponent getParent() {
        return parent;
    }
}

