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
import static com.seal.gl_engine.utils.Utils.map;
import static com.seal.gl_engine.utils.Utils.min;
import static com.seal.gl_engine.utils.Utils.tg;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import android.graphics.Paint;
import android.util.Log;

import com.example.gl_engine.R;
import com.seal.gl_engine.default_adaptors.MainShaderAdaptor;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.touch.TouchProcessor;
import com.seal.gl_engine.engine.main.verticles.SimplePoligon;
import com.seal.gl_engine.maths.Point;
import com.seal.gl_engine.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
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
    //list is for rendering in strict order, dict is for fast searching for duplicates
    private static final HashMap<String, DebugValueFloat> debugValues = new HashMap<>();//later will be replaced with abstract debug value
    private static final List<DebugValueFloat> debugList = new ArrayList<>();
    private static TouchProcessor mainTP;
    //menu rendering
    private final static float shift = 300 * ky;
    private final static float enter = 75 * ky;
    private final static int maxNum = 10;
    private static DebugValueFloat selectedValue = null;
    private static int totalValues = 0;

    public static void debuggerInit() {
        fps_x = 100 * kx;
        fps_y = 100 * ky;
        //open menu button
        //no need in blocking openMenu. because it will not be processed (all touches will be blocked by debugger)
        TouchProcessor openMenu = new TouchProcessor(
                TouchPoint -> (TouchPoint.touchX < fps_x && TouchPoint.touchY < fps_y),
                TouchPoint -> {
                    page = 1;
                    mainTP.unblock();
                    //no need in blocking openMenu. because it will not be processed (all touches will be blocked by debugger)
                    return null;
                }, null, null, null
        );
        //main window touch
        mainTP = new TouchProcessor(
                TouchPoint -> true,
                TouchPoint -> {
                    //shorter the code
                    float tx = TouchPoint.touchX;
                    float ty = TouchPoint.touchY;
                    if (tx < fps_x && ty < fps_y) {
                        page = 0;//exit
                        selectedValue = null;
                        mainTP.block();
                    }
                    if (selectedValue == null) {
                        //processing menu
                        Log.e("number", "" + "number");
                        if (ty > shift && ty < shift + (maxNum + 1) * enter) {
                            //select value zone
                            ty -= shift;
                            int number = (int) ty / (int) enter;
                            number += (page - 1) * maxNum;
                            Log.e("number", "" + number);
                            if (number >= 0 && number < totalValues) {
                                selectedValue = debugList.get(number);//choose value
                            }
                        }
                    } else {
                        //processing slider
                    }
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
        //check for duplicate
        DebugValueFloat debugValueFloat = debugValues.getOrDefault(d.name, null);
        if (debugValueFloat == null) {//not a duplicate
            debugValues.put(d.name, d);
            debugList.add(d);
            totalValues++;
        }
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
        image.textSize(45 * kx);
        image.textAlign(Paint.Align.CENTER);
        if (selectedValue == null) {
            for (int i = (int) Utils.max(0, (page - 1) * maxNum); i < min(page * maxNum, totalValues); i++) {
                image.text(debugList.get(i).name + ": " + debugList.get(i).value, x / 2, shift + enter * (i - page + 1));
            }
        } else {
            image.textAlign(Paint.Align.CENTER);
            image.text(selectedValue.name+":", x / 2, y / 3);
            image.noStroke();
            image.fill(255, 255, 255, 100);
            image.roundRect(100 * kx, y / 2, x - 200 * kx, 50 * ky, 20 * kx, 25 * ky);
            image.fill(100, 100, 100, 255);
            image.strokeWeight(4 * kx);
            image.stroke(0, 0, 0, 255);
            image.roundRect(100 * kx, y / 2, x - map(selectedValue.value, selectedValue.max, selectedValue.min, 200 * kx, x - 40 * kx), 50 * ky, 20 * kx, 25 * ky);
            image.fill(0, 0, 0, 255);
            image.stroke(0, 0, 0, 255);
            image.textAlign(Paint.Align.LEFT);
            image.text(selectedValue.min, 20 * kx, y / 2 + 50 * ky);
            image.textAlign(Paint.Align.RIGHT);
            image.text(selectedValue.max, x - 20 * kx, y / 2 + 50 * ky);
            image.textAlign(Paint.Align.CENTER);
            image.text(selectedValue.value, x / 2, y / 2 - 70 * ky);
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
