package com.seal.gl_engine;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.seal.gl_engine.engine.main.VRAMobject;
import com.seal.gl_engine.engine.config.MainConfigurationFunctions;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.touch.TouchProcessor;
import com.seal.gl_engine.engine.main.verticles.VectriesShapesManager;
import com.seal.gl_engine.utils.Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements Renderer {

    public static float fps;
    private long prevFps;
    private int cadrs;
    public static float[] mMatrix = new float[16];
    private static GamePageInterface gamePage;
    private boolean firstStart = true;
    private static long prevPageChangeTime = 0;


    public OpenGLRenderer(Context context, float width, float height) {
        Utils.x = width;
        Utils.y = height;
        Utils.ky = Utils.y / 1280.0f;
        Utils.kx = Utils.x / 720.0f;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        gaphicsSetup();
        glClearColor(0f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);
        mMatrix = MainConfigurationFunctions.resetTranslateMatrix(mMatrix);
        if (firstStart) {
            Utils.programStartTime = System.currentTimeMillis();
            setup();
            firstStart = false;
        }
        Log.e("gl_error_in_setup", String.valueOf(GLES20.glGetError()));
        if (Utils.millis() > 60 * 60 * 1000) {
            //smth went wrong...
            Utils.programStartTime = System.currentTimeMillis();
            prevPageChangeTime = Utils.millis();
        }
        VectriesShapesManager.redrawAllSetup();
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        Log.e("surface changed", String.valueOf(Utils.x));
    }

    private void gaphicsSetup() {
        Shader.updateAllLocations();
        VRAMobject.onRedraw();
        VectriesShapesManager.onRedrawSetup();
    }

    private void setup() {
        prevPageChangeTime = Utils.millis();
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        //calculate fps:
        if (Utils.millis() - prevFps > 100) {
            fps = 1000.0f / (int) ((Utils.millis() - prevFps) / (float) cadrs);
            prevFps = Utils.millis();
            cadrs = 0;
        }
        cadrs++;
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        draw();
        VectriesShapesManager.redrawAll();
        TouchProcessor.processMotions();
    }

    private void draw() {
        if (gamePage == null) {
            startNewPage(Engine.getStartPage.apply(null));
        }
        gamePage.draw();
    }

    public static void startNewPage(GamePageInterface newPage) {
        prevPageChangeTime = Utils.millis();
        gamePage = null;
        System.gc();
        gamePage = newPage;
        VRAMobject.onPageChange();
        Shader.onPageChange();
        TouchProcessor.onPageChange();
        gamePage.initialize();
    }

    public static long pageMillis() {
        return Utils.millis() - prevPageChangeTime;
    }

    public static String getPageClassName() {
        return gamePage.getClass().getName();
    }
}
