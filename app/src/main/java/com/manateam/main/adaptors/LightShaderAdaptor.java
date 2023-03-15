package com.manateam.main.adaptors;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.opengl.GLES30;

import com.manateam.glengine3.engine.main.shaders.Adaptor;
import com.manateam.glengine3.engine.main.verticles.DrawableShape;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class LightShaderAdaptor extends Adaptor {
    //only for shapes

    private int aPositionLocation;
    private int aTextureLocation;
    private int uTextureUnitLocation;
    private int projectionMatrixLoation;
    private int viewMatrixLocation;
    private int modelMtrixLocation;
    private int normalsLocation;

    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT) * 4;


    @Override
    public int bindData(DrawableShape drawableShape) {
        float[] vertexes = drawableShape.getVertexData();
        float[] textCoord = drawableShape.getTextureData();
        float[] vertices = new float[vertexes.length + textCoord.length];
        int vertexesNumber = 0;
        for (int i = 0; i < vertices.length / 5; i++) {
            vertexesNumber++;
            //3 на координату, 2 на текстуру
            vertices[i * 5] = vertexes[i * 3];
            vertices[i * 5 + 1] = vertexes[i * 3 + 1];
            vertices[i * 5 + 2] = vertexes[i * 3 + 2];
            vertices[i * 5 + 3] = textCoord[i * 2];
            vertices[i * 5 + 4] = textCoord[i * 2 + 1];
        }
        FloatBuffer vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);//4 байта на флоат
        // координаты вершин
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        // координаты текстур
        vertexData.position(POSITION_COUNT);
        glVertexAttribPointer(aTextureLocation, TEXTURE_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aTextureLocation);
        return vertexesNumber;
    }

    @Override
    public void updateLocations() {
        aPositionLocation = glGetAttribLocation(programId, "aPos");
        aTextureLocation = glGetAttribLocation(programId, "aTexCoord");
        uTextureUnitLocation = glGetUniformLocation(programId, "u_TextureUnit");
        projectionMatrixLoation = GLES30.glGetUniformLocation(programId, "projection");
        viewMatrixLocation = GLES30.glGetUniformLocation(programId, "view");
        modelMtrixLocation = GLES30.glGetUniformLocation(programId, "model");
        normalsLocation = GLES30.glGetUniformLocation(programId, "normal");
    }

    @Override
    public int getTransformMatrixLocation() {
        return modelMtrixLocation;
    }

    @Override
    public int getCameraLocation() {
        return viewMatrixLocation;
    }

    @Override
    public int getProjectionLocation() {
        return projectionMatrixLoation;
    }

    @Override
    public int getTextureLocation() {
        return uTextureUnitLocation;
    }
}
