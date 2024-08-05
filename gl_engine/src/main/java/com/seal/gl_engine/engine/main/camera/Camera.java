package com.seal.gl_engine.engine.main.camera;

import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyCameraSettings;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyProjectionMatrix;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

/**
 * Just a container with public CameraSettings and ProjectionMatrixSettings
 */
public class Camera {
    public CameraSettings cameraSettings;
    public ProjectionMatrixSettings projectionMatrixSettings;
    private boolean mode3d = true;

    /**
     * A constructor to create new game camera. Resets CameraSettings and ProjectionMatrix Settings for 3d drawing.
     * Takes x and y dimensions equal to screen size.
     */
    public Camera() {
        cameraSettings = new CameraSettings(x, y);
        projectionMatrixSettings = new ProjectionMatrixSettings(x, y);
        resetFor3d();
    }

    /**
     * A constructor to create new game camera with specified x and y for projectionMatrix and camera. Resets CameraSettings and ProjectionMatrix Settings for 3d drawing.
     *
     * @param x x dimension of projection matrix and camera
     * @param y y dimension of projection matrix and camera
     */
    public Camera(float x, float y) {
        cameraSettings = new CameraSettings(x, y);
        projectionMatrixSettings = new ProjectionMatrixSettings(x, y);
        resetFor3d();
    }

    public void resetFor3d() {
        cameraSettings.resetFor3d();
        projectionMatrixSettings.resetFor3d();
        mode3d = true;
    }

    public void resetFor2d() {
        cameraSettings.resetFor2d();
        projectionMatrixSettings.resetFor2d();
        mode3d = false;
    }

    /**
     * applies camera and projection matrix. Perspective boolean is always true if called after resetFor3d() and false after resetFor2d()
     */
    public void apply() {
        applyCameraSettings(cameraSettings);
        applyProjectionMatrix(projectionMatrixSettings, mode3d);
    }

    /**
     * applies camera and projection matrices.
     *
     * @param perspectiveEnabled enables or disables perspective.
     */
    public void apply(boolean perspectiveEnabled) {
        applyCameraSettings(cameraSettings);
        applyProjectionMatrix(projectionMatrixSettings, perspectiveEnabled);
    }
}
