package com.manateam.main;

import static android.opengl.GLES20.glClearColor;
import static com.seal.gl_engine.OpenGLRenderer.mMatrix;
import static com.seal.gl_engine.OpenGLRenderer.pageMillis;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.frameBuffers.FrameBuffer.connectDefaultFrameBuffer;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.freezeMillis;
import static com.seal.gl_engine.utils.Utils.getMillisFrozen;
import static com.seal.gl_engine.utils.Utils.unfreezeMillis;
import static com.seal.gl_engine.utils.Utils.x;

import android.util.Log;

import com.manateam.main.redrawFunctions.MainRedrawFunctions;
import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.default_adaptors.SectionShaderAdaptor;
import com.seal.gl_engine.default_adaptors.MainShaderAdaptor;
import com.seal.gl_engine.engine.main.animator.Animator;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.debugger.Axes;
import com.seal.gl_engine.engine.main.engine_object.SealObject;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.touch.TouchPoint;
import com.seal.gl_engine.engine.main.touch.TouchProcessor;
import com.seal.gl_engine.engine.main.vertices.SectionPolygon;
import com.seal.gl_engine.engine.main.vertices.Shape;
import com.seal.gl_engine.engine.main.vertices.SimplePolygon;
import com.seal.gl_engine.maths.Section;
import com.seal.gl_engine.maths.PVector;

public class MainRenderer extends GamePageClass {
    private final Shader shader;
    private final Camera camera;
    private static SimplePolygon simplePolygon;
    private final SealObject s;
    boolean f = true;
    private final TouchProcessor touchProcessor;
    //private final FrameBuffer frameBuffer;
    private final SectionPolygon sectionPolygon;
    private final Shader lineShader;
    private final Axes axes;

    public MainRenderer() {
        Animator.initialize();
        shader = new Shader(com.example.gl_engine.R.raw.vertex_shader, com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());
        camera = new Camera();
        if (simplePolygon == null) {
            simplePolygon = new SimplePolygon(MainRedrawFunctions::redrawBox2, true, 0, null, true);
            simplePolygon.redrawNow();
        }

        touchProcessor = new TouchProcessor(this::touchProcHitbox, this::touchStartedCallback, this::touchMovedCallback, this::touchEndCallback, this);
        new TouchProcessor(TouchPoint -> TouchPoint.touchX > x / 2, TouchPoint -> {
            if (!getMillisFrozen()) {
                freezeMillis();
            } else {
                unfreezeMillis();
            }
            return null;
        }, null, null, this);
        TouchProcessor touchProcessor2 = new TouchProcessor(MotionEvent -> true, this::touchStartedCallback, this::touchMovedCallback, this::touchEndCallback, this);

        s = new SealObject(new Shape("building_big.obj", "box.jpg", this));
        s.setObjScale(0.2f);
        s.animMotion(1f, 0f, -6f, 1000, 1000, false);
        s.animRotation(0f, 0f, 90f, 3000, 1000, false);
        s.animRotation(90f, 0, 0, 1000, 3000, false);
        s.animMotion(1f, 0, 0, 500, 6000, true);
        TouchProcessor touchProcessor = new TouchProcessor(this::touchProcHitbox, this::touchStartedCallback, this::touchMovedCallback, this::touchEndCallback, this);
        // frameBuffer = new FrameBuffer((int) x, (int) y, this);
        lineShader = new Shader(com.example.gl_engine.R.raw.line_vertex, com.example.gl_engine.R.raw.line_fragmant, this, new SectionShaderAdaptor());
        sectionPolygon = new SectionPolygon(this);
        axes = new Axes(this);
    }


    @Override
    public void draw() {
        connectDefaultFrameBuffer();
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
        applyShader(lineShader);
        camera.apply();
        applyMatrix(resetTranslateMatrix(new float[16]));
        sectionPolygon.setColor(new PVector(1, 0, 0));
        sectionPolygon.draw(new Section(new PVector(0, 0, 0), new PVector(1, -1, -1)));
        applyShader(shader);
        axes.drawAxes(3, 0.3f, 0.2f,null, camera);
        //connectDefaultFrameBuffer();
        camera.resetFor2d();
        camera.apply(false);
        applyMatrix(mMatrix);
        if (touchProcessor.getTouchAlive()) {
            simplePolygon.prepareAndDraw(0, touchProcessor.lastTouchPoint.touchX, touchProcessor.lastTouchPoint.touchY, 300, 300, 0.01f);
        }
        simplePolygon.prepareAndDraw(0, 500, 500, 300, 300, 0.01f);
        // frameBuffer.drawTexture(new Vec3(Utils.x, Utils.y, 1), new Vec3(0, y, 1), new Vec3(Utils.x, 0, 1));

    }

    @Override
    public void onResume() {
        Log.e("main renderer", "on resume");
    }

    @Override
    public void onPause() {
        Log.e("main renderer", "on pause");
    }

    private Boolean touchProcHitbox(TouchPoint event) {
        return event.touchX < x / 2;
    }

    private Void touchStartedCallback(TouchPoint p) {
        return null;
    }

    private Void touchMovedCallback(TouchPoint p) {
        return null;
    }

    private Void touchEndCallback(TouchPoint t) {
        OpenGLRenderer.startNewPage(new SecondRenderer());//запуск страницы только если тач начался в нужном хитбоксе
        return null;
    }
}
