package com.manateam.glengine3;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glViewport;
import static com.manateam.glengine3.MainActivity.pointsNumber;
import static com.manateam.glengine3.MainActivity.touchEvents;
import static com.manateam.glengine3.MainActivity.touchEventsNumb;
import static com.manateam.glengine3.MainActivity.touches;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.manateam.glengine3.utils.Utils.delay;
import static com.manateam.glengine3.utils.Utils.kx;
import static com.manateam.glengine3.utils.Utils.ky;
import static com.manateam.glengine3.utils.Utils.millis;
import static com.manateam.glengine3.utils.Utils.min;
import static com.manateam.glengine3.utils.Utils.parseInt;
import static com.manateam.glengine3.utils.Utils.programStartTime;
import static com.manateam.glengine3.utils.Utils.x;
import static com.manateam.glengine3.utils.Utils.y;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.manateam.glengine3.engine.config.MainConfigurationFunctions;
import com.manateam.glengine3.engine.main.frameBuffers.FrameBuffer;
import com.manateam.glengine3.engine.main.frameBuffers.FrameBufferUtils;
import com.manateam.glengine3.engine.main.shaders.Shader;
import com.manateam.glengine3.engine.main.textures.Texture;
import com.manateam.glengine3.engine.main.verticles.VectriesShapesManager;
import com.manateam.glengine3.engine.oldEngine.animshapes.VideoShape;
import com.manateam.glengine3.engine.oldEngine.glShape;
import com.manateam.glengine3.utils.Utils;
import com.manateam.main.MainRenderer;

import org.w3c.dom.Text;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements Renderer {

    public static float fps;
    private long prevFps;
    private int cadrs;
    public static float[] mMatrix = new float[16];
    private static GamePageInterface gamePage;
    private boolean firstStart = true;

    private Context context;
    private final String TAG = "opengl renderer"; // for logging

    public OpenGLRenderer(Context context, float width, float height) {
        this.context = context;
        x = width;
        y = height;
        ky = y / 1280.0f;
        kx = x / 720.0f;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        MainConfigurationFunctions.context = context;
        Utils.context = context;
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
        if (millis() > 1 * 60 * 60 * 1000) {
            //smth went wrong...
            programStartTime = System.currentTimeMillis();
        }
        VectriesShapesManager.redrawAllSetup();
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        Log.e("surface changed",String.valueOf(x));
    }

    private void gaphicsSetup() {
        Shader.updateAllLocations();
        Texture.reloadAll();
        VectriesShapesManager.onRedrawSetup();
        FrameBuffer.onRedraw();
        glShape.allShapesRedrawSetup();
        VideoShape.redrawAll();
    }

    private void setup() {
        gamePage = new MainRenderer();
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
                MainActivity.touchEvent = "";
            }
            if (y == 1) {
                touchMoved();
                MainActivity.touchEvent = "";
            }
            if (y == 2) {
                touchEnded();
                MainActivity.touchEvent = "";
            }
        }
        touchEvents = new float[100][22];
        touchEventsNumb = 0;
    }

    public static void startNewPage(GamePageInterface newPage) {
        gamePage = null;
        System.gc();
        gamePage = newPage;
        Texture.onPageChanged();
        FrameBufferUtils.onPageChanged();
        glShape.onPageChanged();
        Shader.onPageChange();
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
