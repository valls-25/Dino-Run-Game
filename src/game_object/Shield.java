package game_object;

public class Shield {
    private int duration;
    private boolean isActive;
    private long activationTime;

    public Shield(int duration) {
        this.duration = duration;
        this.isActive = false;
        this.activationTime = 0;
    }

    public void activate() {
        isActive = true;
        activationTime = System.currentTimeMillis();
    }

    public void update() {
        if (isActive && System.currentTimeMillis() - activationTime > duration * 1000) {
            deactivate();
        }
    }

    public void deactivate() {
        isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public int getRemainingTime() {
        if (!isActive)
            return 0;
        long elapsedTime = System.currentTimeMillis() - activationTime;
        return Math.max(0, (int) ((duration * 1000 - elapsedTime) / 1000));
    }

    public void reset() {
        isActive = false;
        activationTime = 0;
    }

}