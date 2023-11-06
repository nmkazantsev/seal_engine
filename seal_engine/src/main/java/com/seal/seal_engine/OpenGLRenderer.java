package com.seal.seal_engine;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static com.seal.seal_engine.Engine.pointsNumber;
import static com.seal.seal_engine.Engine.touchEvents;
import static com.seal.seal_engine.Engine.touchEventsNumb;
import static com.seal.seal_engine.Engine.touches;
import static com.seal.seal_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.seal_engine.utils.Utils.context;
import static com.seal.seal_engine.utils.Utils.kx;
import static com.seal.seal_engine.utils.Utils.ky;
import static com.seal.seal_engine.utils.Utils.millis;
import static com.seal.seal_engine.utils.Utils.min;
import static com.seal.seal_engine.utils.Utils.parseInt;
import static com.seal.seal_engine.utils.Utils.programStartTime;
import static com.seal.seal_engine.utils.Utils.x;
import static com.seal.seal_engine.utils.Utils.y;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.speech.tts.Voice;
import android.util.Log;

import com.seal.seal_engine.engine.main.frameBuffers.FrameBuffer;
import com.seal.seal_engine.engine.config.MainConfigurationFunctions;
import com.seal.seal_engine.engine.main.frameBuffers.FrameBufferUtils;
import com.seal.seal_engine.engine.main.shaders.Shader;
import com.seal.seal_engine.engine.main.textures.Texture;
import com.seal.seal_engine.engine.main.verticles.VectriesShapesManager;
import com.seal.seal_engine.utils.Utils;

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
        x = width;
        y = height;
        ky = y / 1280.0f;
        kx = x / 720.0f;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        gaphicsSetup();
        glClearColor(0f, 0f, 0f, 1f);
        glEnable(GL_DEPTH_TEST);
        mMatrix = resetTranslateMatrix(mMatrix);
        if (firstStart) {
            programStartTime = System.currentTimeMillis();
            setup();
            firstStart = false;
        }
        Log.e("gl_error_in_setup", String.valueOf(GLES20.glGetError()));
        if (millis() > 60 * 60 * 1000) {
            //smth went wrong...
            programStartTime = System.currentTimeMillis();
            prevPageChangeTime = millis();
        }
        VectriesShapesManager.redrawAllSetup();
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        Log.e("surface changed", String.valueOf(x));
    }

    private void gaphicsSetup() {
        Shader.updateAllLocations();
        Texture.reloadAll();
        VectriesShapesManager.onRedrawSetup();
        FrameBuffer.onRedraw();
    }

    private void setup() {
        prevPageChangeTime = millis();
    }

    @Override
    public void onDrawFrame(GL10 arg0) {
        //calculate fps:
        if (millis() - prevFps > 100) {
            fps = 1000 / (int) ((millis() - prevFps) / (float) cadrs);
            prevFps = millis();
            cadrs = 0;
        }
        cadrs++;
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        draw();
        VectriesShapesManager.redrawAll();
        touchEvent();
    }

    private void draw() {
        if(gamePage==null){
            startNewPage(Engine.getStartPage.apply(null));
        }
        gamePage.draw();
    }

    public void touchEvent() {
        for (int i = 0; i < min(touchEventsNumb, 100); i++) {
            for (int j = 0; j < 10; j++) {
                touches[j].x = touchEvents[i][j * 2];
                touches[j].y = touchEvents[i][j * 2 + 1];
            }
            float y = touchEvents[i][20];
            pointsNumber = parseInt(touchEvents[i][21]);
            if (y == 0) {
                touchStarted();
                Engine.touchEvent = "";
            }
            if (y == 1) {
                touchMoved();
                Engine.touchEvent = "";
            }
            if (y == 2) {
                touchEnded();
                Engine.touchEvent = "";
            }
        }
        touchEvents = new float[100][22];
        touchEventsNumb = 0;
    }

    public static void startNewPage(GamePageInterface newPage) {
        prevPageChangeTime = millis();
        gamePage = null;
        System.gc();
        gamePage = newPage;
        Texture.onPageChanged();
        FrameBufferUtils.onPageChanged();
        Shader.onPageChange();
    }

    public static long pageMillis() {
        return millis() - prevPageChangeTime;
    }

    public static String getPageClassName() {
        return gamePage.getClass().getName();
    }


    static void touchStarted() {
        gamePage.touchStarted();
    }

    static void touchMoved() {
        gamePage.touchMoved();
    }

    static void touchEnded() {
        gamePage.touchEnded();
    }
}
