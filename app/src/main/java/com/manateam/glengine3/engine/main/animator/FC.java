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
        float a = 1 / (1 + pow(e, -(2*k*t - k)));
        float b = 1 / (1 - 2 / (1 + pow(e, k)));
        return ((a - 0.5f) * b) + 0.5f;
    }

    public static float[] rotate(float[] params) {
        float[] matrix = new float[3];
        System.arraycopy(params, 0, matrix, 0, 3);
        for(int i = 0; i < 3; i++) matrix[i] += params[3 + i] * params[6];
        return matrix;
    }

    public static float[] shift(float[] params) {
        float[] matrix = new float[3];
        System.arraycopy(params, 0, matrix, 0, 3);
        for(int i = 0; i < 3; i++) matrix[i] += params[3 + i] * params[6];
        return matrix;
    }

    public static float[] pivotRotation(float[] params) {
        Vec3 pos = new Vec3(params[0], params[1], params[2]);
        Vec3 rot = new Vec3(params[3], params[4], params[5]);
        Vec3 pivPos = new Vec3(params[6], params[7], params[8]);
        Vec3 rotVec = new Vec3(params[9], params[10], params[11]);
        float dt = params[12];
        Vec3 dir = Vec3.sub(pos, pivPos);
        Vec3 rotated = Vec3.rotateToVec(dir, rotVec, Vec3.getAngle(dir, rotVec) * dt);
        return contactArray((normalize(rotated)).mul(dir.length()).add(pivPos).getVector(), rot.getVector());
    }
}
