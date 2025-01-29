package com.manateam.main.lor_test.camera;

import com.seal.gl_engine.engine.main.camera.ProjectionMatrixSettings;

public class GameProjectionMatrixSettings extends ProjectionMatrixSettings {
    public GameProjectionMatrixSettings(float x, float y) {
        super(x, y);
    }

    @Override
    public void resetFor3d() {
        float ratio;
        final int size = 5;
        left = -size;
        right = size;
        bottom = -size;
        top = size;
        near = 10;
        far = 1000;
        if (x < y) {
            ratio = (float) x / y;
            left *= ratio;
            right *= ratio;
        } else {
            ratio = (float) y / x;
            bottom *= ratio;
            top *= ratio;
        }

    }

}
