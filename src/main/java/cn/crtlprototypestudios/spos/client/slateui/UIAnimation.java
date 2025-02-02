package cn.crtlprototypestudios.spos.client.slateui;

public abstract class UIAnimation {
    protected float duration;
    protected float progress;
    protected boolean finished;
    protected UIComponent target;

    public UIAnimation(UIComponent target, float durationSeconds) {
        this.target = target;
        this.duration = durationSeconds * 20; // Convert to ticks
        this.progress = 0;
        this.finished = false;
    }

    public boolean update() {
        if (finished) return true;

        progress = Math.min(progress + 1, duration);
        float normalized = progress / duration;

        animate(normalized);

        if (progress >= duration) {
            finished = true;
        }

        return finished;
    }

    protected abstract void animate(float progress);
}
