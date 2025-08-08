package com.seal.gl_engine.engine.main.debugger;

import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;

import androidx.annotation.NonNull;

import com.example.gl_engine.R;
import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.default_adaptors.SectionShaderAdaptor;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.vertices.SectionPolygon;
import com.seal.gl_engine.maths.Section;
import com.seal.gl_engine.maths.PVector;

public class Axes {
    private final SectionPolygon line;
    private static Shader shader = null;

    public Axes(@NonNull GamePageClass gamePageClass) {
        line = new SectionPolygon(gamePageClass);
        shader = new Shader(R.raw.line_vertex, R.raw.line_fragmant, gamePageClass, new SectionShaderAdaptor()); //compile only once

    }

    public void drawAxes(float limit, float step, float tickSize, float[] matrix, Camera camera) {
        Shader prevShader = Shader.getActiveShader();
        applyShader(shader);
        if (matrix == null) {
            matrix = resetTranslateMatrix(new float[16]);
        }
        applyMatrix(matrix);
        camera.apply();
        //x
        line.setColor(new PVector(1, 0, 0));
        line.draw(new Section(new PVector(0), new PVector(limit, 0, 0)));
        for (float i = 0; i < limit; i += step) {
            line.draw(new Section(new PVector(i, -tickSize, 0), new PVector(i, tickSize, 0)));
            line.draw(new Section(new PVector(i, 0, -tickSize), new PVector(i, 0, tickSize)));
        }

        line.setColor(new PVector(0.5f, 0, 0));
        line.draw(new Section(new PVector(0), new PVector(-limit, 0, 0)));
        for (float i = -limit; i < 0; i += step) {
            line.draw(new Section(new PVector(i, -tickSize, 0), new PVector(i, tickSize, 0)));
            line.draw(new Section(new PVector(i, 0, -tickSize), new PVector(i, 0, tickSize)));
        }

        //y
        line.setColor(new PVector(0, 1, 0));
        line.draw(new Section(new PVector(0), new PVector(0, limit, 0)));
        for (float i = 0; i < limit; i += step) {
            line.draw(new Section(new PVector(-tickSize, i, 0), new PVector(tickSize, i, 0)));
            line.draw(new Section(new PVector(0, i, -tickSize), new PVector(0, i, tickSize)));
        }

        line.setColor(new PVector(0, 0.5f, 0));
        line.draw(new Section(new PVector(0), new PVector(0, -limit, 0)));
        for (float i = -limit; i < 0; i += step) {
            line.draw(new Section(new PVector(-tickSize, i, 0), new PVector(tickSize, i, 0)));
            line.draw(new Section(new PVector(0, i, -tickSize), new PVector(0, i, tickSize)));
        }
        //z
        line.setColor(new PVector(0, 0, 1));
        line.draw(new Section(new PVector(0), new PVector(0, 0, limit)));
        for (float i = 0; i < limit; i += step) {
            line.draw(new Section(new PVector(-tickSize, 0, i), new PVector(tickSize, 0, i)));
            line.draw(new Section(new PVector(0, -tickSize, i), new PVector(0, tickSize, i)));
        }

        line.setColor(new PVector(0, 0, 0.5f));
        line.draw(new Section(new PVector(0), new PVector(0, 0, -limit)));
        for (float i = -limit; i < 0; i += step) {
            line.draw(new Section(new PVector(-tickSize, 0, i), new PVector(tickSize, 0, i)));
            line.draw(new Section(new PVector(0, -tickSize, i), new PVector(0, tickSize, i)));
        }


        applyShader(prevShader);
        camera.apply();
        applyMatrix(matrix);
    }
}
