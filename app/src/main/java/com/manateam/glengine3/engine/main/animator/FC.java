package com.manateam.glengine3.engine.main.animator;

import static com.manateam.glengine3.maths.Vec3.normalize;
import static com.manateam.glengine3.utils.Utils.contactArray;
import static com.manateam.glengine3.utils.Utils.pow;

import com.manateam.glengine3.maths.Vec3;

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
        float a = 1 / (1 + pow(e, -(2 * k * t - k)));
        float b = 1 / (1 - 2 / (1 + pow(e, k)));
        return ((a - 0.5f) * b) + 0.5f;
    }

    public static float[] rotate(Animator.Animation animation) {
        float[] attrs = animation.getAttrs();
        float[] args = animation.getArgs();
        Vec3 rot = new Vec3(attrs, 3);
        Vec3 ang = new Vec3(args, 0);
        float dt = animation.getDeltaT();
        rot.add(ang.mul(dt));
        System.arraycopy(rot.getArray(), 0, attrs, 3, 3);
        return attrs;
    }

    public static float[] shift(Animator.Animation animation) {
        float[] attrs = animation.getAttrs();
        float[] args = animation.getArgs();
        Vec3 pos = new Vec3(animation.getAttrs(), 0);
        Vec3 shift = new Vec3(animation.getArgs(), 0);
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
        Vec3 pos = new Vec3(attrs, 0);
        Vec3 rot = new Vec3(attrs, 3);
        Vec3 pivPos = new Vec3(args, 0);
        Vec3 rotVec = new Vec3(args, 3);
        float dt = animation.getDeltaT();
        Vec3 dir = Vec3.sub(pos, pivPos);
        Vec3 rotated = Vec3.rotateToVec(dir, rotVec, Vec3.getAngle(dir, rotVec) * dt);
        return contactArray((normalize(rotated)).mul(dir.length()).add(pivPos).getArray(), rot.getArray());
    }

    public static float[] moveThroughBezierCurve(Animator.Animation animation) {
        return new float[]{};
    }
}
