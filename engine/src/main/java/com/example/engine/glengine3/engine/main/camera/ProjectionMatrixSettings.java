package com.example.engine.glengine3.engine.main.camera;

public class ProjectionMatrixSettings {
    public float left;
    public float top;
    public float right;
    public float bottom;
    public float near;
    public float far;

    private float x, y;

    public ProjectionMatrixSettings(float x, float y) {
        this.x = x;
        this.y = y;
        resetFor3d();
    }

    public void resetFor3d() {
        float ratio = 1;
        left = -0.1f;
        right = 0.1f;
        bottom = -0.1f;
        top = 0.1f;
        near = 0.1f;
        far = 12;
        if (x > y) {
            ratio = (float) x / y;
            left *= ratio;
            right *= ratio;
        } else {
            ratio = (float) y / x;
            bottom *= ratio;
            top *= ratio;
        }

    }

    public void resetFor2d() {
        /*left = x / 4;
        top = y / 4;
        right = 3 * x / 4;
        bottom = 3 * y / 4;
        near = 10;
        far = 20;

         */
        left = 0;
        top = 0;
        right = x;
        bottom = y;
        near = 10;
        far = 20;
    }
}
/*
для активации настроек проектора нужна строка вида
//apply camera settings call requed later!
    private void applyProjectionMatrixSettings(ProjectionMatrixSettings p) {
        Matrix.frustumM(mProjectionMatrix, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
    }
 */
