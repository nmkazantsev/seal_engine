package com.seal.gl_engine.utils;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.opengl.GLES30;

import com.seal.gl_engine.engine.main.shaders.Adaptor;
import com.seal.gl_engine.engine.main.vertex_bueffer.VertexBuffer;
import com.seal.gl_engine.engine.main.vertices.Face;
import com.seal.gl_engine.maths.PVector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class SkyBoxShaderAdaptor extends Adaptor {

    private int aPositionLocation;
    private int uTextureUnitLocation;
    private int projectionMatrixLoation;
    private int viewMatrixLocation;

    private final static int POSITION_COUNT = 3;
    private static final int STRIDE = (POSITION_COUNT) * 4;

    @Override
    public int bindData(Face[] faces) {
        float[] vertices = new float[12 * 3 * 3];
        int vertexesNumber = 0;
        for (int i = 0; i < 12; i++) {
            System.arraycopy(faces[i].getArrayRepresentationVertexes(), 0, vertices, i * 9, 9);
            vertexesNumber++;
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
        return vertexesNumber;
    }

    @Override
    public int bindData(Face[] faces, VertexBuffer vertexBuffer, boolean vboLoaded) {
        //todo here
        return 0;
    }

    @Override
    public void bindDataLine(PVector a, PVector b, PVector color) {

    }

    @Override
    public void updateLocations() {
        aPositionLocation = glGetAttribLocation(programId, "aPos");
        uTextureUnitLocation = glGetUniformLocation(programId, "skybox");
        projectionMatrixLoation = GLES30.glGetUniformLocation(programId, "projection");
        viewMatrixLocation = GLES30.glGetUniformLocation(programId, "view");
    }

    @Override
    public int getTransformMatrixLocation() {
        return -1;
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

    @Override
    public int getNormalTextureLocation() {
        return -1;
    }

    @Override
    public int getNormalMapEnableLocation() {
        return -1;
    }

    @Override
    public int getCameraPosLlocation() {
        return viewMatrixLocation;
    }
}


