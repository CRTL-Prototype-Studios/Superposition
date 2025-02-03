package cn.crtlprototypestudios.spos.client.slateui;

public class SlideAnimation extends UIAnimation {
    private final int startX;
    private final int targetX;

    public SlideAnimation(UIComponent target, float durationSeconds, boolean slideIn) {
        super(target, durationSeconds);
        if (slideIn) {
            // Slide in from right
            this.targetX = target.x;
            this.startX = target.x + 200; // Slide distance
        } else {
            // Slide out to right
            this.startX = target.x;
            this.targetX = target.x + 200; // Slide distance
        }
    }

    @Override
    protected void animate(float progress) {
        target.x = (int)(startX + (targetX - startX) * easeOutCubic(progress));

        // Update child positions
        int offsetX = target.x - startX;
        for (UIComponent child : target.getChildren()) {
            child.x = child.x + offsetX;
        }
    }

    private float easeOutCubic(float x) {
        return 1 - (float)Math.pow(1 - x, 3);
    }
}

