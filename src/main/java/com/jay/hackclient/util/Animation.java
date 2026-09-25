package com.jay.hackclient.util;

/**
 * Premium animation system used by GUI + TargetHUD.
 * Ease curves match paid Ghost clients (Prestige / Grave / Elusive style).
 */
public final class Animation {

    public enum Direction { FORWARDS, BACKWARDS }

    private double value;
    private double endPoint;
    private long startTime;
    private long duration;
    private Direction direction = Direction.FORWARDS;
    private Easing easing = Easing.EASE_OUT_CUBIC;

    public Animation(long durationMs, double endPoint) {
        this.duration = durationMs;
        this.endPoint = endPoint;
        this.startTime = System.currentTimeMillis();
    }

    public Animation(long durationMs, double endPoint, Easing easing) {
        this(durationMs, endPoint);
        this.easing = easing;
    }

    public void setDirection(Direction dir) {
        if (this.direction != dir) {
            this.direction = dir;
            this.startTime = System.currentTimeMillis() - (long) ((1.0 - getProgress()) * duration);
        }
    }

    public void setDuration(long ms) {
        this.duration = Math.max(1, ms);
    }

    public void setEndPoint(double end) {
        this.endPoint = end;
    }

    public void reset() {
        this.startTime = System.currentTimeMillis();
        this.value = 0;
    }

    public double getOutput() {
        double progress = getProgress();
        double eased = easing.apply(progress);
        if (direction == Direction.FORWARDS) {
            value = eased * endPoint;
        } else {
            value = (1.0 - eased) * endPoint;
        }
        return value;
    }

    public float getOutputF() {
        return (float) getOutput();
    }

    public boolean isDone() {
        return getProgress() >= 1.0;
    }

    public boolean finished(Direction dir) {
        return direction == dir && isDone();
    }

    private double getProgress() {
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.min(1.0, Math.max(0.0, (double) elapsed / duration));
    }

    public enum Easing {
        LINEAR {
            @Override public double apply(double t) { return t; }
        },
        EASE_OUT_CUBIC {
            @Override public double apply(double t) { return 1.0 - Math.pow(1.0 - t, 3); }
        },
        EASE_IN_OUT_CUBIC {
            @Override public double apply(double t) {
                return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
            }
        },
        EASE_OUT_BACK {
            @Override public double apply(double t) {
                final double c1 = 1.70158;
                final double c3 = c1 + 1;
                return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
            }
        },
        EASE_OUT_EXPO {
            @Override public double apply(double t) {
                return t >= 1 ? 1 : 1 - Math.pow(2, -10 * t);
            }
        },
        EASE_OUT_QUAD {
            @Override public double apply(double t) { return 1 - (1 - t) * (1 - t); }
        };

        public abstract double apply(double t);
    }
}
