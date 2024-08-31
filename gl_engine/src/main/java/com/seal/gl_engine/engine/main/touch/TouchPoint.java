package com.seal.gl_engine.engine.main.touch;

/**
 * This class contains last known info about touch. It is being updated every time engine receives callback from Android.
 */
public class TouchPoint {
    public final float touchX, touchY;

    public TouchPoint(float x, float y) {
        this.touchX = x;
        this.touchY = y;
    }
}
