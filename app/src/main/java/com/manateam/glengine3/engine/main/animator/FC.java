package com.manateam.glengine3.engine.main.animator;

import static com.manateam.glengine3.utils.Utils.pow;

// Function Collection
public class FC {
    private float e = 2.718281828f;
    public float linear(float t) {
        return t;
    }
    public float power(float t, float p) {
        return pow(t, p);
    }

    // k is steepness
    public float sigmoid(float t, float k) {
        float a = 1 / (1 + pow(e, -(2*k*t - k)));
        float b = 1 / (1 - 2 / (1 + pow(e, k)));
        return ((a - 0.5f) * b) + 0.5f;
    }
}
