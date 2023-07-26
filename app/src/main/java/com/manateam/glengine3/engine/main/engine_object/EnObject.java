package com.manateam.glengine3.engine.main.engine_object;

import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.resetTranslateMatrix;


import android.opengl.Matrix;

import com.manateam.glengine3.engine.main.animator.Animator;
import com.manateam.glengine3.engine.main.verticles.Shape;

public class EnObject {
    private final Shape shape;
    private float[] posMatrix;
    private float[] rotMatrix;
    public float[] getPosMatrix() {
        return this.posMatrix;
    }

    public float[] getRotMatrix() {
        return this.rotMatrix;
    }

    public void setPosMatrix(float[] m) {
        this.posMatrix = m;
    }
    public void setRotMatrix(float[] m) {
        this.rotMatrix = m;
    }

    public EnObject(Shape shape) {
        this.shape = shape;
        posMatrix = new float[3];
        rotMatrix = new float[3];
    }

    public void animMotion(float x, float y, float z, float duration, long startTiming) {
        new Animator.Animation(this,
                Animator.SHIFT,
                new float[]{x, y, z},
                Animator.LINEAR,
                duration,
                0,
                startTiming);
    }

    public void animRotation(float x, float y, float z, float duration, long startTiming) {
        new Animator.Animation(this,
                Animator.ROTATION,
                new float[]{x, y, z},
                Animator.LINEAR,
                duration,
                0,
                startTiming);
    }

    public void prepareAndDraw() {
        Animator.animate(this);
        float[] b = new float[16];
        resetTranslateMatrix(b);
        Matrix.translateM(b, 0, posMatrix[0], posMatrix[1], posMatrix[2]);
        Matrix.rotateM(b,0,rotMatrix[0],1,0,0);
        Matrix.rotateM(b,0,rotMatrix[1],0,1,0);
        Matrix.rotateM(b,0,rotMatrix[2],0,0,1);
        applyMatrix(b);
        shape.prepareAndDraw();
    }
}
