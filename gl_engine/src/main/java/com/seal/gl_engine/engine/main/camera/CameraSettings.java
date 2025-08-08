package com.seal.gl_engine.engine.main.camera;

import com.seal.gl_engine.maths.PVector;

public class CameraSettings {
    public float eyeX;
    public float eyeY;
    public float eyeZ;

    public float centerX;
    public float centerY;
    public float centerZ;

    public float upX;
    public float upY;
    public float upZ;

    protected float x, y;

    public CameraSettings(float screenx, float screeny) {
        this.x = screenx;
        this.y = screeny;
        resetFor3d();
    }

    public void setPos(PVector pos) {
        eyeX = pos.x;
        eyeY = pos.y;
        eyeZ = pos.z;
    }

    public void SetUpVector(PVector up) {
        upX = up.x;
        upY = up.y;
        upZ = up.z;
    }

    public void setCenter(PVector center) {
        centerX = center.x;
        centerY = center.y;
        centerZ = center.z;
    }

    public void resetFor3d() {
        eyeX = eyeY = 0;
        eyeZ = 7;
        centerX = centerY = centerZ = 0;
        // upX=upZ=0;
        upY = 1;
    }

    public void resetFor2d() {
        eyeX = 0;//-x / 2;
        eyeY = 0;
        eyeZ = 20;

        centerX = 0;
        centerY = 0;
        centerZ = 0;

        upX = 0;
        upY = 1;
        upZ = 0;
    }
}
/*
для активации настроек камеры нужна строка вида
 private void applyCameraSettings(CameraSettings cam) {
        Matrix.setLookAtM(mViewMatrix, 0, cam.eyeX, cam.eyeY, cam.eyeZ, cam.centerX, cam.centerY, cam.centerZ, cam.upX, cam.upY, cam.upZ);
        bindMatrix();
    }
 */
