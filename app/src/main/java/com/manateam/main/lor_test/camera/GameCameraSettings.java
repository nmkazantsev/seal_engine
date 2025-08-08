package com.manateam.main.lor_test.camera;

import com.seal.gl_engine.engine.main.camera.CameraSettings;

public class GameCameraSettings extends CameraSettings {
    public GameCameraSettings(float screenx, float screeny) {
        super(screenx, screeny);
    }
    @Override
    public void resetFor3d(){
        eyeX=eyeY=0;
        eyeZ=10;
        centerX=0;
        centerY=0;
        centerZ=0;
        upX=0;
        upZ=1;
        upY=0;
    }



}
