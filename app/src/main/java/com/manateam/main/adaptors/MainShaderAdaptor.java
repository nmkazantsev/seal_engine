package com.manateam.main.adaptors;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.opengl.GLES30;

import com.manateam.glengine3.engine.main.shaders.Adaptor;
import com.manateam.glengine3.engine.main.vertex_bueffer.VertexBuffer;
import com.manateam.glengine3.engine.main.verticles.DrawableShape;
import com.manateam.glengine3.engine.main.verticles.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MainShaderAdaptor extends Adaptor {

    private int aPositionLocation;
    private int aTextureLocation;
    private int uTextureUnitLocation;
    private int projectionMatrixLoation;
    private int viewMatrixLocation;
    private int modelMtrixLocation;
    private int normalLocation;

    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int NORMAL_COUNT = 3;
    private static final int STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT + NORMAL_COUNT) * 4;

    @Override
    public int bindData(Face[] faces) {
        float[] vertices = new float[faces.length * faces[0].verticesNumber()];
        int vertexesNumber = 0;
        for (int i = 0; i < faces.length; i++) {
            System.arraycopy(faces[i].getArrayRepresentation(), 0, vertices, i * faces[i].verticesNumber(), faces[i].verticesNumber());
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

        // координаты текстур
        vertexData.position(POSITION_COUNT);
        glVertexAttribPointer(aTextureLocation, TEXTURE_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aTextureLocation);

        vertexData.position(POSITION_COUNT + TEXTURE_COUNT);
        glVertexAttribPointer(normalLocation, NORMAL_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(normalLocation);
        return vertexesNumber;
    }

    private void loadDataToBuffer(Face[] faces, VertexBuffer vertexBuffer, int bufferIndex,
                                  int startPosInArrayRepr, int lenInArrRepr) {

        float[] vertices = new float[faces.length * 3];//3 numbers in 1 vectrie
        for (int i = 0; i < faces.length; i++) {
            System.arraycopy(faces[i].getArrayRepresentation(), startPosInArrayRepr, vertices,
                    i * lenInArrRepr, lenInArrRepr);
            //vertexesNumber++;
        }
        FloatBuffer vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);//4 байта на флоат
        vertexBuffer.bindVbo(bufferIndex);//vertex coords
        vertexData.position(0);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * 4, vertexData, GL_STATIC_DRAW);
    }

    @Override
    public int bindData(Face[] faces, VertexBuffer vertexBuffer) {
        //set up positions
        loadDataToBuffer(faces, vertexBuffer, 0, 0, 3);
        //set up uv
        loadDataToBuffer(faces, vertexBuffer, 1, 3, 2);
        //set up normals
        loadDataToBuffer(faces, vertexBuffer, 2, 5, 3);

        /*float[] vertices = new float[faces.length * faces[0].verticesNumber()];

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

        vertexData.position(POSITION_COUNT + TEXTURE_COUNT);
        glVertexAttribPointer(normalLocation, NORMAL_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(normalLocation);
        */
        vertexBuffer.bindDefaultVbo();//vertex coords
        vertexBuffer.bindDefaultVao();
        return vertexesNumber;
    }

    @Override
    public void updateLocations() {
        aPositionLocation = glGetAttribLocation(programId, "aPos");
        aTextureLocation = glGetAttribLocation(programId, "aTexCoord");
        normalLocation = glGetAttribLocation(programId, "normal");
        uTextureUnitLocation = glGetUniformLocation(programId, "u_TextureUnit");
        projectionMatrixLoation = GLES30.glGetUniformLocation(programId, "projection");
        viewMatrixLocation = GLES30.glGetUniformLocation(programId, "view");
        modelMtrixLocation = GLES30.glGetUniformLocation(programId, "model");
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

    @Override
    public int getNormalTextureLocation() {
        return -1;
    }

    @Override
    public int getCameraPosLlocation() {
        return -1;
    }
}
