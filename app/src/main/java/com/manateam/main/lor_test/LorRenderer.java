package com.manateam.main.lor_test;

import static com.seal.gl_engine.OpenGLRenderer.pageMillis;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.kx;
import static com.seal.gl_engine.utils.Utils.map;
import static com.seal.gl_engine.utils.Utils.min;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;

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
    private final Polygon textBox, textBox2; //в 1 текстуру не помещается
    private final float[] matrix;
    private final Shader shader, post_shader;
    private static final float xLeft = -30, xRight = -xLeft, yFar = xRight * 20;
    private final FrameBuffer frameBuffer;
    private Axes axes;

    public LorRenderer() {
        axes = new Axes(this);
        textBox = new Polygon(this::redrawText1, true, 0, this, true);
        textBox2 = new Polygon(this::redrawText2, true, 0, this, true);
        camera = new Camera(x, y);
        camera.cameraSettings = new GameCameraSettings(x, y);
        camera.projectionMatrixSettings = new GameProjectionMatrixSettings(x, y);
        matrix = resetTranslateMatrix(new float[16]);
        shader = new Shader(com.example.gl_engine.R.raw.vertex_shader, com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());
        post_shader = new Shader(com.example.gl_engine.R.raw.vertex_shader, com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());
        frameBuffer = new FrameBuffer((int) x, (int) y, this);
    }

    @Override
    public void draw() {

        applyShader(shader);
        applyMatrix(matrix);
        camera.resetFor3d();
        camera.cameraSettings.setPos(new PVector(0, -70, 30));
        camera.cameraSettings.setCenter(new PVector(0, 100, 0));
        camera.cameraSettings.SetUpVector(new PVector(0, 0, 1));
        //camera.projectionMatrixSettings.far=500;
        camera.apply();
        float move = map(pageMillis(), 0, 200000, 0, yFar);
        frameBuffer.apply();
        textBox.prepareAndDraw(new PVector(xRight, move + yFar / 2.0f, 0), new PVector(xLeft, move + yFar / 2.0f, 0), new PVector(xRight, move, 0));
        //textBox2.prepareAndDraw(new PVector(xLeft, yFar / 2.0f + move, 0), new PVector(xRight, yFar / 2.0f + move, 0), new PVector(xLeft, move, 0));
        axes.drawAxes(100, 10, 3, matrix, camera);
        FrameBuffer.connectDefaultFrameBuffer();
        camera.resetFor2d();
        applyShader(post_shader);
        camera.apply();
        applyMatrix(resetTranslateMatrix(new float[16]));
        frameBuffer.drawTexture(new PVector(0, 0, 1), new PVector(x, 0, 1), new PVector(0, y, 1));

    }

    private PImage redrawText1(List<Object> params) {
        int image_width = 1000;
        PImage img = new PImage(image_width, 4000);
        img.background(0);
        img.textAlign(Paint.Align.CENTER);
        img.fill(255);
        img.textSize(35 * kx);
        String text = "В одном глубоком-глубоком подвале химического факультета была разработанна уникальная пищевая добавка. Долгие годы лучшие умы смело экспериментировали и бесстрашно исследовали, не спали ночами, и вот, наконец, были получены первые прототипы данного избретения.  Результат был ошеломляющим - внешне продукт выглядел как томатный соус, но обладал чуть более густой консистенцией, а главное, обладал невероятными вкусовыми и питательными качествами! Даже самые сухие котлеты из диетической столовой превращались в деликатес, добавление синтезированного учеными соединения в еду заряжало человека энергией и позититивом на весь день, тонизировало работу сердечно-сосудистой системы, позволяло снимать отеки мышц и за";
        //calculate approximately one symbol size
        Rect bounds = new Rect();
        img.paint.getTextBounds(text, 0, text.length(), bounds);
        float width = bounds.width() / (float) text.length();
        //split text to lines
        StringBuilder text2 = getText2(image_width, width, text);
        img.text(text2.toString(), image_width / 2.0f, 0);
        return img;
    }

    private PImage redrawText2(List<Object> params) {
        int image_width = 1000;
        PImage img = new PImage(image_width, 1000);
        img.background(100);
        img.textAlign(Paint.Align.CENTER);
        img.fill(255);
        img.textSize(35 * kx);
        String text = "В одном глубоком-глубоком подвале химического факультета была разработанна уникальная пищевая добавка. Долгие годы лучшие умы смело экспериментировали и бесстрашно исследовали, не спали ночами, и вот, наконец, были получены первые прототипы данного избретения.  Результат был ошеломляющим - внешне продукт выглядел как томатный соус, но обладал чуть более густой консистенцией, а главное, обладал невероятными вкусовыми и питательными качествами! Даже самые сухие котлеты из диетической столовой превращались в деликатес, добавление синтезированного учеными соединения в еду заряжало человека энергией и позититивом на весь день, тонизировало работу сердечно-сосудистой системы, позволяло снимать отеки мышц и за";

        //calculate approximately one symbol size
        Rect bounds = new Rect();
        img.paint.getTextBounds(text, 0, text.length(), bounds);
        float width = bounds.width() / (float) text.length();
        //split text to lines
        StringBuilder text2 = getText2(image_width, width, text);
        img.text(text2.toString(), image_width / 2.0f, -6000);
        return img;
    }

    private static @NonNull StringBuilder getText2(int image_width, float width, String text) {
        int index = 0;
        int step = (int) (image_width / width) - 1;//minus one for borders
        StringBuilder text2 = new StringBuilder();
        while (index < text.length()) {
            int index2 = (int) min(text.length(), index + step);
            String substr = text.substring(index, index2);
            if (substr.indexOf('\n') == -1) {
                int lIndex = substr.lastIndexOf(' ');
                if (lIndex <= 0) {
                    lIndex = step + 1; //читаем до конца подстроки
                }
                text2.append(text.substring(index, (int) min(lIndex + index, index2)));
                text2.append("\n\n");
                index = (int) min(lIndex + index, index2);
            } else {
                index2 = substr.indexOf('\n');
                text2.append(text.substring(index, index + index2 + 1));
                text2.append("\n\n");
                index += index2 + 1;
            }
        }
        return text2;
    }
}
