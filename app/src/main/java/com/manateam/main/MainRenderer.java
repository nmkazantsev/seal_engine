package com.manateam.main;

import static android.opengl.GLES20.glClearColor;
import static com.seal.gl_engine.OpenGLRenderer.mMatrix;
import static com.seal.gl_engine.OpenGLRenderer.pageMillis;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.kx;
import static com.seal.gl_engine.utils.Utils.ky;

import com.example.gl_engine_3_1.R;
import com.manateam.main.adaptors.MainShaderAdaptor;
import com.manateam.main.redrawFunctions.MainRedrawFunctions;
import com.seal.gl_engine.GamePageInterface;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.engine.main.animator.Animator;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.engine_object.EnObject;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.verticles.Poligon;
import com.seal.gl_engine.engine.main.verticles.Shape;
import com.seal.gl_engine.engine.main.verticles.SimplePoligon;
import com.seal.gl_engine.maths.Point;

public class MainRenderer implements GamePageInterface {
    private final Poligon fpsPolygon;
    private final Poligon polygon;
    private final Shader shader;
    private final Camera camera;
    private static SimplePoligon simplePolygon;
    private final EnObject s;
    //private final Shape s2;
    boolean f = true;

    //  private FrameBuffer frameBuffer;
    public MainRenderer() {
        Animator.initialize();
        shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, this, new MainShaderAdaptor());
        fpsPolygon = new Poligon(MainRedrawFunctions::redrawFps, true, 1, this);
        polygon = new Poligon(MainRedrawFunctions::redrawFps, true, 0, this);
        polygon.redrawNow();
        camera = new Camera();
        if (simplePolygon == null) {
            simplePolygon = new SimplePoligon(MainRedrawFunctions::redrawBox2, true, 0, null);
            simplePolygon.redrawNow();
        }
        s = new EnObject(new Shape("building_big.obj", "box.jpg", this)); //new EnObject(new Shape("tank.obj", "cube.png", this));
        s.setObjScale(0.2f);
        s.animMotion(1f, 0f, -6f, 1000, 0, false);
        s.animRotation(0f, 0f, 90f, 3000, 0, false);
        s.animRotation(90f, 0, 0, 1000, 3000, false);
        //s.animPivotRotation(0, 0, 0, 1, 1, 1, 1000, 5000, false);
        s.animMotion(1f, 0, 0, 500, 6000, true);
        // s.animMotion(0, 0, -6, 3000, 600, false);

        // s2 =  new Shape("building_big.obj","box.jpg",this);
    }

    @Override
    public void draw() {
        if (f && pageMillis() >= 500) {
            s.stopAnimations();
            f = false;
        }
        if (pageMillis() >= 1500) s.continueAnimations();
        applyShader(shader);
        glClearColor(1f, 1f, 1f, 1);
        camera.resetFor3d();
        camera.cameraSettings.eyeZ = 5;
        camera.apply();
        // connectFrameBuffer(frameBuffer.getFrameBuffer());
        s.prepareAndDraw();
        // s2.prepareAndDraw();
        fpsPolygon.setRedrawNeeded(true);
        camera.resetFor2d();
        camera.apply(false);
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
