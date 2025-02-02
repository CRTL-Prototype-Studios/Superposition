package cn.crtlprototypestudios.spos.client.slateui;

public class FadeAnimation extends UIAnimation {
    private final float startAlpha;
    private final float targetAlpha;

    public FadeAnimation(UIComponent target, float durationSeconds, float targetAlpha) {
        super(target, durationSeconds);
        this.startAlpha = target.alpha;
        this.targetAlpha = targetAlpha;
    }

    @Override
    protected void animate(float progress) {
        target.alpha = startAlpha + (targetAlpha - startAlpha) * easeInOut(progress);
    }

    private float easeInOut(float x) {
        return x < 0.5f ? 2 * x * x : 1 - (float)Math.pow(-2 * x + 2, 2) / 2;
    }
}

