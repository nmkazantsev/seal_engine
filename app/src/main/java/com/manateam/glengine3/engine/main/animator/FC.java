package com.manateam.glengine3.engine.main.animator;

import static com.manateam.glengine3.utils.Utils.pow;

import android.opengl.Matrix;

// Function Collection
public class FC {
    private static final float e = 2.718281828f;
    public static float linear(float[] params) {
        return params[0];
    }
    public static float power(float[] params) {
        float t = params[0];
        float p = params[0];
        return pow(t, p);
    }

    // k is steepness
    // todo make it work, make animation take one more parameter
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
}
