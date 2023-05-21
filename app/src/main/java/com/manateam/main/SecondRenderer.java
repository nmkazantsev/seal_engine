package com.manateam.main;

import static android.opengl.GLES20.glClearColor;
import static com.manateam.glengine3.OpenGLRenderer.fps;
import static com.manateam.glengine3.OpenGLRenderer.mMatrix;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.applyCameraSettings;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.applyProjectionMatrix;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.manateam.glengine3.engine.main.frameBuffers.FrameBufferUtils.connectDefaultFrameBuffer;
import static com.manateam.glengine3.engine.main.frameBuffers.FrameBufferUtils.connectFrameBuffer;
import static com.manateam.glengine3.engine.main.shaders.Shader.applyShader;
import static com.manateam.glengine3.utils.Utils.kx;
import static com.manateam.glengine3.utils.Utils.ky;
import static com.manateam.glengine3.utils.Utils.map;
import static com.manateam.glengine3.utils.Utils.millis;
import static com.manateam.glengine3.utils.Utils.x;
import static com.manateam.glengine3.utils.Utils.y;

import android.graphics.ImageDecoder;
import android.opengl.Matrix;
import android.util.Log;

import com.example.gl_engine_3_1.R;
import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.OpenGLRenderer;
import com.manateam.glengine3.engine.main.camera.CameraSettings;
import com.manateam.glengine3.engine.main.camera.ProjectionMatrixSettings;
import com.manateam.glengine3.engine.main.shaders.Shader;
import com.manateam.glengine3.engine.main.verticles.Poligon;
import com.manateam.glengine3.engine.main.verticles.Shape;
import com.manateam.glengine3.maths.Point;
import com.manateam.main.adaptors.LightShaderAdaptor;
import com.manateam.main.adaptors.MainShaderAdaptor;
import com.manateam.main.redrawFunctions.MainRedrawFunctions;

public class SecondRenderer implements GamePageInterface {
    private final Poligon fpsPoligon;
    private final Shader shader, lightShader;
    private final ProjectionMatrixSettings projectionMatrixSettings;
    private final CameraSettings cameraSettings;
    private final Shape s;

    public SecondRenderer() {
        shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, this, new MainShaderAdaptor());
        lightShader = new Shader(R.raw.vertex_shader_light, R.raw.fragment_shader_light, this, new LightShaderAdaptor());
        fpsPoligon = new Poligon(MainRedrawFunctions::redrawFps, true, 1, this);
        cameraSettings = new CameraSettings(x, y);
        cameraSettings.resetFor3d();
        projectionMatrixSettings = new ProjectionMatrixSettings(x, y);
        s = new Shape("donut_864.obj", "ponch.jpg", this);
        s.addNormalMap("normal_map.png");
    }

    @Override
    public void draw() {
        //shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, this);
        applyShader(lightShader);
        glClearColor(0f, 0, 0, 0);

        cameraSettings.resetFor3d();
        projectionMatrixSettings.resetFor3d();
        cameraSettings.eyeZ = 5;
        cameraSettings.eyeY = 3;
        applyCameraSettings(cameraSettings);
        applyProjectionMatrix(projectionMatrixSettings);
        mMatrix = resetTranslateMatrix(mMatrix);
        Matrix.rotateM(mMatrix, 0, map(millis() % 10000, 0, 10000, 0, 360), 1, 0.5f, 0);
        //Matrix.scaleM(mMatrix,0,0.5f,0.5f,0.5f);
        applyMatrix(mMatrix);
        s.prepareAndDraw();

        applyShader(shader);
        fpsPoligon.setRedrawNeeded(true);
        cameraSettings.resetFor2d();
        projectionMatrixSettings.resetFor2d();
        applyProjectionMatrix(projectionMatrixSettings, false);
        applyCameraSettings(cameraSettings);
        mMatrix = resetTranslateMatrix(mMatrix);
        applyMatrix(mMatrix);
        fpsPoligon.redrawParams.set(0, String.valueOf(fps));
        fpsPoligon.redrawNow();
        fpsPoligon.prepareAndDraw(new Point(0 * kx, 0, 1), new Point(100 * kx, 0, 1), new Point(0 * kx, 100 * ky, 1));
    }

    @Override
    public void touchStarted() {
        Log.e("touch", "statred");
        OpenGLRenderer.startNewPage(new MainRenderer());
        //startNewPage(new LightRender());
    }

    @Override
    public void touchMoved() {

    }

    @Override
    public void touchEnded() {

    }
}
