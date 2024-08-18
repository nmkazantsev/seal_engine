package com.seal.gl_engine.engine.main.debugger;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glEnable;
import static com.seal.gl_engine.OpenGLRenderer.fps;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.kx;
import static com.seal.gl_engine.utils.Utils.ky;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import android.graphics.Paint;

import com.example.gl_engine.R;
import com.seal.gl_engine.default_adaptors.MainShaderAdaptor;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.touch.TouchPoint;
import com.seal.gl_engine.engine.main.touch.TouchProcessor;
import com.seal.gl_engine.engine.main.verticles.SimplePoligon;
import com.seal.gl_engine.maths.Point;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class Debugger {
    private static boolean enabled = false;
    private static Camera debuggerCamera;
    private static SimplePoligon debuggerPage, fpsPolygon;
    private static Shader shader;
    private static float[] matrix = new float[16];
    private static int page = 0;//0 if no page, then numbers of pages from 1
    private static float fps_x;
    private static float fps_y;
    private static TouchProcessor openMenu;
    private static HashMap<String, DebugValueFloat> debugValues = new HashMap<>();//later will be replaced with abstract debug value
    private static TouchProcessor mainTP;

    public static void debuggerInit() {
        fps_x = 100 * kx;
        fps_y = 100 * ky;
        //open menu button
        openMenu = new TouchProcessor(
                TouchPoint -> (TouchPoint.touchX < fps_x && TouchPoint.touchY < fps_y),
                TouchPoint -> {
                    page = 1;
                    openMenu.block();
                    return null;
                }, null, null, null
        );
        //main window touch
        mainTP = new TouchProcessor(
                TouchPoint -> (TouchPoint.touchX < fps_x && TouchPoint.touchY < fps_y),
                TouchPoint -> {
                    page = 0;
                    openMenu.block();
                    return null;
                }, null, null, null
        );
        enabled = true;
        debuggerCamera = new Camera(x, y);
        debuggerCamera.resetFor2d();
        debuggerPage = new SimplePoligon(drawMianPage, true, 0, null);
        shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, null, new MainShaderAdaptor());
        fpsPolygon = new SimplePoligon(redrawFps, true, 0, null);
        matrix = resetTranslateMatrix(matrix);
    }

    public static TouchProcessor getMainPageTouchProcessor() {
        return mainTP;
    }

    protected static void addDebugValue(DebugValueFloat d) {
        debugValues.put(d.name, d);
    }

    public static void draw() {
        if (enabled) {
            applyShader(shader);
            debuggerCamera.apply();
            applyMatrix(matrix);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_BLEND);
            if (page == 0) {
                fpsPolygon.setRedrawNeeded(true);
                fpsPolygon.redrawNow();
                fpsPolygon.prepareAndDraw(new Point(0 * kx, 0, 1), new Point(fps_x, 0, 1), new Point(0 * kx, fps_y, 1));
            } else {
                debuggerPage.setRedrawNeeded(true);
                debuggerPage.redrawNow();
                debuggerPage.prepareAndDraw(0, 0, 0, x, y, 9);
            }
            glDisable(GL_BLEND);
        }
    }

    public static void setEnabled(boolean debuggerEnabled) {
        enabled = debuggerEnabled;
    }

    private static final Function<List<Object>, PImage> drawMianPage = objects -> {
        PImage image = new PImage(x, y);
        image.background(255, 255, 255, 140);
        image.textSize(26 * kx);
        image.fill(0);
        image.text((int) fps, 10, 10);
        float shift = 100 * ky;
        float enter = 25 * ky;
        int num = 0;
        int dispPage = 1;
        int maxNum = 10;
        image.textSize(50 * kx);
        image.textAlign(Paint.Align.CENTER);
        for (String name : debugValues.keySet()) {
            if (dispPage == page) {
                image.text(name + ": " + String.valueOf(debugValues.get(name).value), x / 2, shift + enter * num);
            }
            num++;
            if (num == maxNum) {
                dispPage++;
                num = 0;
            }
        }
        return image;
    };


    private static final Function<List<Object>, PImage> redrawFps = objects -> {
        PImage image = new PImage(100, 100);
        image.background(150);
        image.textSize(20);
        image.fill(0);
        image.text(fps, 10, 10);
        return image;
    };

    public static int getPage() {
        return page;
    }
}
