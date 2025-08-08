package com.manateam.main.lor_test;

import static com.seal.gl_engine.OpenGLRenderer.pageMillis;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.loadImage;
import static com.seal.gl_engine.utils.Utils.map;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import com.manateam.main.lor_test.camera.GameCameraSettings;
import com.manateam.main.lor_test.camera.GameProjectionMatrixSettings;
import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.default_adaptors.MainShaderAdaptor;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.debugger.Axes;
import com.seal.gl_engine.engine.main.frameBuffers.FrameBuffer;
import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.vertices.Polygon;
import com.seal.gl_engine.maths.PVector;

import java.util.List;

public class LorRenderer extends GamePageClass {
    private final Camera camera;
    private final float[] matrix;
    private final Shader shader, post_shader;
    private final FrameBuffer frameBuffer;
    private final Axes axes;
    private final Page[] pages = new Page[3];

    public LorRenderer() {
        axes = new Axes(this);
        camera = new Camera(x, y);
        camera.cameraSettings = new GameCameraSettings(x, y);
        camera.projectionMatrixSettings = new GameProjectionMatrixSettings(x, y);
        matrix = resetTranslateMatrix(new float[16]);
        shader = new Shader(com.example.gl_engine.R.raw.vertex_shader, com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());
        post_shader = new Shader(com.example.gl_engine.R.raw.vertex_shader, com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());
        frameBuffer = new FrameBuffer((int) x, (int) y, this);
        for (int i = 0; i < pages.length; i++) {
            pages[i] = new Page(i, this);
        }
    }

    @Override
    public void draw() {
        applyShader(shader);
        applyMatrix(matrix);
        camera.resetFor3d();
        camera.cameraSettings.setPos(new PVector(0, -70, 40));
        camera.cameraSettings.setCenter(new PVector(0, 100, 0));
        camera.cameraSettings.SetUpVector(new PVector(0, 0, 1));
        camera.apply();
        float move = map(pageMillis(), 0, 20000, 0, Page.yFar * 2);
        frameBuffer.apply();
        for (int i = 0; i < pages.length; i++) {
            pages[i].draw(move);
        }
        axes.drawAxes(100, 10, 3, matrix, camera);
        FrameBuffer.connectDefaultFrameBuffer();
        camera.resetFor2d();
        applyShader(post_shader);
        camera.apply();
        applyMatrix(resetTranslateMatrix(new float[16]));
        frameBuffer.drawTexture(new PVector(0, 0, 1), new PVector(x, 0, 1), new PVector(0, y, 1));
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }


    private static class Page {
        public float shiftVert = 0;
        public static final float xLeft = -40, xRight = -xLeft, yFar = xRight * 10;
        private final Polygon polygon;
        private final int num;

        Page(int num, GamePageClass pageClass) {
            this.num = num;
            polygon = new Polygon(this::redrawTextLor, true, 0, pageClass, true);
        }

        public void draw(float move) {
            polygon.prepareAndDraw(new PVector(xRight, move - yFar * num + shiftVert, 0),
                    new PVector(xLeft, move - yFar * num + shiftVert, 0),
                    new PVector(xRight, move - yFar - yFar * num + shiftVert, 0),
                    0, 0, 0.8f, 1);
        }

        private PImage redrawTextLor(List<Object> params) {
            return loadImage("lor/LOR_page-000" + (num + 1) + ".jpg");
        }
    }

}
