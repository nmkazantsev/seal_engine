package com.manateam.main;

import static android.opengl.GLES20.glClearColor;
import static com.manateam.glengine3.OpenGLRenderer.fps;
import static com.manateam.glengine3.OpenGLRenderer.mMatrix;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.applyCameraSettings;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.applyProjectionMatrix;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.manateam.glengine3.engine.main.shaders.Shader.applyShader;
import static com.manateam.glengine3.engine.oldEngine.glEngine.prepareAndDraw;
import static com.manateam.glengine3.utils.Utils.kx;
import static com.manateam.glengine3.utils.Utils.ky;
import static com.manateam.glengine3.utils.Utils.x;
import static com.manateam.glengine3.utils.Utils.y;

import android.util.Log;

import com.example.gl_engine_3_1.R;
import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.OpenGLRenderer;
import com.manateam.glengine3.engine.main.camera.CameraSettings;
import com.manateam.glengine3.engine.main.camera.ProjectionMatrixSettings;
import com.manateam.glengine3.engine.main.shaders.Shader;
import com.manateam.glengine3.engine.main.verticles.Poligon;
import com.manateam.glengine3.engine.main.verticles.SimplePoligon;
import com.manateam.glengine3.maths.Point;
import com.manateam.main.adaptors.MainShaderAdaptor;
import com.manateam.main.redrawFunctions.MainRedrawFunctions;

public class MainRenderer implements GamePageInterface {
    private Poligon fpsPoligon,poligon;
    private Shader shader;
    private ProjectionMatrixSettings projectionMatrixSettings;
    private CameraSettings cameraSettings;
    private static SimplePoligon simplePoligon;

    public MainRenderer() {
        shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, this, new MainShaderAdaptor());
        fpsPoligon = new Poligon(MainRedrawFunctions::redrawFps, true, 1, this);
        poligon=new Poligon(MainRedrawFunctions::redrawFps,true,0,this);
        poligon.redrawNow();
        cameraSettings = new CameraSettings(x, y);
        cameraSettings.resetFor3d();
        projectionMatrixSettings = new ProjectionMatrixSettings(x, y);
        simplePoligon=new SimplePoligon(MainRedrawFunctions::redrawBox2,true,0,null);
        simplePoligon.redrawNow();
    }

    @Override
    public void draw() {
        //shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, this);
        applyShader(shader);
        glClearColor(1f, 1f, 1f, 1);
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
        poligon.prepareAndDraw(new Point(110 * kx, 0, 1), new Point(200 * kx, 0, 1), new Point(110 * kx, 100 * ky, 1));
        simplePoligon.prepareAndDraw(0,300,300,300,300,0.01f);
    }

    @Override
    public void touchStarted() {
        Log.e("touch", "statred");
        OpenGLRenderer.startNewPage(new SecondRenderer());
    }

    @Override
    public void touchMoved() {

    }

    @Override
    public void touchEnded() {

    }
}
