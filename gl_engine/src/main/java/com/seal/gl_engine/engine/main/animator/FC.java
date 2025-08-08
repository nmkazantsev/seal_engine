package com.seal.gl_engine.engine.main.animator;

import com.seal.gl_engine.maths.PVector;
import com.seal.gl_engine.utils.Utils;

// Function Collection
public class FC {
    private static final float e = 2.718281828f;

    public static float linear(float[] params) {
        return params[0];
    }

    // k is steepness
    public static float sigmoid(float[] params) {
        float k = params[0];
        float t = params[1];
        float a = 1 / (1 + Utils.pow(e, -(2 * k * t - k)));
        float b = 1 / (1 - 2 / (1 + Utils.pow(e, k)));
        return ((a - 0.5f) * b) + 0.5f;
    }

    public static float[] rotate(Animator.Animation animation) {
        float[] attrs = animation.getAttrs();
        float[] args = animation.getArgs();
        PVector rot = new PVector(attrs, 3);
        PVector ang = new PVector(args, 0);
        float dt = animation.getDeltaT();
        rot.add(ang.mul(dt));
        System.arraycopy(rot.getArray(), 0, attrs, 3, 3);
        return attrs;
    }

    public static float[] shift(Animator.Animation animation) {
        float[] attrs = animation.getAttrs();
        float[] args = animation.getArgs();
        PVector pos = new PVector(animation.getAttrs(), 0);
        PVector shift = new PVector(animation.getArgs(), 0);
        float dt = animation.getDeltaT();
        pos.add(shift.mul(dt)).getArray();
        System.arraycopy(pos.getArray(), 0, attrs, 0, 3);
        return attrs;
    }

    // does not work yet
    // todo: add orientation change
    public static float[] pivotRotation(Animator.Animation animation) {
        float[] attrs = animation.getAttrs();
        float[] args = animation.getArgs();
        PVector pos = new PVector(attrs, 0);
        PVector rot = new PVector(attrs, 3);
        PVector pivPos = new PVector(args, 0);
        PVector rotVec = new PVector(args, 3);
        float dt = animation.getDeltaT();
        PVector dir = PVector.sub(pos, pivPos);
        PVector rotated = PVector.rotateToVec(dir, rotVec, PVector.getAngle(dir, rotVec) * dt);
        return Utils.contactArray((PVector.normalize(rotated)).mul(dir.length()).add(pivPos).getArray(), rot.getArray());
    }

    public static float[] moveThroughBezierCurve(Animator.Animation animation) {
        return new float[]{};
    }
}
