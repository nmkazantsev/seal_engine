package com.seal.gl_engine.engine.main.debugger;

import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.engine.main.shaders.ShaderUtils.createShader;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import com.example.gl_engine.R;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.shaders.ShaderUtils;
import com.seal.gl_engine.engine.main.verticles.SimplePoligon;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Debugger {
    private static boolean enabled = false;
    private static Camera debuggerCamera;
    private static SimplePoligon debuggerPage, fps;
    private static Shader shader;

    public static void debuggerInit() {
        enabled = true;
        debuggerCamera = new Camera(x, y);

        debuggerPage = new SimplePoligon(drawMianPage, true, 0, null);
        shader = new Shader(R.raw, R.raw.fragment_shader, null, new MainShaderAdaptor());
    }

    public static void draw() {
        if (enabled) {
            applyShader(shader);
            debuggerPage.prepareAndDraw(0, 0, 0, x, y, 9);
        }
    }

    public static void setEnabled(boolean debuggerEnabled) {
        enabled = debuggerEnabled;
    }

    private static final Function<List<Object>, PImage> drawMianPage = new Function<List<Object>, PImage>() {
        @Override
        public PImage apply(List<Object> objects) {
            return null;
        }
    };

}
