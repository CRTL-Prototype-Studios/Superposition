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
    protected List<UIAnimation> activeAnimations = new ArrayList<>(); // Add this

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
        // Create a new list to store animations that should be removed
        List<UIAnimation> finishedAnimations = new ArrayList<>();

        // Check which animations are finished
        for (UIAnimation animation : activeAnimations) {
            if (animation.update()) {
                finishedAnimations.add(animation);
            }
        }

        // Remove finished animations
        activeAnimations.removeAll(finishedAnimations);

        // Create a copy of children list to avoid concurrent modification
        new ArrayList<>(children).forEach(UIComponent::tick);
    }


    public void addAnimation(UIAnimation animation) {
        activeAnimations.add(animation);
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

    public List<UIComponent> getChildren() {
        return children;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}

