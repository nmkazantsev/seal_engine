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

import com.example.gl_engine.R;
import com.seal.gl_engine.default_adaptors.MainShaderAdaptor;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.verticles.SimplePoligon;
import com.seal.gl_engine.maths.Point;

import java.util.List;
import java.util.function.Function;

public class Debugger {
    private static boolean enabled = false;
    private static Camera debuggerCamera;
    private static SimplePoligon debuggerPage, fpsPolygon;
    private static Shader shader;
    private static float[] matrix = new float[16];

    public static void debuggerInit() {
        enabled = true;
        debuggerCamera = new Camera(x, y);
        debuggerCamera.resetFor2d();
        debuggerPage = new SimplePoligon(drawMianPage, true, 0, null);
        shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, null, new MainShaderAdaptor());
        fpsPolygon = new SimplePoligon(redrawFps, true, 0, null);
        matrix = resetTranslateMatrix(matrix);
    }

    public static void draw() {
        if (enabled) {
            applyShader(shader);
            debuggerCamera.apply();
            applyMatrix(matrix);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            glEnable(GL_BLEND);
            fpsPolygon.setRedrawNeeded(true);
            fpsPolygon.redrawNow();
            fpsPolygon.prepareAndDraw(new Point(0 * kx, 0, 1), new Point(100 * kx, 0, 1), new Point(0 * kx, 100 * ky, 1));

            debuggerPage.prepareAndDraw(0, 0, 0, x, y, 9);
            glDisable(GL_BLEND);
        }
    }

    public static void setEnabled(boolean debuggerEnabled) {
        enabled = debuggerEnabled;
    }

    private static final Function<List<Object>, PImage> drawMianPage = objects -> {
        PImage image = new PImage(x, y);
        image.background(255, 255, 255, 140);
        return image;
    };
    /*
     public static PImage redrawFps(List<Object> param) {
        PImage image = new PImage(100, 100);
        image.background(150);
        image.textSize(20);
        image.fill(0);
        if (param.size() > 0) {
            image.text((String) param.get(0), 10, 10);
        }
        return image;
    }
     */

    private static final Function<List<Object>, PImage> redrawFps = objects -> {
        PImage image = new PImage(100, 100);
        image.background(150);
        image.textSize(20);
        image.fill(0);
        image.text(fps, 10, 10);
        return image;
    };
}
