package com.seal.gl_engine.engine.main.vertices;

import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glGetUniformLocation;

import android.opengl.GLES30;

import androidx.annotation.NonNull;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.shaders.ShaderData;
import com.seal.gl_engine.maths.Section;
import com.seal.gl_engine.maths.PVector;

import java.lang.ref.WeakReference;

//in fact implementation of interface, that automatically redraws textures needed for bind data to be called when needed
public class SectionPolygon implements VerticesSet {
    private GamePageClass page;
    private final String creatorClassName;
    private PVector color = new PVector(1);
    ShaderData lineColorData;

    public SectionPolygon(@NonNull GamePageClass page) {
        VerticesShapesManager.allShapes.add(new WeakReference<>(this));//add link to this object

        creatorClassName = page.getClass().getName();

        lineColorData = new ShaderData(page) {
            private int colorLoc;

            @Override
            protected void getLocations(int programId) {
                colorLoc = glGetUniformLocation(programId, "vColor");
            }

            @Override
            protected void forwardData() {
                GLES30.glUniform3f(colorLoc, color.x, color.y, color.z);
            }

            @Override
            protected void delete() {

            }
        };
    }

    private void bindData(Section section) {
        Shader.getActiveShader().getAdaptor().bindDataLine(section.getBaseVector(), section.getBaseVector().add(section.getDirectionVector()), color);

    }

    public void draw(Section section) {
        lineColorData.forwardNow();
        bindData(section);
        glDrawArrays(GL_LINES, 0, 2);
    }

    public void setColor(PVector color) {
        this.color = color;
    }

    //do not need these methods as we are going to pass color each time draw function is called
    @Override
    public void onRedrawSetup() {

    }

    @Override
    public void setRedrawNeeded(boolean redrawNeeded) {

    }

    @Override
    public boolean isRedrawNeeded() {
        return false;
    }

    @Override
    public void onRedraw() {

    }

    @Override
    public String getCreatorClassName() {
        return creatorClassName;
    }

    @Override
    public void onFrameBegin() {

    }

    @Override
    public void delete() {

    }
}
