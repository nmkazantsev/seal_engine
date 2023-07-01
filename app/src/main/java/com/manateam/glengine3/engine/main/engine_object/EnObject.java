package com.manateam.glengine3.engine.main.engine_object;

import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.applyMatrix;

import com.manateam.glengine3.engine.main.animator.Animator;
import com.manateam.glengine3.engine.main.animator.FC;
import com.manateam.glengine3.engine.main.verticles.Shape;

// todo make it work
public class EnObject {
    private Shape shape;
    private float[] drawMatrix;
    public float[] getMatrix() {
        return this.drawMatrix;
    }

    public EnObject(Shape shape) {
        this.shape = shape;
        drawMatrix = new float[16];
    }

    public void animMotion(float[] direction, float duration) {
        new Animator.Animation(this,
                params -> FC.shift((float[]) params),
                direction,
                params1 -> FC.linear((float[]) params1),
                duration,
                0);
    }

    public void prepareAndDraw() {
        applyMatrix(drawMatrix);
        shape.prepareAndDraw();
    }
}
