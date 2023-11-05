package com.manateam.main;

import static android.opengl.GLES20.glClearColor;
import static com.example.engine.glengine3.OpenGLRenderer.fps;
import static com.example.engine.glengine3.OpenGLRenderer.mMatrix;
import static com.example.engine.glengine3.OpenGLRenderer.pageMillis;
import static com.example.engine.glengine3.engine.config.MainConfigurationFunctions.applyCameraSettings;
import static com.example.engine.glengine3.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.example.engine.glengine3.engine.config.MainConfigurationFunctions.applyProjectionMatrix;
import static com.example.engine.glengine3.engine.main.shaders.Shader.applyShader;
import static com.example.engine.glengine3.utils.Utils.kx;
import static com.example.engine.glengine3.utils.Utils.ky;
import static com.example.engine.glengine3.utils.Utils.millis;
import static com.example.engine.glengine3.utils.Utils.x;
import static com.example.engine.glengine3.utils.Utils.y;

import com.example.engine.glengine3.GamePageInterface;

import com.example.engine.glengine3.OpenGLRenderer;
import com.example.engine.glengine3.engine.main.camera.ProjectionMatrixSettings;
import com.example.engine.glengine3.engine.main.shaders.Shader;
import com.example.engine.glengine3.engine.main.verticles.Shape;
import com.example.engine.glengine3.maths.Point;
import com.example.gl_engine_3_1.R;
import com.manateam.main.adaptors.MainShaderAdaptor;
import com.manateam.main.redrawFunctions.MainRedrawFunctions;
import com.example.engine.glengine3.engine.main.verticles.Poligon;
import com.example.engine.glengine3.engine.main.verticles.SimplePoligon;
import com.example.engine.glengine3.engine.main.engine_object.EnObject;
import com.example.engine.glengine3.engine.main.animator.Animator;
import com.example.engine.glengine3.engine.main.camera.CameraSettings;

public class MainRenderer implements GamePageInterface {
    private final Poligon fpsPolygon;
    private final Poligon polygon;
    private final Shader shader;
    private final ProjectionMatrixSettings projectionMatrixSettings;
    private final CameraSettings cameraSettings;
    private static SimplePoligon simplePolygon;
    private final EnObject s;

    //  private FrameBuffer frameBuffer;
    public MainRenderer() {
        Animator.initialize();
        shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, this, new MainShaderAdaptor());
        fpsPolygon = new Poligon(MainRedrawFunctions::redrawFps, true, 1, this);
        polygon = new Poligon(MainRedrawFunctions::redrawFps, true, 0, this);
        polygon.redrawNow();
        cameraSettings = new CameraSettings(x, y);
        cameraSettings.resetFor3d();
        projectionMatrixSettings = new ProjectionMatrixSettings(x, y);
        if (simplePolygon == null) {
            simplePolygon = new SimplePoligon(MainRedrawFunctions::redrawBox2, true, 0, null);
            simplePolygon.redrawNow();
        }
        s = new EnObject(new Shape("cube.obj", "cube.png", this));
        s.animMotion(1f, 0f, -1f, 1000, 0);
        s.animMotion(0.f, 3f, 0f, 1000, 0);
        s.animRotation(0f, 0f, 90f, 3000, 0);
        s.animRotation(90f, 0, 0, 1000, 3000);
        s.animPivotRotation(0, 0, 0, 1, 1, 1, 1000, 5000);
    }

    @Override
    public void draw() {
        applyShader(shader);
        glClearColor(1f, 1f, 1f, 1);
        cameraSettings.resetFor3d();
        projectionMatrixSettings.resetFor3d();
        cameraSettings.eyeZ = 5;
        applyCameraSettings(cameraSettings);
        applyProjectionMatrix(projectionMatrixSettings);
        // connectFrameBuffer(frameBuffer.getFrameBuffer());
        s.prepareAndDraw();

        fpsPolygon.setRedrawNeeded(true);
        cameraSettings.resetFor2d();
        projectionMatrixSettings.resetFor2d();
        applyProjectionMatrix(projectionMatrixSettings, false);
        applyCameraSettings(cameraSettings);
        applyMatrix(mMatrix);
        fpsPolygon.redrawParams.set(0, String.valueOf(pageMillis()));
        fpsPolygon.redrawNow();
        fpsPolygon.prepareAndDraw(new Point(0 * kx, 0, 1), new Point(100 * kx, 0, 1), new Point(0 * kx, 100 * ky, 1));
        polygon.prepareAndDraw(new Point(110 * kx, 0, 1), new Point(200 * kx, 0, 1), new Point(110 * kx, 100 * ky, 1));
        simplePolygon.prepareAndDraw(0, 300, 300, 300, 300, 0.01f);
        // frameBuffer.drawTexture(new Point(x/3,y/2),new Point(2*x/3,y/2),new Point(x/3,y));
    }

    @Override
    public void touchStarted() {
        OpenGLRenderer.startNewPage(new SecondRenderer());
    }

    @Override
    public void touchMoved() {

    }

    @Override
    public void touchEnded() {

    }
}
